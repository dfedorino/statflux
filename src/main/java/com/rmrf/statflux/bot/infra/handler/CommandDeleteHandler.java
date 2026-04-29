package com.rmrf.statflux.bot.infra.handler;

import com.rmrf.statflux.bot.core.Chain;
import com.rmrf.statflux.bot.core.TelegramBotContext;
import com.rmrf.statflux.bot.infra.l10n.Localization;
import com.rmrf.statflux.service.ServiceLayer;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
public class CommandDeleteHandler implements Chain.Node<TelegramBotContext> {
    private final ServiceLayer serviceLayer;
    private final Localization l10n;

    public CommandDeleteHandler(ServiceLayer serviceLayer, Localization l10n) {
        this.serviceLayer = serviceLayer;
        this.l10n = l10n;
    }

    @Override
    public void handle(TelegramBotContext ctx, Consumer<TelegramBotContext> next) {
        Message message = ctx.update().getMessage();
        if (!ctx.update().hasMessage() || !message.hasText() ||
            !message.getText().startsWith("/delete")) {
            next.accept(ctx);
            return;
        }
        log.debug("/delete handling");

        String[] parts = message.getText().split("\\s+");
        if (parts.length != 2 || !parts[1].matches("\\d+")) {
            sendReply(ctx, l10n.delete.usage);
            return;
        }

        long linkId = Long.parseLong(parts[1]);
        var result = serviceLayer.deleteVideo(message.getChatId(), linkId);

        if (result.isFailure()) {
            log.error("/delete failed for chatId={} linkId={} reason={}",
                message.getChatId(), linkId, result.asFailure().exception().getMessage());
            sendReply(ctx, l10n.delete.error);
            return;
        }

        sendReply(ctx, l10n.delete.success);
    }

    private void sendReply(TelegramBotContext ctx, String text) {
        Message message = ctx.update().getMessage();
        SendMessage responseMessage = SendMessage.builder()
            .chatId(message.getChatId())
            .replyToMessageId(message.getMessageId())
            .text(text)
            .build();
        try {
            ctx.client().execute(responseMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
        }
    }
}
