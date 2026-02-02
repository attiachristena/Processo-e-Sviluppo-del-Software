package dev.danvega.books;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

//    @QueryMapping
//    public List<Book> books() {
//        return bookService.findAll();
//    }

    @QueryMapping
    public Book book(@Argument String id) {
        return bookService.findById(id);
    }

    @QueryMapping
    public List<Book> books(@Argument BookFilter filter) {
        if (filter == null) {
            return bookService.findAll();
        }

        return bookService.findAll().stream()
                .filter(book -> matchesFilter(book, filter))
                .collect(Collectors.toList());
    }

    // This creates the N+1 problem!
//    @SchemaMapping
//    public List<Book> books(Author author) {
//        System.out.println("Loading books for author: " + author.name());
//        return bookService.findAll().stream()
//                .filter(book -> book.author().id().equals(author.id()))
//                .collect(Collectors.toList());
//    }

    @BatchMapping
    public Map<Author, List<Book>> books(List<Author> authors) {
        System.out.println("🚀 BATCH LOADING books for " + authors.size() + " authors in ONE call!");

        List<String> authorIds = authors.stream()
                .map(Author::id)
                .toList();

        List<Book> allBooks = bookService.findBooksByAuthorIds(authorIds);
        System.out.println("✅ Loaded " + allBooks.size() + " books total");
        Map<Author, List<Book>> booksByAuthor = allBooks.stream()
                .collect(Collectors.groupingBy(Book::author));

        // Ensure every author has an entry, even if empty
        for (Author author : authors) {
            booksByAuthor.putIfAbsent(author, Collections.emptyList());
        }

        return booksByAuthor;
    }

    @QueryMapping
    public List<Object> search(@Argument String text) {
        List<Object> results = new ArrayList<>();

        // Search books by title
        bookService.findAll().stream()
                .filter(book -> book.title().toLowerCase().contains(text.toLowerCase()))
                .forEach(results::add);

        // Search authors by name
        bookService.findAllAuthors().stream()
                .filter(author -> author.name().toLowerCase().contains(text.toLowerCase()))
                .forEach(results::add);

        return results;
    }

    private boolean matchesFilter(Book book, BookFilter filter) {
        if (filter.authorName() != null &&
                !book.author().name().toLowerCase().contains(filter.authorName().toLowerCase())) {
            return false;
        }

        if (filter.publishedAfter() != null &&
                book.publishedYear() < filter.publishedAfter()) {
            return false;
        }

        return true;
    }

}
