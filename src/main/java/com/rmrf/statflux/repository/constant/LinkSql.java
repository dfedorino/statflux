package com.rmrf.statflux.repository.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class LinkSql {

    public static final String INSERT =
        """
            INSERT INTO links (
                chat_id,
                hosting_name,
                raw_link,
                hosting_id,
                title,
                views,
                updated_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    public static final String UPDATE =
        """
            UPDATE links
            SET raw_link = ?,
                title = ?,
                views = ?,
                updated_at = ?
            WHERE chat_id = ?
              AND hosting_name = ?
              AND hosting_id = ?
            """;

    public static final String FIND_ALL =
        "SELECT * FROM links";

    public static final String FIND_ALL_FOR_UPDATE =
        "SELECT * FROM links FOR UPDATE";

    public static final String GET_TOTAL_VIEW_SUM =
        "SELECT sum(views) FROM links";

    public static final String GET_TOTAL_LINK_COUNT =
        "SELECT count(id) FROM links";

    public static final String FIND_FIRST_PAGE =
        "SELECT * FROM links ORDER BY id ASC LIMIT ?";

    public static final String FIND_NEXT_PAGE =
        "SELECT * FROM links WHERE id > ? ORDER BY id ASC LIMIT ?";

    public static final String FIND_PREVIOUS_PAGE =
        "SELECT * FROM links WHERE id < ? ORDER BY id DESC LIMIT ?";

    public static final String FIND_BETWEEN_IDS =
        "SELECT * FROM links WHERE id BETWEEN ? AND ? ORDER BY id ASC";
}
