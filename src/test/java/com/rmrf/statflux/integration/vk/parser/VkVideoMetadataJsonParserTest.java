package com.rmrf.statflux.integration.vk.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.rmrf.statflux.domain.dto.VideoMetadataResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VkVideoMetadataJsonParserTest {

    /**
     * VkVideoMetadataParser объект.
     */
    private VkVideoMetadataJsonParser parser;

    /**
     * Set Up для каждого теста.
     */
    @BeforeEach
    void setUp() {
        parser = new VkVideoMetadataJsonParser();
    }

    /**
     * Тест корректного парсинга одного видео.
     */
    @Test
    void parse_validSingleItem() {
        String json = """
        {
          "response": {
            "count": 1,
            "items": [
              {
                "owner_id": -1,
                "id": 1,
                "title": "video",
                "views": 10
              }
            ]
          }
        }
        """;

        List<VideoMetadataResponse> result = parser.parse(json);

        assertThat(result).hasSize(1);

        VideoMetadataResponse video = result.get(0);
        assertThat(video.id()).isEqualTo("-1_1");
        assertThat(video.title()).isEqualTo("video");
        assertThat(video.views()).isEqualTo(10L);
    }

    /**
     * Тест корректного парсинга нескольких видео.
     */
    @Test
    void parse_multipleItems() {
        String json = """
        {
          "response": {
            "count": 2,
            "items": [
              { "owner_id": -1, "id": 1, "title": "a", "views": 10 },
              { "owner_id": -2, "id": 2, "title": "b", "views": 20 }
            ]
          }
        }
        """;

        List<VideoMetadataResponse> result = parser.parse(json);

        assertThat(result).hasSize(2);
    }

    /**
     * Тест пропуска элемента при отсутствии обязательных полей.
     */
    @Test
    void parse_missingRequiredFields_skipsItem() {
        String json = """
        {
          "response": {
            "items": [
              { "owner_id": -1, "title": "no id" }
            ]
          }
        }
        """;

        List<VideoMetadataResponse> result = parser.parse(json);

        assertThat(result).isEmpty();
    }

    /**
     * Тест обработки null и пустого JSON.
     */
    @Test
    void parse_nullOrBlank_returnsEmptyList() {
        assertThat(parser.parse(null)).isEmpty();
        assertThat(parser.parse("")).isEmpty();
        assertThat(parser.parse("   ")).isEmpty();
    }

    /**
     * Тест отсутствия поля response.
     */
    @Test
    void parse_noResponseField_returnsEmptyList() {
        String json = """
        {
          "unexpected": {}
        }
        """;

        List<VideoMetadataResponse> result = parser.parse(json);

        assertThat(result).isEmpty();
    }

    /**
     * Тест пустого массива items.
     */
    @Test
    void parse_emptyItems_returnsEmptyList() {
        String json = """
        {
          "response": {
            "items": []
          }
        }
        """;

        List<VideoMetadataResponse> result = parser.parse(json);

        assertThat(result).isEmpty();
    }

    /**
     * Тест некорректного JSON.
     */
    @Test
    void parse_invalidJson_throwsException() {
        String json = "{ invalid json }";

        assertThatThrownBy(() -> parser.parse(json))
            .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Тест частично заполненного объекта (views отсутствует).
     */
    @Test
    void parse_partialFields_viewsNullable() {
        String json = """
        {
          "response": {
            "items": [
              { "owner_id": -1, "id": 1, "title": "video" }
            ]
          }
        }
        """;

        List<VideoMetadataResponse> result = parser.parse(json);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).views()).isNull();
    }
}
