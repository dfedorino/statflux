package com.rmrf.statflux.bot.infra.handler;

import com.rmrf.statflux.bot.core.Chain;
import com.rmrf.statflux.bot.core.TelegramBotContext;
import org.slf4j.Logger;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public abstract class AbstractCallbackHandler implements Chain.Node<TelegramBotContext> {
    protected final Logger log;

    protected AbstractCallbackHandler(Logger log) {
        this.log = log;
    }

    protected void answerCallbackQuery(TelegramBotContext ctx) {
        CallbackQuery callbackQuery = ctx.update().getCallbackQuery();
        try {
            ctx.client().execute(
                    AnswerCallbackQuery.builder()
                            .callbackQueryId(callbackQuery.getId())
                            .build()
            );
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
        }
    }

    protected void handleFailure(TelegramBotContext ctx, String reason) {
        MaybeInaccessibleMessage message = ctx.update().getCallbackQuery().getMessage();

        SendMessage answerMessage = SendMessage.builder()
                .chatId(message.getChatId())
                .replyToMessageId(message.getMessageId())
                .text(reason)
                .build();
        try {
            ctx.client().execute(answerMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
        }
        answerCallbackQuery(ctx);
    }
}
