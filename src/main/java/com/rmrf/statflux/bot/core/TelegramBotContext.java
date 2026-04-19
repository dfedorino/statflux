package com.rmrf.statflux.bot.core;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public record TelegramBotContext(Update update, TelegramClient client) {
}
