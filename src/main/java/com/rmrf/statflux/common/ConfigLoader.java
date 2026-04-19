package com.rmrf.statflux.common;

import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    public static Properties load(String filename) {
        Properties props = new Properties();

        try (InputStream input = ConfigLoader.class
                .getClassLoader()
                .getResourceAsStream(filename)) {

            if (input == null) {
                throw new RuntimeException("Config not found: " + filename);
            }

            props.load(input);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return props;
    }
}
