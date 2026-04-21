package com.rmrf.statflux.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import com.rmrf.statflux.AbstractIntegrationTest;
import com.rmrf.statflux.domain.dto.AddVideoResponse;
import com.rmrf.statflux.domain.dto.VideoMetadataResponse;
import com.rmrf.statflux.domain.result.Success;
import com.rmrf.statflux.integration.VideoProvider;
import com.rmrf.statflux.integration.VideoProviderFactory;
import com.rmrf.statflux.service.config.ServiceConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ServiceLayerIT extends AbstractIntegrationTest {

    private final ServiceConfig serviceConfig = new ServiceConfig();
    private final VideoProviderFactory mockHostingApiFactory = Mockito.mock(VideoProviderFactory.class);
    private final VideoProvider mockHostingApi = Mockito.mock(VideoProvider.class);
    private ServiceLayer serviceLayer = serviceConfig.serviceLayer(mockHostingApiFactory);

    @Test
    public void should_add_link() {
        assertThat(serviceLayer).isNotNull();

        given(mockHostingApiFactory.forUrl(anyString()))
            .willReturn(Success.of(mockHostingApi));

        String rawUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";
        String title = "My video";
        long views = 12345L;
        String hostingId = "dQw4w9WgXcQ";

        given(mockHostingApi.metadataById(anyString()))
            .willReturn(Success.of(new VideoMetadataResponse(
                hostingId, title, views
            )));

        String hostingName = "YouTube";
        given(mockHostingApi.hostingName())
            .willReturn(hostingName);

        var addedLink = serviceLayer.addVideo(rawUrl);

        assertThat(addedLink).isEqualTo(Success.of(
            new AddVideoResponse(
                hostingName,
                title,
                rawUrl,
                views
            )
        ));

        var foundLinks = serviceLayer.getVideos(1L, 2L);

        assertThat(foundLinks.isSuccess()).isTrue();
        var found = foundLinks.get();

        assertThat(found.getItems())
            .hasSize(1)
            .first()
            .satisfies(link -> {
                assertThat(link.id()).isEqualTo("1");
                assertThat(link.name()).isEqualTo(title);
                assertThat(link.rawUrl()).isEqualTo(rawUrl);
                assertThat(link.views()).isEqualTo(views);
            });
        assertThat(found.hasNext()).isFalse();
        assertThat(found.hasPrev()).isFalse();
        assertThat(found.getTotalVideos()).isOne();
        assertThat(found.getTotalViews()).isEqualTo(views);

    }

}
