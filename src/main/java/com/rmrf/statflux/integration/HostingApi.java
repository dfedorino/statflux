package com.rmrf.statflux.integration;

import com.rmrf.statflux.domain.dto.VideoMetadataResponse;
import com.rmrf.statflux.domain.result.Result;
import java.util.List;
import lombok.NonNull;

public interface HostingApi {

    // использовать metadataById
    @Deprecated
    Result<VideoMetadataResponse> metadataByLink(String rawLink);

    @NonNull
    Result<VideoMetadataResponse> metadataById(String id);

    @NonNull
    Result<List<VideoMetadataResponse>> metadataByIds(List<String> ids);

    @NonNull
    String hostingName();
}
