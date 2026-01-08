package com.anexya.app.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.anexya.app.crypto.ReferenceCodeCrypto;
import com.anexya.app.domain.TagRead;

import lombok.RequiredArgsConstructor;

@Repository
@Profile("mysql")
@RequiredArgsConstructor
public class JdbcTagReadRepository implements TagReadRepository
{

    private final JdbcTemplate jdbcTemplate;
    private final ReferenceCodeCrypto referenceCodeCrypto;

    private final RowMapper<TagRead> mapper = new RowMapper<TagRead>()
    {
        @Override
        public TagRead mapRow(ResultSet rs, int rowNum) throws SQLException
        {
            return TagRead.builder().id(UUID.fromString(rs.getString("id"))).siteName(rs.getString("site_name"))
                    .epc(rs.getString("epc")).referenceCode(referenceCodeCrypto.decrypt(rs.getString("reference_code")))
                    .location(rs.getString("location")).rssi(rs.getDouble("rssi"))
                    .readAt(rs.getTimestamp("read_at").toInstant()).build();
        }
    };

    @Override
    public Optional<TagRead> findById(UUID id)
    {
        List<TagRead> results = jdbcTemplate.query(
                "select id, site_name, epc, reference_code, location, rssi, read_at from tag_reads where id = ?",
                mapper, id.toString());
        return results.stream().findFirst();
    }

    @Override
    public TagRead save(TagRead tx)
    {
        int updated = jdbcTemplate.update(
                "insert into tag_reads (id, site_name, epc, reference_code, location, rssi, read_at) values (?, ?, ?, ?, ?, ?, ?) "
                        + "on duplicate key update site_name = values(site_name), epc = values(epc), reference_code = values(reference_code), location = values(location), rssi = values(rssi), read_at = values(read_at)",
                ps -> {
                    ps.setString(1, tx.getId().toString());
                    ps.setString(2, tx.getSiteName());
                    ps.setString(3, tx.getEpc());
                    ps.setString(4, referenceCodeCrypto.encrypt(tx.getReferenceCode()));
                    ps.setString(5, tx.getLocation());
                    ps.setDouble(6, tx.getRssi());
                    ps.setTimestamp(7, java.sql.Timestamp.from(tx.getReadAt()));
                });
        if (updated == 0)
        {
            throw new IllegalStateException("Failed to save tag read " + tx.getId());
        }
        return tx;
    }

    @Override
    public void deleteById(UUID id)
    {
        jdbcTemplate.update("delete from tag_reads where id = ?", id.toString());
    }

    @Override
    public List<TagRead> findByFilters(Optional<String> epc, Optional<String> location, Optional<String> siteName)
    {
        StringBuilder sql = new StringBuilder(
                "select id, site_name, epc, reference_code, location, rssi, read_at from tag_reads where 1=1");
        List<Object> params = new ArrayList<>();

        epc.ifPresent(val -> {
            sql.append(" and epc = ?");
            params.add(val);
        });
        location.ifPresent(val -> {
            sql.append(" and location = ?");
            params.add(val);
        });
        siteName.ifPresent(val -> {
            sql.append(" and site_name = ?");
            params.add(val);
        });

        sql.append(" order by read_at desc");

        return jdbcTemplate.query(sql.toString(), mapper, params.toArray());
    }
}
