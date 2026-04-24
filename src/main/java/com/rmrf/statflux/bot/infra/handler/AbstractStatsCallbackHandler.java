package com.rmrf.statflux.bot.infra.handler;

import com.rmrf.statflux.bot.core.TelegramBotContext;
import com.rmrf.statflux.bot.infra.l10n.Localization;
import com.rmrf.statflux.constructor.StatsMessageConstructor;
import com.rmrf.statflux.domain.dto.VideoStatsResponse;
import org.slf4j.Logger;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public abstract class AbstractStatsCallbackHandler extends AbstractCallbackHandler {
    private final Localization.Stats l10n;

    protected AbstractStatsCallbackHandler(Localization.Stats l10n, Logger log) {
        super(log);
        this.l10n = l10n;
    }

    protected void editMessageWith(TelegramBotContext ctx, VideoStatsResponse videoStatsResponse) {
        MaybeInaccessibleMessage message = ctx.update().getCallbackQuery().getMessage();

        StatsMessageConstructor statsMessageConstructor = new StatsMessageConstructor(videoStatsResponse, l10n);
        try {
            ctx.client().execute(
                    EditMessageText.builder()
                            .chatId(message.getChatId())
                            .messageId(message.getMessageId())
                            .text(statsMessageConstructor.getText())
                            .replyMarkup(statsMessageConstructor.getMarkup())
                            .parseMode("MarkdownV2")
                            .build()
            );
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
        }
    }

    protected void handleSuccess(TelegramBotContext ctx, VideoStatsResponse videoStatsResponse) {
        editMessageWith(ctx, videoStatsResponse);
        answerCallbackQuery(ctx);
    }
}
