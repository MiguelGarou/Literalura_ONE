package com.literatura.challenge.Principal;

import com.literatura.challenge.Model.Autor;
import com.literatura.challenge.Model.Datos;
import com.literatura.challenge.Model.DatosAutor;
import com.literatura.challenge.Model.DatosIdioma;
import com.literatura.challenge.Model.DatosLibro;
import com.literatura.challenge.Model.Idioma;
import com.literatura.challenge.Model.Libro;
import com.literatura.challenge.Repository.AutorRepository;
import com.literatura.challenge.Repository.LibroRepository;
import com.literatura.challenge.Service.ConsumoAPI;
import com.literatura.challenge.Service.ConvierteDatos;
//import org.antlr.v4.runtime.InputMismatchException;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Principal {
    private static final String URL_BASE= "https://gutendex.com/books/";
    private static final String URL_LANGUAGE_CODE = "https://wiiiiams-c.github.io/language-iso-639-1-json-spanish/language-iso-639-1.json";
    private ConsumoAPI consumoApi = new ConsumoAPI();
    private ConvierteDatos conversor = new ConvierteDatos();
    private Scanner teclado = new Scanner(System.in);
    private LibroRepository repositoryLibro;
    private AutorRepository repositoryAutor;
    private Libro libro;
    private List<Libro> libros;
    private List<Autor> autores;
    private Optional<Autor> autor;

    public Principal(AutorRepository repositoryAutor, LibroRepository repositoryLibro) {
        this.repositoryAutor = repositoryAutor;
        this.repositoryLibro = repositoryLibro;
    }

    public void inicio() {
        var muestraMenu = -1;

        while (muestraMenu != 0) {
            var menu = """
                                    ¡Bienvenido!
                         selecione la opción que dese marcar:
                    1) Buscar libro por titulo
                    2) Listar libros registrados
                    3) Listar autores registrados
                    4) Listar autores vivos por un determinado año
                    5) Listar libros por idiomas
                    6) Top 10 libros más descargados
                    7) Estadistícas
                    
                    0) Salir
                    """;
            try {
                System.out.println(menu);
                muestraMenu = teclado.nextInt();
                teclado.nextLine();
            } catch (InputMismatchException e) {
                teclado.nextLine();
                muestraMenu = -1;
            }

            switch (muestraMenu) {
                case 0:
                    System.out.println("- - - - - -Saliendo del Programa- - - - - -");
                    break;
                case 1:
                    buscarLibro();
                    break;
                case 2:
                    listarLibros();
                    break;
                case 3:
                    listarAutores();
                    break;
                case 4:
                    consultarAutoresVivosPorUnAnio();
                    break;
                case 5:
                    consultarlibrosPorIdioma();
                case 6:
                    top10Libros();
                    break;
                case 7:
                    muestraEstadisticas();
                    break;
                default:
                    System.out.println("Opción invalida");


            }
        }
    }

    private void muestraEstadisticas() {
        libros = repositoryLibro.findAll();
        var cabezaEstadistica = """
                        Estadisticas
                
                Total de libros         : %s
                Libros más descargados  : %s
                Libros menos descargados: %s
                Media de descargas      : %s
                """;
        LongSummaryStatistics estadisticas = libros.stream()
                .filter(l -> l.getNumeroDescargas() > 0)
                .collect(Collectors.summarizingLong(Libro::getNumeroDescargas));

        System.out.println(cabezaEstadistica.formatted(
                estadisticas.getCount(),
                estadisticas.getMax() + " -> " + libros.get(0).getTitulo(),
                estadisticas.getMin() + " -> " + libros.get(9).getTitulo(),
                Math.round(estadisticas.getAverage())
        ));
    }

    private void top10Libros() {
        libros = repositoryLibro.findTop10ByOrderByNumeroDescargasDesc();
        if (libros.isEmpty()) {
            System.out.println("No hay libros en este top");
        } else {
            var cabezaTop = """
                    Top 10 libros más descargados
                    """;
            System.out.println("\n" + cabezaTop + "\n");
            datosLibros(libros);
        }
    }

    private void listarLibros() {
        libros = repositoryLibro.findAll();
        var cabezaListarLibros = """
                Libros Almacenados
                """;
        System.out.println("\n" + cabezaListarLibros + "\n");
        if (libros.isEmpty()) {
            System.out.println("No hay libros registrados");
        } else {
            var cuentaLibros = libros.size();
            datosLibros(libros);
            System.out.println("Total de libro: %s\n".formatted(cuentaLibros));
        }
    }

    private void consultarAutoresVivosPorUnAnio() {
        try {
            System.out.println("Ingrese el año a consultar:");
            int anio = teclado.nextInt();
            autores = repositoryAutor.obtenerAutorVivoAnio(anio);
            var cabezaAnioAutor = """
                    Atores vivos
                    """;
            System.out.println("\n" + cabezaAnioAutor);
            if (autores.isEmpty()) {
                System.out.println("No hay autores vivos en el año ingresado");
            } else {
                datosAutor(autores);
                System.out.println("Total de autores: %s\n".formatted(autores.size()));
            }
        } catch (InputMismatchException e) {
            System.out.println("Año ingresado no valido");
            teclado.nextLine();
        }
    }

    private void consultarlibrosPorIdioma() {
        var idiomasLibro = repositoryLibro.obtenerListaUnicaIdioma();
        var jsonIdiomas = consumoApi.obtenerDatos(URL_LANGUAGE_CODE);
        var datosIdioma = conversor.obtenerDatos(jsonIdiomas, DatosIdioma.class);
        List<Idioma> idiomaDisponible = new ArrayList<>();

        if (idiomasLibro.isEmpty()) {
            System.out.println("Libro no encontrado por idioma");
        } else {
            for (String codigoidioma : idiomasLibro) {
                var d = datosIdioma.idiomas().stream().filter(i -> i.codigoIdioma().contains(codigoidioma)).collect(Collectors.toList());
                idiomaDisponible.add(d.get(0));
            }

            var cabezaListaIdioma = """
                    Lista de idiomas disponibles
                    """;
            System.out.println("\n" + cabezaListaIdioma);
            idiomaDisponible.forEach(i -> System.out.println(i.codigoIdioma() + " - " + i.idioma()));
            System.out.println("\n Escriba la sigla del idioma que desea buscar...\n");
            String inputCoIdioma = teclado.nextLine();
            if (visionInputTeclado(inputCoIdioma)) {
                System.out.println("\n Codgigo incorrecto, Ingrese la sigla correcta \n");
            } else {
                libros = repositoryLibro.findByIdioma(inputCoIdioma);

                if (libros.isEmpty()) {
                    System.out.println("\n No se encontraron libros con ese idioma \n");

                } else {
                    var cuentaLibros = libros.size();
                    datosLibros(libros);
                    System.out.println("\n Total de libros: %s \n".formatted(cuentaLibros));
                }
            }
        }
    }

    private void listarAutores() {
        var cabezaListarAutores = """
                Autores registrados
                """;
        System.out.println("\n" + cabezaListarAutores + "\n");
        autores = repositoryAutor.findAllByOrderByNombreAsc();
        if (autores.isEmpty()) {
            System.out.println("\n No hay autores registrados \n");
        } else {
            var cuentaAutores = autores.size();
            datosAutor(autores);
            System.out.println("Total de autores: %s\n".formatted(cuentaAutores));
        }
    }

    private void buscarLibro() {
        System.out.println("\n Ingrese el nombre del libro: ");

        var nombreLibro = teclado.nextLine();
        var json = consumoApi.obtenerDatos(URL_BASE + "?search=" + nombreLibro.replace(" ", "+"));
        var datosBuscador = conversor.obtenerDatos(json, Datos.class);

        Optional<DatosLibro> libroBuscado = datosBuscador.libros().stream()
                .filter(l -> l.titulo().toLowerCase().contains(nombreLibro.toLowerCase()))
                .findFirst();
        if (libroBuscado.isPresent()) {

            DatosAutor datosAutor = libroBuscado.get().autor().get(0);
            if (repositoryLibro.findByIdLibro(libroBuscado.get().idlibro()).isPresent()) {
                System.out.println("\n El libro se encuentra guardado \n");
            } else {
                System.out.println("\n Libro encontrado \n");
                var libroCabezaEncontrado = """
                        Datos del libro
                        
                        Titulo      : %s
                        Autor       : %s
                        Idiomas     : %s
                        Descargas   :%s
                        """;
                System.out.println(libroCabezaEncontrado.formatted(
                        libroBuscado.get().titulo(),
                        libroBuscado.get().autor().get(0).nombre(),
                        libroBuscado.get().idioma().get(0),
                        libroBuscado.get().numeroDescargas()
                ));

                autor = repositoryAutor.findByNombre(datosAutor.nombre());

                if (autor.isPresent()) {
                    libro = new Libro(libroBuscado.get());
                    libro.setAutor(autor.get());
                    repositoryLibro.save(libro);
                } else {
                    libros = libroBuscado.stream()
                            .map(l -> new Libro(l))
                            .collect(Collectors.toList());
                    Autor autorClass = new Autor(datosAutor);
                    autorClass.setLibros(libros);
                    repositoryAutor.save(autorClass);
                }

                System.out.println("\n Libro guardado \n");
            }
        } else {
            System.out.println("\n No se encontró el libro, [BUSQUEDA ERROR] \n");
        }
    }

    private void datosLibros(List<Libro> listado) {
        var muestraLibro = """
                Datos del libro
                
                Titulo      :%s
                Autor       :%s
                Idiomas     :%s
                Descargas   :%s
                """;
        listado.forEach(l -> System.out.println(
                muestraLibro.formatted(
                        l.getTitulo(),
                        l.getAutor().getNombre(),
                        l.getIdioma(),
                        l.getNumeroDescargas()
                )
        ));
    }

    private void datosAutor(List<Autor> autorLista) {
        var muestraAutor = """
                Datos del autor
                
                Nombre      :%s
                Nacimiento  :%s
                Óbito       :%s
                """;
        autorLista.forEach(a -> System.out.println(
                muestraAutor.formatted(
                        a.getNombre(),
                        (a.getNacimiento()==null?"N/D":a.getNacimiento()),
                        (a.getObito()==null?"N/D":a.getObito()),
                        repositoryLibro.obtenerLibrosPorAutor(a.getIdAutor()).stream()
                                .map(l -> l.getTitulo())
                                .collect(Collectors.joining( "|", "[", "]"))
                )
        ));
    }

    private boolean visionInputTeclado(String inputTeclado) {
        Pattern regularExpresion = Pattern.compile("^[\\+-]?\\d+$");
        Matcher hacerMatch = regularExpresion.matcher(inputTeclado);
        return hacerMatch.matches();
    }
}