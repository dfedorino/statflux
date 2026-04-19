package com.rmrf.statflux.service;

import com.rmrf.statflux.domain.dto.AddVideoResponse;
import com.rmrf.statflux.domain.dto.RefreshVideosPagedResponse;
import com.rmrf.statflux.domain.dto.RefreshVideosResponse;
import com.rmrf.statflux.domain.dto.VideoStatsItem;
import com.rmrf.statflux.domain.dto.VideoStatsResponse;
import com.rmrf.statflux.domain.result.Failure;
import com.rmrf.statflux.domain.result.Result;
import com.rmrf.statflux.domain.result.Success;
import com.rmrf.statflux.repository.LinkRepository;
import com.rmrf.statflux.repository.PaginationStateRepository;
import com.rmrf.statflux.repository.dto.PaginationStateDto;
import java.time.ZonedDateTime;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserSessionServiceImpl implements UserSessionService {

    private final ServiceLayer serviceLayer;
    private final LinkRepository linkRepository;
    private final PaginationStateRepository paginationStateRepository;

    private static final int ITEMS_PER_PAGE_TEMP = 20;

    public UserSessionServiceImpl(ServiceLayer serviceLayer, LinkRepository linkRepository,
        PaginationStateRepository paginationStateRepository) {
        this.serviceLayer = serviceLayer;
        this.linkRepository = linkRepository;
        this.paginationStateRepository = paginationStateRepository;
    }

    @Override
    public @NonNull Result<AddVideoResponse> addVideo(@NonNull String rawUrl) {
        return serviceLayer.addVideo(rawUrl);
    }

    @Override
    public @NonNull Result<VideoStatsResponse> getVideos(@NonNull Long userId,
        @NonNull Long messageId) {
        try {
            var items = linkRepository.findFirstPage(ITEMS_PER_PAGE_TEMP);
            var totalLinks = linkRepository.getTotalLinkCount();
            var totalViews = linkRepository.getTotalViewSum();

            var firstSeenId = items.isEmpty() ? 0 : items.getFirst().id();
            var lastSeenId = items.isEmpty() ? 0 : items.getLast().id();
            var state = new PaginationStateDto(userId, messageId, firstSeenId, lastSeenId,
                ZonedDateTime.now());
            paginationStateRepository.save(state);

            var responseItems = items.stream()
                .map(l -> new VideoStatsItem(l.rawLink(), l.title(), l.rawLink(), l.views(),
                    l.updatedAt()))
                .toList();
            var hasMore = totalLinks > items.size();
            var resp = new VideoStatsResponse(responseItems, totalLinks, hasMore, false,
                totalViews);
            return Success.of(resp);
        } catch (Throwable e) {
            log.error(
                "UserSessionServiceImpl[getVideos] userId={} messageId={} unhandled exception",
                userId, messageId, e);
            return Failure.of(e);
        }
    }

    @Override
    public @NonNull Result<VideoStatsResponse> getNextVideos(@NonNull Long userId,
        @NonNull Long messageId) {
        try {
            return updateOrInitSession(userId, messageId, paginationState -> {
                var items = linkRepository.findNextPage(paginationState.lastSeenId(),
                    ITEMS_PER_PAGE_TEMP);
                var totalLinks = linkRepository.getTotalLinkCount();
                var totalViews = linkRepository.getTotalViewSum();

                var firstSeenId = items.isEmpty() ? 0 : items.getFirst().id();
                var lastSeenId = items.isEmpty() ? 0 : items.getLast().id();

                var state = new PaginationStateDto(userId, messageId, firstSeenId, lastSeenId,
                    ZonedDateTime.now());
                paginationStateRepository.save(state);

                var responseItems = items.stream()
                    .map(l -> new VideoStatsItem(l.rawLink(), l.title(), l.rawLink(), l.views(),
                        l.updatedAt()))
                    .toList();
                var hasMore = totalLinks > items.size();
                var resp = new VideoStatsResponse(responseItems, totalLinks, hasMore, true,
                    totalViews);
                return Success.of(resp);

            });
        } catch (Throwable e) {
            log.error(
                "UserSessionServiceImpl[getNextVideos] userId={} messageId={} unhandled exception",
                userId, messageId, e);
            return Failure.of(e);
        }
    }

    @Override
    public @NonNull Result<VideoStatsResponse> getPreviousVideos(@NonNull Long userId,
        @NonNull Long messageId) {
        try {
            return updateOrInitSession(userId, messageId, paginationState -> {
                var items = linkRepository.findPreviousPage(paginationState.firstSeenId(),
                    ITEMS_PER_PAGE_TEMP);
                var totalLinks = linkRepository.getTotalLinkCount();
                var totalViews = linkRepository.getTotalViewSum();

                var firstSeenId = items.isEmpty() ? 0 : items.getFirst().id();
                var lastSeenId = items.isEmpty() ? 0 : items.getLast().id();

                var state = new PaginationStateDto(userId, messageId, firstSeenId, lastSeenId,
                    ZonedDateTime.now());
                paginationStateRepository.save(state);

                var responseItems = items.stream()
                    .map(l -> new VideoStatsItem(l.rawLink(), l.title(), l.rawLink(), l.views(),
                        l.updatedAt()))
                    .toList();
                var hasMore = totalLinks > items.size();
                var resp = new VideoStatsResponse(responseItems, totalLinks, hasMore, false,
                    totalViews);
                return Success.of(resp);
            });
        } catch (Throwable e) {
            log.error(
                "UserSessionServiceImpl[getPreviousVideos] userId={} messageId={} unhandled exception",
                userId, messageId, e);
            return Failure.of(e);
        }
    }

    private @NonNull Result<VideoStatsResponse> updateOrInitSession(Long userId, Long messageId,
        Function<PaginationStateDto, Result<VideoStatsResponse>> f) {
        var maybePaginationState = paginationStateRepository.find(userId, messageId);
        if (maybePaginationState.isEmpty()) {
            log.info(
                "UserSessionServiceImpl[updateOrInitSession] userId={} messageId={} session not found",
                userId, messageId);
            return getVideos(userId, messageId);
        }
        var paginationState = maybePaginationState.get();
        return f.apply(paginationState);
    }

    @Override
    public void refreshVideos(@NonNull Long userId, @NonNull Long messageId,
        Consumer<Result<RefreshVideosPagedResponse>> callback) {
        Consumer<Result<RefreshVideosResponse>> onRefresh = (refreshResp) -> {
            switch (refreshResp) {
                case Success<RefreshVideosResponse> s -> {
                    try {
                        var paginationState = paginationStateRepository.find(userId, messageId);
                        var firstSeenId =
                            paginationState.isPresent() ? paginationState.get().firstSeenId() : 0L;
                        var items = linkRepository.findNextPage(firstSeenId, ITEMS_PER_PAGE_TEMP);
                        var totalVideos = linkRepository.getTotalLinkCount();
                        var totalViews = linkRepository.getTotalViewSum();
                        var responseItems = items.stream()
                            .map(l -> new VideoStatsItem(l.rawLink(), l.title(), l.rawLink(),
                                l.views(),
                                l.updatedAt()))
                            .toList();
                        var hasNext = responseItems.size() < totalVideos;
                        var hasPrev = firstSeenId > 0;
                        var response = new RefreshVideosPagedResponse(responseItems, totalVideos,
                            hasNext,
                            hasPrev, totalViews, s.get().hasErrors());
                        callback.accept(Success.of(response));
                    } catch (Throwable e) {
                        log.error(
                            "UserSessionServiceImpl[refreshVideos] userId={} messageId={} unhandled exception",
                            userId, messageId, e);
                        callback.accept(Failure.of(e));
                    }

                }
                case Failure<RefreshVideosResponse> f -> callback.accept(f.swap());
            }
        };
        serviceLayer.refreshVideos(onRefresh);
    }
}
