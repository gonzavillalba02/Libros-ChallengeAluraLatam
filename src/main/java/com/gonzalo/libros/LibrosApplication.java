package com.gonzalo.libros;

import com.gonzalo.libros.main.Main;
import com.gonzalo.libros.repository.AutorRepository;
import com.gonzalo.libros.repository.LibroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LibrosApplication implements CommandLineRunner  {

	@Autowired
	private LibroRepository libroRepository;
	@Autowired
	private AutorRepository autorRepository;

	public static void main(String[] args) {
		SpringApplication.run(LibrosApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		Main main = new Main(libroRepository, autorRepository);
		main.muestraElMenu();
	}
}
