import java.util.ArrayList;

/**
 * Created by Ignas on 2017-04-27.
 */
public class Journey {

    // Lenkija, Lodz
    private static final double LATITUDE = 51.742503;
    private static final double LONGITUDE = 19.432956;
    private static final double DISTANCE_LIMIT = 2000;

    public static void main(String[] args) {
        HamiltonInsertion hi = new HamiltonInsertion(LATITUDE, LONGITUDE, DISTANCE_LIMIT);
        hi.openDBHelper();

        ArrayList<HamiltonBrewery> hamiltonRoute = hi.calculateHamiltonRoute(LATITUDE, LONGITUDE, DISTANCE_LIMIT);
        printData(hamiltonRoute);

        double routeDistance = hi.calculateRouteDistance(hamiltonRoute);
        printData("Route distance: ", routeDistance);

        ArrayList<Beer> beers = hi.calculateDistinctBeerKinds(hamiltonRoute);
        printData(beers);

        hi.closeDBHelper();
    }

    /**
     * Print message followed by value
     *
     * @param message
     * @param value
     */
    private static void printData(String message, double value) {
        System.out.println(message + value);
    }

    /**
     * Print array of data using toString() method
     *
     * @param data
     */
    private static void printData(ArrayList<? extends Object> data) {
        for (Object o : data) {
            System.out.println(o.toString());
        }
        System.out.println("Total records: " + data.size());
        System.out.println();
    }

    /**
     * Print cordinates as LATITUDE,LONGITUDE from  given array
     *
     * @param hamiltonRoute
     */
    private void printCoordinates(ArrayList<HamiltonBrewery> hamiltonRoute) {
        for (HamiltonBrewery bw : hamiltonRoute) {
            System.out.println(bw.getLatitude() + "," + bw.getLongitude());
        }
    }
}