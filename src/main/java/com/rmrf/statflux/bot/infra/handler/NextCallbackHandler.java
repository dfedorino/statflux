package com.rmrf.statflux.bot.infra.handler;

import com.rmrf.statflux.bot.core.TelegramBotContext;
import com.rmrf.statflux.bot.infra.l10n.Localization;
import com.rmrf.statflux.domain.dto.VideoStatsResponse;
import com.rmrf.statflux.service.ServiceLayer;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage;

import java.util.function.Consumer;

@Slf4j
public class NextCallbackHandler extends AbstractStatsCallbackHandler {
    private final ServiceLayer serviceLayer;
    private final Localization l10n;

    public NextCallbackHandler(ServiceLayer serviceLayer, Localization l10n) {
        super(l10n.stats, log);
        this.serviceLayer = serviceLayer;
        this.l10n = l10n;
    }

    @Override
    public void handle(TelegramBotContext ctx, Consumer<TelegramBotContext> next) {
        CallbackQuery callbackQuery = ctx.update().getCallbackQuery();
        if (!ctx.update().hasCallbackQuery() || !"next".equals(callbackQuery.getData())) {
            next.accept(ctx);
            return;
        }
        log.debug("callback 'next' handling");

        MaybeInaccessibleMessage message = callbackQuery.getMessage();
        var statsResult = serviceLayer.getNextVideos(message.getChatId(), (long) message.getMessageId());
        if (statsResult.isFailure()) {
            handleFailure(ctx, l10n.callbackQueries.nextError);
            return;
        }
        VideoStatsResponse videoStatsResponse = statsResult.get();

        handleSuccess(ctx, videoStatsResponse);
    }
}
