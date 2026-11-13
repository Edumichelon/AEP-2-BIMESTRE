package br.unicesumar.maridodealuguel;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * RNF01: a busca com filtros deve responder em até 2 segundos com 5.000 prestadores cadastrados.
 * Gera a carga num PostgreSQL real (schema.sql + seed.sql + 5.000 prestadores com 2 serviços cada).
 */
@SpringBootTest
@AutoConfigureMockMvc
class DesempenhoBuscaTest {

    private static final int PRESTADORES = 5_000;
    private static final long LIMITE_MS = 2_000;
    private static final EmbeddedPostgres POSTGRES = iniciarBancoComCarga();

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;

    private static EmbeddedPostgres iniciarBancoComCarga() {
        try {
            EmbeddedPostgres pg = EmbeddedPostgres.builder().start();
            try (Connection conexao = pg.getPostgresDatabase().getConnection();
                 Statement sql = conexao.createStatement()) {
                sql.execute(Files.readString(Path.of("../database/schema.sql")));
                sql.execute(Files.readString(Path.of("../database/seed.sql")));
                // prestadores espalhados num raio de ~10 km do centro de Maringá
                sql.execute("""
                        INSERT INTO usuarios (nome, email, telefone, senha_hash, endereco, latitude, longitude,
                                              tipo_usuario, nota_media, quantidade_avaliacoes)
                        SELECT 'Prestador de carga ' || g, 'carga' || g || '@teste.com', '(44) 99999-0000', 'x',
                               'Maringá', -23.42 + (random() - 0.5) * 0.18, -51.93 + (random() - 0.5) * 0.18,
                               'PRESTADOR', round((random() * 5)::numeric, 2), 10
                        FROM generate_series(1, %d) g""".formatted(PRESTADORES));
                sql.execute("""
                        INSERT INTO servicos (prestador_id, categoria, descricao, raio_atendimento_km, tipo_cobranca, valor_base)
                        SELECT u.id, (ARRAY['Elétrica', 'Hidráulica', 'Pintura', 'Montagem de móveis'])[1 + (u.id + n) % 4],
                               'Serviço de carga ' || u.id || '-' || n, 3 + (u.id % 20),
                               CASE WHEN (u.id + n) % 2 = 0 THEN 'POR_HORA' ELSE 'POR_EMPREITADA' END,
                               40 + ((u.id * 7 + n) % 400)
                        FROM usuarios u CROSS JOIN generate_series(1, 2) n
                        WHERE u.email LIKE 'carga%@teste.com'""");
                sql.execute("ANALYZE");
            }
            return pg;
        } catch (IOException | SQLException e) {
            throw new IllegalStateException("Falha ao preparar a carga de teste", e);
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
    void buscaComFiltrosRespondeEmAteDoisSegundos() throws Exception {
        String token = json.readTree(mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", "camila@email.com", "senha", "senha123"))))
                .andReturn().getResponse().getContentAsString()).get("token").asText();

        String[] buscas = {
                "categoria=Elétrica&ordenarPor=DISTANCIA",
                "categoria=Elétrica&precoMin=50&precoMax=300&notaMin=3&distanciaMaxKm=10&ordenarPor=PRECO",
                "categoria=Hidráulica&notaMin=4&ordenarPor=NOTA&horas=3",
                "categoria=Pintura&distanciaMaxKm=5&ordenarPor=DISTANCIA",
        };
        mvc.perform(get("/api/servicos/busca?" + buscas[0]).header("Authorization", "Bearer " + token)); // aquecimento

        for (String filtros : buscas) {
            long inicio = System.nanoTime();
            String corpo = mvc.perform(get("/api/servicos/busca?" + filtros).header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            long ms = (System.nanoTime() - inicio) / 1_000_000;
            System.out.printf("RNF01 | %d prestadores | %-90s | %4d ms | %d resultados%n",
                    PRESTADORES, filtros, ms, json.readTree(corpo).size());
            assertThat(ms).as("tempo da busca '%s'", filtros).isLessThan(LIMITE_MS);
        }
    }
}
