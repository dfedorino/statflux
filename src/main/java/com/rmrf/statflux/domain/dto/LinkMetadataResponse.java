package com.rmrf.statflux.domain.dto;

import lombok.NonNull;

public record LinkMetadataResponse(@NonNull String platformId, @NonNull String name, Long views) {

}
