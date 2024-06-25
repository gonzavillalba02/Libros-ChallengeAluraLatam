package com.gonzalo.libros.main;

import com.gonzalo.libros.model.*;
import com.gonzalo.libros.repository.AutorRepository;
import com.gonzalo.libros.repository.LibroRepository;
import com.gonzalo.libros.service.ConsumoAPI;
import com.gonzalo.libros.service.ConvierteDatos;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;
import java.util.Scanner;

public class Main {
    private Scanner sc = new Scanner(System.in);
    private ConsumoAPI consumoApi = new ConsumoAPI();
    private ConvierteDatos conversor = new ConvierteDatos();
    private final String URL_BASE = "https://gutendex.com/books/";
    private LibroRepository libroRepository;
    private AutorRepository autorRepository;

    public Main(LibroRepository libroRepository, AutorRepository autorRepository){
        this.libroRepository = libroRepository;
        this.autorRepository = autorRepository;
    }

    public void muestraElMenu(){
        var json = consumoApi.obtenerJson(URL_BASE);
        Datos datos = conversor.obtenerDatos(json, Datos.class);

        int op = -1;
        while(op != 0){
            System.out.println("""
                    Elija la opción a través de su número:
                    1- Buscar libro por titulo
                    2- Listar libros registrados
                    3- Listar autores registrados
                    4- Listar autroes vivos en un determinado año
                    5- Listar libros por idioma
                    
                    0- Salir""");
            try{
                op = sc.nextInt();
                sc.nextLine();
                switch(op){
                    case 1:
                        buscarLibroPorNombre();
                        break;
                    case 2:
                        mostrarLibrosRegistrados();
                    case 0:
                        System.out.println("Cerrando aplicación...");
                        break;
                    default:
                        System.out.println("Opción invalida");
                        break;
                }
            } catch (NumberFormatException e){
                sc.next();
                System.out.println("Ingrese una opción valida");
            }
        }
    }

    public void buscarLibroPorNombre(){
        System.out.println("Ingrese el nombre del libro que desea buscar");
        String tituloLibro = sc.nextLine();
        String json = consumoApi.obtenerJson(URL_BASE+"?search=" + tituloLibro.replace(" ","+"));
        Datos datosBusqueda = conversor.obtenerDatos(json, Datos.class);
        Optional<DatosLibro> libroBuscado = datosBusqueda.libros().stream()
                .filter(l -> l.titulo().toUpperCase().contains(tituloLibro.toUpperCase()))
                .findFirst();
        if(libroBuscado.isPresent()){
            Optional<DatosAutor> datosAutor = libroBuscado.get().autor().stream().findFirst();
            if(datosAutor.isPresent()) {
                Autor autor = autorRepository.findByNombre(datosAutor.get().nombre())
                        .orElse(new Autor(datosAutor.get()));
                Libro libro = new Libro(libroBuscado.get());
                libro.setAutor(autor);
                autor.getLibros().add(libro);
                try {
                    autorRepository.save(autor);
                    libroRepository.save(libro);
                    System.out.println(String.format("""
                                    ----- LIBRO -----
                                    Titulo : %s
                                    Autor: %s
                                    Idioma: %s
                                    Numero de descargas: %d
                                    -----------------""",
                            libroBuscado.get().titulo(),
                            libro.getAutor().getNombre(),
                            libroBuscado.get().idiomas().toString(),
                            libroBuscado.get().descargas()));
                } catch (DataIntegrityViolationException e){
                    System.out.println("Libro ya registrado");
                }
            }
        }else {
            System.out.println("Libro no encontrado");
        }
    }

    public void mostrarLibrosRegistrados(){
        libroRepository.findAll().forEach(
                l -> System.out.println(String.format("""
                        ----- LIBRO -----
                        Titulo : %s
                        Autor: %s
                        Idioma: %s
                        Numero de descargas: %d
                        -----------------\n""",
                        l.getTitulo(),
                        l.getAutor().getNombre(),
                        l.getIdiomas().toString(),
                        l.getDescargas())));
    }
}

