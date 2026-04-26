package com.rmrf.statflux.bot.infra.handler;

import com.rmrf.statflux.bot.core.Chain;
import com.rmrf.statflux.bot.core.TelegramBotContext;
import com.rmrf.statflux.bot.infra.l10n.Localization;
import com.rmrf.statflux.bot.infra.util.TelegramBotFormatter;
import com.rmrf.statflux.domain.dto.AddVideoResponse;
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
        var responseResult = serviceLayer.addVideo(ctx.update().getMessage().getChatId(), link);
        if (responseResult.isFailure()) {
            handleIncorrect(ctx);
            return;
        }

        handleSuccess(ctx, responseResult.get());
    }

    private void handleSuccess(TelegramBotContext ctx, AddVideoResponse response) {
        Message message = ctx.update().getMessage();

        String text = new StringBuilder()
                .append(localization.videoAddedSuccessfully)
                .append(Localization.DOUBLE_CARRY)
                .append('[')
                .append(TelegramBotFormatter.escapeSpecial(response.title()))
                .append(']')
                .append('(')
                .append(response.rawUrl())
                .append(')')
                .append(Localization.DOUBLE_CARRY)
                .append(localization.views)
                .append(' ')
                .append(response.views())
                .append(Localization.DOUBLE_CARRY)
                .append(localization.statsMotivationText)
                .toString();

        try {
            ctx.client().execute(
                    SendMessage.builder()
                            .chatId(message.getChatId())
                            .text(text)
                            .parseMode("MarkdownV2")
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
                            .text(localization.error)
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
