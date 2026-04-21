package com.rmrf.statflux.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.rmrf.statflux.repository.dto.PaginationStateDto;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PaginationStateRepositoryIT extends BaseRepositoryTest {

    private PaginationStateRepository paginationStateRepository;

    @BeforeEach
    public void setup() {
        paginationStateRepository = repositoryConfig.paginationStateRepository();
    }

    @Test
    public void should_save_new_pagination_state() {
        ZonedDateTime now = ZonedDateTime
            .now(ZoneOffset.UTC)
            .truncatedTo(ChronoUnit.MICROS);

        PaginationStateDto paginationState = new PaginationStateDto(
            1L,
            2L,
            3L,
            4L,
            now
        );
        assertThat(tx.execute(() -> paginationStateRepository.save(paginationState))).isTrue();
        assertThat(tx.execute(() -> paginationStateRepository.find(1L, 2L)))
            .contains(paginationState);

    }

    @Test
    public void should_update_pagination_state() {
        ZonedDateTime now = ZonedDateTime
            .now(ZoneOffset.UTC)
            .truncatedTo(ChronoUnit.MICROS);

        PaginationStateDto paginationState = new PaginationStateDto(
            1L,
            2L,
            3L,
            4L,
            now
        );
        assertThat(tx.execute(() -> paginationStateRepository.save(paginationState))).isTrue();

        PaginationStateDto newPaginationState = new PaginationStateDto(
            1L,
            2L,
            4L,
            5L,
            now
        );
        assertThat(tx.execute(() -> paginationStateRepository.save(newPaginationState))).isTrue();
        assertThat(tx.execute(() -> paginationStateRepository.find(1L, 2L)))
            .contains(newPaginationState);

    }
}
