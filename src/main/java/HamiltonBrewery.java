import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Ignas on 2017-05-03.
 */
public class HamiltonBrewery extends Brewery implements HamiltonPoint {
    private double leftDistance;
    private double rightDistance;

    /**
     * Fill instance fields from given SQL result set. If any field is not found error occurs
     *
     * @param resultSet
     * @return
     */
    @Override
    public boolean fillData(ResultSet resultSet) {
        boolean result = super.fillData(resultSet);

        try {
            setLeftDistance(resultSet.getDouble("distance_in_km_left"));
            setRightDistance(resultSet.getDouble("distance_in_km_right"));
        } catch (SQLException e) {
            e.printStackTrace();
            result = false;
        }

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public double getLeftDistance() {
        return leftDistance;
    }

    public double getRightDistance() {
        return rightDistance;
    }

    public void setLeftDistance(double distance) {
        leftDistance = distance;
    }

    public void setRightDistance(double distance) {
        rightDistance = distance;
    }

    /**
     * Generates HamiltonBrewery instance with given coordinates and distance
     * @param latitude Latitude
     * @param longitude Longitude
     * @param distance Distande to nearby elements (distance, left, right)
     * @param name Name of the brewery
     * @return HamiltonBrewery instance
     */
    public static HamiltonBrewery getInstance(double latitude, double longitude, double distance, String name) {
        HamiltonBrewery brewery = new HamiltonBrewery();

        brewery.setDistance(distance);
        brewery.setLeftDistance(distance);
        brewery.setRightDistance(distance);
        brewery.setLatitude(latitude);
        brewery.setLongitude(longitude);
        brewery.setName(name);

        return brewery;
    }

    @Override
    public String toString() {
        return String.format("%s | %s", getName(), getCity());
    }
}