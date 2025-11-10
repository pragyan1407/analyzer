package the12thman.data;
/*

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.Ordered;
import java.sql.SQLException;
import java.util.Map;

public class DatabaseMetadataAdvisor implements BaseAdvisor {
    private static final String DEFAULT_SYSTEM_TEXT = """
You are a Postgres expert. Please help to generate a Postgres query, then run the query to answer the question. The output should be in tabular format.

===Tables

{table_schemas}
""";

    private final String tableSchemas;

    public DatabaseMetadataAdvisor(DatabaseMetadataHelper helper) {
        try {
            this.tableSchemas = helper.extractMetadataJson();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ChatClientRequest before(ChatClientRequest req, AdvisorChain chain) {
        var systemText = new PromptTemplate(DEFAULT_SYSTEM_TEXT).render(Map.of("table_schemas", tableSchemas));
        return req.mutate().prompt(req.prompt().augmentSystemMessage(systemText)).build();
    }
    @Override public ChatClientResponse after(ChatClientResponse r, AdvisorChain c) { return r; }
    @Override public String getName() { return getClass().getSimpleName(); }
    @Override public int getOrder() { return Ordered.HIGHEST_PRECEDENCE; }
}

*/
