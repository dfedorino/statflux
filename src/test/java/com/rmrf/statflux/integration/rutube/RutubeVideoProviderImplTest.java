package com.rmrf.statflux.integration.rutube;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.rmrf.statflux.domain.dto.VideoMetadataResponse;
import com.rmrf.statflux.domain.result.Failure;
import com.rmrf.statflux.domain.result.Result;
import com.rmrf.statflux.integration.utils.SimpleHttpClient;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RutubeVideoProviderImplTest {

    private static final String BASE_URL = "https://rutube.ru/api/video";
    private static final String VALID_ID = "10b3a03fc01d5bbcc632a2f3514e8aab";
    private static final String VALID_URL = "https://rutube.ru/video/" + VALID_ID + "/";

    private static final String OK_JSON = """
        {
          "id": "10b3a03fc01d5bbcc632a2f3514e8aab",
          "title": "Test video title",
          "hits": 12345,
          "duration": 120,
          "thumbnail_url": "https://...",
          "extra_field": "ignored"
        }
        """;

    @Mock
    private SimpleHttpClient httpClient;

    private RutubeVideoProviderImpl provider;

    @BeforeEach
    void setUp() {
        provider = new RutubeVideoProviderImpl(BASE_URL, httpClient);
    }

    @Test
    @DisplayName("metadataByIds should return failure when ids is null")
    void metadataByIds_ShouldReturnFailure_WhenIdsNull() {
        Result<List<VideoMetadataResponse>> result = provider.metadataByIds(null);
        assertInstanceOf(Failure.class, result);
    }

    @Test
    @DisplayName("metadataByIds should return failure when all requests fail")
    void metadataByIds_ShouldReturnFailure_WhenAllRequestsFail() throws Exception {
        String secondId = "aaaabbbbccccddddeeeeffffaaaabbbb";
        when(httpClient.get(anyString(), anyMap())).thenThrow(new RuntimeException("boom"));

        Result<List<VideoMetadataResponse>> result = provider.metadataByIds(List.of(VALID_ID, secondId));

        assertTrue(result.isFailure());
    }

    @Test
    @DisplayName("metadataByIds should parse JSON and return metadata on happy path")
    void metadataByIds_ShouldReturnSuccess_WhenHappyPath() throws Exception {
        when(httpClient.get(anyString(), anyMap())).thenReturn(OK_JSON);

        Result<List<VideoMetadataResponse>> result = provider.metadataByIds(List.of(VALID_ID));

        assertTrue(result.isSuccess());
        List<VideoMetadataResponse> list = result.get();
        assertEquals(1, list.size());
        assertEquals(VALID_ID, list.getFirst().id());
        assertEquals("Test video title", list.getFirst().title());
        assertEquals(12345L, list.getFirst().views());
    }

    @Test
    @DisplayName("metadataById should accept URL and extract hash before request")
    void metadataById_ShouldExtractHashFromUrl() throws Exception {
        when(httpClient.get(contains(VALID_ID), anyMap())).thenReturn(OK_JSON);

        Result<VideoMetadataResponse> result = provider.metadataById(VALID_URL);

        assertTrue(result.isSuccess());
        assertEquals(VALID_ID, result.get().id());
    }

    @Test
    @DisplayName("metadataById should return failure for invalid URL")
    void metadataById_ShouldReturnFailure_WhenUrlInvalid() {
        Result<VideoMetadataResponse> result = provider.metadataById("https://google.com");
        assertTrue(result.isFailure());
    }

    @Test
    @DisplayName("metadataByIds should return failure when ids is empty")
    void metadataByIds_ShouldReturnFailure_WhenIdsEmpty() {
        Result<List<VideoMetadataResponse>> result = provider.metadataByIds(List.of());
        assertInstanceOf(Failure.class, result);
    }

    @Test
    @DisplayName("metadataByIds should return partial results when one of two requests fails")
    void metadataByIds_ShouldReturnPartialResults_WhenOneHttpFails() throws Exception {
        String failId = "aaaabbbbccccddddeeeeffffaaaabbbb";
        when(httpClient.get(contains(VALID_ID), anyMap())).thenReturn(OK_JSON);
        when(httpClient.get(contains(failId), anyMap())).thenThrow(new RuntimeException("boom"));

        Result<List<VideoMetadataResponse>> result = provider.metadataByIds(List.of(VALID_ID, failId));

        assertTrue(result.isSuccess());
        assertEquals(1, result.get().size());
        assertEquals(VALID_ID, result.get().getFirst().id());
    }

    @Test
    @DisplayName("metadataById should return failure when response has null id")
    void metadataById_ShouldReturnFailure_WhenResponseHasNullId() throws Exception {
        String nullIdJson = """
            {"id": null, "title": "Test title", "hits": 100}
            """;
        when(httpClient.get(anyString(), anyMap())).thenReturn(nullIdJson);

        Result<VideoMetadataResponse> result = provider.metadataById(VALID_ID);

        assertTrue(result.isFailure());
    }

    @Test
    @DisplayName("metadataById should return failure when response has null title")
    void metadataById_ShouldReturnFailure_WhenResponseHasNullTitle() throws Exception {
        String nullTitleJson = """
            {"id": "10b3a03fc01d5bbcc632a2f3514e8aab", "title": null, "hits": 100}
            """;
        when(httpClient.get(anyString(), anyMap())).thenReturn(nullTitleJson);

        Result<VideoMetadataResponse> result = provider.metadataById(VALID_ID);

        assertTrue(result.isFailure());
    }

    @Test
    @DisplayName("metadataById should return failure when JSON is malformed")
    void metadataById_ShouldReturnFailure_WhenJsonMalformed() throws Exception {
        when(httpClient.get(anyString(), anyMap())).thenReturn("not valid json {{{");

        Result<VideoMetadataResponse> result = provider.metadataById(VALID_ID);

        assertTrue(result.isFailure());
    }

    @Test
    @DisplayName("metadataById should call HTTP with exact URL and User-Agent header")
    void metadataById_ShouldCallHttpWithExpectedUrlAndUserAgent() throws Exception {
        String expectedUrl = BASE_URL + "/" + VALID_ID;
        Map<String, String> expectedHeaders = Map.of("User-Agent", "statflux/1.0");
        when(httpClient.get(eq(expectedUrl), eq(expectedHeaders))).thenReturn(OK_JSON);

        Result<VideoMetadataResponse> result = provider.metadataById(VALID_ID);

        assertTrue(result.isSuccess());
        verify(httpClient).get(eq(expectedUrl), eq(expectedHeaders));
    }

    @Test
    @DisplayName("metadataByIds should return views=0 when hits is null in response")
    void metadataByIds_ShouldReturnZeroViews_WhenHitsNull() throws Exception {
        String nullHitsJson = """
            {
              "id": "10b3a03fc01d5bbcc632a2f3514e8aab",
              "title": "Test video title",
              "hits": null
            }
            """;
        when(httpClient.get(anyString(), anyMap())).thenReturn(nullHitsJson);

        Result<List<VideoMetadataResponse>> result = provider.metadataByIds(List.of(VALID_ID));

        assertTrue(result.isSuccess());
        assertEquals(0L, result.get().getFirst().views());
    }
}
