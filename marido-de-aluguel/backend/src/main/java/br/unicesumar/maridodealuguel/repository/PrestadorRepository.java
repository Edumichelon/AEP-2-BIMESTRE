package br.unicesumar.maridodealuguel.repository;

import br.unicesumar.maridodealuguel.domain.Prestador;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrestadorRepository extends JpaRepository<Prestador, Integer> {
}
