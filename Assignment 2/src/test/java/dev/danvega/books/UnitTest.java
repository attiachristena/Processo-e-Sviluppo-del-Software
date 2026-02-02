package dev.danvega.books;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach; 
import static org.junit.jupiter.api.Assertions.*;

class UnitTest {

    private BookService bookService; 

    @BeforeEach 
    void setup() {
        this.bookService = new BookService(); 
        this.bookService.loadData(); 
    }
    
    // Verifica che il metodo findAll() restituisca il numero corretto di libri
    @Test
    void returnNumberOfBooks() {
        var books = bookService.findAll();
        assertNotNull(books);
        assertEquals(25, books.size(), "Dovrebbero esserci 25 libri nel dataset in-memory.");
    }

   // Verifica che il metodo findById() gestisca correttamente un ID inesistente
    @Test
    void returnNull_ifNotExists() {
        var book = bookService.findById("999");
        assertNull(book, "Il libro con ID '999' non dovrebbe esistere.");
    }

    // Verifica che il libro corretto sia restituito per un ID valido
    @Test
    void findSpecificBookByTitle() {
        var book = bookService.findById("1");
        assertNotNull(book);
        assertEquals("Effective Java", book.title());
    }
}