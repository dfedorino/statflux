package com.rmrf.statflux.util.result;

public sealed interface Result<T> permits Failure, Success {

    boolean isSuccess();

    default boolean isFailure() {
        return !isSuccess();
    }

    default T get() {
        return switch (this) {
            case Success<T> s -> s.result();
            case Failure<T> v ->
                throw new IllegalArgumentException("[get] the Result is a Failure");
        };
    }

    default Failure<T> asFailure() {
        return switch (this) {
            case Failure<T> v -> v;
            case Success<T> s ->
                throw new IllegalArgumentException("[asFailure] the Result is a Success");
        };
    }

    default boolean contains(T item) {
        return switch (this) {
            case Failure<T> v -> false;
            case Success<T> s -> s.result().equals(item);
        };
    }
}
