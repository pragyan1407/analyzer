package the12thman.analyzer;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import the12thman.data.DatabaseMetadataHelper;
import the12thman.data.SqlGenerator;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/analyze")
public class ChatController {
    private final SqlGenerator sqlGeneratorAiService;
    private final DatabaseMetadataHelper metadataHelper;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ChatController(SqlGenerator sqlGeneratorAiService,
                          DatabaseMetadataHelper metadataHelper,
                          JdbcTemplate jdbcTemplate) {
        this.sqlGeneratorAiService = sqlGeneratorAiService;
        this.metadataHelper = metadataHelper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody String userQuestion) throws Exception {
        String schema = metadataHelper.extractMetadataJson();
        String sql = sqlGeneratorAiService.generateSql(userQuestion, schema);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

        // Simple string representation — you can improve formatting as needed
        return ResponseEntity.ok(rows.toString());
    }
}

