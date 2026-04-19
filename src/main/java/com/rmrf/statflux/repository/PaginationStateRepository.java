package com.rmrf.statflux.repository;

import com.rmrf.statflux.repository.dto.PaginationStateDto;
import java.util.Optional;
import lombok.NonNull;

public interface PaginationStateRepository {

    Optional<PaginationStateDto> find(@NonNull Long chatId, @NonNull Long messageId);

    boolean save(PaginationStateDto state);
}
