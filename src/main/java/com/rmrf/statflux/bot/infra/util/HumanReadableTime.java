package com.rmrf.statflux.bot.infra.util;

import com.rmrf.statflux.bot.infra.l10n.Localization;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Форматирование момента времени в человекочитаемый вид.
 * <p>
 * Слова ({@code сегодня}, {@code вчера}, {@code в}) и название локали
 * отображения берутся из {@link Localization.TimeFormat}, чтобы не было
 * хардкода и чтобы локализация полностью управлялась yaml-файлом.
 * <p>
 * Часовой пояс отображения сейчас зафиксирован как {@code Europe/Moscow},
 * при необходимости легко вынести в конфиг.
 */
public final class HumanReadableTime {

    private static final ZoneId DISPLAY_ZONE = ZoneId.of("Europe/Moscow");
    private static final Locale RU = Locale.forLanguageTag("ru");

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm", RU);
    private static final DateTimeFormatter DATE_SAME_YEAR = DateTimeFormatter.ofPattern("d MMMM", RU);
    private static final DateTimeFormatter DATE_FULL = DateTimeFormatter.ofPattern("d MMMM yyyy", RU);

    private HumanReadableTime() {
    }

    /**
     * Возвращает строку вида:
     * <ul>
     *     <li>{@code "<today> <at> 14:30"} - если момент сегодня;</li>
     *     <li>{@code "<yesterday> <at> 14:30"} - если момент был вчера;</li>
     *     <li>{@code "26 апреля <at> 14:30"} - в текущем году;</li>
     *     <li>{@code "26 апреля 2025 <at> 14:30"} - для более старых дат.</li>
     * </ul>
     * Конкретные слова берутся из {@link Localization.TimeFormat}.
     */
    public static String format(ZonedDateTime moment, Localization.TimeFormat l10n) {
        return format(moment, ZonedDateTime.now(DISPLAY_ZONE), l10n);
    }

    static String format(ZonedDateTime moment, ZonedDateTime now, Localization.TimeFormat l10n) {
        ZonedDateTime local = moment.withZoneSameInstant(DISPLAY_ZONE);
        ZonedDateTime nowLocal = now.withZoneSameInstant(DISPLAY_ZONE);

        LocalDate momentDate = local.toLocalDate();
        LocalDate today = nowLocal.toLocalDate();

        String time = local.format(TIME);
        String connector = " " + l10n.at + " ";

        if (momentDate.equals(today)) {
            return l10n.today + connector + time;
        }
        if (momentDate.equals(today.minusDays(1))) {
            return l10n.yesterday + connector + time;
        }
        if (momentDate.getYear() == today.getYear()) {
            return local.format(DATE_SAME_YEAR) + connector + time;
        }
        return local.format(DATE_FULL) + connector + time;
    }
}
