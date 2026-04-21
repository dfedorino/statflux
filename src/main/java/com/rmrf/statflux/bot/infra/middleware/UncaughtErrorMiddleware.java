package com.rmrf.statflux.bot.infra.middleware;

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
public class UncaughtErrorMiddleware implements Chain.Node<TelegramBotContext> {
    private final Localization.Common l10n;

    public UncaughtErrorMiddleware(Localization.Common l10n) {
        this.l10n = l10n;
    }

    @Override
    public void handle(TelegramBotContext ctx, Consumer<TelegramBotContext> next) {
        try {
            next.accept(ctx);
        } catch (Throwable t) {
            handleUncaughtError(ctx, t);
        }
    }

    private void handleUncaughtError(TelegramBotContext ctx, Throwable t) {
        log.error("Caught uncaught error", t);
        if (!ctx.update().hasMessage()) {
            return;
        }
        Message message = ctx.update().getMessage();

        var answerMessage = SendMessage.builder()
                .chatId(message.getChatId())
                .text(l10n.uncaughtError)
                .replyParameters(
                        ReplyParameters.builder()
                            .messageId(message.getMessageId())
                            .build()
                )
                .build();

        try {
            ctx.client().execute(answerMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
        }
    }
}
