package com.rmrf.statflux.integration.vk;

import static org.assertj.core.api.Assertions.assertThat;

import com.rmrf.statflux.common.ConfigLoader;
import com.rmrf.statflux.integration.config.IntegrationConfig;
import com.rmrf.statflux.integration.config.IntegrationLayerConfig;
import java.lang.reflect.Method;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class VkVideoProviderTest {

    /**
     * VkLinkStatsProvider объект.
     */
    private final VkVideoProvider provider =
        new IntegrationLayerConfig(new IntegrationConfig()).vkVideoProvider();

    /**
     * Тест на корректную работу метода makeUrl.
     */
    @Test
    void makeUrl_correct() throws Exception {
        Properties props = ConfigLoader.load("application.properties");

        System.out.println(props);

        String expectedUrl = String.format(
            "%s/method/video.get?videos=%s&extended=0&v=%s",
            props.getProperty("vk.api.url"),
            "-113367061_456239070",
            props.getProperty("vk.api.version")
        );

        Method method = VkVideoProviderImpl.class
            .getDeclaredMethod("makeUrl", String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(this.provider, "-113367061_456239070");

        assertThat(result).isEqualTo(expectedUrl);
    }
}
