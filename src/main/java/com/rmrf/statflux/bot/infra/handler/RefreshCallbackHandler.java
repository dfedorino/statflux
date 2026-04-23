package com.rmrf.statflux.bot.infra.handler;

import com.rmrf.statflux.bot.core.TelegramBotContext;
import com.rmrf.statflux.bot.infra.l10n.Localization;
import com.rmrf.statflux.domain.dto.RefreshVideosPagedResponse;
import com.rmrf.statflux.domain.dto.VideoStatsResponse;
import com.rmrf.statflux.domain.result.Result;
import com.rmrf.statflux.service.ServiceLayer;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage;

import java.util.function.Consumer;

@Slf4j
public class RefreshCallbackHandler extends AbstractStatsCallbackHandler {
    private final ServiceLayer serviceLayer;
    private final Localization l10n;

    public RefreshCallbackHandler(ServiceLayer serviceLayer, Localization l10n) {
        super(l10n.stats, log);
        this.serviceLayer = serviceLayer;
        this.l10n = l10n;
    }

    @Override
    public void handle(TelegramBotContext ctx, Consumer<TelegramBotContext> next) {
        CallbackQuery callbackQuery = ctx.update().getCallbackQuery();
        if (!ctx.update().hasCallbackQuery() || !"refresh".equals(callbackQuery.getData())) {
            next.accept(ctx);
            return;
        }
        log.debug("callback 'refresh' handling");

        MaybeInaccessibleMessage message = callbackQuery.getMessage();

        serviceLayer.refreshVideos(
                message.getChatId(),
                (long) message.getMessageId(),
                (statsResult) -> handleRefreshStatsResult(ctx, statsResult)
        );
    }

    private void handleRefreshStatsResult(TelegramBotContext ctx,
                                          Result<RefreshVideosPagedResponse> statsResult) {
        log.debug("after refresh");
        if (statsResult.isFailure()) {
            log.debug("refresh failure");
            handleFailure(ctx, l10n.callbackQueries.refreshError);
            return;
        }
        RefreshVideosPagedResponse refreshResponse = statsResult.get();
        if (refreshResponse.hasErrors()) {
            log.debug("refresh with errors");
            handleFailure(ctx, l10n.callbackQueries.refreshError);
            return;
        }

        log.debug("successful refresh");
        handleSuccessRefreshStatsResult(ctx, refreshResponse);
    }

    private void handleSuccessRefreshStatsResult(TelegramBotContext ctx, VideoStatsResponse videoStatsResponse) {
        editMessageWith(ctx, videoStatsResponse);
        answerCallbackQuery(ctx);
    }
}
