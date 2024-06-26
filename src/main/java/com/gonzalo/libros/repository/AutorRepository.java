package com.gonzalo.libros.repository;

import com.gonzalo.libros.model.Autor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AutorRepository extends JpaRepository<Autor, Long> {
    Optional<Autor> findByNombre(String nombre);

    @Query("SELECT a FROM Autor a WHERE a.anoNacimiento <= :ano AND a.anoMuerte >= :ano")
    List<Autor> findAutorByAno(@Param("ano") int ano);
}
