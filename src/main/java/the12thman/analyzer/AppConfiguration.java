package the12thman.analyzer;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;

import the12thman.data.DatabaseMetadataHelper;
import the12thman.data.RunSqlQueryTool;

import javax.sql.DataSource;

@Configuration
public class AppConfiguration {
    @Bean
    public DatabaseMetadataHelper metadataHelper(DataSource ds, ObjectMapper mapper) {
        return new DatabaseMetadataHelper(ds, mapper);
    }

    @Bean
    public JdbcClient jdbcClient(DataSource ds) { return JdbcClient.create(ds); }

    @Bean
public ChatLanguageModel chatLanguageModel(){
        String apiKey = "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";

        return OpenAiChatModel.builder().baseUrl("https://openrouter.ai/api/v1").apiKey(apiKey).modelName("qwen/qwen3-coder:free").build();
    }
    /* @Bean
    @Tool(name = "runSqlQuery", description = "Query database using SQL")
    public RunSqlQueryTool runSqlQuery(JdbcClient jdbcClient) { return new RunSqlQueryTool(jdbcClient); }*/
}

