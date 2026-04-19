package com.rmrf.statflux.service;

import static com.rmrf.statflux.Stubs.vkStub;
import static com.rmrf.statflux.Stubs.youtubeStub;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.rmrf.statflux.domain.dto.RefreshVideosPagedResponse;
import com.rmrf.statflux.domain.dto.VideoStatsItem;
import com.rmrf.statflux.domain.dto.VideoStatsResponse;
import com.rmrf.statflux.domain.exceptions.PageIsOutsideOfBoundsException;
import com.rmrf.statflux.domain.result.Failure;
import com.rmrf.statflux.domain.result.Result;
import com.rmrf.statflux.domain.result.Success;
import com.rmrf.statflux.integration.HostingApiFactory;
import com.rmrf.statflux.repository.BaseRepositoryTest;
import com.rmrf.statflux.repository.LinkRepository;
import com.rmrf.statflux.repository.PaginationStateRepository;
import com.rmrf.statflux.repository.TestDataFactory;
import java.time.ZonedDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServiceLayerPaginationTest extends BaseRepositoryTest {

    private LinkRepository linkRepository;
    private PaginationStateRepository paginationStateRepository;
    private ServiceLayer serviceLayer;

    private static final int VIDEO_COUNT = 30;
    private static final int VIDEOS_PER_PAGE = 5;
    private static final long USER_ID = 42L;
    private static final long MESSAGE_ID = 4242L;

    @BeforeEach
    public void setup() {
        linkRepository = repositoryConfig.linkRepository();
        paginationStateRepository = repositoryConfig.paginationStateRepository();
        serviceLayer = new ServiceLayerImpl(linkRepository, paginationStateRepository,
            new HostingApiFactory(youtubeStub, vkStub), 100L, VIDEOS_PER_PAGE);
    }

    @AfterEach
    void tearDown() {
        dataSource.close();
    }

    @Test
    public void getVideosReturnsFirstPagesAndInitsState() {
        TestDataFactory.insertLinks(linkRepository, ZonedDateTime.now(), VIDEO_COUNT);

        var maybePaginationState = paginationStateRepository.find(USER_ID, MESSAGE_ID);
        assertTrue(maybePaginationState.isEmpty());

        var resp = serviceLayer.getVideos(USER_ID, MESSAGE_ID).get();
        assertTrue(resp.hasNext());
        assertFalse(resp.hasPrev());
        assertEquals(VIDEO_COUNT, resp.totalVideos());

        var paginationState = paginationStateRepository.find(USER_ID, MESSAGE_ID).get();
        assertEquals(1, paginationState.firstSeenId());
        assertEquals(VIDEOS_PER_PAGE, paginationState.lastSeenId());
    }

    @Test
    public void getNextVideosReturnsNextPageAndAdvancesState() {
        TestDataFactory.insertLinks(linkRepository, ZonedDateTime.now(), VIDEO_COUNT);

        var resp = serviceLayer.getVideos(USER_ID, MESSAGE_ID).get();
        assertTrue(resp.hasNext());
        assertFalse(resp.hasPrev());
        assertEquals(VIDEO_COUNT, resp.totalVideos());

        var paginationState = paginationStateRepository.find(USER_ID, MESSAGE_ID).get();
        assertEquals(1, paginationState.firstSeenId());
        assertEquals(VIDEOS_PER_PAGE, paginationState.lastSeenId());

        var nextPageResp = serviceLayer.getNextVideos(USER_ID, MESSAGE_ID).get();
        assertTrue(nextPageResp.hasPrev());
        assertTrue(nextPageResp.hasNext());
        assertThat(nextPageResp.items()).extracting(VideoStatsItem::id)
            .containsExactly("https://youtube.com/v/6", "https://youtube.com/v/7",
                "https://youtube.com/v/8", "https://youtube.com/v/9", "https://youtube.com/v/10");

        var nextNextPageResp = serviceLayer.getNextVideos(USER_ID, MESSAGE_ID).get();
        assertTrue(nextNextPageResp.hasPrev());
        assertTrue(nextNextPageResp.hasNext());
        assertThat(nextNextPageResp.items()).extracting(VideoStatsItem::id)
            .containsExactly("https://youtube.com/v/11", "https://youtube.com/v/12",
                "https://youtube.com/v/13", "https://youtube.com/v/14", "https://youtube.com/v/15");
    }

    @Test
    public void getPreviousVideosReturnsPreviousPageAndFallbacksState() {
        TestDataFactory.insertLinks(linkRepository, ZonedDateTime.now(), VIDEO_COUNT);

        var resp = serviceLayer.getVideos(USER_ID, MESSAGE_ID).get();
        assertTrue(resp.hasNext());
        assertFalse(resp.hasPrev());
        assertEquals(VIDEO_COUNT, resp.totalVideos());

        var paginationState = paginationStateRepository.find(USER_ID, MESSAGE_ID).get();
        assertEquals(1, paginationState.firstSeenId());
        assertEquals(VIDEOS_PER_PAGE, paginationState.lastSeenId());

        serviceLayer.getNextVideos(USER_ID, MESSAGE_ID);
        serviceLayer.getNextVideos(USER_ID, MESSAGE_ID);
        serviceLayer.getNextVideos(USER_ID, MESSAGE_ID);
        serviceLayer.getNextVideos(USER_ID, MESSAGE_ID);

        var previousPageResp = serviceLayer.getPreviousVideos(USER_ID, MESSAGE_ID).get();

        assertThat(previousPageResp.items()).extracting(VideoStatsItem::id)
            .containsExactly("https://youtube.com/v/16", "https://youtube.com/v/17",
                "https://youtube.com/v/18", "https://youtube.com/v/19", "https://youtube.com/v/20");
    }

    @Test
    public void getPreviousVideosReturnsHasPrevCorrectly() {
        TestDataFactory.insertLinks(linkRepository, ZonedDateTime.now(), VIDEO_COUNT);

        var resp = serviceLayer.getVideos(USER_ID, MESSAGE_ID).get();
        assertTrue(resp.hasNext());
        assertFalse(resp.hasPrev());
        assertEquals(VIDEO_COUNT, resp.totalVideos());

        var paginationState = paginationStateRepository.find(USER_ID, MESSAGE_ID).get();
        assertEquals(1, paginationState.firstSeenId());
        assertEquals(VIDEOS_PER_PAGE, paginationState.lastSeenId());

        serviceLayer.getNextVideos(USER_ID, MESSAGE_ID);
        serviceLayer.getNextVideos(USER_ID, MESSAGE_ID);
        serviceLayer.getNextVideos(USER_ID, MESSAGE_ID);
        serviceLayer.getNextVideos(USER_ID, MESSAGE_ID);

        serviceLayer.getPreviousVideos(USER_ID, MESSAGE_ID);
        var prevPageResp = serviceLayer.getPreviousVideos(USER_ID, MESSAGE_ID).get();
        assertTrue(prevPageResp.hasPrev());
        assertTrue(prevPageResp.hasNext());

        serviceLayer.getPreviousVideos(USER_ID, MESSAGE_ID);
        var backToFirstPageResp = serviceLayer.getPreviousVideos(USER_ID, MESSAGE_ID).get();
        assertFalse(backToFirstPageResp.hasPrev());
        assertTrue(backToFirstPageResp.hasNext());
    }

    @Test
    public void subsequentGetNextVideosReturnsHasNextCorrectly() {
        TestDataFactory.insertLinks(linkRepository, ZonedDateTime.now(), VIDEO_COUNT);
        serviceLayer.getVideos(USER_ID, MESSAGE_ID).get();
        VideoStatsResponse lastResp = null;
        for (int i = 0; i < (VIDEO_COUNT / VIDEOS_PER_PAGE) - 1; i++) {
            lastResp = serviceLayer.getNextVideos(USER_ID, MESSAGE_ID).get();
        }

        assertTrue(lastResp.hasPrev());
        assertThat(lastResp.items()).extracting(VideoStatsItem::id)
            .containsExactly("https://youtube.com/v/26", "https://youtube.com/v/27",
                "https://youtube.com/v/28", "https://youtube.com/v/29", "https://youtube.com/v/30");
        assertFalse(lastResp.hasNext());
    }

    @Test
    public void subsequentGetPreviousVideosReturnsHasPrevCorrectly() {
        TestDataFactory.insertLinks(linkRepository, ZonedDateTime.now(), VIDEO_COUNT);
        serviceLayer.getVideos(USER_ID, MESSAGE_ID).get();
        var steps = (VIDEO_COUNT / VIDEOS_PER_PAGE) - 1;
        VideoStatsResponse lastResp = null;
        for (int i = 0; i < steps; i++) {
            lastResp = serviceLayer.getNextVideos(USER_ID, MESSAGE_ID).get();
        }
        for (int i = 0; i < steps; i++) {
            lastResp = serviceLayer.getPreviousVideos(USER_ID, MESSAGE_ID).get();
        }

        assertFalse(lastResp.hasPrev());
        assertThat(lastResp.items()).extracting(VideoStatsItem::id)
            .containsExactly("https://youtube.com/v/1", "https://youtube.com/v/2",
                "https://youtube.com/v/3", "https://youtube.com/v/4", "https://youtube.com/v/5");
        assertTrue(lastResp.hasNext());
    }

    @Test
    public void getNextVideosReturnsAnErrorWhenOverflowing() {
        TestDataFactory.insertLinks(linkRepository, ZonedDateTime.now(), VIDEO_COUNT);
        serviceLayer.getVideos(USER_ID, MESSAGE_ID).get();
        Result<VideoStatsResponse> lastResp = null;
        for (int i = 0; i < (VIDEO_COUNT / VIDEOS_PER_PAGE); i++) {
            lastResp = serviceLayer.getNextVideos(USER_ID, MESSAGE_ID);
        }

        assertThat(lastResp).isInstanceOf(Failure.class);
        assertThat(lastResp.asFailure().exception()).isInstanceOf(
            PageIsOutsideOfBoundsException.class);
    }

    @Test
    public void getPrevVideosReturnsAnErrorWhenUnderflowing() {
        TestDataFactory.insertLinks(linkRepository, ZonedDateTime.now(), VIDEO_COUNT);
        serviceLayer.getVideos(USER_ID, MESSAGE_ID).get();
        Result<VideoStatsResponse> lastResp = serviceLayer.getPreviousVideos(USER_ID, MESSAGE_ID);

        assertThat(lastResp).isInstanceOf(Failure.class);
        assertThat(lastResp.asFailure().exception()).isInstanceOf(
            PageIsOutsideOfBoundsException.class);
    }

    @Test
    public void refreshReturnsCorrectPage() throws InterruptedException {
        TestDataFactory.insertLinks(linkRepository, ZonedDateTime.now(), VIDEO_COUNT);
        serviceLayer.getVideos(USER_ID, MESSAGE_ID);
        serviceLayer.getNextVideos(USER_ID, MESSAGE_ID);
        serviceLayer.getNextVideos(USER_ID, MESSAGE_ID);

        var responseRef = new AtomicReference<Result<RefreshVideosPagedResponse>>(null);
        var latch = new CountDownLatch(1);
        serviceLayer.refreshVideos(USER_ID, MESSAGE_ID, c -> {
            responseRef.set(c);
            latch.countDown();
        });
        var discard = latch.await(30L, TimeUnit.SECONDS);
        var responseEither = responseRef.get();
        assertThat(responseEither).isInstanceOf(Success.class);
        var response = responseEither.get();

        assertThat(response.items()).extracting(VideoStatsItem::id)
            .containsExactly("https://youtube.com/v/11", "https://youtube.com/v/12",
                "https://youtube.com/v/13", "https://youtube.com/v/14", "https://youtube.com/v/15");
        assertTrue(response.hasErrors());
    }
}
