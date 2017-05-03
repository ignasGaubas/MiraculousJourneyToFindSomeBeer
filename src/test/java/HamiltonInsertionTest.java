import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Created by Ignas on 2017-05-03.
 */
public class HamiltonInsertionTest {

    private HamiltonInsertion hi;
    private static final double LATITUDE = 51.742503;
    private static final double LONGITUDE = 19.432956;
    private static final double DISTANCE = 2000;

    @Before
    public void setUp() throws Exception {
        hi = new HamiltonInsertion(LATITUDE, LONGITUDE, DISTANCE);
        hi.openDBHelper();
    }

    @After
    public void tearDown() throws Exception {
        hi.closeDBHelper();
    }

    @Test
    public void openAndCloseDBHelper() throws Exception {
        boolean closeResult = hi.closeDBHelper();
        boolean openResult = hi.openDBHelper();

        assertTrue(closeResult);
        assertTrue(openResult);
    }

    @Test
    public void calculateHamiltonRoute() throws Exception {
        ArrayList<HamiltonBrewery> route = hi.calculateHamiltonRoute(LATITUDE, LONGITUDE, DISTANCE);

        assertEquals("Initial", route.get(0).getName());
        assertEquals("Browar Zywiec", route.get(1).getName());
        assertEquals("Klosterbrauerei Weltenburg", route.get(8).getName());
        assertEquals("Initial", route.get(route.size() - 1).getName());
    }

    @Test
    public void copyBreweriesToHash() throws Exception {
        Brewery[] breweriesArray = new Brewery[3];
        Brewery bw;

        bw = new Brewery();
        bw.setId(3);
        bw.setName("First BW");
        breweriesArray[0] = bw;

        bw = new Brewery();
        bw.setId(5);
        bw.setName("Second BW");
        breweriesArray[1] = bw;

        bw = new Brewery();
        bw.setId(9);
        bw.setName("Last BW");
        breweriesArray[2] = bw;

        HashMap<Integer, Brewery> breweriesHash = hi.copyBreweriesToHash(breweriesArray);

        assertEquals(breweriesArray[0].getName(), breweriesHash.get(breweriesArray[0].getId()).getName());
        assertEquals(breweriesArray[1].getName(), breweriesHash.get(breweriesArray[1].getId()).getName());
        assertEquals(breweriesArray[2].getName(), breweriesHash.get(breweriesArray[2].getId()).getName());
        assertEquals(null, breweriesHash.get(123));
    }

    @Test
    public void initializeHamiltonRoute() throws Exception {
        ArrayList<HamiltonBrewery> route = hi.initializeHamiltonRoute(LATITUDE, LONGITUDE);

        assertTrue(route.get(0).equals(route.get(1)));
        assertEquals("Initial", route.get(0).getName());
        assertEquals("Initial", route.get(1).getName());
        assertEquals(0.0, route.get(0).getRightDistance(), 0.000_1);
        assertEquals(0.0, route.get(1).getLeftDistance(), 0.000_1);
    }

    @Test
    public void insertLocation() throws Exception {
        ArrayList<HamiltonBrewery> route = hi.initializeHamiltonRoute(LATITUDE, LONGITUDE);
        HashMap<Integer, Brewery> available = new HashMap<>();

        Brewery bw = new Brewery();
        bw.setId(307);
        bw.setName("Browar Okocim");
        available.put(bw.getId(), bw);

        hi.insertLocation(route, available, Double.MAX_VALUE);

        assertEquals(3, route.size());
        assertEquals("Browar Okocim", route.get(1).getName());
        assertEquals("Initial", route.get(0).getName());
    }

    @Test
    public void getCandidates() throws Exception {
        ArrayList<HamiltonBrewery> route = hi.initializeHamiltonRoute(LATITUDE, LONGITUDE);
        HashMap<Integer, Brewery> available = new HashMap<>();

        Brewery bw = new Brewery();
        bw.setId(307);
        bw.setName("Browar Okocim");
        available.put(bw.getId(), bw);

        ArrayList<HamiltonBrewery> candidates = hi.getCandidates(route, available);

        assertEquals(1, candidates.size());
        assertEquals(307, candidates.get(0).getId());
        assertEquals("Browar Okocim", candidates.get(0).getName());
    }

    @Test
    public void getOptimalCandidateIndex() throws Exception {
        ArrayList<HamiltonBrewery> route = hi.initializeHamiltonRoute(LATITUDE, LONGITUDE);
        HashMap<Integer, Brewery> available = new HashMap<>();
        Brewery bw;

        bw = new Brewery();
        bw.setId(307);
        bw.setName("Browar Okocim");
        available.put(bw.getId(), bw);

        bw = new Brewery();
        bw.setId(309);
        bw.setName("Browar Zywiec");
        available.put(bw.getId(), bw);

        bw = new Brewery();
        bw.setId(990);
        bw.setName("Pivovar Hradec Krlov");
        available.put(bw.getId(), bw);

        hi.insertLocation(route, available, Double.MAX_VALUE);

        ArrayList<HamiltonBrewery> candidates = hi.getCandidates(route, available);
        int optimalIndex = hi.getOptimalCandidateIndex(candidates);

        assertEquals(0, optimalIndex);
    }

    @Test
    public void findClosestBrewery() throws Exception {
        HashMap<Integer, Brewery> available = new HashMap<>();

        HamiltonBrewery bwLeft;
        bwLeft = new HamiltonBrewery();
        bwLeft.setId(1394);
        bwLeft.setName("Aviator Brewing Company");
        bwLeft.setLatitude(35.61970139);
        bwLeft.setLongitude(-78.8085022);

        HamiltonBrewery bwRight;
        bwRight = new HamiltonBrewery();
        bwRight.setId(1393);
        bwRight.setName("Baxter Brewing");
        bwRight.setLatitude(44.10039902);
        bwRight.setLongitude(-70.21479797);

        Brewery bw1;
        bw1 = new Brewery();
        bw1.setId(1391);
        bw1.setName("Yazoo Brewing");
        bw1.setLatitude(36.15100098);
        bw1.setLongitude(-86.78209686);
        available.put(bw1.getId(), bw1);

        Brewery bw2;
        bw2 = new Brewery();
        bw2.setId(1390);
        bw2.setName("Yellowstone Valley Brewing");
        bw2.setLatitude(45.78609848);
        bw2.setLongitude(-108.4980011);
        available.put(bw2.getId(), bw2);

        HamiltonBrewery closest = hi.findClosestBrewery(bwLeft, bwRight, available, Double.MAX_VALUE);

        assertTrue(closest.equals(bw1));
    }

    @Test
    public void calculateDistinctBeerKinds() throws Exception {
        ArrayList<HamiltonBrewery> route = hi.initializeHamiltonRoute(LATITUDE, LONGITUDE);
        HashMap<Integer, Brewery> available = new HashMap<>();

        Brewery bw = new Brewery();
        bw.setId(307);
        bw.setName("Browar Okocim");
        available.put(bw.getId(), bw);

        hi.insertLocation(route, available, Double.MAX_VALUE);

        ArrayList<Beer> beers = hi.calculateDistinctBeerKinds(route);

        assertEquals(2, beers.size());
    }

    @Test
    public void calculateRouteDistance() throws Exception {
        ArrayList<HamiltonBrewery> route = hi.calculateHamiltonRoute(LATITUDE, LONGITUDE, DISTANCE);
        double distance = hi.calculateRouteDistance(route);

        assertEquals(1914.130630335571, distance, 0.000_1);
    }
}