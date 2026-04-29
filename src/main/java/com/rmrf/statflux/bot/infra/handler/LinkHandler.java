package com.rmrf.statflux.bot.infra.handler;

import com.rmrf.statflux.bot.core.Chain;
import com.rmrf.statflux.bot.core.TelegramBotContext;
import com.rmrf.statflux.bot.infra.l10n.Localization;
import com.rmrf.statflux.bot.infra.util.MessageUrlsExtractor;
import com.rmrf.statflux.bot.infra.util.TelegramBotFormatter;
import com.rmrf.statflux.domain.dto.AddVideoResponse;
import com.rmrf.statflux.service.ServiceLayer;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.ReplyParameters;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
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
        if (!ctx.update().hasMessage()) {
            next.accept(ctx);
            return;
        }
        Message message = ctx.update().getMessage();
        List<String> candidates = MessageUrlsExtractor.extract(message);
        if (candidates.isEmpty()) {
            next.accept(ctx);
            return;
        }

        if (candidates.size() > 1) {
            log.info("LinkHandler[handle] found {} url candidates, picking first valid",
                candidates.size());
        }

        Long chatId = message.getChatId();
        AddVideoResponse success = null;
        for (String url : candidates) {
            var result = serviceLayer.addVideo(chatId, url);
            if (result.isSuccess()) {
                success = result.get();
                break;
            }
        }

        if (success == null) {
            handleIncorrect(ctx);
            return;
        }
        handleSuccess(ctx, success);
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
                .append('\n')
                .append('_')
                .append(localization.views)
                .append(' ')
                .append(TelegramBotFormatter.groupThousands(response.views()))
                .append('_')
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
