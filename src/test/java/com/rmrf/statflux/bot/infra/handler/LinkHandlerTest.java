package com.rmrf.statflux.bot.infra.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.rmrf.statflux.bot.core.TelegramBotContext;
import com.rmrf.statflux.bot.infra.l10n.Localization;
import com.rmrf.statflux.domain.dto.AddVideoResponse;
import com.rmrf.statflux.domain.result.Failure;
import com.rmrf.statflux.domain.result.Result;
import com.rmrf.statflux.domain.result.Success;
import com.rmrf.statflux.service.ServiceLayer;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;

class LinkHandlerTest {

    private static final long CHAT_ID = 42L;

    private ServiceLayer service;
    private TelegramClient client;
    private LinkHandler handler;
    private Localization.Link l10n;

    @BeforeEach
    void setUp() {
        service = mock(ServiceLayer.class);
        client = mock(TelegramClient.class);
        l10n = new Localization.Link();
        l10n.videoAddedSuccessfully = "ok!";
        l10n.views = "views:";
        l10n.statsMotivationText = "/stats";
        l10n.error = "fail";
        handler = new LinkHandler(service, l10n);
    }

    private static TelegramBotContext ctx(String text, TelegramClient client) {
        Update upd = mock(Update.class);
        Message msg = mock(Message.class);
        lenient().when(upd.hasMessage()).thenReturn(true);
        lenient().when(upd.getMessage()).thenReturn(msg);
        lenient().when(msg.hasText()).thenReturn(text != null && !text.isEmpty());
        lenient().when(msg.getText()).thenReturn(text);
        lenient().when(msg.hasEntities()).thenReturn(false);
        lenient().when(msg.getEntities()).thenReturn(List.of());
        lenient().when(msg.getChatId()).thenReturn(CHAT_ID);
        lenient().when(msg.getMessageId()).thenReturn(1);
        return new TelegramBotContext(upd, client);
    }

    private static Result<AddVideoResponse> ok(String url) {
        return Success.of(new AddVideoResponse("YouTube", "title-" + url, url, 100L));
    }

    private static Result<AddVideoResponse> fail() {
        return Failure.of(new RuntimeException("nope"));
    }

    private AtomicBoolean nextSpy() {
        return new AtomicBoolean(false);
    }

    private static Consumer<TelegramBotContext> nextOf(AtomicBoolean called) {
        return c -> called.set(true);
    }

    @Test
    void thirdValidYoutubeIsSavedWhenFirstTwoAreFailures() throws Exception {
        var broken1 = "https://youtube.com/oops";
        var broken2 = "https://youtube.com/watch?v=BAD";
        var valid = "https://youtu.be/dQw4w9WgXcQ";
        when(service.addVideo(CHAT_ID, broken1)).thenReturn(fail());
        when(service.addVideo(CHAT_ID, broken2)).thenReturn(fail());
        when(service.addVideo(CHAT_ID, valid)).thenReturn(ok(valid));

        AtomicBoolean nextCalled = nextSpy();
        handler.handle(ctx(broken1 + " " + broken2 + " " + valid, client), nextOf(nextCalled));

        verify(service, times(1)).addVideo(CHAT_ID, broken1);
        verify(service, times(1)).addVideo(CHAT_ID, broken2);
        verify(service, times(1)).addVideo(CHAT_ID, valid);
        // успешно отправили "ok!" с ссылкой на валидное видео
        ArgumentCaptor<SendMessage> sent = ArgumentCaptor.forClass(SendMessage.class);
        verify(client).execute(sent.capture());
        assertThat(sent.getValue().getText()).contains(valid);
        assertThat(nextCalled).isFalse();
    }

    @Test
    void thirdValidRutubeIsSavedWhenFirstTwoAreFailures() throws Exception {
        var broken1 = "https://rutube.ru/video/short";
        var broken2 = "https://rutube.ru/video/" + "z".repeat(31);
        var valid = "https://rutube.ru/video/" + "a".repeat(32);
        when(service.addVideo(CHAT_ID, broken1)).thenReturn(fail());
        when(service.addVideo(CHAT_ID, broken2)).thenReturn(fail());
        when(service.addVideo(CHAT_ID, valid)).thenReturn(ok(valid));

        handler.handle(ctx(broken1 + "\n" + broken2 + "\n" + valid, client), c -> { });

        verify(service).addVideo(CHAT_ID, broken1);
        verify(service).addVideo(CHAT_ID, broken2);
        verify(service).addVideo(CHAT_ID, valid);
        ArgumentCaptor<SendMessage> sent = ArgumentCaptor.forClass(SendMessage.class);
        verify(client).execute(sent.capture());
        assertThat(sent.getValue().getText()).contains(valid);
    }

