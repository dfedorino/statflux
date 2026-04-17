package com.rmrf.statflux.service;

import com.rmrf.statflux.domain.dto.AddVideoResponse;
import com.rmrf.statflux.util.result.Result;
import lombok.NonNull;

public interface ServiceLayer {

    @NonNull Result<AddVideoResponse> addVideo(@NonNull String rawUrl);
}
