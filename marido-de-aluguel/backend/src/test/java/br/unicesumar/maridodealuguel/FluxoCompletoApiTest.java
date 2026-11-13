package br.unicesumar.maridodealuguel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de integração ponta a ponta contra um PostgreSQL real (embutido), criado com os mesmos
 * database/schema.sql e database/seed.sql do repositório. Valida também o ddl-auto=validate.
 */
@SpringBootTest
@AutoConfigureMockMvc
class FluxoCompletoApiTest {

    private static final EmbeddedPostgres POSTGRES = iniciarBanco();

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;
    @Autowired
    private JdbcTemplate jdbc;

    private static EmbeddedPostgres iniciarBanco() {
        try {
            EmbeddedPostgres pg = EmbeddedPostgres.builder().start();
            try (Connection conexao = pg.getPostgresDatabase().getConnection();
                 Statement sql = conexao.createStatement()) {
                sql.execute(Files.readString(Path.of("../database/schema.sql")));
                sql.execute(Files.readString(Path.of("../database/seed.sql")));
            }
            return pg;
        } catch (IOException | SQLException e) {
            throw new IllegalStateException("Falha ao preparar o PostgreSQL de teste", e);
        }
    }

    @DynamicPropertySource
    static void banco(DynamicPropertyRegistry registro) {
        registro.add("spring.datasource.url", () -> POSTGRES.getJdbcUrl("postgres", "postgres"));
        registro.add("spring.datasource.username", () -> "postgres");
        registro.add("spring.datasource.password", () -> "");
    }

    @AfterAll
    static void pararBanco() throws IOException {
        POSTGRES.close();
    }

    @Test
    void fluxoCompletoDoProduto() throws Exception {
        String prestador = login("douglas@email.com");
        String cliente = login("camila@email.com");

        // RNF02: sem token e com perfil errado
        mvc.perform(get("/api/ordens")).andExpect(status().isUnauthorized());
        chamar(post("/api/servicos"), cliente, servico("POR_HORA", "70.00")).andExpect(status().isForbidden());

        // RF02: Create
        JsonNode criado = corpo(chamar(post("/api/servicos"), prestador, servico("POR_HORA", "70.00"))
                .andExpect(status().isCreated()));
        int servicoId = criado.get("id").asInt();
        assertThat(criado.get("tipoCobranca").asText()).isEqualTo("POR_HORA");

        // RF02: Read
        chamar(get("/api/servicos/meus"), prestador, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(servicoId));

        // RF02: Update (inclusive trocando o modelo de cobrança -> outra subclasse)
        chamar(put("/api/servicos/" + servicoId), prestador, servico("POR_EMPREITADA", "150.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoCobranca").value("POR_EMPREITADA"))
                .andExpect(jsonPath("$.valorBase").value(150.00));

        // Bean Validation
        chamar(post("/api/servicos"), prestador, servico("POR_HORA", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.valorBase").exists());

        // RF03: busca com Haversine, raio de atendimento e ordenação por preço
        JsonNode resultados = corpo(chamar(get("/api/servicos/busca?categoria=Elétrica&ordenarPor=PRECO&horas=2"),
                cliente, null).andExpect(status().isOk()));
        assertThat(resultados.findValues("servicoId")).extracting(JsonNode::asInt)
                .contains(servicoId)
                .doesNotContain(12); // prestador de Sarandi com raio de 4 km não alcança a Zona 07
        double anterior = 0;
        for (JsonNode r : resultados) {
            assertThat(r.get("valorBase").asDouble()).isGreaterThanOrEqualTo(anterior);
            anterior = r.get("valorBase").asDouble();
        }
        chamar(get("/api/servicos/busca?categoria=Elétrica&notaMin=4.4&distanciaMaxKm=3"), cliente, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].notaMedia").value(org.hamcrest.Matchers.everyItem(
                        org.hamcrest.Matchers.greaterThanOrEqualTo(4.4))))
                .andExpect(jsonPath("$[*].distanciaKm").value(org.hamcrest.Matchers.everyItem(
                        org.hamcrest.Matchers.lessThanOrEqualTo(3.0))));

        // RF05: abertura (polimorfismo no valor) e transições
        JsonNode ordem = corpo(chamar(post("/api/ordens"), cliente, Map.of("servicoId", servicoId))
                .andExpect(status().isCreated()));
        int ordemId = ordem.get("id").asInt();
        assertThat(ordem.get("status").asText()).isEqualTo("ABERTA");
        assertThat(ordem.get("valorFinal").decimalValue()).isEqualByComparingTo("150.00");

