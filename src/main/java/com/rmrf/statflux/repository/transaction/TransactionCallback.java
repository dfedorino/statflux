package com.rmrf.statflux.repository.transaction;

@FunctionalInterface
public interface TransactionCallback<T> {
    T doInTransaction();
}
