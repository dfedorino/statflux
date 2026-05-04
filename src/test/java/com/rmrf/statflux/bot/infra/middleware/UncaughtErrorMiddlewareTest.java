package com.rmrf.statflux.bot.infra.middleware;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.rmrf.statflux.bot.core.TelegramBotContext;
import com.rmrf.statflux.bot.infra.l10n.Localization;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

class UncaughtErrorMiddlewareTest {

    private static final long CHAT_ID = 42L;
    private static final int MESSAGE_ID = 7;
    private static final String ERROR_TEXT = "Что-то пошло не так";

    private TelegramClient client;
    private UncaughtErrorMiddleware middleware;

    @BeforeEach
    void setUp() {
        client = mock(TelegramClient.class);
        Localization.Common l10n = new Localization.Common();
        l10n.uncaughtError = ERROR_TEXT;
        middleware = new UncaughtErrorMiddleware(l10n);
    }

    private TelegramBotContext ctxWithMessage() {
        Update upd = mock(Update.class);
        Message msg = mock(Message.class);
        lenient().when(upd.hasMessage()).thenReturn(true);
        lenient().when(upd.getMessage()).thenReturn(msg);
        lenient().when(msg.getChatId()).thenReturn(CHAT_ID);
        lenient().when(msg.getMessageId()).thenReturn(MESSAGE_ID);
        return new TelegramBotContext(upd, client);
    }

    private TelegramBotContext ctxWithoutMessage() {
        Update upd = mock(Update.class);
        lenient().when(upd.hasMessage()).thenReturn(false);
        return new TelegramBotContext(upd, client);
    }

    private static Consumer<TelegramBotContext> throwing(RuntimeException ex) {
        return ctx -> { throw ex; };
    }

    @Test
    void happy_path_does_not_send_error_message() throws TelegramApiException {
        var nextCalled = new AtomicBoolean(false);
        middleware.handle(ctxWithMessage(), ctx -> nextCalled.set(true));

        assertThat(nextCalled).isTrue();
        verify(client, never()).execute(any(SendMessage.class));
    }

    @Test
    void exception_with_message_context_sends_error_reply() throws TelegramApiException {
        middleware.handle(ctxWithMessage(), throwing(new RuntimeException("boom")));

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(client).execute(captor.capture());
        SendMessage sent = captor.getValue();
        assertThat(sent.getChatId()).isEqualTo(String.valueOf(CHAT_ID));
        assertThat(sent.getText()).isEqualTo(ERROR_TEXT);
        assertThat(sent.getReplyParameters().getMessageId()).isEqualTo(MESSAGE_ID);
    }

    @Test
    void exception_without_message_context_does_not_send_reply() throws TelegramApiException {
        middleware.handle(ctxWithoutMessage(), throwing(new RuntimeException("boom")));

        verify(client, never()).execute(any(SendMessage.class));
    }

    @Test
    void telegram_api_failure_during_error_reply_is_swallowed() throws TelegramApiException {
        doThrow(new TelegramApiException("API down")).when(client).execute(any(SendMessage.class));

        // вторичная ошибка не должна прорасти наружу
        assertThatNoException().isThrownBy(() ->
            middleware.handle(ctxWithMessage(), throwing(new RuntimeException("original")))
        );
    }

    @Test
    void java_error_is_also_caught_because_middleware_catches_throwable() throws TelegramApiException {
        // Error — не Exception, но Middleware ловит Throwable
        middleware.handle(ctxWithMessage(), ctx -> { throw new OutOfMemoryError("test"); });

        verify(client).execute(any(SendMessage.class));
    }
}
