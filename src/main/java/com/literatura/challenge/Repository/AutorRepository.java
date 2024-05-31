package com.literatura.challenge.Repository;

import com.literatura.challenge.Model.Autor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AutorRepository extends JpaRepository<Autor, Long>{
    List<Autor> findAllByOrderByNombreAsc();
    Optional<Autor> findByNombre(String nombre);

    @Query("SELECT a FROM Autor a WHERE a.nacimiento < :anio AND a.obito > :anio ORDER BY a.nombre ASC")
    List<Autor> obtenerAutorVivoAnio(int anio);
}

