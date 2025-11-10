package the12thman.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

@Component
public class DatabasePopulator implements CommandLineRunner {

    private final DataSource dataSource;
    private final String jsonDir;

    public DatabasePopulator(DataSource dataSource,
                             @Value("${cricsheet.json-dir}") String jsonDir) {
        this.dataSource = dataSource;
        this.jsonDir = jsonDir;
    }

    @Override
    public void run(String... args) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            if (isDataLoaded(conn)) {
                System.out.println("Database already populated. Skipping JSON import.");
            } else {
                System.out.println("Populating database with match data...");
                loadData(conn);
                System.out.println("All matches and deliveries loaded successfully.");
            }
        }
    }

    private boolean isDataLoaded(Connection conn) throws SQLException {
        String sql = "SELECT EXISTS (SELECT 1 FROM matches LIMIT 1)";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() && rs.getBoolean(1);
        }
    }

    private void loadData(Connection conn) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        conn.setAutoCommit(false);

        try (
                PreparedStatement matchStmt = conn.prepareStatement(
                        "INSERT INTO matches (match_id, event_name, match_number, city, venue, match_type, outcome_result, outcome_winner, start_date, end_date) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (match_id) DO NOTHING"
                );
                PreparedStatement deliveryStmt = conn.prepareStatement(
                        "INSERT INTO deliveries (match_id, inning, over, ball, batting_team, bowling_team, batter, bowler, non_striker, runs_batter, runs_extras, runs_total, extras_legbyes, extras_byes, extras_noballs, extras_wides, extras_penalty, wicket_kind, player_out) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                )
        ) {
            List<Path> jsonPaths = Files.list(Paths.get(jsonDir))
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .toList();

            for (Path jsonFile : jsonPaths) {
                JsonNode root = mapper.readTree(jsonFile.toFile());
                String matchId = jsonFile.getFileName().toString().replace(".json", "");

                // Extract match info (as before)
                JsonNode info = root.get("info");
                String eventName = info.has("event") && info.get("event").has("name") ? info.get("event").get("name").asText(null) : null;
                Integer matchNumber = info.has("event") && info.get("event").has("match_number") ? info.get("event").get("match_number").asInt() : null;
                String city = info.has("city") ? info.get("city").asText(null) : null;
                String venue = info.has("venue") ? info.get("venue").asText(null) : null;
                String matchType = info.has("match_type") ? info.get("match_type").asText(null) : null;
                String outcomeResult = info.has("outcome") && info.get("outcome").has("result") ? info.get("outcome").get("result").asText(null) : null;
                String outcomeWinner = info.has("outcome") && info.get("outcome").has("winner") ? info.get("outcome").get("winner").asText(null) : null;

                ArrayNode datesNode = (ArrayNode) info.get("dates");
                LocalDate startDate = datesNode != null && datesNode.size() > 0 ? LocalDate.parse(datesNode.get(0).asText()) : null;
                LocalDate endDate = datesNode != null && datesNode.size() > 0 ? LocalDate.parse(datesNode.get(datesNode.size() - 1).asText()) : null;

                matchStmt.setString(1, matchId);
                matchStmt.setString(2, eventName);
                if (matchNumber != null) matchStmt.setInt(3, matchNumber); else matchStmt.setNull(3, Types.INTEGER);
                matchStmt.setString(4, city);
                matchStmt.setString(5, venue);
                matchStmt.setString(6, matchType);
                matchStmt.setString(7, outcomeResult);
                matchStmt.setString(8, outcomeWinner);
                if (startDate != null) matchStmt.setDate(9, Date.valueOf(startDate)); else matchStmt.setNull(9, Types.DATE);
                if (endDate != null) matchStmt.setDate(10, Date.valueOf(endDate)); else matchStmt.setNull(10, Types.DATE);
                matchStmt.addBatch();

                // Deliveries (as before)
                ArrayNode innings = (ArrayNode) root.get("innings");
                if (innings != null) {
                    int inningNum = 1;
                    for (JsonNode inningNode : innings) {
                        String battingTeam = inningNode.get("team").asText(null);
                        ArrayNode overs = (ArrayNode) inningNode.get("overs");
                        if (overs != null) {
                            for (JsonNode overNode : overs) {
                                int over = overNode.get("over").asInt();
                                ArrayNode deliveries = (ArrayNode) overNode.get("deliveries");
                                if (deliveries != null) {
                                    int ball = 1;
                                    for (JsonNode ballNode : deliveries) {
                                        deliveryStmt.setString(1, matchId);
                                        deliveryStmt.setInt(2, inningNum);
                                        deliveryStmt.setInt(3, over);
                                        deliveryStmt.setInt(4, ball++);
                                        deliveryStmt.setString(5, battingTeam);
                                        String bowlingTeam = ballNode.has("bowling_team") ? ballNode.get("bowling_team").asText(null) : null;
                                        deliveryStmt.setString(6, bowlingTeam);
                                        deliveryStmt.setString(7, ballNode.get("batter").asText(null));
                                        deliveryStmt.setString(8, ballNode.get("bowler").asText(null));
                                        deliveryStmt.setString(9, ballNode.get("non_striker").asText(null));
                                        JsonNode runs = ballNode.get("runs");
                                        deliveryStmt.setInt(10, runs != null && runs.has("batter") ? runs.get("batter").asInt() : 0);
                                        deliveryStmt.setInt(11, runs != null && runs.has("extras") ? runs.get("extras").asInt() : 0);
                                        deliveryStmt.setInt(12, runs != null && runs.has("total") ? runs.get("total").asInt() : 0);
                                        JsonNode extras = ballNode.get("extras");
                                        deliveryStmt.setInt(13, extras != null && extras.has("legbyes") ? extras.get("legbyes").asInt() : 0);
                                        deliveryStmt.setInt(14, extras != null && extras.has("byes") ? extras.get("byes").asInt() : 0);
                                        deliveryStmt.setInt(15, extras != null && extras.has("noballs") ? extras.get("noballs").asInt() : 0);
                                        deliveryStmt.setInt(16, extras != null && extras.has("wides") ? extras.get("wides").asInt() : 0);
                                        deliveryStmt.setInt(17, extras != null && extras.has("penalty") ? extras.get("penalty").asInt() : 0);
                                        String wicketKind = null, playerOut = null;
                                        JsonNode wicketsNode = ballNode.get("wickets");
                                        if (wicketsNode != null && wicketsNode.isArray() && wicketsNode.size() > 0) {
                                            JsonNode wicket = wicketsNode.get(0);
                                            wicketKind = wicket.has("kind") ? wicket.get("kind").asText(null) : null;
                                            playerOut = wicket.has("player_out") ? wicket.get("player_out").asText(null) : null;
                                        }
                                        deliveryStmt.setString(18, wicketKind);
                                        deliveryStmt.setString(19, playerOut);
                                        deliveryStmt.addBatch();
                                    }
                                }
                            }
                        }
                        inningNum++;
                    }
                }

                matchStmt.executeBatch();
                conn.commit();
                deliveryStmt.executeBatch();
                conn.commit();
            }
        }
    }
}
