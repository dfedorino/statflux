package com.rmrf.statflux.repository;

import com.rmrf.statflux.repository.constant.LinkSql;
import com.rmrf.statflux.repository.dto.LinkDto;
import com.rmrf.statflux.repository.query.ResultSetMapper;
import com.rmrf.statflux.repository.util.Queries;

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import lombok.NonNull;

public class JdbcLinkRepository implements LinkRepository {

    private static final ResultSetMapper<LinkDto> LINK_DTO_RESULT_SET_MAPPER = rs -> new LinkDto(
        rs.getLong("id"),
        rs.getLong("chat_id"),
        rs.getString("hosting_name"),
        rs.getString("raw_link"),
        rs.getString("hosting_id"),
        rs.getString("title"),
        rs.getLong("views"),
        rs.getTimestamp("updated_at")
            .toInstant()
            .atZone(ZoneOffset.UTC)
    );

    @Override
    public boolean save(@NonNull LinkDto linkDto) {
        // TODO: improvement - upsert
        int updated = Queries.update(
            LinkSql.UPDATE,
            linkDto.rawLink(),
            linkDto.title(),
            linkDto.views(),
            Timestamp.from(linkDto.updatedAt().toInstant()),
            linkDto.chatId(),
            linkDto.hostingName(),
            linkDto.hostingId());

        return updated == 1 || Queries.update(
            LinkSql.INSERT,
            linkDto.chatId(),
            linkDto.hostingName(),
            linkDto.rawLink(),
            linkDto.hostingId(),
            linkDto.title(),
            linkDto.views(),
            Timestamp.from(linkDto.updatedAt().toInstant())
        ) > 0;
    }

    @Override
    public List<LinkDto> findAll() {
        return Queries.query(
            LinkSql.FIND_ALL,
            LINK_DTO_RESULT_SET_MAPPER);
    }

    @Override
    public List<LinkDto> findBetweenIds(long minId, long maxId) {
        return Queries.query(
            LinkSql.FIND_BETWEEN_IDS,
            LINK_DTO_RESULT_SET_MAPPER,
            minId,
            maxId);
    }

    @Override
    public List<LinkDto> findAllForUpdate() {
        return Queries.query(
            LinkSql.FIND_ALL_FOR_UPDATE,
            LINK_DTO_RESULT_SET_MAPPER);
    }

    @Override
    public int getTotalLinkCount() {
        return Queries.query(
                LinkSql.GET_TOTAL_LINK_COUNT,
                rs -> rs.getInt(1)
            )
            .getFirst();
    }

    @Override
    public int getTotalLinkCount(long chatId) {
        return Queries.query(
                LinkSql.GET_TOTAL_LINK_COUNT_BY_CHAT_ID,
                rs -> rs.getInt(1),
                chatId
            )
            .getFirst();
    }

    @Override
    public long getTotalViewSum() {
        return Queries.query(
                LinkSql.GET_TOTAL_VIEW_SUM,
                rs -> rs.getLong(1)
            )
            .getFirst();
    }

    @Override
    public long getTotalViewSum(long chatId) {
        return Queries.query(
                LinkSql.GET_TOTAL_VIEW_SUM_BY_CHAT_ID,
                rs -> rs.getLong(1),
                chatId
            )
            .getFirst();
    }

    @Override
    public List<LinkDto> findFirstPage(int limit) {
        return Queries.query(
            LinkSql.FIND_FIRST_PAGE,
            LINK_DTO_RESULT_SET_MAPPER,
            limit
        );
    }

    @Override
    public List<LinkDto> findFirstPage(long chatId, int limit) {
        return Queries.query(
            LinkSql.FIND_FIRST_PAGE_BY_CHAT_ID,
            LINK_DTO_RESULT_SET_MAPPER,
            chatId,
            limit
        );
    }

    @Override
    public List<LinkDto> findNextPage(long lastSeenId, int limit) {
        return Queries.query(
            LinkSql.FIND_NEXT_PAGE,
            LINK_DTO_RESULT_SET_MAPPER,
            lastSeenId,
            limit
        );
    }

    @Override
    public List<LinkDto> findPreviousPage(long firstSeenId, int limit) {
        List<LinkDto> result = Queries.query(
            LinkSql.FIND_PREVIOUS_PAGE,
            LINK_DTO_RESULT_SET_MAPPER,
            firstSeenId,
            limit
        );

        Collections.reverse(result);
        return result;
    }

    @Override
    public boolean delete(long chatId, long linkId) {
        return Queries.update(
            LinkSql.DELETE,
            linkId,
            chatId
        ) > 0;
    }
}
