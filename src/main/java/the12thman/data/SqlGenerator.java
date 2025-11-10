package the12thman.data;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface SqlGenerator {

    @SystemMessage("""
        You are a PostgreSQL expert for cricket analytics.
        Given the following database schema in JSON:
        {schema}
        Generate a single, safe SQL query that best answers the following user request.
        Do not reply in English; only provide the SQL.
    """)
    @UserMessage("{userQuestion}")
    String generateSql(@V("userquestion") String userQuestion, @V("schema") String schema);
}
