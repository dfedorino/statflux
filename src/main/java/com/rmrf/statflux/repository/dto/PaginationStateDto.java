package com.rmrf.statflux.repository.dto;

import java.time.ZonedDateTime;

public record PaginationStateDto(
    long chatId,
    long messageId,
    Long firstSeenId,
    Long lastSeenId,
    ZonedDateTime updatedAt
) {}
