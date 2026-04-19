package com.rmrf.statflux.repository.dto;

import java.time.ZonedDateTime;

public record LinkDto(
    String hostingName,
    String rawLink,
    String hostingId,
    String title,
    long views,
    ZonedDateTime updatedAt
) {

}
