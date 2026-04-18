package com.rmrf.statflux.bot.core;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public record TelegramBotRootConsumer(Chain<TelegramBotContext> chain, TelegramClient telegramClient) implements LongPollingSingleThreadUpdateConsumer {
    @Override
    public void consume(Update update) {
        TelegramBotContext ctx = new TelegramBotContext(update, telegramClient);
        chain.accept(ctx);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<Chain.Node<TelegramBotContext>> nodes;
        private TelegramClient telegramClient;

        public Builder() {
            nodes = new ArrayList<>();
        }

        public Builder withClient(TelegramClient telegramClient) {
            this.telegramClient = telegramClient;
            return this;
        }

        public Builder use(Chain.Node<TelegramBotContext> node) {
            nodes.add(node);
            return this;
        }

        public TelegramBotRootConsumer build() {
            return new TelegramBotRootConsumer(Chain.of(nodes), telegramClient);
        }
    }
}
