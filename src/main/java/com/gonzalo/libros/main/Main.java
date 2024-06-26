package com.gonzalo.libros.main;

import com.gonzalo.libros.model.*;
import com.gonzalo.libros.repository.AutorRepository;
import com.gonzalo.libros.repository.LibroRepository;
import com.gonzalo.libros.service.ConsumoAPI;
import com.gonzalo.libros.service.ConvierteDatos;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
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
                    -----------------------------------------------
                    Elija la opción a través de su número:
                    1- Buscar libro por titulo
                    2- Listar libros registrados
                    3- Listar autores registrados
                    4- Listar autroes vivos en un determinado año
                    5- Listar libros por idioma
                    
                    0- Salir
                    -----------------------------------------------""");
            try{
                op = sc.nextInt();
                sc.nextLine();
                switch(op){
                    case 1:
                        buscarLibroPorNombre();
                        break;
                    case 2:
                        mostrarLibrosRegistrados();
                        break;
                    case 3:
                        mostrarAutoresRegistrados();
                        break;
                    case 4:
                        mostrarAutoresVivos();
                        break;
                    case 5:
                        mostrarLibrosPorIdioma();
                        break;
                    case 0:
                        System.out.println("Cerrando aplicación...");
                        return;
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

    public void mostrarAutoresRegistrados(){
        autorRepository.findAll().forEach( a -> {
                System.out.println(String.format("""
                        Autor: %s
                        Fecha de nacimiento: %d
                        Fecha de muerte: %d
                        Libros: %s\n""",
                        a.getNombre(),
                        a.getAnoNacimiento(),
                        a.getAnoMuerte(),
                        traerLibrosDeUnAutor(a.getId())));
        });
    }

    public String traerLibrosDeUnAutor(Long id){
        List<Libro> libros = libroRepository.findLibrosByAutorId(id);
        final String[] lista = {"["};
        libros.forEach(l -> lista[0] += (l.getTitulo() + ", "));
        if (lista[0].length() > 1) {
            lista[0] = lista[0].substring(0, lista[0].length() - 2);
        }
        lista[0] += "]";
        return lista[0];
    }

    public void mostrarAutoresVivos(){
        System.out.println("Ingrese año: ");
        int ano = sc.nextInt();
        List<Autor> autores = autorRepository.findAutorByAno(ano);
        autores.stream().forEach( a -> {
            System.out.println(String.format("""
                            Autor: %s
                            Fecha de nacimiento: %d
                            Fecha de muerte: %d
                            Libros: %s\n""",
                    a.getNombre(),
                    a.getAnoNacimiento(),
                    a.getAnoMuerte(),
                    traerLibrosDeUnAutor(a.getId())));
        });
    }

    public void mostrarLibrosPorIdioma(){
        System.out.println("""
                Ingrese el idioma para buscar los libros:
                es- español
                en- inglés
                fr- francés
                pt- portugués""");
        String idioma = sc.nextLine();
        List<Libro> libros = libroRepository.findLibrosByIdioma(idioma);
        if(libros.size() > 0){
            libros.stream().forEach(
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
        } else {
            System.out.println("Ningun libro encontrado");
        }

        }
}

