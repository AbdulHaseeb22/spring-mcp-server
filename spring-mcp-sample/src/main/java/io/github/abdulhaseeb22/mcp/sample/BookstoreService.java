package io.github.abdulhaseeb22.mcp.sample;

import io.github.abdulhaseeb22.mcp.annotations.McpParam;
import io.github.abdulhaseeb22.mcp.annotations.McpServer;
import io.github.abdulhaseeb22.mcp.annotations.McpTool;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Demo MCP server component that exposes bookstore tools to AI model clients.
 *
 * <p>All data is hardcoded — no database is required. This class is intended
 * to show how a realistic multi-tool {@code @McpServer} looks and to exercise
 * the full annotation-to-HTTP pipeline end-to-end.
 */
@Service
@McpServer(name = "bookstore", description = "A simple bookstore service for searching and retrieving book information")
public class BookstoreService {

    // -------------------------------------------------------------------------
    // Static catalogue
    // -------------------------------------------------------------------------

    private static final List<Book> CATALOGUE = List.of(
            new Book("978-0-06-112008-4", "To Kill a Mockingbird",   "Harper Lee",            "Fiction",     14.99),
            new Book("978-0-7432-7356-5", "The Great Gatsby",         "F. Scott Fitzgerald",   "Fiction",     12.99),
            new Book("978-0-14-028329-7", "1984",                     "George Orwell",         "Fiction",     13.99),
            new Book("978-0-06-093546-9", "A Brief History of Time",  "Stephen Hawking",       "Science",     16.99),
            new Book("978-0-19-852092-5", "The Selfish Gene",         "Richard Dawkins",       "Science",     15.99),
            new Book("978-0-465-02310-5", "Six Easy Pieces",          "Richard Feynman",       "Science",     14.49),
            new Book("978-0-13-468599-1", "Clean Code",               "Robert C. Martin",      "Technology",  45.99),
            new Book("978-0-201-63361-0", "Design Patterns",          "Gang of Four",          "Technology",  54.99),
            new Book("978-0-13-235088-4", "The Pragmatic Programmer", "David Thomas",          "Technology",  49.99),
            new Book("978-0-06-231609-7", "Sapiens",                  "Yuval Noah Harari",     "History",     18.99),
            new Book("978-0-14-303943-3", "Guns, Germs, and Steel",   "Jared Diamond",         "History",     17.99),
            new Book("978-1-4767-2818-5", "Elon Musk",                "Ashlee Vance",          "Biography",   17.99),
            new Book("978-1-5011-2609-5", "Steve Jobs",               "Walter Isaacson",       "Biography",   19.99)
    );

    /** Hardcoded inventory counts keyed by category (case-insensitive lookup). */
    private static final Map<String, Integer> INVENTORY = Map.of(
            "fiction",    42,
            "science",    28,
            "technology", 15,
            "history",    31,
            "biography",  19
    );

    // -------------------------------------------------------------------------
    // MCP tools
    // -------------------------------------------------------------------------

    /**
     * Searches the catalogue for books whose title, author, or category contains
     * the given query string (case-insensitive). Returns up to 5 matches.
     *
     * @param query the search term
     * @return list of matching {@link Book} records serialized as JSON
     */
    @McpTool(
            name        = "search_books",
            description = "Search for books by title, author, or category. Returns up to 5 matching results.")
    public List<Book> searchBooks(
            @McpParam(name = "query", description = "Search term — matched against title, author, and category") String query) {

        String lower = query.toLowerCase();
        return CATALOGUE.stream()
                .filter(b -> b.title().toLowerCase().contains(lower)
                          || b.author().toLowerCase().contains(lower)
                          || b.category().toLowerCase().contains(lower))
                .limit(5)
                .toList();
    }

    /**
     * Returns full details for a single book identified by its ISBN, or a
     * descriptive error string if the ISBN is not found in the catalogue.
     *
     * @param isbn the ISBN to look up (e.g. "978-0-06-112008-4")
     * @return a {@link Book} record, or a "not found" message string
     */
    @McpTool(
            name        = "get_book_details",
            description = "Look up full details for a book by its ISBN. Returns the book object, or an error message if not found.")
    public Object getBookDetails(
            @McpParam(name = "isbn", description = "ISBN of the book (e.g. 978-0-06-112008-4)") String isbn) {

        return CATALOGUE.stream()
                .filter(b -> b.isbn().equalsIgnoreCase(isbn.trim()))
                .findFirst()
                .<Object>map(b -> b)
                .orElse("No book found for ISBN: " + isbn);
    }

    /**
     * Returns the current inventory count for the given category.
     * Unrecognised categories return {@code 0}.
     *
     * @param category the book category (e.g. "Fiction", "Technology")
     * @return number of copies in stock
     */
    @McpTool(
            name        = "get_inventory_count",
            description = "Get the number of books currently in stock for a given category (Fiction, Science, Technology, History, Biography).")
    public int getInventoryCount(
            @McpParam(name = "category", description = "Book category: Fiction, Science, Technology, History, or Biography") String category) {

        return INVENTORY.getOrDefault(category.toLowerCase().trim(), 0);
    }
}
