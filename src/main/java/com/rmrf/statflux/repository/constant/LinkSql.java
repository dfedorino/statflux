package com.rmrf.statflux.repository.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class LinkSql {

    public static final String INSERT =
        """
            INSERT INTO links (
                hosting_name,
                raw_link,
                hosting_id,
                title,
                views,
                updated_at
            )
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    public static final String UPDATE =
        """
            UPDATE links
            SET raw_link = ?,
                title = ?,
                views = ?,
                updated_at = ?
            WHERE hosting_name = ?
              AND hosting_id = ?
            """;

    public static final String FIND_ALL =
        "SELECT * FROM links";

    public static final String GET_TOTAL_VIEW_SUM =
        "SELECT sum(views) FROM links";

    public static final String GET_TOTAL_LINK_COUNT =
        "SELECT count(id) FROM links";
}
