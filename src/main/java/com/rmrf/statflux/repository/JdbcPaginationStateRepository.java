package com.rmrf.statflux.repository;

import com.rmrf.statflux.repository.constant.PaginationStateSql;
import com.rmrf.statflux.repository.dto.PaginationStateDto;
import com.rmrf.statflux.repository.util.Queries;
import com.rmrf.statflux.repository.query.ResultSetMapper;

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JdbcPaginationStateRepository implements PaginationStateRepository {

    private static final ResultSetMapper<PaginationStateDto> PAGINATION_STATE_DTO_RESULT_SET_MAPPER =
        rs -> new PaginationStateDto(
            rs.getLong("chat_id"),
            rs.getLong("message_id"),
            rs.getObject("first_seen_id") != null ? rs.getLong("first_seen_id") : null,
            rs.getObject("last_seen_id") != null ? rs.getLong("last_seen_id") : null,
            rs.getTimestamp("updated_at")
                .toInstant()
                .atZone(ZoneOffset.UTC)
        );

    @Override
    public Optional<PaginationStateDto> find(@NonNull Long chatId, @NonNull Long messageId) {
        return Queries.query(
            PaginationStateSql.FIND,
            PAGINATION_STATE_DTO_RESULT_SET_MAPPER,
            chatId,
            messageId
        ).stream().findFirst();
    }

    @Override
    public boolean save(PaginationStateDto state) {

        int updated = Queries.update(
            PaginationStateSql.UPDATE,
            state.firstSeenId(),
            state.lastSeenId(),
            Timestamp.from(state.updatedAt().toInstant()),
            state.chatId(),
            state.messageId()

        );

        return updated == 1 || Queries.update(
            PaginationStateSql.INSERT,
            state.chatId(),
            state.messageId(),
            state.firstSeenId(),
            state.lastSeenId(),
            Timestamp.from(state.updatedAt().toInstant())
        ) > 0;
    }
}
