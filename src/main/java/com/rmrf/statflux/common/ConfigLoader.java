package com.rmrf.statflux.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class ConfigLoader {
    private static final ObjectMapper YAML_MAPPER =
            new ObjectMapper(new YAMLFactory());

    public static Properties loadProperties(String filename) {
        Properties props = new Properties();

        try (InputStream input = ConfigLoader.class
                .getClassLoader()
                .getResourceAsStream(filename)) {

            if (input == null) {
                throw new RuntimeException("Config not found: " + filename);
            }

            InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8);
            props.load(reader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return props;
    }

    public static <T> T loadL10n(String filename, Class<T> clazz) {
        try (InputStream input = ConfigLoader.class
                .getClassLoader()
                .getResourceAsStream(filename)) {

            if (input == null) {
                throw new RuntimeException("L10n not found: " + filename);
            }

            return YAML_MAPPER.readValue(input, clazz);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load L10n: " + filename, e);
        }
    }
}
