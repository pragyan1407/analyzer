package the12thman.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;

public class DatabaseMetadataHelper {
    private final DataSource dataSource;
    private final ObjectMapper objectMapper;

    public DatabaseMetadataHelper(DataSource dataSource, ObjectMapper objectMapper) {
        this.dataSource = dataSource;
        this.objectMapper = objectMapper;
    }

    public String extractMetadataJson() throws SQLException {
        var metadata = extractMetadata();
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            return Objects.toString(metadata);
        }
    }

    public DatabaseMetadata extractMetadata() throws SQLException {
        var metadata = dataSource.getConnection().getMetaData();
        var tablesInfo = new ArrayList<TableInfo>();
        try (var tables = metadata.getTables(null, null, null, new String[]{"TABLE"})) {
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                String tableDescription = tables.getString("REMARKS");
                String tableCatalog = tables.getString("TABLE_CAT");
                String tableSchema = tables.getString("TABLE_SCHEM");
                var columnsInfo = new ArrayList<ColumnInfo>();
                try (var columns = metadata.getColumns(null, null, tableName, null)) {
                    while (columns.next()) {
                        columnsInfo.add(new ColumnInfo(
                                columns.getString("COLUMN_NAME"),
                                columns.getString("TYPE_NAME"),
                                columns.getString("REMARKS")
                        ));
                    }
                }
                tablesInfo.add(new TableInfo(tableName, tableDescription, tableCatalog, tableSchema, columnsInfo));
            }
        }
        return new DatabaseMetadata(tablesInfo);
    }
}

