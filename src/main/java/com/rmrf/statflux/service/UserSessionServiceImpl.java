package com.rmrf.statflux.service;

import com.rmrf.statflux.domain.dto.AddVideoResponse;
import com.rmrf.statflux.domain.dto.RefreshVideosPagedResponse;
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
    public @NonNull Result<VideoStatsResponse> getVideos(@NonNull String userId,
        @NonNull String messageId) {
        try {
            var items = linkRepository.findFirstPage(ITEMS_PER_PAGE_TEMP);
            var totalLinks = linkRepository.getTotalLinkCount();
            var totalViews = linkRepository.getTotalViewSum();

            var firstSeenId = items.isEmpty() ? 0 : items.getFirst().id();
            var lastSeenId = items.isEmpty() ? 0 : items.getLast().id();
            var state = new PaginationStateDto(Long.parseLong(userId), Long.parseLong(messageId),
                firstSeenId, lastSeenId, ZonedDateTime.now());
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
            log.error("UserSessionServiceImpl[getVideos] unhandled exception", e);
            return Failure.of(e);
        }
    }

    @Override
    public @NonNull Result<VideoStatsResponse> getNextVideos(@NonNull String userId,
        @NonNull String messageId) {
        return null;
    }

    @Override
    public @NonNull Result<VideoStatsResponse> getPreviousVideos(@NonNull String userId,
        @NonNull String messageId) {
        return null;
    }

    @Override
    public void refreshVideos(@NonNull String userId, @NonNull String messageId,
        Consumer<Result<RefreshVideosPagedResponse>> callback) {

    }
}
