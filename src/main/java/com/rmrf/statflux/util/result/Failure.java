package com.rmrf.statflux.util.result;

import java.util.Objects;
import lombok.NonNull;

public record Failure<T>(@NonNull Throwable exception) implements Result<T> {

    public <R> Failure<R> swap() {
        return new Failure<>(exception());
    }

    public static <T> Failure<T> of(@NonNull Throwable e) {
        Objects.requireNonNull(e);
        return new Failure<>(e);
    }

    @Override
    public boolean isSuccess() {
        return false;
    }
}
