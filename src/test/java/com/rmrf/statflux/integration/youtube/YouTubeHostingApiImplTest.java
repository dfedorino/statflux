package com.rmrf.statflux.integration.youtube;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmrf.statflux.domain.dto.VideoMetadataResponse;
import com.rmrf.statflux.domain.result.Failure;
import com.rmrf.statflux.domain.result.Result;
import com.rmrf.statflux.integration.utils.SimpleHttpClient;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class YouTubeHostingApiImplTest {
    @Mock
    private SimpleHttpClient httpClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private YouTubeHostingApiImpl youTubeApi;

    private static final String TEST_API_KEY = "test-api-key";

    @BeforeEach
    void setUp() {
        youTubeApi = new YouTubeHostingApiImpl(TEST_API_KEY, httpClient);
    }

    @Test
    @DisplayName("metadataByIds should return failure when ids is null")
    void metadataByIds_ShouldReturnFailure_WhenIdsNull() {
        Result<List<VideoMetadataResponse>> result = youTubeApi.metadataByIds(null);
        assertTrue(result instanceof Failure, "Result should be Failure");
    }

    @Test
    @DisplayName("metadataByIds should return failure when HTTP request fails")
    void metadataByIds_ShouldReturnFailure_WhenHttpFails() throws Exception {
        when(httpClient.get(anyString())).thenThrow(new RuntimeException("Network error"));

        Result<List<VideoMetadataResponse>> result = youTubeApi.metadataByIds(List.of("test123"));

        assertTrue(result.isFailure());
        verify(httpClient, times(1)).get(anyString());
    }

    @Test
    @DisplayName("metadataById should return failure when id is invalid")
    void metadataById_ShouldReturnFailure_WhenIdInvalid() {
        Result<VideoMetadataResponse> result = youTubeApi.metadataById("invalid-id");
        assertTrue(result.isFailure());
    }
}
