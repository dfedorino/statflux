package com.rmrf.statflux.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.rmrf.statflux.repository.dto.LinkDto;
import com.rmrf.statflux.repository.dto.PaginationStateDto;
import java.time.ZonedDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FullFlowIT extends BaseRepositoryTest {
    private LinkRepository linkRepository;
    private PaginationStateRepository paginationStateRepository;

    @BeforeEach
    public void setup() {
        linkRepository = repositoryConfig.linkRepository();
        paginationStateRepository = repositoryConfig.paginationStateRepository();
    }

    @Test
    public void should_return_all_pages_in_order() {
        TestDataFactory.insertLinks(linkRepository, ZonedDateTime.now(), 7);

        // 1, 2, 3
        Optional<PaginationStateDto> chatPaginationState = paginationStateRepository.find(1L, 2L);
        assertThat(chatPaginationState).isEmpty();

        var links = linkRepository.findFirstPage(3);
        assertThat(links).extracting(LinkDto::id).containsExactly(1L, 2L, 3L);

        paginationStateRepository.save(new PaginationStateDto(
            1L, 2L, links.getFirst().id(), links.getLast().id(), ZonedDateTime.now()
        ));

        // 4, 5, 6
        chatPaginationState = paginationStateRepository.find(1L, 2L);
        assertThat(chatPaginationState).isNotEmpty();

        Long lastSeenId = chatPaginationState.get().lastSeenId();
        assertThat(lastSeenId).isEqualTo(3L);

        links = linkRepository.findNextPage(lastSeenId, 3);
        assertThat(links).extracting(LinkDto::id).containsExactly(4L, 5L, 6L);
        paginationStateRepository.save(new PaginationStateDto(
            1L, 2L, links.getFirst().id(), links.getLast().id(), ZonedDateTime.now()
        ));

        // 7
        chatPaginationState = paginationStateRepository.find(1L, 2L);
        assertThat(chatPaginationState).isNotEmpty();

        lastSeenId = chatPaginationState.get().lastSeenId();
        assertThat(lastSeenId).isEqualTo(6L);

        links = linkRepository.findNextPage(lastSeenId, 3);
        assertThat(links).extracting(LinkDto::id).containsExactly(7L);
        paginationStateRepository.save(new PaginationStateDto(
            1L, 2L, links.getFirst().id(), links.getLast().id(), ZonedDateTime.now()
        ));

        // 4, 5, 6
        chatPaginationState = paginationStateRepository.find(1L, 2L);
        assertThat(chatPaginationState).isNotEmpty();

        Long firstSeenId = chatPaginationState.get().firstSeenId();
        assertThat(firstSeenId).isEqualTo(7L);

        links = linkRepository.findPreviousPage(firstSeenId, 3);
        assertThat(links).extracting(LinkDto::id).containsExactly(4L, 5L, 6L);

        paginationStateRepository.save(new PaginationStateDto(
            1L, 2L, links.getFirst().id(), links.getLast().id(), ZonedDateTime.now()
        ));

        // 1, 2, 3
        chatPaginationState = paginationStateRepository.find(1L, 2L);
        assertThat(chatPaginationState).isNotEmpty();

        firstSeenId = chatPaginationState.get().firstSeenId();
        assertThat(firstSeenId).isEqualTo(4L);

        links = linkRepository.findPreviousPage(firstSeenId, 3);
        assertThat(links).extracting(LinkDto::id).containsExactly(1L, 2L, 3L);
        paginationStateRepository.save(new PaginationStateDto(
            1L, 2L, links.getFirst().id(), links.getLast().id(), ZonedDateTime.now()
        ));
    }

}