        chamar(patch("/api/ordens/" + ordemId + "/status"), cliente, Map.of("status", "ACEITA"))
                .andExpect(status().isForbidden());
        chamar(post("/api/ordens/" + ordemId + "/avaliacao"), cliente, Map.of("nota", 5))
                .andExpect(status().isUnprocessableEntity());
        chamar(patch("/api/ordens/" + ordemId + "/status"), prestador, Map.of("status", "CONCLUIDA"))
                .andExpect(status().isUnprocessableEntity());
        for (String s : new String[]{"ACEITA", "EM_EXECUCAO", "CONCLUIDA"}) {
            chamar(patch("/api/ordens/" + ordemId + "/status"), prestador, Map.of("status", s))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(s));
        }
        chamar(get("/api/ordens/" + ordemId), cliente, null)
                .andExpect(jsonPath("$.historico.length()").value(4))
                .andExpect(jsonPath("$.podeAvaliar").value(true));

        // outro prestador não enxerga a ordem
        chamar(get("/api/ordens/" + ordemId), login("paulo@email.com"), null).andExpect(status().isForbidden());

        // oferta com ordem não pode ser excluída
        chamar(delete("/api/servicos/" + servicoId), prestador, null).andExpect(status().isConflict());

        // RF06 / RNF04: uma avaliação por ordem, média recalculada (5,4,5,4 + 2 = 4,00)
        chamar(post("/api/ordens/" + ordemId + "/avaliacao"), cliente, Map.of("nota", 2, "comentario", "Atrasou"))
                .andExpect(status().isCreated());
        chamar(post("/api/ordens/" + ordemId + "/avaliacao"), cliente, Map.of("nota", 5))
                .andExpect(status().isConflict());
        chamar(get("/api/prestadores/4"), cliente, null)
                .andExpect(jsonPath("$.notaMedia").value(4.00))
                .andExpect(jsonPath("$.quantidadeAvaliacoes").value(5));
        assertThatThrownBy(() -> jdbc.update("UPDATE avaliacoes SET nota = 5 WHERE ordem_id = ?", ordemId))
                .hasMessageContaining("RNF04");

        // RF07
        chamar(get("/api/relatorios/prestador"), prestador, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ordensConcluidas[?(@.ordemId == " + ordemId + ")]").exists())
                .andExpect(jsonPath("$.evolucaoNotaMedia.length()").value(org.hamcrest.Matchers.greaterThan(0)));
        chamar(get("/api/relatorios/prestador"), cliente, null).andExpect(status().isForbidden());

        // RF07: exportação segura (só o próprio prestador, com token, sem cache)
        var exportacao = chamar(get("/api/relatorios/prestador/exportar"), prestador, null)
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("Content-Disposition", org.hamcrest.Matchers.startsWith("attachment")))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("Cache-Control", org.hamcrest.Matchers.containsString("no-store")))
                .andReturn().getResponse();
        assertThat(exportacao.getContentType()).startsWith("text/csv");
        String csv = new String(exportacao.getContentAsByteArray(), java.nio.charset.StandardCharsets.UTF_8);
        assertThat(csv).contains("Douglas Oliveira").contains(String.format("OS-%05d", ordemId)).contains("150,00")
                .doesNotContain("Sérgio Andrade");
        chamar(get("/api/relatorios/prestador/exportar"), cliente, null).andExpect(status().isForbidden());
        mvc.perform(get("/api/relatorios/prestador/exportar")).andExpect(status().isUnauthorized());

        // RF02: Delete de uma oferta nunca contratada
        int descartavel = corpo(chamar(post("/api/servicos"), prestador, servico("POR_HORA", "40.00")))
                .get("id").asInt();
        chamar(delete("/api/servicos/" + descartavel), prestador, null).andExpect(status().isNoContent());
        chamar(get("/api/servicos/" + descartavel), prestador, null).andExpect(status().isNotFound());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM servicos WHERE id = ?", Integer.class, descartavel))
                .isZero();
    }

    @Test
    void cadastroValidaEmailTelefoneEUnicidade() throws Exception {
        Map<String, Object> invalido = Map.of("nome", "Teste", "email", "sem-arroba", "telefone", "123",
                "senha", "segredo1", "latitude", -23.42, "longitude", -51.93, "perfil", "CLIENTE");
        chamar(post("/api/auth/cadastro"), null, invalido)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.email").exists())
                .andExpect(jsonPath("$.campos.telefone").exists());

        Map<String, Object> valido = Map.of("nome", "Novo Prestador", "email", "novo@email.com",
                "telefone", "(44) 99123-4567", "senha", "segredo1", "latitude", -23.42, "longitude", -51.93,
                "perfil", "PRESTADOR");
        chamar(post("/api/auth/cadastro"), null, valido)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario.perfil").value("PRESTADOR"))
                .andExpect(jsonPath("$.token").isNotEmpty());
        chamar(post("/api/auth/cadastro"), null, valido).andExpect(status().isConflict());
        assertThat(jdbc.queryForObject("SELECT senha_hash FROM usuarios WHERE email = 'novo@email.com'", String.class))
                .startsWith("$2a$");

        chamar(post("/api/auth/login"), null, Map.of("email", "novo@email.com", "senha", "errada"))
                .andExpect(status().isUnauthorized());
    }

    // ---------- apoio ----------

    private Map<String, Object> servico(String tipo, String valor) {
        return Map.of("categoria", "Elétrica",
                "descricao", "Instalação de tomadas (teste automatizado)", "raioAtendimentoKm", 10, "tipoCobranca", tipo,
                "valorBase", new java.math.BigDecimal(valor));
    }

    private String login(String email) throws Exception {
        return corpo(chamar(post("/api/auth/login"), null, Map.of("email", email, "senha", "senha123"))
                .andExpect(status().isOk())).get("token").asText();
    }

    private ResultActions chamar(MockHttpServletRequestBuilder requisicao, String token, Object corpo) throws Exception {
        if (token != null) requisicao.header("Authorization", "Bearer " + token);
        if (corpo != null) requisicao.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(corpo));
        return mvc.perform(requisicao);
    }

    private JsonNode corpo(ResultActions resultado) throws Exception {
        return json.readTree(resultado.andReturn().getResponse().getContentAsString());
    }
}
