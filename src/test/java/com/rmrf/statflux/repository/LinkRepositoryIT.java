package com.rmrf.statflux.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.rmrf.statflux.repository.config.RepositoryConfig;
import com.rmrf.statflux.repository.datasource.DataSource;
import com.rmrf.statflux.repository.dto.LinkDto;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LinkRepositoryIT {

    protected RepositoryConfig repositoryConfig;
    protected DataSource dataSource;
    private LinkRepository linkRepository;

    @BeforeEach
    void setUp() {
        repositoryConfig = new RepositoryConfig();
        dataSource = repositoryConfig.dataSource();
        repositoryConfig.sqlScriptRunner().run("schema.sql");
    }

    @AfterEach
    void tearDown() {
        dataSource.close();
    }

    @BeforeEach
    public void setup() {
        linkRepository = repositoryConfig.linkRepository();
    }

    @Test
    void should_insert_video() {

        ZonedDateTime now = ZonedDateTime
            .now(ZoneOffset.UTC)
            .truncatedTo(ChronoUnit.MICROS);

        LinkDto link = new LinkDto(
            "YouTube",
            "https://youtube.com/v/123",
            "123",
            "My video",
            1000L,
            now
        );
        assertThat(linkRepository.save(link)).isTrue();

        var links = linkRepository.findAll();

        assertThat(links)
            .hasSize(1)
            .first()
            .isEqualTo(link);
    }

    @Test
    void should_update_views() {

        ZonedDateTime initialUpdate = ZonedDateTime.now(ZoneOffset.UTC)
            .truncatedTo(ChronoUnit.MICROS);

        LinkDto initial = new LinkDto(
            "YouTube",
            "https://youtube.com/v/123",
            "123",
            "My video",
            1000L,
            initialUpdate
        );
        assertThat(linkRepository.save(initial)).isTrue();

        ZonedDateTime update = ZonedDateTime.now(ZoneOffset.UTC)
            .truncatedTo(ChronoUnit.MICROS);

        LinkDto updated = new LinkDto(
            "YouTube",
            "https://youtube.com/v/123",
            "123",
            "My video",
            1005L,
            update
        );

        assertThat(linkRepository.save(updated)).isTrue();

        var links = linkRepository.findAll();

        assertThat(links)
            .hasSize(1)
            .first()
            .isEqualTo(updated);
    }

    @Test
    void should_get_total_links() {

        assertThat(linkRepository.save(new LinkDto(
            "YouTube",
            "https://youtube.com/v/123",
            "123",
            "My video",
            1000L,
            ZonedDateTime.now(ZoneOffset.UTC)
        ))).isTrue();

        assertThat(linkRepository.save(new LinkDto(
            "YouTube",
            "https://youtube.com/v/456",
            "456",
            "My another video",
            500L,
            ZonedDateTime.now(ZoneOffset.UTC)
        ))).isTrue();

        var links = linkRepository.findAll();

        assertThat(links)
            .hasSize(2);

        assertThat(linkRepository.getTotalLinkCount())
            .isEqualTo(2);
    }

    @Test
    void should_get_total_views() {

        assertThat(linkRepository.save(new LinkDto(
            "YouTube",
            "https://youtube.com/v/123",
            "123",
            "My video",
            1000L,
            ZonedDateTime.now(ZoneOffset.UTC)
        ))).isTrue();

        assertThat(linkRepository.save(new LinkDto(
            "YouTube",
            "https://youtube.com/v/456",
            "456",
            "My another video",
            500L,
            ZonedDateTime.now(ZoneOffset.UTC)
        ))).isTrue();

        var links = linkRepository.findAll();

        assertThat(links)
            .hasSize(2);

        assertThat(linkRepository.getTotalViewSum())
            .isEqualTo(1500L);
    }
}
