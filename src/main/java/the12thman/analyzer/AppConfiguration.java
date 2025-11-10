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
        //provide your own api key or put it in properties file or configure via cmdb property
        String apiKey = "";

        return OpenAiChatModel.builder().baseUrl("https://openrouter.ai/api/v1").apiKey(apiKey).modelName("qwen/qwen3-coder:free").build();
    }
    /* @Bean
    @Tool(name = "runSqlQuery", description = "Query database using SQL")
    public RunSqlQueryTool runSqlQuery(JdbcClient jdbcClient) { return new RunSqlQueryTool(jdbcClient); }*/
}

