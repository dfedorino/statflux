package com.rmrf.statflux.bot.infra.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.message.Message;

class MessageUrlsExtractorTest {

    private static Message msg(String text, MessageEntity... entities) {
        Message m = mock(Message.class);
        lenient().when(m.hasText()).thenReturn(text != null && !text.isEmpty());
        lenient().when(m.getText()).thenReturn(text);
        if (entities.length == 0) {
            lenient().when(m.hasEntities()).thenReturn(false);
            lenient().when(m.getEntities()).thenReturn(List.of());
        } else {
            lenient().when(m.hasEntities()).thenReturn(true);
            lenient().when(m.getEntities()).thenReturn(List.of(entities));
        }
        return m;
    }

    private static MessageEntity urlEntity(int offset, int length) {
        return new MessageEntity("url", offset, length);
    }

    private static MessageEntity textLinkEntity(int offset, int length, String url) {
        MessageEntity e = new MessageEntity("text_link", offset, length);
        e.setUrl(url);
        return e;
    }

    @Test
    void nullMessageReturnsEmpty() {
        assertThat(MessageUrlsExtractor.extract(null)).isEmpty();
    }

    @Test
    void emptyTextReturnsEmpty() {
        assertThat(MessageUrlsExtractor.extract(msg(""))).isEmpty();
    }

    @Test
    void plainTextWithoutUrlsReturnsEmpty() {
        assertThat(MessageUrlsExtractor.extract(msg("Просто текст без ссылок"))).isEmpty();
    }

    @Test
    void singleUrlIsReturned() {
        var url = "https://youtu.be/dQw4w9WgXcQ";
        assertThat(MessageUrlsExtractor.extract(msg(url))).containsExactly(url);
    }

    @Test
    void urlInsideTextIsReturned() {
        var text = "посмотри https://youtu.be/dQw4w9WgXcQ — топ";
        assertThat(MessageUrlsExtractor.extract(msg(text)))
            .containsExactly("https://youtu.be/dQw4w9WgXcQ");
    }

    @Test
    void twoUrlsSeparatedBySpace() {
        var a = "https://youtu.be/AAAAAAAAAAA";
        var b = "https://rutube.ru/video/" + "a".repeat(32) + "/";
        assertThat(MessageUrlsExtractor.extract(msg(a + " " + b))).containsExactly(a, b);
    }

    @Test
    void twoUrlsSeparatedByNewline() {
        var a = "https://youtu.be/AAAAAAAAAAA";
        var b = "https://vk.com/video-1_2";
        assertThat(MessageUrlsExtractor.extract(msg(a + "\n" + b))).containsExactly(a, b);
    }

    @Test
    void concatenatedUrlsWithoutSeparatorAreSplit() {
        var a = "https://youtube.com/watch?v=AAAAAAAAAAA";
        var b = "https://youtube.com/watch?v=BBBBBBBBBBB";
        assertThat(MessageUrlsExtractor.extract(msg(a + b))).containsExactly(a, b);
    }

    @Test
    void concatenatedHttpAndHttpsAreSplit() {
        var a = "http://youtube.com/watch?v=AAAAAAAAAAA";
        var b = "https://youtu.be/BBBBBBBBBBB";
        assertThat(MessageUrlsExtractor.extract(msg(a + b))).containsExactly(a, b);
    }

    @Test
    void trailingPunctuationIsTrimmed() {
        var url = "https://youtu.be/dQw4w9WgXcQ";
        assertThat(MessageUrlsExtractor.extract(msg("Глянь " + url + ".")))
            .containsExactly(url);
        assertThat(MessageUrlsExtractor.extract(msg("(" + url + ")")))
            .containsExactly(url);
        assertThat(MessageUrlsExtractor.extract(msg(url + "!!!")))
            .containsExactly(url);
    }

    @Test
    void textLinkEntityProvidesUrl() {
        var text = "клик";
        var hidden = "https://youtu.be/dQw4w9WgXcQ";
        var m = msg(text, textLinkEntity(0, text.length(), hidden));
        assertThat(MessageUrlsExtractor.extract(m)).containsExactly(hidden);
    }

    @Test
    void urlEntitySliceMatchesPlainRegex() {
        var url = "https://youtu.be/AAAAAAAAAAA";
        var text = "вот: " + url + " смотри";
        // entity покрывает только URL
        var m = msg(text, urlEntity(5, url.length()));
        assertThat(MessageUrlsExtractor.extract(m)).containsExactly(url);
    }

