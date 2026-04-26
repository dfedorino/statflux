package com.rmrf.statflux.repository;

import com.rmrf.statflux.repository.dto.LinkDto;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;

public interface LinkRepository {

    boolean save(@NonNull LinkDto linkDto);

    List<LinkDto> findAll();

    List<LinkDto> findBetweenIds(long minId, long maxId);

    List<LinkDto> findAllForUpdate();

    int getTotalLinkCount();

    int getTotalLinkCount(long chatId);

    long getTotalViewSum();

    long getTotalViewSum(long chatId);

    List<LinkDto> findFirstPage(int limit);

    List<LinkDto> findFirstPage(long chatId, int limit);

    List<LinkDto> findNextPage(long lastSeenId, int limit);

    List<LinkDto> findPreviousPage(long firstSeenId, int limit);

    boolean delete(long chatId, long linkId);

    Optional<Long> findMinId(long chatId);

    Optional<Long> findMaxId(long chatId);

}
