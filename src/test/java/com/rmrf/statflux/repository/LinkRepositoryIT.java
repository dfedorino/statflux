package com.rmrf.statflux.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.dockerjava.api.model.Repository;
import com.rmrf.statflux.AbstractIntegrationTest;
import com.rmrf.statflux.repository.config.RepositoryConfig;
import com.rmrf.statflux.repository.dto.LinkDto;
import com.rmrf.statflux.repository.transaction.TransactionManager;
import com.rmrf.statflux.repository.util.Queries;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LinkRepositoryIT extends AbstractIntegrationTest {
    private final RepositoryConfig repositoryConfig = new RepositoryConfig();
    private final TransactionManager tx = new TransactionManager(repositoryConfig.pooledDataSource());
    private LinkRepository linkRepository;

    @BeforeEach
    public void setup() {
        linkRepository = repositoryConfig.linkRepository();
    }

    @AfterEach
    public void tearDown() {
        tx.executeWithoutResult(
            () -> Queries.update("TRUNCATE TABLE links RESTART IDENTITY CASCADE"));
    }

    @Test
    void should_insert_video() {

        ZonedDateTime now = ZonedDateTime
            .now(ZoneOffset.UTC)
            .truncatedTo(ChronoUnit.MICROS);

        assertThat(tx.execute(() -> linkRepository.save(new LinkDto(
            null,
            "YouTube",
            "https://youtube.com/v/123",
            "123",
            "My video",
            1000L,
            now
        )))).isTrue();

        var links = tx.execute(() -> linkRepository.findAll());

        assertThat(links)
            .hasSize(1)
            .first()
            .isEqualTo(new LinkDto(
                1L,
                "YouTube",
                "https://youtube.com/v/123",
                "123",
                "My video",
                1000L,
                now
            ));
    }

    @Test
    void should_update_views() {

        ZonedDateTime initialUpdate = ZonedDateTime.now(ZoneOffset.UTC)
            .truncatedTo(ChronoUnit.MICROS);

        assertThat(tx.execute(() -> linkRepository.save(new LinkDto(
            null,
            "YouTube",
            "https://youtube.com/v/123",
            "123",
            "My video",
            1000L,
            initialUpdate
        )))).isTrue();

        ZonedDateTime update = ZonedDateTime.now(ZoneOffset.UTC)
            .truncatedTo(ChronoUnit.MICROS);

        assertThat(tx.execute(() -> linkRepository.save(new LinkDto(
            1L,
            "YouTube",
            "https://youtube.com/v/123",
            "123",
            "My video",
            1005L,
            update
        )))).isTrue();

        var links = tx.execute(() -> linkRepository.findAll());

        assertThat(links)
            .hasSize(1)
            .first()
            .isEqualTo(new LinkDto(
                1L,
                "YouTube",
                "https://youtube.com/v/123",
                "123",
                "My video",
                1005L,
                update
            ));
    }

    @Test
    void should_get_total_links() {

        assertThat(tx.execute(() -> linkRepository.save(new LinkDto(
            null,
            "YouTube",
            "https://youtube.com/v/123",
            "123",
            "My video",
            1000L,
            ZonedDateTime.now(ZoneOffset.UTC)
        )))).isTrue();

        assertThat(tx.execute(() -> linkRepository.save(new LinkDto(
            null,
            "YouTube",
            "https://youtube.com/v/456",
            "456",
            "My another video",
            500L,
            ZonedDateTime.now(ZoneOffset.UTC)
        )))).isTrue();

        var links = tx.execute(() -> linkRepository.findAll());

        assertThat(links)
            .hasSize(2);

        assertThat(tx.execute(() -> linkRepository.getTotalLinkCount()))
            .isEqualTo(2);
    }

    @Test
    void should_get_total_views() {

        assertThat(tx.execute(() -> linkRepository.save(new LinkDto(
            null,
            "YouTube",
            "https://youtube.com/v/123",
            "123",
            "My video",
            1000L,
            ZonedDateTime.now(ZoneOffset.UTC)
        )))).isTrue();

        assertThat(tx.execute(() -> linkRepository.save(new LinkDto(
            null,
            "YouTube",
            "https://youtube.com/v/456",
            "456",
            "My another video",
            500L,
            ZonedDateTime.now(ZoneOffset.UTC)
        )))).isTrue();

        var links = tx.execute(() -> linkRepository.findAll());

        assertThat(links)
            .hasSize(2);

        assertThat(tx.execute(() -> linkRepository.getTotalViewSum()))
            .isEqualTo(1500L);
    }

    @Test
    void should_get_first_page() {

        var now = ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MICROS);

        tx.executeWithoutResult(() -> TestDataFactory.insertLinks(linkRepository, now, 3));

        var actual = tx.execute(() -> linkRepository.findFirstPage(2));

        assertThat(actual)
            .hasSize(2)
            .containsExactly(
                new LinkDto(
                    1L,
                    "YouTube",
                    "https://youtube.com/v/1",
                    "1",
                    "My video 1",
                    1000L,
                    now
                ),
                new LinkDto(
                    2L,
                    "YouTube",
                    "https://youtube.com/v/2",
                    "2",
                    "My video 2",
                    1000L,
                    now
                )
            );
    }

    @Test
    void should_get_first_page_when_no_videos() {
        var actual = tx.execute(() -> linkRepository.findFirstPage(5));
        assertThat(actual).isEmpty();
    }

    @Test
    void should_get_first_page_when_less_than_limit_videos() {
        var now = ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MICROS);
        tx.executeWithoutResult(() -> TestDataFactory.insertLinks(linkRepository, now, 3));
        var actual = tx.execute(() -> linkRepository.findFirstPage(5));
        assertThat(actual)
            .hasSize(3)
            .containsExactly(
                new LinkDto(
                    1L,
                    "YouTube",
                    "https://youtube.com/v/1",
                    "1",
                    "My video 1",
                    1000L,
                    now
                ),
                new LinkDto(
                    2L,
                    "YouTube",
                    "https://youtube.com/v/2",
                    "2",
                    "My video 2",
                    1000L,
                    now
                ),
                new LinkDto(
                    3L,
                    "YouTube",
                    "https://youtube.com/v/3",
                    "3",
                    "My video 3",
                    1000L,
                    now
                )
            );
    }

    @Test
    void should_get_next_page() {

        var now = ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MICROS);

        tx.executeWithoutResult(() -> TestDataFactory.insertLinks(linkRepository, now, 3));

        var actual= tx.execute(() ->  linkRepository.findNextPage(1, 2));

        assertThat(actual)
            .hasSize(2)
            .containsExactly(
                new LinkDto(
                    2L,
                    "YouTube",
                    "https://youtube.com/v/2",
                    "2",
                    "My video 2",
                    1000L,
                    now
                ),
                new LinkDto(
                    3L,
                    "YouTube",
                    "https://youtube.com/v/3",
                    "3",
                    "My video 3",
                    1000L,
                    now
                )
            );
    }

    @Test
    void should_get_next_page_when_less_limit_videos() {

        var now = ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MICROS);

        tx.executeWithoutResult(() -> TestDataFactory.insertLinks(linkRepository, now, 4));

        var actual = tx.execute(() ->  linkRepository.findNextPage(1, 5));

        assertThat(actual)
            .hasSize(3)
            .containsExactly(
                new LinkDto(
                    2L,
                    "YouTube",
                    "https://youtube.com/v/2",
                    "2",
                    "My video 2",
                    1000L,
                    now
                ),
                new LinkDto(
                    3L,
                    "YouTube",
                    "https://youtube.com/v/3",
                    "3",
                    "My video 3",
                    1000L,
                    now
                ),
                new LinkDto(
                    4L,
                    "YouTube",
                    "https://youtube.com/v/4",
                    "4",
                    "My video 4",
                    1000L,
                    now
                )
            );
    }

    @Test
    void should_get_previous_page() {

        var now = ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MICROS);

        tx.executeWithoutResult(() -> TestDataFactory.insertLinks(linkRepository, now, 3));

        var actual = tx.execute(() -> linkRepository.findPreviousPage(3, 2));

        assertThat(actual)
            .hasSize(2)
            .containsExactly(
                new LinkDto(
                    1L,
                    "YouTube",
                    "https://youtube.com/v/1",
                    "1",
                    "My video 1",
                    1000L,
                    now
                ),
                new LinkDto(
                    2L,
                    "YouTube",
                    "https://youtube.com/v/2",
                    "2",
                    "My video 2",
                    1000L,
                    now
                )
            );
    }

    @Test
    void should_get_previous_page_when_less_limit_videos() {

        var now = ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MICROS);

        tx.executeWithoutResult(() -> TestDataFactory.insertLinks(linkRepository, now, 4));

        var actual = tx.execute(() -> linkRepository.findPreviousPage(4, 5));

        assertThat(actual)
            .hasSize(3)
            .containsExactly(
                new LinkDto(
                    1L,
                    "YouTube",
                    "https://youtube.com/v/1",
                    "1",
                    "My video 1",
                    1000L,
                    now
                ),
                new LinkDto(
                    2L,
                    "YouTube",
                    "https://youtube.com/v/2",
                    "2",
                    "My video 2",
                    1000L,
                    now
                ),
                new LinkDto(
                    3L,
                    "YouTube",
                    "https://youtube.com/v/3",
                    "3",
                    "My video 3",
                    1000L,
                    now
                )
            );
    }
}
