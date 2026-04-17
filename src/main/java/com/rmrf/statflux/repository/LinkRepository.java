package com.rmrf.statflux.repository;

import com.rmrf.statflux.repository.dto.LinkDto;
import java.util.List;
import lombok.NonNull;

public interface LinkRepository {

    boolean save(@NonNull LinkDto linkDto);

    List<LinkDto> findAll();

    int getTotalLinkCount();

    long getTotalViewSum();

}
