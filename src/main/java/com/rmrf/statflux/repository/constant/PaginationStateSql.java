package com.rmrf.statflux.repository.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PaginationStateSql {

    public static final String FIND =
        """
        SELECT chat_id, message_id, first_seen_id, last_seen_id, updated_at
        FROM pagination_state
        WHERE chat_id = ? AND message_id = ?
        """;

    public static final String UPDATE =
        """
        UPDATE pagination_state
        SET
            first_seen_id = ?,
            last_seen_id = ?,
            updated_at = ?
        WHERE
            chat_id = ?
            AND message_id = ?
        """;

    public static final String INSERT =
        """
        INSERT INTO pagination_state
        (chat_id, message_id, first_seen_id, last_seen_id, updated_at)
        VALUES (?, ?, ?, ?, ?)
        """;

}
