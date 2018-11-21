package edu.calvin.cs262.pilot.knight_ranker;

import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.google.api.server.spi.config.ApiMethod.HttpMethod.GET;

public class LeaderboardResource {
    private static final Logger log = Logger.getLogger(LeaderboardResource.class.getName());
    /**
     * GET
     * Gets the leaderboard for a given sport
     * @return JSON-formatted list of PlayerRanks
     * @throws SQLException
     */
    @ApiMethod(path = "leaderboard", httpMethod = GET)
    public List<PlayerRank> getLeaderboard (@Named("sport") String sport) throws SQLException {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        List<PlayerRank> result = new ArrayList<>();
        try {
            log.info("Sport:" + sport);
            connection = DriverManager.getConnection(System.getProperty("cloudsql"));
            statement = connection.createStatement();
            resultSet = selectPlayerRanks(statement, sport);
            log.info("Results: " + resultSet.toString());
            while (resultSet.next()) {
                PlayerRank p = new PlayerRank(
                        Integer.parseInt(resultSet.getString(1)),
                        resultSet.getString(2)
                );
                result.add(p);
            }
        } catch (SQLException e) {
            throw (e);
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
        return result;
    }

    /**
     * Gets the leaderboard for a given sport
     * @param statement
     * @param sport
     * @return
     * @throws SQLException
     */
    private ResultSet selectPlayerRanks(Statement statement, String sport) throws SQLException {
        return statement.executeQuery(
                String.format("SELECT rank, emailAddress " +
                                "FROM Player, Sport, Follow " +
                                "WHERE Sport.name = '%s' " +
                                "AND Follow.sportID = Sport.id " +
                                "AND Follow.playerID = Player.id " +
                                "ORDER BY rank ASC",
                    sport
                )
        );
    }
}
