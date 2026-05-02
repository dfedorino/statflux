package com.rmrf.statflux.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.rmrf.statflux.AbstractIntegrationTest;
import com.rmrf.statflux.repository.config.RepositoryConfig;
import com.rmrf.statflux.repository.dto.LinkDto;
import com.rmrf.statflux.repository.dto.PaginationStateDto;
import com.rmrf.statflux.repository.transaction.TransactionManager;
import com.rmrf.statflux.repository.util.Queries;
import java.time.ZonedDateTime;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FullFlowIT extends AbstractIntegrationTest {
    private final RepositoryConfig repositoryConfig = new RepositoryConfig();
    private final TransactionManager tx = new TransactionManager(repositoryConfig.pooledDataSource());
    private LinkRepository linkRepository;
    private PaginationStateRepository paginationStateRepository;

    @BeforeEach
    public void setup() {
        linkRepository = repositoryConfig.linkRepository();
        paginationStateRepository = repositoryConfig.paginationStateRepository();
    }

    @AfterEach
    public void tearDown() {
        tx.executeWithoutResult(
            () -> Queries.update("TRUNCATE TABLE links, pagination_state RESTART IDENTITY CASCADE"));
    }

    @Test
    public void should_return_all_pages_in_order() {
        tx.executeWithoutResult(() -> TestDataFactory.insertLinks(linkRepository,
            ZonedDateTime.now(), 7));

        // 1, 2, 3
        tx.executeWithoutResult(() -> {
            Optional<PaginationStateDto> chatPaginationState = paginationStateRepository.find(1L, 2L);
            assertThat(chatPaginationState).isEmpty();

            var links = linkRepository.findFirstPage(3);
            assertThat(links).extracting(LinkDto::id).containsExactly(1L, 2L, 3L);

            paginationStateRepository.save(new PaginationStateDto(
                1L, 2L, links.getFirst().id(), links.getLast().id(), ZonedDateTime.now()
            ));
        });


        // 4, 5, 6
        tx.executeWithoutResult(() -> {
            var chatPaginationState = paginationStateRepository.find(1L, 2L);
            assertThat(chatPaginationState).isNotEmpty();

            Long lastSeenId = chatPaginationState.get().lastSeenId();
            assertThat(lastSeenId).isEqualTo(3L);

            var links = linkRepository.findNextPage(lastSeenId, 3);
            assertThat(links).extracting(LinkDto::id).containsExactly(4L, 5L, 6L);
            paginationStateRepository.save(new PaginationStateDto(
                1L, 2L, links.getFirst().id(), links.getLast().id(), ZonedDateTime.now()
            ));
        });

        // 7
        tx.executeWithoutResult(() -> {
            var chatPaginationState = paginationStateRepository.find(1L, 2L);
            assertThat(chatPaginationState).isNotEmpty();

            var lastSeenId = chatPaginationState.get().lastSeenId();
            assertThat(lastSeenId).isEqualTo(6L);

            var links = linkRepository.findNextPage(lastSeenId, 3);
            assertThat(links).extracting(LinkDto::id).containsExactly(7L);
            paginationStateRepository.save(new PaginationStateDto(
                1L, 2L, links.getFirst().id(), links.getLast().id(), ZonedDateTime.now()
            ));
        });

        // 4, 5, 6
        tx.executeWithoutResult(() -> {
            var chatPaginationState = paginationStateRepository.find(1L, 2L);
            assertThat(chatPaginationState).isNotEmpty();

            Long firstSeenId = chatPaginationState.get().firstSeenId();
            assertThat(firstSeenId).isEqualTo(7L);

            var links = linkRepository.findPreviousPage(firstSeenId, 3);
            assertThat(links).extracting(LinkDto::id).containsExactly(4L, 5L, 6L);

            paginationStateRepository.save(new PaginationStateDto(
                1L, 2L, links.getFirst().id(), links.getLast().id(), ZonedDateTime.now()
            ));
        });

        // 1, 2, 3
        tx.executeWithoutResult(() -> {
            var chatPaginationState = paginationStateRepository.find(1L, 2L);
            assertThat(chatPaginationState).isNotEmpty();

            var firstSeenId = chatPaginationState.get().firstSeenId();
            assertThat(firstSeenId).isEqualTo(4L);

            var links = linkRepository.findPreviousPage(firstSeenId, 3);
            assertThat(links).extracting(LinkDto::id).containsExactly(1L, 2L, 3L);
            paginationStateRepository.save(new PaginationStateDto(
                1L, 2L, links.getFirst().id(), links.getLast().id(), ZonedDateTime.now()
            ));
        });
    }

    @Test
    public void should_handle_deletion_of_boundary_links() {
        tx.executeWithoutResult(() -> TestDataFactory.insertLinks(linkRepository,
            ZonedDateTime.now(), 7));

        // 1, 2, 3
        tx.executeWithoutResult(() -> {
            var links = linkRepository.findFirstPage(3);
            assertThat(links).extracting(LinkDto::id).containsExactly(1L, 2L, 3L);

            paginationStateRepository.save(new PaginationStateDto(
                1L, 2L, links.getFirst().id(), links.getLast().id(), ZonedDateTime.now()
            ));
        });

        // delete last_seen_id (3) and first_seen_id (1)
        tx.executeWithoutResult(() -> {
            boolean deleted3 = linkRepository.delete(1L, 3L);
            assertThat(deleted3).isTrue();

            boolean deleted1 = linkRepository.delete(1L, 1L);
            assertThat(deleted1).isTrue();
        });

        // next page using stale last_seen_id=3 (deleted) — should still work
        tx.executeWithoutResult(() -> {
            var state = paginationStateRepository.find(1L, 2L);
            assertThat(state).isNotEmpty();

            Long lastSeenId = state.get().lastSeenId();
            assertThat(lastSeenId).isEqualTo(3L);

            var links = linkRepository.findNextPage(lastSeenId, 3);
            assertThat(links).extracting(LinkDto::id).containsExactly(4L, 5L, 6L);

            paginationStateRepository.save(new PaginationStateDto(
                1L, 2L, links.getFirst().id(), links.getLast().id(), ZonedDateTime.now()
            ));
        });

        // prev page using stale first_seen_id=1 (deleted) — should still work
        tx.executeWithoutResult(() -> {
            // restore state to first page boundaries to test prev
            paginationStateRepository.save(new PaginationStateDto(
                1L, 2L, 1L, 3L, ZonedDateTime.now()
            ));

            var state = paginationStateRepository.find(1L, 2L);
            Long firstSeenId = state.get().firstSeenId();
            assertThat(firstSeenId).isEqualTo(1L);

            var links = linkRepository.findPreviousPage(firstSeenId, 3);
            assertThat(links).isEmpty(); // nothing before id=1
        });

        // refresh with both boundaries deleted — returns remaining links in range
        tx.executeWithoutResult(() -> {
            paginationStateRepository.save(new PaginationStateDto(
                1L, 2L, 1L, 3L, ZonedDateTime.now()
            ));

            var state = paginationStateRepository.find(1L, 2L);
            Long firstSeenId = state.get().firstSeenId();
            Long lastSeenId = state.get().lastSeenId();

            var links = linkRepository.findBetweenIds(firstSeenId, lastSeenId);
            assertThat(links).extracting(LinkDto::id).containsExactly(2L); // only 2 survives
        });
    }

}
