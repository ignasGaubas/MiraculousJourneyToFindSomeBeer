import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * Created by Ignas on 2017-05-03.
 */
public class BeerDBHelperTest {

    private BeerDBHelper dbHelper;

    @Before
    public void setUp() throws Exception {
        dbHelper = new BeerDBHelper("127.0.0.1", "beerdb", "root", "");
        dbHelper.openConnection();
    }

    @After
    public void tearDown() throws Exception {
        dbHelper.closeConnection();
    }

    @Test
    public void openAndCloseConnection() throws Exception {
        boolean closeResult = dbHelper.closeConnection();
        boolean openResult = dbHelper.openConnection();

        assertTrue(closeResult);
        assertTrue(openResult);
    }

    @Test
    public void getBreweriesList() throws Exception {
        ResultSet breweries = dbHelper.getBreweriesList();

        breweries.next();
        assertEquals("name", breweries.getString("name"));
        assertEquals("address1", breweries.getString("address1"));

        breweries.last();
        assertEquals("Brasserie du Bouffay", breweries.getString("name"));
        assertEquals("Carquefou", breweries.getString("city"));
        assertEquals("", breweries.getString("state"));
    }

    @Test
    public void getBreweriesRaw() throws Exception {
        ResultSet breweries = dbHelper.getBreweriesRaw(0.0, 0.0, 2000.0);

        breweries.next();
        assertEquals(1140, breweries.getInt("id"));
        assertEquals("Sierra Leone Brewery", breweries.getString("name"));
        assertEquals("Freetown", breweries.getString("city"));
        assertEquals("Sierra Leone", breweries.getString("state"));
        assertEquals(1741.6201963867202, breweries.getDouble("distance_in_km"), 0.000_1);
    }

    @Test
    public void getBreweries() throws Exception {
        Brewery[] breweries = dbHelper.getBreweries(0.0, 0.0, 2000.0);

        assertEquals(1140, breweries[0].getId());
        assertEquals("Sierra Leone Brewery", breweries[0].getName());
        assertEquals("Freetown", breweries[0].getCity());
        assertEquals(8.484100342, breweries[0].getLatitude(), 0.000_1);
        assertEquals(1741.6201963867202, breweries[0].getDistance(), 0.000_1);
    }

    @Test
    public void getAdjacentBreweriesRaw() throws Exception {
        ResultSet breweries = dbHelper.getAdjacentBreweriesRaw(51.506, -0.129, 34.506, -0.129, 2000.0);

        breweries.next();
        assertEquals(1342, breweries.getInt("id"));
        assertEquals("Watney Brewery", breweries.getString("name"));
        assertEquals("London", breweries.getString("city"));
        assertEquals("London", breweries.getString("state"));
        assertEquals(0.6730868725681334, breweries.getDouble("distance_in_km_left"), 0.000_1);

        breweries.last();
        assertEquals(890, breweries.getInt("id"));
        assertEquals("Morland and Co.", breweries.getString("name"));
        assertEquals("Abingdon", breweries.getString("city"));
        assertEquals("Oxford", breweries.getString("state"));
        assertEquals(81.85940270808554, breweries.getDouble("distance_in_km_left"), 0.000_1);
    }

    @Test
    public void getAdjacentBreweries() throws Exception {
        Brewery bwLeft = new Brewery();
        bwLeft.setLatitude(51.506);
        bwLeft.setLongitude(-0.129);

        Brewery bwRight = new Brewery();
        bwRight.setLatitude(34.506);
        bwRight.setLongitude(-0.129);

        HamiltonBrewery[] breweries = dbHelper.getAdjacentBreweries(bwLeft, bwRight, 2000.0);

        assertEquals(1342, breweries[0].getId());
        assertEquals("Watney Brewery", breweries[0].getName());
        assertEquals("London", breweries[0].getCity());
        assertEquals(0.6730868725681334, breweries[0].getLeftDistance(), 0.000_1);
    }

    @Test
    public void getDistinctBeersRaw() throws Exception {
        Stream<Integer> breweries = Stream.of(1099, 501);
        ResultSet beers = dbHelper.getDistinctBeersRaw(breweries);

        beers.next();
        assertEquals("Winter Welcome 2007-2008", beers.getString("name"));
        assertEquals("Samuel Smith Old Brewery (Tadcaster)", beers.getString("brewery"));

        beers.last();
        assertEquals(28, beers.getRow());
    }

    @Test
    public void getDistinctBeers() throws Exception {
        Stream<Integer> breweries = Stream.of(1099, 501);
        ArrayList<Beer> beers = dbHelper.getDistinctBeers(breweries);

        assertEquals("Winter Welcome 2007-2008", beers.get(0).getName());
        assertEquals("Samuel Smith Old Brewery (Tadcaster)", beers.get(0).getBreweryName());

        assertEquals("Winter Welcome 2008-2009", beers.get(27).getName());
        assertEquals("Winter Warmer", beers.get(27).getStyle());
    }
}