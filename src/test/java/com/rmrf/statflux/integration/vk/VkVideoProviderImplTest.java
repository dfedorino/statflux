package com.rmrf.statflux.integration.vk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.rmrf.statflux.domain.constant.Platform;
import com.rmrf.statflux.domain.dto.VideoMetadataResponse;
import com.rmrf.statflux.domain.result.Result;
import com.rmrf.statflux.integration.utils.SimpleHttpClient;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class VkVideoProviderImplTest {

    /**
     * SimpleHttpClient объект.
     */
    private SimpleHttpClient httpClient;

    /**
     * VkVideoProviderImpl объект.
     */
    private VkVideoProviderImpl provider;

    /**
     * Set Up для каждого теста.
     */
    @BeforeEach
    void setUp() {
        this.httpClient = Mockito.mock(SimpleHttpClient.class);

        String baseUrl = "https://api.vk.com";
        String token = "test-token";
        String apiVersion = "5.199";

        this.provider = new VkVideoProviderImpl(baseUrl, token, apiVersion, httpClient);
    }

    /**
     * Тест корректной работы метода metadataById.
     */
    @Test
    void metadataById_success() throws IOException, InterruptedException {
        String id = "-1_1";

        String json = """
        {
          "response": {
            "items": [
              { "owner_id": -1, "id": 1, "title": "video", "views": 10 }
            ]
          }
        }
        """;

        when(httpClient.get(Mockito.anyString(), Mockito.anyMap()))
            .thenReturn(json);

        Result<VideoMetadataResponse> result = provider.metadataById(id);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get().id()).isEqualTo(id);
        assertThat(result.get().title()).isEqualTo("video");
        assertThat(result.get().views()).isEqualTo(10L);
    }

    /**
     * Тест ошибки в методе metadataById.
     */
    @Test
    void metadataById_httpError() throws IOException, InterruptedException {
        when(httpClient.get(Mockito.anyString(), Mockito.anyMap()))
            .thenThrow(new RuntimeException("network error"));

        Result<VideoMetadataResponse> result = provider.metadataById("-1_1");

        assertThat(result.isFailure()).isTrue();
        assertThat(result.asFailure().exception()).isInstanceOf(RuntimeException.class);
    }

    /**
     * Тест обработки пустого JSON в методе metadataById.
     */
    @Test
    void metadataById_emptyJson() throws IOException, InterruptedException {
        when(httpClient.get(Mockito.anyString(), Mockito.anyMap()))
            .thenReturn("");

        Result<VideoMetadataResponse> result = provider.metadataById("-1_1");

        assertThat(result.isFailure()).isTrue();
    }

    /**
     * Тест корректной работы в методе metadataById.
     */
    @Test
    void metadataByIds_success() throws IOException, InterruptedException {
        List<String> ids = List.of("-1_1", "-2_2");

        String json = """
        {
          "response": {
            "items": [
              { "owner_id": -1, "id": 1, "title": "a", "views": 10 },
              { "owner_id": -2, "id": 2, "title": "b", "views": 20 }
            ]
          }
        }
        """;

        when(httpClient.get(Mockito.anyString(), Mockito.anyMap()))
            .thenReturn(json);

        Result<List<VideoMetadataResponse>> result = provider.metadataByIds(ids);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).hasSize(2);
    }

    /**
     * Тест обработки пустого JSON в методе metadataByIds.
     */
    @Test
    void metadataByIds_emptyInput() {
        Result<List<VideoMetadataResponse>> result =
            provider.metadataByIds(List.of());

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).isEmpty();
    }

    /**
     * Тест пустых элементов в методе metadataByIds.
     */
    @Test
    void metadataByIds_allNulls() {
        Result<List<VideoMetadataResponse>> result =
            provider.metadataByIds(Arrays.asList(null, null));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).isEmpty();
    }

    /**
     * Тест ошибки HTTP в методе metadataByIds.
     */
    @Test
    void metadataByIds_httpError() throws IOException, InterruptedException {
        when(httpClient.get(Mockito.anyString(), Mockito.anyMap()))
            .thenThrow(new RuntimeException("network error"));

        Result<List<VideoMetadataResponse>> result =
            provider.metadataByIds(List.of("-1_1"));

        assertThat(result.isFailure()).isTrue();
    }

    /**
     * Тест корректной работы в методе hostingName.
     */
    @Test
    void hostingName_returnsVk() {
        assertThat(provider.hostingName())
            .isEqualTo(Platform.VK.getDisplayName());
    }
}
