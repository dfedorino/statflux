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
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.ReplyParameters;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.function.Consumer;

@Slf4j
public class CommandStatsHandler implements Chain.Node<TelegramBotContext> {
    private final ServiceLayer serviceLayer;
    private final Localization l10n;

    public CommandStatsHandler(ServiceLayer serviceLayer, Localization l10n) {
        this.serviceLayer = serviceLayer;
        this.l10n = l10n;
    }

    @Override
    public void handle(TelegramBotContext ctx, Consumer<TelegramBotContext> next) {
        Message message = ctx.update().getMessage();
        if (!ctx.update().hasMessage() || !message.hasText() ||
            !message.getText().startsWith("/stats")) {
            next.accept(ctx);
            return;
        }

        log.debug("/stats handling");

//        var statsResult = serviceLayer.getVideos(message.getChatId(), (long) message.getMessageId());
        var statsResult = Success.of(new VideoStatsResponse(
                Arrays.asList(
                        new VideoStatsItem("test", "sme", "https://youtube.com/123", 337L, ZonedDateTime.now()),
                        new VideoStatsItem("test2", "sme2", "https://youtube.com/124", 338L, ZonedDateTime.now()),
                        new VideoStatsItem("test3", "sme2", "https://youtube.com/125", 339L, ZonedDateTime.now()),
                        new VideoStatsItem("test4", "sme2", "https://youtube.com/126", 100000L, ZonedDateTime.now()),
                        new VideoStatsItem("Test5", "sme2", "https://youtube.com/127", 100L, ZonedDateTime.now()),
                        new VideoStatsItem("Test6", "sme2", "https://youtube.com/128", 10L, ZonedDateTime.now()),
                        new VideoStatsItem("test91232113", "sme2", "https://youtube.com/129", 7L, ZonedDateTime.now())
                ),
                10,
                true,
                true,
                100000
        ));
        VideoStatsResponse videoStatsResponse = statsResult.get();

        StatsMessageConstructor messageConstructor = new StatsMessageConstructor(videoStatsResponse, l10n.stats);
        SendMessage responseMessage = SendMessage.builder()
                .chatId(ctx.update().getMessage().getChatId())
                .replyParameters(ReplyParameters.builder()
                        .messageId(ctx.update().getMessage().getMessageId())
                        .build()
                )
                .text(messageConstructor.getText())
                .parseMode("MarkdownV2")
                .replyMarkup(messageConstructor.getMarkup())
                .build();

        try {
            ctx.client().execute(responseMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
        }
    }
}
