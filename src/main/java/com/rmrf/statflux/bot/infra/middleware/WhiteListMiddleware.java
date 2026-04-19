package com.rmrf.statflux.bot.infra.middleware;

import com.rmrf.statflux.bot.core.Chain;
import com.rmrf.statflux.bot.core.TelegramBotContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.function.Consumer;

@Slf4j
public class WhiteListMiddleware implements Chain.Node<TelegramBotContext> {
     private final Set<String> allowedUsernames;

    public WhiteListMiddleware(Set<String> allowedUsernames) {
        this.allowedUsernames = allowedUsernames;
    }

    @Override
    public void handle(TelegramBotContext ctx, Consumer<TelegramBotContext> next) {
        if (!ctx.update().hasMessage()) {
            next.accept(ctx);
            return;
        }

        log.debug("Filtering");
        String username = ctx.update().getMessage().getFrom().getUserName();
        if (allowedUsernames.contains(username)) {
            log.info(String.format("User '%s' was verified successfully", username));
            next.accept(ctx);
        } else {
            log.debug(String.format("User '%s' is not in white list", username));
        }
    }
}
