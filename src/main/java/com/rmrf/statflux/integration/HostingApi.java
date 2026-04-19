package com.rmrf.statflux.integration;

import com.rmrf.statflux.domain.dto.LinkMetadataResponse;
import com.rmrf.statflux.domain.result.Result;
import java.util.List;
import lombok.NonNull;

public interface HostingApi {

    @NonNull
    Result<LinkMetadataResponse> linkMetadata(String rawLink);

    @NonNull
    Result<List<LinkMetadataResponse>> metadataByIds(List<String> ids);

    @NonNull
    String hostingName();
}