    @Test
    void duplicatesFromEntitiesAndRegexAreCollapsed() {
        var url = "https://youtu.be/AAAAAAAAAAA";
        var m = msg(url, urlEntity(0, url.length()));
        assertThat(MessageUrlsExtractor.extract(m)).containsExactly(url);
    }

    @Test
    void unknownEntityTypesAreIgnored() {
        var url = "https://youtu.be/AAAAAAAAAAA";
        var text = "/start " + url;
        MessageEntity command = new MessageEntity("bot_command", 0, 6);
        MessageEntity urlEntity = urlEntity(7, url.length());
        assertThat(MessageUrlsExtractor.extract(msg(text, command, urlEntity)))
            .containsExactly(url);
    }

    @Test
    void orderIsPreservedAcrossSources() {
        // entity покрывает второй URL, а regex наткнётся на оба по порядку текста
        var first = "https://youtu.be/AAAAAAAAAAA";
        var second = "https://rutube.ru/video/" + "b".repeat(32);
        var text = first + " " + second;
        var m = msg(text, urlEntity(first.length() + 1, second.length()));
        assertThat(MessageUrlsExtractor.extract(m))
            .containsExactly(first, second);
    }

    @Test
    void noEntitiesFallbackToRegexOnly() {
        // имитируем апдейт без entities: hasEntities=false
        var url = "https://vkvideo.ru/video-113367061_456239070";
        Message m = mock(Message.class);
        lenient().when(m.hasText()).thenReturn(true);
        lenient().when(m.getText()).thenReturn("вот ссылка: " + url + " смотри");
        lenient().when(m.hasEntities()).thenReturn(false);
        assertThat(MessageUrlsExtractor.extract(m)).containsExactly(url);
    }

    @Test
    void invalidEntityOffsetIsIgnored() {
        var text = "короткий текст";
        // entity указывает за пределы текста — должен быть проигнорирован, а не упасть
        var m = msg(text, urlEntity(100, 50));
        assertThat(MessageUrlsExtractor.extract(m)).isEmpty();
    }

    @Test
    void threeYoutubeUrlsTwoBrokenAreAllExtracted() {
        // extractor сам не валидирует платформу — он отдаёт кандидатов в порядке текста.
        // Битые "по платформе" URL отвалятся уже в LinkHandler через ServiceLayer.addVideo.
        var broken1 = "https://youtube.com/oops";
        var broken2 = "https://youtube.com/watch?v=BAD";
        var valid = "https://youtu.be/dQw4w9WgXcQ";
        assertThat(MessageUrlsExtractor.extract(msg(broken1 + " " + broken2 + " " + valid)))
            .containsExactly(broken1, broken2, valid);
    }

    @Test
    void threeRutubeUrlsTwoBrokenAreAllExtracted() {
        var broken1 = "https://rutube.ru/video/short";
        var broken2 = "https://rutube.ru/video/" + "z".repeat(31);
        var valid = "https://rutube.ru/video/" + "a".repeat(32);
        assertThat(MessageUrlsExtractor.extract(msg(broken1 + "\n" + broken2 + "\n" + valid)))
            .containsExactly(broken1, broken2, valid);
    }

    @Test
    void threeVkUrlsTwoBrokenAreAllExtracted() {
        var broken1 = "https://vk.com/video";
        var broken2 = "https://vkvideo.ru/abc";
        var valid = "https://vk.com/video-113367061_456239070";
        assertThat(MessageUrlsExtractor.extract(msg(broken1 + " " + broken2 + " " + valid)))
            .containsExactly(broken1, broken2, valid);
    }

    @Test
    void brokenAndBrokenAndValidConcatenatedAreSplit() {
        // три ссылки склеены без разделителя — самый каверзный кейс
        var broken1 = "https://youtube.com/oops";
        var broken2 = "https://rutube.ru/video/short";
        var valid = "https://vk.com/video-113367061_456239070";
        assertThat(MessageUrlsExtractor.extract(msg(broken1 + broken2 + valid)))
            .containsExactly(broken1, broken2, valid);
    }

    @Test
    void mixedDomainsBrokenFirstThenValidAreExtractedInTextOrder() {
        // example.com — заведомо незнакомая платформа (factory вернёт Failure),
        // но extractor должен честно отдать её первой по порядку
        var broken = "https://example.com/some/path";
        var youtube = "https://youtu.be/dQw4w9WgXcQ";
        var rutube = "https://rutube.ru/video/" + "a".repeat(32);
        assertThat(MessageUrlsExtractor.extract(msg(broken + "\n" + youtube + " — а ещё " + rutube)))
            .containsExactly(broken, youtube, rutube);
    }
}
