package com.rmrf.statflux.bot.infra.handler;

import com.rmrf.statflux.bot.core.Chain;
import com.rmrf.statflux.bot.core.TelegramBotContext;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.function.Consumer;

@Slf4j
public class RefreshCallbackHandler implements Chain.Node<TelegramBotContext> {

    @Override
    public void handle(TelegramBotContext ctx, Consumer<TelegramBotContext> next) {
        CallbackQuery callbackQuery = ctx.update().getCallbackQuery();
        if (!ctx.update().hasCallbackQuery() || !"refresh".equals(callbackQuery.getData())) {
            next.accept(ctx);
            return;
        }

        log.debug("callback 'refresh' handling");

        MaybeInaccessibleMessage message = callbackQuery.getMessage();
        try {
            ctx.client().execute(
                    EditMessageText.builder()
                            .chatId(message.getChatId())
                            .messageId(message.getMessageId())
                            // Прикрутить общую модель с LinkHandler и формировать базу сообщения там
                            .text("Отредактированное сообщение")
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
