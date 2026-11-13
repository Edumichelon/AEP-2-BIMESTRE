package br.unicesumar.maridodealuguel.config;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Perfil "local": sobe um PostgreSQL real (binários oficiais embutidos) dentro da própria aplicação,
 * para rodar o projeto sem Docker e sem instalar o PostgreSQL. O banco é recriado a cada execução
 * a partir de database/schema.sql e database/seed.sql.
 */
@Configuration
@Profile("local")
public class BancoLocalConfig {

    private static final Logger LOG = LoggerFactory.getLogger(BancoLocalConfig.class);

    @Bean(destroyMethod = "close")
    public EmbeddedPostgres postgresLocal(@Value("${app.local.scripts:../database}") String pastaScripts,
                                          @Value("${app.local.porta:0}") int porta)
            throws IOException, SQLException {
        Path scripts = Path.of(pastaScripts);
        EmbeddedPostgres.Builder builder = EmbeddedPostgres.builder();
        if (porta > 0) {
            builder.setPort(porta); // 0 = porta livre escolhida a cada execução
        }
        EmbeddedPostgres postgres = builder.start();
        try (Connection conexao = postgres.getPostgresDatabase().getConnection();
             Statement sql = conexao.createStatement()) {
            sql.execute(Files.readString(scripts.resolve("schema.sql")));
            sql.execute(Files.readString(scripts.resolve("seed.sql")));
        }
        LOG.info("PostgreSQL local pronto na porta {} (schema.sql + seed.sql de {})",
                postgres.getPort(), scripts.toAbsolutePath().normalize());
        return postgres;
    }

    @Bean
    public DataSource dataSource(EmbeddedPostgres postgresLocal) {
        return postgresLocal.getPostgresDatabase();
    }
}
