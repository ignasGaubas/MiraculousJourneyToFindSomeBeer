import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import java.sql.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.StringJoiner;
import java.util.stream.Stream;

/**
 * Created by Ignas on 2017-04-27.
 */
public class BeerDBHelper {

    private int MYSQL_PORT = 3306;

    MysqlDataSource mysqlDataSource;
    private Connection connection;
    private Statement statement;

    private String serverIP;
    private String dbName;
    private String dbUserName;
    private String password;

    public BeerDBHelper(String serverIP, String dbName, String dbUserName, String password) {
        this.serverIP = serverIP;
        this.dbName = dbName;
        this.dbUserName = dbUserName;
        this.password = password;
    }

    /**
     * Sets connection parameters and opens connection to MySQL database
     *
     * @return true if success, false if failed
     */
    public boolean openConnection() {
        boolean success = true;

        mysqlDataSource = new MysqlDataSource();
        mysqlDataSource.setServerName(this.serverIP);
        mysqlDataSource.setPort(MYSQL_PORT);
        mysqlDataSource.setDatabaseName(this.dbName);
        mysqlDataSource.setUser(this.dbUserName);
        mysqlDataSource.setPassword(this.password);

        try {
            connection = mysqlDataSource.getConnection();
            statement = connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
            success = false;
        }

        return success;
    }

    /**
     * Safely close connection and release resources
     *
     * @return true if success, false if failed
     */
    public boolean closeConnection() {
        boolean success = true;

        try {
            this.statement.close();
            this.connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            success = false;
        }

        return success;
    }

