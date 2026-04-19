package com.rmrf.statflux.bot.infra.handler;

import com.rmrf.statflux.bot.core.Chain;
import com.rmrf.statflux.bot.core.TelegramBotContext;
import com.rmrf.statflux.bot.infra.l10n.Localization;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.ReplyParameters;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
public class CommandStatsHandler implements Chain.Node<TelegramBotContext> {
    private final Localization.Stats localization;

    public CommandStatsHandler(Localization.Stats localization) {
        this.localization = localization;
    }

    @Override
    public void handle(TelegramBotContext ctx, Consumer<TelegramBotContext> next) {
        Message message = ctx.update().getMessage();
        if (!ctx.update().hasMessage() || !message.hasText() ||
            !message.getText().startsWith("/stats")) {
            next.accept(ctx);
            return;
        }

        log.debug("/stats handling");
        StringBuilder textBuilder = new StringBuilder()
                .append(localization.introduction)
                .append('\n')
                .append('\n')
                // Добавить отображение статистики видео
                .append("_Mock video statistics_")
                .append('\n')
                .append('\n')
                .append("\\-\\-\\-\\-\\-\\-\\-\\-\\-\\-\\-\\-\\-\\-\\-\\-\\-\\-\\-\\-\\-\\-\\-\\-")
                .append('\n')
                .append(localization.totalViews)
                .append(' ')
                .append(300000)
                .append('\n')
                .append(localization.totalLinkCount)
                .append(' ')
                .append(10);

        String text = textBuilder.toString();

        SendMessage responseMessage = SendMessage.builder()
                .chatId(ctx.update().getMessage().getChatId())
                .replyParameters(ReplyParameters.builder()
                        .messageId(ctx.update().getMessage().getMessageId())
                        .build()
                )
                .text(text)
                .parseMode("MarkdownV2")
                .replyMarkup(getMarkup(true, true))
                .build();

        try {
            ctx.client().execute(responseMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
        }
    }

    private InlineKeyboardMarkup getMarkup(boolean hasPrevious, boolean hasNext) {
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        if (hasPrevious) {
            buttons.add(
                    InlineKeyboardButton.builder()
                            .text("<")
                            .callbackData("prev")
                            .build()
            );
        }
        buttons.add(
                InlineKeyboardButton.builder()
                        .text(localization.refresh)
                        .callbackData("refresh")
                        .build()
        );
        if (hasNext) {
            buttons.add(
                    InlineKeyboardButton.builder()
                            .text(">")
                            .callbackData("next")
                            .build()
            );
        }

        return InlineKeyboardMarkup.builder()
                .keyboardRow(
                        new InlineKeyboardRow(
                                buttons
                        )
                )
                .build();
    }
}
