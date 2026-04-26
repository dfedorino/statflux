package com.rmrf.statflux.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.rmrf.statflux.AbstractIntegrationTest;
import com.rmrf.statflux.domain.dto.RefreshVideosPagedResponse;
import com.rmrf.statflux.domain.dto.VideoStatsItem;
import com.rmrf.statflux.domain.dto.VideoStatsResponse;
import com.rmrf.statflux.domain.exceptions.LinkIdNotFoundException;
import com.rmrf.statflux.domain.exceptions.PageIsOutsideOfBoundsException;
import com.rmrf.statflux.domain.exceptions.RefreshInProgressException;
import com.rmrf.statflux.domain.result.Failure;
import com.rmrf.statflux.domain.result.Result;
import com.rmrf.statflux.domain.result.Success;
import com.rmrf.statflux.integration.VideoProviderFactory;
import com.rmrf.statflux.repository.LinkRepository;
import com.rmrf.statflux.repository.PaginationStateRepository;
import com.rmrf.statflux.repository.TestDataFactory;
import com.rmrf.statflux.repository.config.RepositoryConfig;
import com.rmrf.statflux.repository.datasource.DataSource;
import com.rmrf.statflux.repository.transaction.TransactionManager;
import com.rmrf.statflux.repository.util.Queries;
import com.rmrf.statflux.service.config.ServiceConfig;
import java.time.ZonedDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ServiceLayerPaginationIT extends AbstractIntegrationTest {

    private final RepositoryConfig repositoryConfig = new RepositoryConfig();
    private DataSource dataSource;
    private TransactionManager tx;
    private final ServiceConfig serviceConfig = new ServiceConfig();
    private final VideoProviderFactory mockHostingApiFactory = Mockito.mock(VideoProviderFactory.class);
    private LinkRepository linkRepository;
    private PaginationStateRepository paginationStateRepository;
    private ServiceLayer serviceLayer;

    private static final int VIDEO_COUNT = 30;
    private static final int VIDEOS_PER_PAGE = 5;
    private static final long USER_ID = 42L;
    private static final long MESSAGE_ID = 4242L;

    @BeforeEach
    public void setup() {
        dataSource = repositoryConfig.pooledDataSource();
        tx = new TransactionManager(dataSource);
        linkRepository = repositoryConfig.linkRepository();
        paginationStateRepository = repositoryConfig.paginationStateRepository();
        serviceLayer = serviceConfig.serviceLayer(mockHostingApiFactory);

        tx.executeWithoutResult(
            () -> TestDataFactory.insertLinks(linkRepository, USER_ID, ZonedDateTime.now(), VIDEO_COUNT));
    }

    @AfterEach
    public void tearDown() {
        tx.executeWithoutResult(
            () -> Queries.update("TRUNCATE TABLE links, pagination_state RESTART IDENTITY CASCADE"));
        dataSource.close();
    }

    @Test
    public void getVideosReturnsFirstPagesAndInitsState() {
        var maybePaginationState = tx.execute(
            () -> paginationStateRepository.find(USER_ID, MESSAGE_ID));
        assertTrue(maybePaginationState.isEmpty());

        var resp = serviceLayer.getVideos(USER_ID, MESSAGE_ID).get();
        assertTrue(resp.hasNext());
        assertFalse(resp.hasPrev());
        assertEquals(VIDEO_COUNT, resp.getTotalVideos());

        var paginationState = tx.execute(
            () -> paginationStateRepository.find(USER_ID, MESSAGE_ID).get());
        assertEquals(1, paginationState.firstSeenId());
        assertEquals(VIDEOS_PER_PAGE, paginationState.lastSeenId());
    }

    @Test
    public void getNextVideosReturnsNextPageAndAdvancesState() {
        var resp = serviceLayer.getVideos(USER_ID, MESSAGE_ID).get();
        assertTrue(resp.hasNext());
        assertFalse(resp.hasPrev());
        assertEquals(VIDEO_COUNT, resp.getTotalVideos());

        var paginationState = tx.execute(
            () -> paginationStateRepository.find(USER_ID, MESSAGE_ID).get());
        assertEquals(1, paginationState.firstSeenId());
        assertEquals(VIDEOS_PER_PAGE, paginationState.lastSeenId());

        var nextPageResp = serviceLayer.getNextVideos(USER_ID, MESSAGE_ID).get();
        assertTrue(nextPageResp.hasPrev());
        assertTrue(nextPageResp.hasNext());
        assertThat(nextPageResp.getItems()).extracting(VideoStatsItem::id)
            .containsExactly("6", "7", "8", "9", "10");

        var nextNextPageResp = serviceLayer.getNextVideos(USER_ID, MESSAGE_ID).get();
        assertTrue(nextNextPageResp.hasPrev());
        assertTrue(nextNextPageResp.hasNext());
        assertThat(nextNextPageResp.getItems()).extracting(VideoStatsItem::id)
            .containsExactly("11", "12", "13", "14", "15");
    }

    @Test
    public void getPreviousVideosReturnsPreviousPageAndFallbacksState() {
        var resp = serviceLayer.getVideos(USER_ID, MESSAGE_ID).get();
        assertTrue(resp.hasNext());
        assertFalse(resp.hasPrev());
        assertEquals(VIDEO_COUNT, resp.getTotalVideos());

        var paginationState = tx.execute(
            () -> paginationStateRepository.find(USER_ID, MESSAGE_ID).get());
        assertEquals(1, paginationState.firstSeenId());
        assertEquals(VIDEOS_PER_PAGE, paginationState.lastSeenId());

        serviceLayer.getNextVideos(USER_ID, MESSAGE_ID);
        serviceLayer.getNextVideos(USER_ID, MESSAGE_ID);
        serviceLayer.getNextVideos(USER_ID, MESSAGE_ID);
        serviceLayer.getNextVideos(USER_ID, MESSAGE_ID);

        var previousPageResp = serviceLayer.getPreviousVideos(USER_ID, MESSAGE_ID).get();

        assertThat(previousPageResp.getItems()).extracting(VideoStatsItem::id)
            .containsExactly("16", "17", "18", "19", "20");
    }

    @Test
    public void getPreviousVideosReturnsHasPrevCorrectly() {
        var resp = serviceLayer.getVideos(USER_ID, MESSAGE_ID).get();
        assertTrue(resp.hasNext());
        assertFalse(resp.hasPrev());
        assertEquals(VIDEO_COUNT, resp.getTotalVideos());

        var paginationState = tx.execute(
            () -> paginationStateRepository.find(USER_ID, MESSAGE_ID).get());
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
        serviceLayer.getVideos(USER_ID, MESSAGE_ID).get();
        VideoStatsResponse lastResp = null;
        for (int i = 0; i < (VIDEO_COUNT / VIDEOS_PER_PAGE) - 1; i++) {
            lastResp = serviceLayer.getNextVideos(USER_ID, MESSAGE_ID).get();
        }

        assertTrue(lastResp.hasPrev());
        assertThat(lastResp.getItems()).extracting(VideoStatsItem::id)
            .containsExactly("26", "27", "28", "29", "30");
        assertFalse(lastResp.hasNext());
    }

    @Test
    public void subsequentGetPreviousVideosReturnsHasPrevCorrectly() {
//        TestDataFactory.insertLinks(linkRepository, ZonedDateTime.now(), VIDEO_COUNT);
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
        assertThat(lastResp.getItems()).extracting(VideoStatsItem::id)
            .containsExactly("1", "2", "3", "4", "5");
        assertTrue(lastResp.hasNext());
    }

    @Test
    public void getNextVideosReturnsAnErrorWhenOverflowing() {
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
        serviceLayer.getVideos(USER_ID, MESSAGE_ID).get();
        Result<VideoStatsResponse> lastResp = serviceLayer.getPreviousVideos(USER_ID, MESSAGE_ID);

        assertThat(lastResp).isInstanceOf(Failure.class);
        assertThat(lastResp.asFailure().exception()).isInstanceOf(
            PageIsOutsideOfBoundsException.class);
    }

    @Test
    public void refreshReturnsCorrectPage() throws InterruptedException {
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

        assertThat(response.getItems()).extracting(VideoStatsItem::id)
            .containsExactly("11", "12", "13", "14", "15");
        assertTrue(response.hasErrors());
    }

    @Test
    public void simultaneousRefreshesAreForbidden() throws InterruptedException {
        serviceLayer.getVideos(USER_ID, MESSAGE_ID);
        serviceLayer.getNextVideos(USER_ID, MESSAGE_ID);
        serviceLayer.getNextVideos(USER_ID, MESSAGE_ID);

        var responseRef = new AtomicReference<Result<RefreshVideosPagedResponse>>(null);
        var latch = new CountDownLatch(2);
        serviceLayer.refreshVideos(USER_ID, MESSAGE_ID, c -> {
            responseRef.set(c);
            latch.countDown();
        });
        var expectedFailedResponseRef = new AtomicReference<Result<RefreshVideosPagedResponse>>(
            null);
        serviceLayer.refreshVideos(USER_ID, MESSAGE_ID, c -> {
            expectedFailedResponseRef.set(c);
            latch.countDown();
        });
        var discard = latch.await(30L, TimeUnit.SECONDS);
        var responseEither = responseRef.get();
        assertThat(responseEither).isInstanceOf(Success.class);
        var response = responseEither.get();

        assertThat(response.getItems()).extracting(VideoStatsItem::id)
            .containsExactly("11", "12", "13", "14", "15");
        assertTrue(response.hasErrors());

        var failedResponseEither = expectedFailedResponseRef.get();
        assertThat(failedResponseEither).isInstanceOf(Failure.class);
        assertThat(failedResponseEither.asFailure().exception()).isInstanceOf(
            RefreshInProgressException.class);
    }

    @Test
    public void deleteVideo_happyPath() {
        var links = tx.execute(() -> linkRepository.findFirstPage(USER_ID, VIDEOS_PER_PAGE));
        long linkId = links.getFirst().id();

        var result = serviceLayer.deleteVideo(USER_ID, linkId);
        assertTrue(result.isSuccess());
        assertTrue(result.get());
    }

    @Test
    public void deleteVideo_notFound() {
        var result = serviceLayer.deleteVideo(USER_ID, 99999L);
        assertTrue(result.isFailure());
        assertInstanceOf(LinkIdNotFoundException.class, result.asFailure().exception());
    }

    @Test
    public void deleteVideo_wrongUser() {
        var links = tx.execute(() -> linkRepository.findFirstPage(VIDEOS_PER_PAGE));
        long linkId = links.getFirst().id();

        var result = serviceLayer.deleteVideo(999L, linkId);
        assertTrue(result.isFailure());
        assertInstanceOf(LinkIdNotFoundException.class, result.asFailure().exception());
    }

    @Test
    public void deleteVideo_middleLinkFromPage2_nextCursorStillWorks() {
        // navigate to page 2
        serviceLayer.getVideos(USER_ID, MESSAGE_ID).get();
        var page2 = serviceLayer.getNextVideos(USER_ID, MESSAGE_ID).get();

        long linkId = Long.parseLong(page2.getItems().get(2).id());
        serviceLayer.deleteVideo(USER_ID, linkId);

        var page3 = serviceLayer.getNextVideos(USER_ID, MESSAGE_ID).get();
        assertTrue(page3.hasNext());
        assertTrue(page3.hasPrev());
        assertThat(page3.getItems()).extracting(VideoStatsItem::id)
            .doesNotContain(String.valueOf(linkId));
    }

    @Test
    public void deleteVideo_lastLinkFromLastPage_prevCursorStillWorks() {
        // navigate to last page
        var lastPage = serviceLayer.getVideos(USER_ID, MESSAGE_ID).get();
        while (lastPage.hasNext()) {
            lastPage = serviceLayer.getNextVideos(USER_ID, MESSAGE_ID).get();
        }

        var linkId = lastPage.getItems().getLast().id();
        serviceLayer.deleteVideo(USER_ID, Long.parseLong(linkId));

        var prevPage = serviceLayer.getPreviousVideos(USER_ID, MESSAGE_ID).get();
        assertEquals(VIDEOS_PER_PAGE, prevPage.getItems().size());

        lastPage = serviceLayer.getNextVideos(USER_ID, MESSAGE_ID).get();

        assertThat(lastPage.getItems())
            .hasSize(4)
            .extracting(VideoStatsItem::id)
            .doesNotContain(linkId);
    }

    @Test
    public void deleteVideo_onlyLink_getVideosReturnsEmpty() {
        var newChatId = 43L;
        tx.executeWithoutResult(() -> TestDataFactory.insertLinks(linkRepository, newChatId, ZonedDateTime.now(), 1));

        var page1 = serviceLayer.getVideos(newChatId, MESSAGE_ID).get();
        assertFalse(page1.hasPrev());
        assertFalse(page1.hasNext());
        assertEquals(1, page1.getItems().size());

        long linkId = Long.parseLong(page1.getItems().getFirst().id());
        assertTrue(serviceLayer.deleteVideo(newChatId, linkId).isSuccess());

        var resp = serviceLayer.getVideos(newChatId, MESSAGE_ID).get();
        assertFalse(resp.hasNext());
        assertFalse(resp.hasPrev());
        assertEquals(0, resp.getTotalVideos());
        assertThat(resp.getItems()).isEmpty();
    }

    @Test
    public void deleteVideo_allLinksOnPage_nextAndPrevBehaveCorrectly() {
        // navigate to page 2
        var page1 = serviceLayer.getVideos(USER_ID, MESSAGE_ID).get().getItems();

        assertThat(page1).extracting(VideoStatsItem::id)
            .extracting(Integer::parseInt)
            .containsExactly(1, 2, 3, 4, 5);

        var page2 = serviceLayer.getNextVideos(USER_ID, MESSAGE_ID).get().getItems();

        assertThat(page2).extracting(VideoStatsItem::id)
            .extracting(Integer::parseInt)
            .containsExactly(6, 7, 8, 9, 10);

        // delete all links on page 2
        page2.forEach(item -> serviceLayer.deleteVideo(USER_ID, Long.parseLong(item.id())));

        // next should skip the empty range and return page 3 items
        var next = serviceLayer.getNextVideos(USER_ID, MESSAGE_ID).get();
        assertTrue(next.hasPrev());
        assertThat(next.getItems())
            .extracting(VideoStatsItem::id)
            .extracting(Integer::parseInt)
            .containsExactly(11, 12, 13, 14, 15);

        // prev should go back to page 1
        var prev = serviceLayer.getPreviousVideos(USER_ID, MESSAGE_ID).get();
        assertFalse(prev.hasPrev());
        assertThat(prev.getItems())
            .extracting(VideoStatsItem::id)
            .extracting(Integer::parseInt)
            .containsExactly(1, 2, 3, 4, 5);
    }

    @Test
    public void deleteVideo_boundaryIds_nextAndPrevStillNavigateCorrectly() {
        var page1 = serviceLayer.getVideos(USER_ID, MESSAGE_ID)
            .get().getItems();

        assertThat(page1).extracting(VideoStatsItem::id)
            .extracting(Integer::parseInt)
            .containsExactly(1, 2, 3, 4, 5);

        // navigate to page 2
        var page2 = serviceLayer.getNextVideos(USER_ID, MESSAGE_ID).get().getItems();

        assertThat(page2).extracting(VideoStatsItem::id)
            .extracting(Integer::parseInt)
            .containsExactly(6, 7, 8, 9, 10);

        // delete first_seen_id and last_seen_id of page 2
        long firstSeenId = Long.parseLong(page2.getFirst().id());
        assertThat(firstSeenId).isEqualTo(6);
        long lastSeenId = Long.parseLong(page2.getLast().id());
        assertThat(lastSeenId).isEqualTo(10);

        assertThat(serviceLayer.deleteVideo(USER_ID, firstSeenId).isSuccess()).isTrue();
        assertThat(serviceLayer.deleteVideo(USER_ID, lastSeenId).isSuccess()).isTrue();

        // next uses stale last_seen_id as cursor — should still return page 3
        var next = serviceLayer.getNextVideos(USER_ID, MESSAGE_ID).get();
        assertTrue(next.hasPrev());
        assertThat(next.getItems())
            .extracting(VideoStatsItem::id)
            .extracting(Integer::parseInt)
            .containsExactly(11, 12, 13, 14, 15);

        var prev = serviceLayer.getPreviousVideos(USER_ID, MESSAGE_ID).get();
        assertTrue(prev.hasPrev());
        assertThat(prev.getItems())
            .extracting(VideoStatsItem::id)
            .extracting(Integer::parseInt)
            .containsExactly(4, 5, 7, 8, 9);
    }
}
