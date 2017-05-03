import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Stream;

/**
 * Created by Ignas on 2017-05-03.
 */
public class HamiltonInsertion {

    private static final String SERVER_IP = "127.0.0.1";
    private static final String DB_NAME = "beerdb";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "";

    private BeerDBHelper dh;
    private double distanceLimit;

    public HamiltonInsertion(double latitude, double longitude, double distanceLimit) {
        this.dh = new BeerDBHelper(SERVER_IP, DB_NAME, DB_USERNAME, DB_PASSWORD);
        this.distanceLimit = distanceLimit;
    }

    /**
     * Open connection in database helper, since some methods require CBHelper services
     * Manual connection open implemented to avoid crashing in constructor when opening a connection
     *
     * @return
     */
    public boolean openDBHelper() {
        return dh.openConnection();
    }

    /**
     * Close connection in database helper, since some methods require CBHelper services
     * Manual connection open implemented to avoid crashing in constructor when opening a connection
     *
     * @return
     */
    public boolean closeDBHelper() {
        return dh.closeConnection();
    }

    /**
     * Produces Hamilton route using insertion algorithm.
     * Starting at (latitude,longitude) algorithm searches for nearest
     * points within reachable radius
     *
     * @param latitude      latitude to start
     * @param longitude     longitude to start
     * @param distanceLimit distance limit from starting point
     * @return Collection with visited points which first and last elements are starting points
     */
    public ArrayList<HamiltonBrewery> calculateHamiltonRoute(double latitude, double longitude, double distanceLimit) {
        Brewery[] allBreweries = dh.getBreweries(latitude, longitude, distanceLimit);
        HashMap<Integer, Brewery> availableLocations = copyBreweriesToHash(allBreweries);
        ArrayList<HamiltonBrewery> hamiltonRoute = initializeHamiltonRoute(latitude, longitude);
        double totalDistance = 0;
        while (totalDistance < distanceLimit) {
            double distanceReserve = distanceLimit - totalDistance;
            int idx = insertLocation(hamiltonRoute, availableLocations, distanceReserve);
            if (idx == -1) {
                break;
            } else {
                double oldDistance = hamiltonRoute.get(idx - 1).getRightDistance();
                double newLeftDistance = hamiltonRoute.get(idx).getLeftDistance();
                double newRightDistance = hamiltonRoute.get(idx).getRightDistance();

                hamiltonRoute.get(idx - 1).setRightDistance(newLeftDistance);
                hamiltonRoute.get(idx + 1).setLeftDistance(newRightDistance);

                totalDistance = totalDistance - oldDistance + newLeftDistance + newRightDistance;
            }
        }
        return hamiltonRoute;
    }

    /**
     * Copies list of breweries to HashMap
     *
     * @param breweries
     * @return HashMap filled with breweries
     */
    public HashMap<Integer, Brewery> copyBreweriesToHash(Brewery[] breweries) {
        HashMap<Integer, Brewery> result = new HashMap<>(breweries.length);
        for (Brewery bw : breweries) {
            result.put(bw.getId(), bw);
        }
        return result;
    }

    /**
     * Creates a Hamilton route with initial element ar the beginning and end
     *
     * @param latitude  Initial element's initialLatitude
     * @param longitude Initial element's initialLongitude
     * @return Hamilton route with 2 elements
     */
    public ArrayList<HamiltonBrewery> initializeHamiltonRoute(double latitude, double longitude) {
        ArrayList<HamiltonBrewery> hamiltonRoute = new ArrayList<>();

        hamiltonRoute.add(HamiltonBrewery.getInstance(latitude, longitude, 0.0, "Initial"));
        hamiltonRoute.add(HamiltonBrewery.getInstance(latitude, longitude, 0.0, "Initial"));

        return hamiltonRoute;
    }

    /**
     * Finds and inserts element into hamilton route such that total distanceLimit grows minimally
     * When new node is added, it is removed from availableLocations
     *
     * @param hamiltonRoute
     * @param availableLocations
     * @param distanceReserve
     * @return Inserted element index in the collection. If no element was inserted, returns -1
     */
    public int insertLocation(ArrayList<HamiltonBrewery> hamiltonRoute, HashMap<Integer, Brewery> availableLocations, double distanceReserve) {
        ArrayList<HamiltonBrewery> candidates = getCandidates(hamiltonRoute, availableLocations);

        int optimalCandidateIdx = getOptimalCandidateIndex(candidates);

        HamiltonBrewery optimalCandidate = candidates.get(optimalCandidateIdx);

        double distanceIncrease = optimalCandidate.getDistance() - hamiltonRoute.get(optimalCandidateIdx).getRightDistance();
        if (distanceIncrease > distanceReserve || distanceIncrease < 0.0) {
            return -1;
        } else {
            hamiltonRoute.add(optimalCandidateIdx + 1, optimalCandidate);

            availableLocations.remove(optimalCandidate.getId());

            return optimalCandidateIdx + 1;
        }
    }

