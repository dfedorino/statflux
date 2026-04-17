package com.rmrf.statflux.domain.dto;

import lombok.NonNull;

public record ParseVideoResponse(@NonNull String platformId, @NonNull String name, Long views) {

}