    @Test
    void thirdValidVkIsSavedWhenFirstTwoAreFailures() throws Exception {
        var broken1 = "https://vk.com/video";
        var broken2 = "https://vkvideo.ru/abc";
        var valid = "https://vk.com/video-113367061_456239070";
        when(service.addVideo(CHAT_ID, broken1)).thenReturn(fail());
        when(service.addVideo(CHAT_ID, broken2)).thenReturn(fail());
        when(service.addVideo(CHAT_ID, valid)).thenReturn(ok(valid));

        handler.handle(ctx(broken1 + " " + broken2 + " " + valid, client), c -> { });

        verify(service).addVideo(CHAT_ID, broken1);
        verify(service).addVideo(CHAT_ID, broken2);
        verify(service).addVideo(CHAT_ID, valid);
        ArgumentCaptor<SendMessage> sent = ArgumentCaptor.forClass(SendMessage.class);
        verify(client).execute(sent.capture());
        assertThat(sent.getValue().getText()).contains(valid);
    }

    @Test
    void mixedPlatformsBrokenFirstThenValidYoutubeIsSaved() throws Exception {
        var broken = "https://example.com/garbage";
        var youtube = "https://youtu.be/dQw4w9WgXcQ";
        var rutube = "https://rutube.ru/video/" + "a".repeat(32);
        when(service.addVideo(CHAT_ID, broken)).thenReturn(fail());
        when(service.addVideo(CHAT_ID, youtube)).thenReturn(ok(youtube));
        // rutube не должен дёрнуться — итерация останавливается на первом Success
        when(service.addVideo(CHAT_ID, rutube)).thenReturn(ok(rutube));

        handler.handle(ctx(broken + " " + youtube + " " + rutube, client), c -> { });

        verify(service).addVideo(CHAT_ID, broken);
        verify(service).addVideo(CHAT_ID, youtube);
        verify(service, never()).addVideo(CHAT_ID, rutube);
        ArgumentCaptor<SendMessage> sent = ArgumentCaptor.forClass(SendMessage.class);
        verify(client).execute(sent.capture());
        assertThat(sent.getValue().getText()).contains(youtube);
    }

    @Test
    void allThreeBrokenLeadsToErrorMessage() throws Exception {
        var u1 = "https://youtube.com/oops";
        var u2 = "https://rutube.ru/video/short";
        var u3 = "https://example.com/garbage";
        when(service.addVideo(any(), any())).thenReturn(fail());

        AtomicBoolean nextCalled = nextSpy();
        handler.handle(ctx(u1 + " " + u2 + " " + u3, client), nextOf(nextCalled));

        verify(service, times(3)).addVideo(any(), any());
        ArgumentCaptor<SendMessage> sent = ArgumentCaptor.forClass(SendMessage.class);
        verify(client).execute(sent.capture());
        assertThat(sent.getValue().getText()).isEqualTo(l10n.error);
        assertThat(nextCalled).isFalse();
    }

    @Test
    void concatenatedTwoBrokenAndOneValidAreSplitAndIterated() throws Exception {
        // тот самый Cmd+V-кейс без разделителей, но три ссылки и две битых
        var broken1 = "https://youtube.com/oops";
        var broken2 = "https://rutube.ru/video/short";
        var valid = "https://youtu.be/dQw4w9WgXcQ";
        when(service.addVideo(CHAT_ID, broken1)).thenReturn(fail());
        when(service.addVideo(CHAT_ID, broken2)).thenReturn(fail());
        when(service.addVideo(CHAT_ID, valid)).thenReturn(ok(valid));

        handler.handle(ctx(broken1 + broken2 + valid, client), c -> { });

        verify(service).addVideo(CHAT_ID, broken1);
        verify(service).addVideo(CHAT_ID, broken2);
        verify(service).addVideo(CHAT_ID, valid);
        ArgumentCaptor<SendMessage> sent = ArgumentCaptor.forClass(SendMessage.class);
        verify(client).execute(sent.capture());
        assertThat(sent.getValue().getText()).contains(valid);
    }

    @Test
    void noUrlsInMessagePassesToNext() throws Exception {
        AtomicBoolean nextCalled = nextSpy();
        handler.handle(ctx("просто привет, без ссылок", client), nextOf(nextCalled));

        verify(service, never()).addVideo(any(), any());
        verify(client, never()).execute(any(SendMessage.class));
        assertThat(nextCalled).isTrue();
    }
}
