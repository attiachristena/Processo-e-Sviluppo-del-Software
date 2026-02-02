package dev.danvega.books;

public record BookFilter(
        String authorName,
        Integer publishedAfter
) {}