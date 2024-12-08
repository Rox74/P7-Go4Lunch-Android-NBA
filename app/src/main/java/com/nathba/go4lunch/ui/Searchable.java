package com.nathba.go4lunch.ui;

public interface Searchable {

    /**
     * Gère la recherche en fonction du texte saisi.
     *
     * @param query Texte de la recherche.
     */
    void onSearch(String query);

    /**
     * Gère le tri des restaurants en fonction d'un critère.
     * @param criterion
     */
    void onSort(String criterion);
}