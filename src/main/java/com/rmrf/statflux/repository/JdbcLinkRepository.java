package com.rmrf.statflux.repository;

import com.rmrf.statflux.repository.constant.LinkSql;
import com.rmrf.statflux.repository.dto.LinkDto;
import com.rmrf.statflux.repository.query.QueryExecutor;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JdbcLinkRepository implements LinkRepository {

    private final QueryExecutor queryExecutor;

    @Override
    public boolean save(@NonNull LinkDto linkDto) {
        // TODO: improvement - upsert
        int updated = queryExecutor.update(
            LinkSql.UPDATE,
            linkDto.rawLink(),
            linkDto.title(),
            linkDto.views(),
            linkDto.updatedAt(),
            linkDto.hostingName(),
            linkDto.hostingId());

        return updated == 1 || queryExecutor.update(
            LinkSql.INSERT,
            linkDto.hostingName(),
            linkDto.rawLink(),
            linkDto.hostingId(),
            linkDto.title(),
            linkDto.views(),
            linkDto.updatedAt()
        ) > 0;
    }

    @Override
    public List<LinkDto> findAll() {
        return queryExecutor.query(
            LinkSql.FIND_ALL,
            rs -> new LinkDto(
                rs.getString("hosting_name"),
                rs.getString("raw_link"),
                rs.getString("hosting_id"),
                rs.getString("title"),
                rs.getLong("views"),
                rs.getTimestamp("updated_at")
                    .toInstant()
                    .atZone(ZoneOffset.UTC)
                    .truncatedTo(ChronoUnit.MICROS)
            ));
    }

    @Override
    public int getTotalLinkCount() {
        return queryExecutor.query(
                LinkSql.GET_TOTAL_LINK_COUNT,
                rs -> rs.getInt(1)
            )
            .getFirst();
    }

    @Override
    public long getTotalViewSum() {
        return queryExecutor.query(
                LinkSql.GET_TOTAL_VIEW_SUM,
                rs -> rs.getLong(1)
            )
            .getFirst();
    }
}
