package com.rmrf.statflux.util.result;

import java.util.Objects;
import lombok.NonNull;

public record Success<T>(T result) implements Result<T> {

    public static <T> Success<T> of(@NonNull T r) {
        Objects.requireNonNull(r);
        return new Success<>(r);
    }
}