    /**
     * Finds best candidates to be added to hamilton route for each gap individually.
     * If route with 5 elements is passed, then 4 candidates, each per gap, will be returned
     *
     * @param hamiltonRoute
     * @param availableLocations
     * @return Candidates collection for each gap (i..i+1)
     */
    public ArrayList<HamiltonBrewery> getCandidates(ArrayList<HamiltonBrewery> hamiltonRoute, HashMap<Integer, Brewery> availableLocations) {
        ArrayList<HamiltonBrewery> candidates = new ArrayList<>(hamiltonRoute.size() - 1);

        for (int i = 0; i < hamiltonRoute.size() - 1; i++) {
            HamiltonBrewery candidate = findClosestBrewery(hamiltonRoute.get(i), hamiltonRoute.get(i + 1), availableLocations);
            candidates.add(i, candidate);
        }

        return candidates;
    }

    /**
     * Finds one candidate that has shortest distance
     *
     * @param candidates
     * @return Candidate index
     */
    public int getOptimalCandidateIndex(ArrayList<HamiltonBrewery> candidates) {

        double shortestDistance = Double.MAX_VALUE;
        int optimalCandidateIdx = -1;
        for (int i = 0; i < candidates.size(); i++) {
            double distance = candidates.get(i).getDistance();
            if (distance < shortestDistance) {
                shortestDistance = distance;
                optimalCandidateIdx = i;
            }
        }

        return optimalCandidateIdx;
    }

    public HamiltonBrewery findClosestBrewery(HamiltonBrewery breweryLeft, HamiltonBrewery breweryRight, HashMap<Integer, Brewery> available) {
        return findClosestBrewery(breweryLeft, breweryRight, available, this.distanceLimit);
    }

    /**
     * Given collection of available breweries and two actual breweries
     * find one brewery which common distanceLimit to both other breweries is smallest.
     *
     * @param breweryLeft
     * @param breweryRight
     * @param available    List of available breweries, that are not used in hamilton route yet.
     * @return If action was successfull returns Brewery with distances to the left and right ones.
     * If no success, null is returned.
     */
    public HamiltonBrewery findClosestBrewery(HamiltonBrewery breweryLeft, HamiltonBrewery breweryRight, HashMap<Integer, Brewery> available, double distanceLimit) {
        HamiltonBrewery closestBrewery = null;
        HamiltonBrewery[] allAdjacentBreweries = dh.getAdjacentBreweries(breweryLeft, breweryRight, distanceLimit);

        for (HamiltonBrewery brewery : allAdjacentBreweries) {
            Integer idKey = brewery.getId();
            Brewery adjacent = available.get(idKey);
            if (adjacent != null) {
                closestBrewery = brewery;
                break;
            }
        }
        return closestBrewery;
    }

    /**
     * Gets all distinct beers that are manufactured within given breweries
     *
     * @param hamiltonRoute
     * @return Distinct list of beers
     */
    public ArrayList<Beer> calculateDistinctBeerKinds(ArrayList<HamiltonBrewery> hamiltonRoute) {
        Stream<Integer> breweriesIds = hamiltonRoute.stream()
                .filter(bw -> bw.getId() >= 0)
                .map(bw -> bw.getId());
        ArrayList<Beer> beers = dh.getDistinctBeers(breweriesIds);

        return beers;
    }

    /**
     * Calculate distance of given Hamilton route.
     * Route contains n+1 elements: first and last elements are equal and represent route begin and end
     *
     * @param hamiltonRoute
     * @return Distance of route
     */
    public double calculateRouteDistance(ArrayList<HamiltonBrewery> hamiltonRoute) {
        double routeDistance = 0.0;
        for (int i = 0; i < hamiltonRoute.size() - 1; i++) {
            HamiltonBrewery bw = hamiltonRoute.get(i);
            routeDistance += bw.getRightDistance();
        }

        return routeDistance;
    }
}