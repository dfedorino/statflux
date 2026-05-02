package com.rmrf.statflux.constructor;

import com.rmrf.statflux.bot.infra.l10n.Localization;
import com.rmrf.statflux.bot.infra.util.HumanReadableTime;
import com.rmrf.statflux.bot.infra.util.TelegramBotFormatter;
import com.rmrf.statflux.domain.dto.VideoStatsItem;
import com.rmrf.statflux.domain.dto.VideoStatsResponse;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class StatsMessageConstructor {
    private final VideoStatsResponse statsResponse;
    private final Localization.Stats l10n;
    private final Localization.TimeFormat timeFormatL10n;

    public String getText() {
        StringBuilder videosStatsInfo = new StringBuilder();
        for (VideoStatsItem video : statsResponse.getItems()) {
            videosStatsInfo
                    .append("\\[")
                    .append(video.id())
                    .append("\\]")
                    .append(' ')
                    .append('[')
                    .append(TelegramBotFormatter.escapeSpecial(video.name()))
                    .append(']')
                    .append('(')
                    .append(TelegramBotFormatter.escapeSpecial(video.rawUrl()))
                    .append(')')
                    .append('\n')
                    .append('_')
                    .append(l10n.views)
                    .append(": ")
                    .append(TelegramBotFormatter.groupThousands(video.views()))
                    .append('_')
                    .append('\n')
                    .append('_')
                    .append(l10n.updatedAt)
                    .append(": ")
                    .append(HumanReadableTime.format(video.updatedAt(), timeFormatL10n))
                    .append('_')
                    .append('\n')
                    .append('\n');
        }

        String text = new StringBuilder()
                .append(TelegramBotFormatter.escapeSpecial(l10n.introduction))
                .append('\n')
                .append('\n')
                .append(videosStatsInfo)
                .append("──────────")
                .append('\n')
                .append(l10n.totalViews)
                .append(' ')
                .append(TelegramBotFormatter.groupThousands(statsResponse.getTotalViews()))
                .append('\n')
                .append(l10n.totalLinkCount)
                .append(' ')
                .append(TelegramBotFormatter.groupThousands(statsResponse.getTotalVideos()))
                .toString();

        return text;
    }

    public InlineKeyboardMarkup getMarkup() {
        List<InlineKeyboardButton> paginationButtons = new ArrayList<>();
        if (statsResponse.hasPrev()) {
            paginationButtons.add(
                    InlineKeyboardButton.builder()
                            .text(l10n.prev)
                            .callbackData("prev")
                            .build()
            );
        }
        if (statsResponse.hasNext()) {
            paginationButtons.add(
                    InlineKeyboardButton.builder()
                            .text(l10n.next)
                            .callbackData("next")
                            .build()
            );
        }

        return InlineKeyboardMarkup.builder()
                .keyboard(
                        List.of(
                            new InlineKeyboardRow(
                                InlineKeyboardButton.builder()
                                        .text(l10n.refresh)
                                        .callbackData("refresh")
                                        .build()
                            ),
                            new InlineKeyboardRow(
                                paginationButtons
                            )
                        )
                )
                .build();
    }
}
