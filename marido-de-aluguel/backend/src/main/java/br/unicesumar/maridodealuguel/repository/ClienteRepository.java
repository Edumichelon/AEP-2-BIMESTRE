package br.unicesumar.maridodealuguel.repository;

import br.unicesumar.maridodealuguel.domain.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
}
