package org.example.shadowdba;

import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.springframework.stereotype.Component;

@Component
public class AiSqlInspector implements StatementInspector {

    @Override
    public String inspect(String sql) {
        // Save the raw SQL into our thread-safe context
        SqlContext.setSql(sql);

        // Return the SQL untouched so the user's app continues normally
        return sql;
    }
}