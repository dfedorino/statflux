package com.rmrf.statflux.bot.infra.handler;

import com.rmrf.statflux.bot.core.Chain;
import com.rmrf.statflux.bot.core.TelegramBotContext;
import com.rmrf.statflux.bot.infra.l10n.Localization;
import com.rmrf.statflux.constructor.StatsMessageConstructor;
import com.rmrf.statflux.domain.dto.VideoStatsItem;
import com.rmrf.statflux.domain.dto.VideoStatsResponse;
import com.rmrf.statflux.domain.result.Success;
import com.rmrf.statflux.service.ServiceLayer;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.function.Consumer;

@Slf4j
public class RefreshCallbackHandler implements Chain.Node<TelegramBotContext> {
    private final ServiceLayer serviceLayer;
    private final Localization l10n;

    public RefreshCallbackHandler(ServiceLayer serviceLayer, Localization l10n) {
        this.serviceLayer = serviceLayer;
        this.l10n = l10n;
    }

    @Override
    public void handle(TelegramBotContext ctx, Consumer<TelegramBotContext> next) {
        CallbackQuery callbackQuery = ctx.update().getCallbackQuery();
        if (!ctx.update().hasCallbackQuery() || !"refresh".equals(callbackQuery.getData())) {
            next.accept(ctx);
            return;
        }

        log.debug("callback 'refresh' handling");

        MaybeInaccessibleMessage message = callbackQuery.getMessage();

        var statsResult = Success.of(new VideoStatsResponse(
                Arrays.asList(
                        new VideoStatsItem("test", "sme", "https://youtube.com/123", 1000L, ZonedDateTime.now()),
                        new VideoStatsItem("test2", "sme2", "https://youtube.com/124", 10001L, ZonedDateTime.now()),
                        new VideoStatsItem("test3", "sme2", "https://youtube.com/125", 2000L, ZonedDateTime.now()),
                        new VideoStatsItem("test4", "sme2", "https://youtube.com/126", 1000337L, ZonedDateTime.now()),
                        new VideoStatsItem("Test5", "sme2", "https://youtube.com/127", 103L, ZonedDateTime.now()),
                        new VideoStatsItem("Test6", "sme2", "https://youtube.com/128", 102L, ZonedDateTime.now()),
                        new VideoStatsItem("test91232113", "sme2", "https://youtube.com/129", 7L, ZonedDateTime.now())
                ),
                10,
                true,
                true,
                100000
        ));
        VideoStatsResponse videoStatsResponse = statsResult.get();
        StatsMessageConstructor statsMessageConstructor = new StatsMessageConstructor(videoStatsResponse, l10n.stats);

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
        try {
            ctx.client().execute(
                    AnswerCallbackQuery.builder()
                            .callbackQueryId(callbackQuery.getId())
                            .build());
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
        }
    }
}
