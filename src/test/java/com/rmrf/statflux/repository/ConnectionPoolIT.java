package com.rmrf.statflux.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.rmrf.statflux.AbstractIntegrationTest;
import com.rmrf.statflux.repository.config.RepositoryConfig;
import com.rmrf.statflux.repository.datasource.DataSource;
import com.rmrf.statflux.repository.dto.LinkDto;
import com.rmrf.statflux.repository.transaction.TransactionManager;
import com.rmrf.statflux.repository.util.SqlScripts;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
public class ConnectionPoolIT extends AbstractIntegrationTest {

    private LinkRepository linkRepository;
    private TransactionManager tx;

    @BeforeEach
    public void setup() {
        RepositoryConfig repositoryConfig = new RepositoryConfig();
        linkRepository = repositoryConfig.linkRepository();
        tx = new TransactionManager(repositoryConfig.pooledDataSource());
        tx.executeWithoutResult(() -> SqlScripts.run("schema.sql"));
    }

    @Test
    public void should_use_different_connections() throws InterruptedException {
        var start = new CountDownLatch(1);

        tx.executeWithoutResult(() -> TestDataFactory.insertLinks(linkRepository,
            ZonedDateTime.now(), 10));

        var t1 = Thread.ofVirtual().name("user-1").uncaughtExceptionHandler(
            (t, e) -> {
                log.error("Exception in thread user-1", e);
                Assertions.fail(e);
            }).start(() -> {
            safeAwait(start);
            log.info("user-1 started");
            tx.executeWithoutResult(() -> linkRepository.findAllForUpdate()
                .forEach(link -> linkRepository.save(new LinkDto(
                    link.id(),
                    1L,
                    link.hostingName(),
                    link.rawLink(),
                    link.hostingId(),
                    link.title(),
                    link.views() + 100,
                    ZonedDateTime.now()
                ))));
        });

        var t2 = Thread.ofVirtual().name("user-2").uncaughtExceptionHandler(
            (t, e) -> {
                log.error("Exception in thread user-2", e);
                Assertions.fail(e);
            }).start(() -> {
            safeAwait(start);
            log.info("user-2 started");
            tx.executeWithoutResult(() -> linkRepository.findAllForUpdate()
                .forEach(link -> {
                linkRepository.save(new LinkDto(
                    link.id(),
                    1L,
                    link.hostingName(),
                    link.rawLink(),
                    link.hostingId(),
                    link.title(),
                    link.views() + 100,
                    ZonedDateTime.now()
                ));
            }));
        });

        log.info("start test");
        start.countDown();
        t1.join();
        t2.join();

        List<LinkDto> actual = tx.execute(() -> linkRepository.findAll());
        assertThat(actual)
            .hasSize(10)
            .allMatch(link -> link.views() == 1200);
    }

    private static void safeAwait(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Assertions.fail(e);
        }
    }


}
