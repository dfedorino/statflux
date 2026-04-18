package com.rmrf.statflux.repository.query;

import com.rmrf.statflux.repository.exception.SqlScriptException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SqlScriptRunner {

    private final QueryExecutor executor;

    public void run(@NonNull String classpathFile) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(classpathFile)) {
            if (is == null) {
                log.error("SqlScriptRunner[run] file not found {}", classpathFile);
                throw new SqlScriptException("File not found: " + classpathFile);
            }

            String sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            for (String statement : sql.split(";")) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    executor.update(trimmed);
                }
            }

        } catch (IOException e) {
            log.error("SqlScriptRunner[run] failed to run script {}", classpathFile, e);
            throw new SqlScriptException("Failed to run SQL script: " + classpathFile, e);
        }
    }
}
