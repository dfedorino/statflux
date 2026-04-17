package com.rmrf.statflux.domain.dto;

import java.time.ZonedDateTime;
import lombok.NonNull;

public record VideoStatsItem(@NonNull String id, @NonNull String name, @NonNull Long views,
                             @NonNull ZonedDateTime updatedAt) {

}
