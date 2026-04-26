package com.rmrf.statflux.bot.infra.handler;

import com.rmrf.statflux.bot.core.Chain;
import com.rmrf.statflux.bot.core.TelegramBotContext;
import com.rmrf.statflux.bot.infra.l10n.Localization;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
    import org.telegram.telegrambots.meta.api.objects.message.Message;
    import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.function.Consumer;

@Slf4j
public class CommandStartHandler implements Chain.Node<TelegramBotContext> {
    private final Localization.Start localization;

    public CommandStartHandler(Localization.Start localization) {
        this.localization = localization;
    }

    @Override
    public void handle(TelegramBotContext ctx, Consumer<TelegramBotContext> next) {
        Message message = ctx.update().getMessage();
        if (!ctx.update().hasMessage() || !message.hasText() ||
            !message.getText().startsWith("/start")) {
            next.accept(ctx);
            return;
        }

        log.debug("/start handling");
        SendMessage responseMessage = SendMessage.builder()
                .chatId(message.getChatId())
                .replyToMessageId(message.getMessageId())
                .text(localization.greeting)
                .build();
        try {
            ctx.client().execute(responseMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
        }
    }
}
