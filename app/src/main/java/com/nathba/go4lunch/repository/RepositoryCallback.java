package com.nathba.go4lunch.repository;

public interface RepositoryCallback<T> {
    void onSuccess(T data);  // Méthode pour retourner les données
    void onError(Throwable t);  // Méthode pour gérer les erreurs
}