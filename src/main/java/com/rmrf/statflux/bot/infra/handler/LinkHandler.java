package com.rmrf.statflux.bot.infra.handler;

import com.rmrf.statflux.bot.core.Chain;
import com.rmrf.statflux.bot.core.TelegramBotContext;
import com.rmrf.statflux.bot.infra.l10n.Localization;
import com.rmrf.statflux.service.ServiceLayer;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.ReplyParameters;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.function.Consumer;

@Slf4j
public class LinkHandler implements Chain.Node<TelegramBotContext> {
    private final ServiceLayer serviceLayer;
    private final Localization.Link localization;

    public LinkHandler(ServiceLayer serviceLayer, Localization.Link localization) {
        this.serviceLayer = serviceLayer;
        this.localization = localization;
    }

    @Override
    public void handle(TelegramBotContext ctx, Consumer<TelegramBotContext> next) {
        Message message = ctx.update().getMessage();
        if (!ctx.update().hasMessage() || !message.hasText() ||
            !message.getText().startsWith("https://")) {
            next.accept(ctx);
            return;
        }

        String link = ctx.update().getMessage().getText();
        log.debug("link '{}' handling", link);
        if (link.startsWith("https://rutube.ru")) {
            handleSuccess(ctx, link);
            return;
        }
        if (link.startsWith("https://youtube.com")) {
            handleSuccess(ctx, link);
            return;
        }

        handleIncorrect(ctx);
    }

    private void handleSuccess(TelegramBotContext ctx, String link) {
        Message message = ctx.update().getMessage();

        String text = new StringBuilder()
                .append(localization.videoStatistics)
                .append(' ')
                // Временно
                .append(link)
                .append('\n')
                .append('\n')
                .append(localization.statsMotivationText)
                .toString();

        try {
            ctx.client().execute(
                    SendMessage.builder()
                            .chatId(message.getChatId())
                            .text(text)
                            .replyParameters(
                                    ReplyParameters.builder()
                                            .messageId(message.getMessageId())
                                            .build()
                            )
                            .build()
            );
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void handleIncorrect(TelegramBotContext ctx) {
        Message message = ctx.update().getMessage();

        try {
            ctx.client().execute(
                    SendMessage.builder()
                            .chatId(message.getChatId())
                            .text(localization.incorrect)
                            .replyParameters(
                                    ReplyParameters.builder()
                                            .messageId(message.getMessageId())
                                            .build()
                            )
                            .build()
            );
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
        }
    }
}
