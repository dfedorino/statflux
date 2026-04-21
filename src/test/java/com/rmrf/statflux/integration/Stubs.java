package com.rmrf.statflux.integration;

import com.rmrf.statflux.domain.dto.VideoMetadataResponse;
import com.rmrf.statflux.domain.result.Result;
import com.rmrf.statflux.integration.vk.VkVideoProvider;
import com.rmrf.statflux.integration.youtube.YouTubeVideoProvider;
import java.util.List;
import lombok.NonNull;

public class Stubs {

    protected static final YouTubeVideoProvider youtubeStub = new YouTubeVideoProvider() {
        @Override
        public @NonNull Result<VideoMetadataResponse> metadataByLink(String rawLink) {
            throw new UnsupportedOperationException();
        }

        @Override
        public @NonNull Result<VideoMetadataResponse> metadataById(String id) {
            return null;
        }

        @Override
        public @NonNull Result<List<VideoMetadataResponse>> metadataByIds(List<String> ids) {
            throw new UnsupportedOperationException();
        }

        @Override
        public @NonNull String hostingName() {
            throw new UnsupportedOperationException();
        }
    };

    protected static final VkVideoProvider vkStub = new VkVideoProvider() {
        @Override
        public @NonNull Result<VideoMetadataResponse> metadataByLink(String rawLink) {
            throw new UnsupportedOperationException();
        }

        @Override
        public @NonNull Result<VideoMetadataResponse> metadataById(String id) {
            return null;
        }

        @Override
        public @NonNull Result<List<VideoMetadataResponse>> metadataByIds(List<String> ids) {
            throw new UnsupportedOperationException();
        }

        @Override
        public @NonNull String hostingName() {
            throw new UnsupportedOperationException();
        }
    };
}
