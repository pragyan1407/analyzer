package the12thman.data;

import org.springframework.jdbc.core.simple.JdbcClient;
import java.io.IOException;
import java.util.Map;
import java.util.function.Function;
import org.apache.commons.csv.CSVFormat;
import org.springframework.util.CollectionUtils;

public class RunSqlQueryTool implements Function<RunSqlQueryRequest, RunSqlQueryResponse> {
    private final JdbcClient jdbcClient;
    public RunSqlQueryTool(JdbcClient jdbcClient) { this.jdbcClient = jdbcClient; }

    @Override
    public RunSqlQueryResponse apply(RunSqlQueryRequest req) {
        try {
            return new RunSqlQueryResponse(runQuery(req.query()), null);
        } catch (Exception e) {
            return new RunSqlQueryResponse(null, e.getMessage());
        }
    }
    private String runQuery(String query) throws IOException {
        var rows = jdbcClient.sql(query).query().listOfRows();
        if (CollectionUtils.isEmpty(rows)) return "";
        var fields = rows.getFirst().keySet().stream().sorted().toList();
        var printer = CSVFormat.DEFAULT.builder()
                .setHeader(fields.toArray(new String[0]))
                .setSkipHeaderRecord(false)
                .setRecordSeparator('\n')
                .build();
        var builder = new StringBuilder();
        for (Map<String, Object> row : rows) {
            printer.printRecord(builder, fields.stream().map(row::get).toArray());
        }
        return builder.toString();
    }
}

