package com.rmrf.statflux.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.rmrf.statflux.repository.dto.LinkDto;
import java.time.ZonedDateTime;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TestDataFactory {

    public static void insertLinks(LinkRepository linkRepository, ZonedDateTime now, int amount) {
        insertLinks(linkRepository, 1000L, now, amount);
    }

    public static void insertLinks(LinkRepository linkRepository, long views, ZonedDateTime now, int amount) {
        for (int i = 1; i <= amount; i++) {
            assertThat(linkRepository.save(new LinkDto(
                null,
                "YouTube",
                "https://youtube.com/v/" + i,
                "" + i,
                "My video " + i,
                views,
                now
            ))).isTrue();
        }
    }

}
