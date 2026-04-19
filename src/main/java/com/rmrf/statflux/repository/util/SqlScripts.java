package com.rmrf.statflux.repository.util;

import com.rmrf.statflux.repository.exception.SqlScriptException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class SqlScripts {

    public static void run(@NonNull String classpathFile) {
        try (InputStream is = SqlScripts.class.getClassLoader().getResourceAsStream(classpathFile)) {
            if (is == null) {
                log.error("SqlScriptRunner[run] file not found {}", classpathFile);
                throw new SqlScriptException("File not found: " + classpathFile);
            }

            String sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            for (String statement : sql.split(";")) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    Queries.update(trimmed);
                }
            }

        } catch (IOException e) {
            log.error("SqlScriptRunner[run] failed to run script {}", classpathFile, e);
            throw new SqlScriptException("Failed to run SQL script: " + classpathFile, e);
        }
    }
}
