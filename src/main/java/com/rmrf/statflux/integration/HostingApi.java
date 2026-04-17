package com.rmrf.statflux.integration;

import com.rmrf.statflux.domain.dto.ParseVideoResponse;
import com.rmrf.statflux.util.result.Result;
import lombok.NonNull;

public interface HostingApi {

    @NonNull
    Result<ParseVideoResponse> viewCount(String rawLink);

    @NonNull
    String hostingName();
}
