package com.rmrf.statflux.repository;

import com.rmrf.statflux.repository.dto.LinkDto;
import java.util.List;
import lombok.NonNull;

public interface LinkRepository {

    boolean save(@NonNull LinkDto linkDto);

    List<LinkDto> findAll();

    List<LinkDto> findAllForUpdate();

    int getTotalLinkCount();

    long getTotalViewSum();

    List<LinkDto> findFirstPage(int limit);

    List<LinkDto> findNextPage(long lastSeenId, int limit);

    List<LinkDto> findPreviousPage(long firstSeenId, int limit);

}
