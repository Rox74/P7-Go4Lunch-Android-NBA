package com.nathba.go4lunch.ui;

/**
 * Interface defining searchable and sortable behaviors for fragments or components.
 */
public interface Searchable {

    /**
     * Handles search functionality based on the input text.
     *
     * @param query The search text entered by the user.
     */
    void onSearch(String query);

    /**
     * Handles sorting of items (e.g., restaurants) based on a specified criterion.
     *
     * @param criterion The sorting criterion (e.g., "distance", "rating").
     */
    void onSort(String criterion);
}