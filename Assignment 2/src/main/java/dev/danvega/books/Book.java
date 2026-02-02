package dev.danvega.books;

public record Book(
        String id,
        String title,
        Author author,
        Integer publishedYear
) {}
