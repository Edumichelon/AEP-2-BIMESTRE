package br.unicesumar.maridodealuguel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

// Sem o usuário em memória padrão do Spring: a autenticação é só por JWT (ver security/)
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class MaridoDeAluguelApplication {

    public static void main(String[] args) {
        SpringApplication.run(MaridoDeAluguelApplication.class, args);
    }
}
