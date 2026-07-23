package com.academy.library;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class ReportService {

    private final LibraryService libraryService;

    public ReportService(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    public void displaySummaryReport() {
        // TODO: compute totalBooks, borrowedBooks, availableBooks, totalMembers
        // TODO: findMostPopularCategory(); print Reports block matching solution format
        throw new UnsupportedOperationException("TODO");
    }

    public Path exportReportToFile(String fileName) throws IOException {
        // TODO: build same summary + category breakdown; Files.writeString; return Path
        throw new UnsupportedOperationException("TODO");
    }

    private String findMostPopularCategory() {
        // TODO: max entry by value from getCategoryBookCount(); orElse "N/A"
        throw new UnsupportedOperationException("TODO");
    }
}
