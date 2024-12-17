package com.nathba.go4lunch.repository;

/**
 * A generic callback interface for repository operations.
 * <p>
 * This interface is used to handle asynchronous operations, such as data retrieval
 * or network calls, by providing success and error methods. It can be implemented
 * to return data of any type or handle errors in a repository layer.
 *
 * @param <T> The type of data returned when the operation is successful.
 */
public interface RepositoryCallback<T> {

    /**
     * Called when the repository operation is successful.
     *
     * @param data The result of the operation, of type {@code T}.
     */
    void onSuccess(T data);

    /**
     * Called when an error occurs during the repository operation.
     *
     * @param t The {@link Throwable} representing the error or exception that occurred.
     */
    void onError(Throwable t);
}