package com.rmrf.statflux.constructor;

import com.rmrf.statflux.bot.infra.l10n.Localization;
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

    public String getText() {
        StringBuilder videosStatsInfo = new StringBuilder();
        for (VideoStatsItem video : statsResponse.getItems()) {
            videosStatsInfo = videosStatsInfo.append('[')
                    .append(TelegramBotFormatter.escapeSpecial(video.name()))
                    .append(']')
                    .append('(')
                    .append(TelegramBotFormatter.escapeSpecial(video.rawUrl()))
                    .append(')')
                    .append('\n')
                    .append('_')
                    .append(l10n.views)
                    .append(": ")
                    .append(video.views())
                    .append('_')
                    .append('\n')
                    .append('\n');
        }

        String text = new StringBuilder()
                .append(TelegramBotFormatter.escapeSpecial(l10n.introduction))
                .append('\n')
                .append('\n')
                .append(videosStatsInfo)
                .append(TelegramBotFormatter.escapeSpecial("------------------"))
                .append('\n')
                .append(l10n.totalViews)
                .append(' ')
                .append(statsResponse.getTotalViews())
                .append('\n')
                .append(l10n.totalLinkCount)
                .append(' ')
                .append(statsResponse.getTotalVideos())
                .toString();

        return text;
    }

    public InlineKeyboardMarkup getMarkup() {
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        if (statsResponse.hasPrev()) {
            buttons.add(
                    InlineKeyboardButton.builder()
                            .text("<")
                            .callbackData("prev")
                            .build()
            );
        }
        buttons.add(
                InlineKeyboardButton.builder()
                        .text(l10n.refresh)
                        .callbackData("refresh")
                        .build()
        );
        if (statsResponse.hasNext()) {
            buttons.add(
                    InlineKeyboardButton.builder()
                            .text(">")
                            .callbackData("next")
                            .build()
            );
        }

        return InlineKeyboardMarkup.builder()
                .keyboardRow(
                        new InlineKeyboardRow(
                                buttons
                        )
                )
                .build();
    }
}
