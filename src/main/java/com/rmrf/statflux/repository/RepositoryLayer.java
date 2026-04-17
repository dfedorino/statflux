package com.rmrf.statflux.repository;

import lombok.NonNull;

public interface RepositoryLayer {

    boolean save(@NonNull String hostingName, @NonNull String rawLink, @NonNull String hostingId,
        @NonNull String name, Long views);
}
