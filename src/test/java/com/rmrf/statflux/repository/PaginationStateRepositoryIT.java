package com.rmrf.statflux.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.rmrf.statflux.AbstractIntegrationTest;
import com.rmrf.statflux.repository.config.RepositoryConfig;
import com.rmrf.statflux.repository.dto.PaginationStateDto;
import com.rmrf.statflux.repository.transaction.TransactionManager;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import com.rmrf.statflux.repository.datasource.DataSource;
import com.rmrf.statflux.repository.util.Queries;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PaginationStateRepositoryIT extends AbstractIntegrationTest {
    private DataSource dataSource;
    private TransactionManager tx;
    private PaginationStateRepository paginationStateRepository;

    @BeforeEach
    public void setup() {
        RepositoryConfig repositoryConfig = new RepositoryConfig();
        dataSource = repositoryConfig.pooledDataSource();
        tx = new TransactionManager(dataSource);
        paginationStateRepository = repositoryConfig.paginationStateRepository();
    }

    @AfterEach
    public void tearDown() {
        tx.executeWithoutResult(
            () -> Queries.update("TRUNCATE TABLE links, pagination_state RESTART IDENTITY CASCADE"));
        dataSource.close();
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
