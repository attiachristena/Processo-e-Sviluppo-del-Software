package dev.danvega.books;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final List<Book> books = new ArrayList<>();
    private final List<Author> authors = new ArrayList<>();

    @PostConstruct
    public void loadData() {
        // Core Java Authors
        Author joshuaBloch = new Author("1", "Joshua Bloch");
        Author herbertSchildt = new Author("2", "Herbert Schildt");
        Author raoulgabrielUrma = new Author("3", "Raoul-Gabriel Urma");
        Author brianGoetz = new Author("4", "Brian Goetz");

        // Spring Framework Authors
        Author craigWalls = new Author("5", "Craig Walls");
        Author gregTurnquist = new Author("6", "Greg Turnquist");
        Author markHeckler = new Author("7", "Mark Heckler");
        Author thomasVitale = new Author("8", "Thomas Vitale");
        Author joshLong = new Author("9", "Josh Long");

        // Spring Ecosystem Authors
        Author dineshRajput = new Author("10", "Dinesh Rajput");
        Author johnCarnell = new Author("11", "John Carnell");
        Author laurentiuSpilca = new Author("12", "Laurentiu Spilca");
        Author petriKainulainen = new Author("13", "Petri Kainulainen");

        // Architecture & Design Authors
        Author rodJohnson = new Author("14", "Rod Johnson");
        Author martinFowler = new Author("15", "Martin Fowler");
        Author nealFord = new Author("16", "Neal Ford");

        // Modern Java Authors
        Author kenKousen = new Author("17", "Ken Kousen");
        Author dmitryJemerov = new Author("18", "Dmitry Jemerov");
        Author venkatSubramaniam = new Author("19", "Venkat Subramaniam");

        // Testing & Best Practices Authors
        Author petarTahchiev = new Author("20", "Petar Tahchiev");
        Author robertMartin = new Author("21", "Robert C. Martin");
        Author andrewHunt = new Author("22", "Andrew Hunt");

        // Add all authors to the collection
        authors.addAll(List.of(
                joshuaBloch, herbertSchildt, raoulgabrielUrma, brianGoetz,
                craigWalls, gregTurnquist, markHeckler, thomasVitale, joshLong,
                dineshRajput, johnCarnell, laurentiuSpilca, petriKainulainen,
                rodJohnson, martinFowler, nealFord,
                kenKousen, dmitryJemerov, venkatSubramaniam,
                petarTahchiev, robertMartin, andrewHunt
        ));

        // Create books
        books.addAll(List.of(
                // Core Java
                new Book("1", "Effective Java", joshuaBloch, 2017),
                new Book("2", "Java: The Complete Reference", herbertSchildt, 2021),
                new Book("3", "Modern Java in Action", raoulgabrielUrma, 2018),
                new Book("4", "Java Concurrency in Practice", brianGoetz, 2006),

                // Spring Framework & Boot
                new Book("5", "Spring in Action", craigWalls, 2020),
                new Book("6", "Spring Boot in Action", craigWalls, 2015),
                new Book("7", "Learning Spring Boot 3.0", gregTurnquist, 2022),
                new Book("8", "Spring Boot: Up and Running", markHeckler, 2021),
                new Book("9", "Cloud Native Spring in Action", thomasVitale, 2021),
                new Book("10", "Reactive Spring", joshLong, 2020),

                // Spring Ecosystem & Microservices
                new Book("11", "Building Microservices with Spring Boot", dineshRajput, 2020),
                new Book("12", "Spring Microservices in Action", johnCarnell, 2021),
                new Book("13", "Spring Security in Action", laurentiuSpilca, 2020),
                new Book("14", "Spring Data JPA", petriKainulainen, 2019),

                // Architecture & Design Patterns
                new Book("15", "Expert One-on-One J2EE Design and Development", rodJohnson, 2002),
                new Book("16", "Patterns of Enterprise Application Architecture", martinFowler, 2002),
                new Book("17", "Refactoring", martinFowler, 2019),
                new Book("18", "Building Evolutionary Architectures", nealFord, 2017),

                // Modern Java Development
                new Book("19", "Modern Java Recipes", kenKousen, 2017),
                new Book("20", "Kotlin in Action", dmitryJemerov, 2017),
                new Book("21", "Java 8 in Action", raoulgabrielUrma, 2014),
                new Book("22", "Functional Programming in Java", venkatSubramaniam, 2014),

                // Testing & Best Practices
                new Book("23", "JUnit in Action", petarTahchiev, 2020),
                new Book("24", "Clean Code", robertMartin, 2008),
                new Book("25", "The Pragmatic Programmer", andrewHunt, 2019)
        ));
    }

    public List<Book> findAll() {
        return List.of(books.toArray(new Book[0]));
    }

    public Book findById(String id) {
        return books.stream()
                .filter(book -> book.id().equals(id))
                .findFirst()
                .orElse(null);
    }

    public List<Author> findAllAuthors() {
        return List.of(authors.toArray(new Author[0]));
    }

    public Author findAuthorById(String id) {
        return authors.stream()
                .filter(author -> author.id().equals(id))
                .findFirst()
                .orElse(null);
    }

    public List<Book> findBooksByAuthorIds(List<String> authorIds) {
        return books.stream()
                .filter(book -> authorIds.contains(book.author().id()))
                .collect(Collectors.toList());
    }
}
