package com.rmrf.statflux.integration;

import com.rmrf.statflux.domain.dto.LinkMetadataResponse;
import com.rmrf.statflux.util.result.Result;
import lombok.NonNull;

public interface HostingApi {

    @NonNull
    Result<LinkMetadataResponse> linkMetadata(String rawLink);

    @NonNull
    String hostingName();
}
