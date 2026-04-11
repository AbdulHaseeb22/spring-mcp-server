package io.github.abdulhaseeb22.mcp.sample;

/**
 * Represents a book in the bookstore inventory.
 *
 * @param isbn     International Standard Book Number (e.g. "978-0-06-112008-4").
 * @param title    Full title of the book.
 * @param author   Primary author name.
 * @param category Genre / section (e.g. "Fiction", "Technology").
 * @param price    Retail price in USD.
 */
public record Book(
        String isbn,
        String title,
        String author,
        String category,
        double price
) {}
