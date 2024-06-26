package com.gonzalo.libros.repository;

import com.gonzalo.libros.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LibroRepository extends JpaRepository<Libro, Long> {
    @Query("SELECT l FROM Libro l WHERE l.autor.id = :autorId")
    List<Libro> findLibrosByAutorId(@Param("autorId") Long autorId);

    @Query(value = "SELECT * FROM libros WHERE :idioma = ANY(idiomas)", nativeQuery = true)
    List<Libro> findLibrosByIdioma(@Param("idioma") String idioma);
}
