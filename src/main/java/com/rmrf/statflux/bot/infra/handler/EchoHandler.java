package com.rmrf.statflux.bot.infra.handler;

import com.rmrf.statflux.bot.core.Chain;
import com.rmrf.statflux.bot.core.TelegramBotContext;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.function.Consumer;

@Slf4j
public class EchoHandler implements Chain.Node<TelegramBotContext> {
    @Override
    public void handle(TelegramBotContext ctx, Consumer<TelegramBotContext> next) {
        log.debug("in Echo");
        if (ctx.update().hasMessage() && ctx.update().getMessage().hasText()) {
            String text = ctx.update().getMessage().getText();
            try {
                ctx.client().execute(new SendMessage(
                        String.valueOf(ctx.update().getMessage().getChatId()),
                        text
                ));
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
            }
        }
    }
}