    /**
     * Get list of breweries from DB
     *
     * @return List as ResultSet
     */
    public ResultSet getBreweriesList() {
        ResultSet rs = null;
        String queryFormat = "SELECT * FROM `%s`.`breweries`";
        String query = String.format(queryFormat, this.dbName);

        try {
            rs = this.statement.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rs;
    }

    /**
     * Get list of breweries that are at most radius kilometres away from given coordinates
     *
     * @param latitude
     * @param longitude
     * @param radius
     * @return ResultSet with breweries
     */
    public ResultSet getBreweriesRaw(double latitude, double longitude, double radius) {
        ResultSet resultSet = null;
        Brewery[] breweries = null;
        String queryFormat = String.join(" ",
//                "set @lat := %f, @lng := %f;",
                "SELECT",
                "*",
                "FROM",
                "(SELECT",
                "bw.*,",
                "g.latitude,",
                "g.longitude,",
                "111.111 * DEGREES(ACOS(COS(RADIANS({0})) * COS(RADIANS(g.latitude)) * COS(RADIANS({1} - g.longitude)) + SIN(RADIANS({0})) * SIN(RADIANS(g.latitude)))) AS distance_in_km",
                "FROM",
                "breweries bw",
                "JOIN geocodes g ON g.brewery_id = bw.id) AS tbl",
                "WHERE",
                "distance_in_km <= %f",
                "ORDER BY distance_in_km ASC"
        );
        String query = MessageFormat.format(queryFormat, latitude, longitude);
        query = String.format(query, radius);

        try {
            resultSet = this.statement.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return resultSet;
    }

    /**
     * Get breweries as an array
     *
     * @param latitude
     * @param longitude
     * @param radius
     * @return array of Brewery
     */
    public Brewery[] getBreweries(double latitude, double longitude, double radius) {
        ResultSet resultSet = this.getBreweriesRaw(latitude, longitude, radius);
        Brewery[] breweries = getBreweriesFromResultSet(resultSet);
        return breweries;
    }

    /**
     * Get all breweries whose sum of distances to both points is no more than maxDistance. Ordered ASC by distance
     *
     * @param latitude0
     * @param longitude0
     * @param latitude1
     * @param longitude1
     * @param maxDistance
     * @return ResultSet of breweries
     */
    public ResultSet getAdjacentBreweriesRaw(double latitude0, double longitude0, double latitude1, double longitude1, double maxDistance) {
        ResultSet resultSet = null;
        String queryFormat = String.join(" ",
                "SELECT  * FROM",
                "(SELECT",
                "*, distance_in_km_left + distance_in_km_right as distance_in_km",
                "FROM",
                "(SELECT",
                "bw.*,",
                "g.latitude,",
                "g.longitude,",
                "111.111 * DEGREES(ACOS(COS(RADIANS({0})) * COS(RADIANS(g.latitude)) * COS(RADIANS({1} - g.longitude)) + SIN(RADIANS({0})) * SIN(RADIANS(g.latitude)))) AS distance_in_km_left,",
                "111.111 * DEGREES(ACOS(COS(RADIANS({2})) * COS(RADIANS(g.latitude)) * COS(RADIANS({3} - g.longitude)) + SIN(RADIANS({2})) * SIN(RADIANS(g.latitude)))) AS distance_in_km_right",
                "FROM",
                "breweries bw",
                "JOIN geocodes g ON g.brewery_id = bw.id) AS tbl) AS tbl2",
                "WHERE",
                "distance_in_km <= %f",
                "ORDER BY distance_in_km ASC"
        );
        String query = MessageFormat.format(queryFormat, latitude0, longitude0, latitude1, longitude1);
        query = String.format(query, maxDistance);

        try {
            resultSet = this.statement.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return resultSet;
    }

    /**
     * Get breweries array
     *
     * @param breweryLeft
     * @param breweryRight
     * @param maxDistance
     * @return Array of breweries
     */
    public HamiltonBrewery[] getAdjacentBreweries(Brewery breweryLeft, Brewery breweryRight, double maxDistance) {
        ResultSet resultSet = this.getAdjacentBreweriesRaw(
                breweryLeft.getLatitude(),
                breweryLeft.getLongitude(),
                breweryRight.getLatitude(),
                breweryRight.getLongitude(),
                maxDistance);
        HamiltonBrewery[] breweries = getHamiltonBreweriesFromResultSet(resultSet);
        return breweries;
    }

    /**
     * Transforms ResultSet into array of Brewery
     *
     * @param resultSet
     * @return Array of Brewery
     */
    private Brewery[] getBreweriesFromResultSet(ResultSet resultSet) {
        Brewery[] breweries = null;
        try {
            int size;
            resultSet.last();
            size = resultSet.getRow();
            breweries = new Brewery[size];
            for (resultSet.beforeFirst(); resultSet.next(); ) {
                int i = resultSet.getRow() - 1;
                breweries[i] = new Brewery();
                breweries[i].fillData(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return breweries;
    }

    /**
     * Transforms ResultSet into array of HamiltonBrewery
     *
     * @param resultSet
     * @return Array of HamiltonBrewery
     */
    private HamiltonBrewery[] getHamiltonBreweriesFromResultSet(ResultSet resultSet) {
        HamiltonBrewery[] breweries = null;
        try {
            int size;
            resultSet.last();
            size = resultSet.getRow();
            breweries = new HamiltonBrewery[size];
            for (resultSet.beforeFirst(); resultSet.next(); ) {
                int i = resultSet.getRow() - 1;
                breweries[i] = new HamiltonBrewery();
                breweries[i].fillData(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return breweries;
    }

    /**
     * Gets distinct list of beers manufactured in given breweries
     *
     * @param breweriesIds
     * @return ResultSet of beers
     */
    public ResultSet getDistinctBeersRaw(Stream<Integer> breweriesIds) {
        String queryFormat = String.join(" ",
                "SELECT DISTINCT(b.name), c.cat_name, s.style_name, bw.name AS brewery",
                "FROM",
                "beers b",
                "LEFT JOIN",
                "categories c ON b.cat_id = c.id",
                "LEFT JOIN",
                "styles s ON b.style_id = s.id",
                "LEFT JOIN",
                "breweries bw ON b.brewery_id = bw.id",
                "WHERE",
                "bw.id IN ({0})");
        StringJoiner stringJoiner = new StringJoiner(",", "", "");
        breweriesIds.forEach(id -> stringJoiner.add(Integer.toString(id)));
        String breweries = stringJoiner.toString();
        String query = MessageFormat.format(queryFormat, breweries);
        ResultSet resultSet = null;
        try {
            resultSet = statement.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return resultSet;
    }

    /**
     * Gets distinct beers that are manufactured in given breweries.
     *
     * @param breweriesIds
     * @return Array of Beer
     */
    public ArrayList<Beer> getDistinctBeers(Stream<Integer> breweriesIds) {
        ResultSet resultSet = getDistinctBeersRaw(breweriesIds);
        ArrayList<Beer> beers = new ArrayList<>();
        try {
            while (resultSet.next()) {
                Beer beer = new Beer(resultSet.getString("name"),
                        resultSet.getString("brewery"),
                        resultSet.getString("cat_name"),
                        resultSet.getString("style_name"));
                beers.add(beer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return beers;
    }
}