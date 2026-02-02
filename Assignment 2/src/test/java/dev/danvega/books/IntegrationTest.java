package dev.danvega.books;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = Application.class 
)
class IntegrationTest {

    @Autowired
    private BookController bookController; 

    @Test
    public void SearchingResultsTest() {
        
        String searchText = "Spring"; 

        List<Object> searchResults = bookController.search(searchText); 
        
        assertThat(searchResults).as("I risultati della ricerca per 'Spring' non dovrebbero essere null.").isNotNull();
        
        assertThat(searchResults.size())
            .as("La ricerca per 'Spring' dovrebbe restituire 10 risultati.")
            .isEqualTo(10);

        assertThat(searchResults).extracting("title").contains("Spring in Action");
    }
    
    @Test
    void testFindBookViaController() {
        Book book = bookController.book("1"); 
        assertThat(book).isNotNull();
        assertThat(book.title()).isEqualTo("Effective Java");
    }
}