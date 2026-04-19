package com.rmrf.statflux;

import com.rmrf.statflux.domain.dto.LinkMetadataResponse;
import com.rmrf.statflux.integration.vk.VkHostingApi;
import com.rmrf.statflux.integration.youtube.YouTubeHostingApi;
import com.rmrf.statflux.domain.result.Result;
import java.util.List;
import lombok.NonNull;

public class Stubs {

    protected static final YouTubeHostingApi youtubeStub = new YouTubeHostingApi() {
        @Override
        public @NonNull Result<LinkMetadataResponse> metadataByLink(String rawLink) {
            throw new UnsupportedOperationException();
        }

        @Override
        public @NonNull Result<List<LinkMetadataResponse>> metadataByIds(List<String> ids) {
            throw new UnsupportedOperationException();
        }

        @Override
        public @NonNull String hostingName() {
            throw new UnsupportedOperationException();
        }
    };

    protected static final VkHostingApi vkStub = new VkHostingApi() {
        @Override
        public @NonNull Result<LinkMetadataResponse> metadataByLink(String rawLink) {
            throw new UnsupportedOperationException();
        }

        @Override
        public @NonNull Result<List<LinkMetadataResponse>> metadataByIds(List<String> ids) {
            throw new UnsupportedOperationException();
        }

        @Override
        public @NonNull String hostingName() {
            throw new UnsupportedOperationException();
        }
    };
}
