package com.rmrf.statflux.bot.infra.middleware;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import com.rmrf.statflux.bot.core.TelegramBotContext;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;

class WhiteListMiddlewareTest {

    private static final String ALLOWED = "alice";

    private TelegramClient client;
    private WhiteListMiddleware middleware;

    @BeforeEach
    void setUp() {
        client = mock(TelegramClient.class);
        middleware = new WhiteListMiddleware(Set.of(ALLOWED));
    }

    private TelegramBotContext ctxWithMessage(String username) {
        Update upd = mock(Update.class);
        Message msg = mock(Message.class);
        User user = mock(User.class);
        lenient().when(upd.hasMessage()).thenReturn(true);
        lenient().when(upd.getMessage()).thenReturn(msg);
        lenient().when(msg.getFrom()).thenReturn(user);
        lenient().when(user.getUserName()).thenReturn(username);
        return new TelegramBotContext(upd, client);
    }

    private TelegramBotContext ctxWithoutMessage() {
        Update upd = mock(Update.class);
        lenient().when(upd.hasMessage()).thenReturn(false);
        return new TelegramBotContext(upd, client);
    }

    private static AtomicBoolean nextSpy() {
        return new AtomicBoolean(false);
    }

    private static Consumer<TelegramBotContext> nextOf(AtomicBoolean flag) {
        return ctx -> flag.set(true);
    }

    @Test
    void update_without_message_passes_through_without_checking_username() {
        var nextCalled = nextSpy();
        middleware.handle(ctxWithoutMessage(), nextOf(nextCalled));
        assertThat(nextCalled).isTrue();
    }

    @Test
    void allowed_user_passes_through_to_next() {
        var nextCalled = nextSpy();
        middleware.handle(ctxWithMessage(ALLOWED), nextOf(nextCalled));
        assertThat(nextCalled).isTrue();
    }

    @Test
    void unknown_user_is_blocked() {
        var nextCalled = nextSpy();
        middleware.handle(ctxWithMessage("eve"), nextOf(nextCalled));
        assertThat(nextCalled).isFalse();
    }

    @Test
    void user_without_telegram_username_is_blocked() {
        // Telegram позволяет не устанавливать username — getFrom().getUserName() возвращает null
        var nextCalled = nextSpy();
        middleware.handle(ctxWithMessage(null), nextOf(nextCalled));
        assertThat(nextCalled).isFalse();
    }

    @Test
    void empty_whitelist_blocks_all_users() {
        middleware = new WhiteListMiddleware(Set.of());
        var nextCalled = nextSpy();
        middleware.handle(ctxWithMessage(ALLOWED), nextOf(nextCalled));
        assertThat(nextCalled).isFalse();
    }

    @Test
    void whitelist_check_is_case_sensitive() {
        // "Alice" != "alice" — регистр важен
        var nextCalled = nextSpy();
        middleware.handle(ctxWithMessage("Alice"), nextOf(nextCalled));
        assertThat(nextCalled).isFalse();
    }
}
