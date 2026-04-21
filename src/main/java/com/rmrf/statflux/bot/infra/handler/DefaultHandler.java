package com.rmrf.statflux.bot.infra.handler;

import com.rmrf.statflux.bot.core.Chain;
import com.rmrf.statflux.bot.core.TelegramBotContext;
import com.rmrf.statflux.bot.infra.l10n.Localization;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.ReplyParameters;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.function.Consumer;

@Slf4j
public class DefaultHandler implements Chain.Node<TelegramBotContext> {
    private final Localization.Common l10n;

    public DefaultHandler(Localization.Common l10n) {
        this.l10n = l10n;
    }

    @Override
    public void handle(TelegramBotContext ctx, Consumer<TelegramBotContext> next) {
        Message message = ctx.update().getMessage();
        if (!ctx.update().hasMessage() || !message.hasText()) {
            next.accept(ctx);
            return;
        }

        log.debug("handling default case");

        SendMessage responseMessage = SendMessage.builder()
                .chatId(ctx.update().getMessage().getChatId())
                .text(l10n.useKnownCommands)
                .replyParameters(
                        ReplyParameters.builder()
                                .messageId(message.getMessageId())
                                .chatId(message.getChatId())
                                .build()
                )
                .build();
        try {
            ctx.client().execute(responseMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
        }
    }
}
