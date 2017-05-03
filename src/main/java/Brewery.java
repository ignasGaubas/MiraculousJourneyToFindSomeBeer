import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Ignas on 2017-05-02.
 */
public class Brewery {
    private int id;
    private double distance;
    private double latitude;
    private double longitude;
    private String city;
    private String name;

    /**
     * Fill instance fields from given SQL result set. If any field is not found error occurs
     *
     * @param resultSet
     * @return
     */
    public boolean fillData(ResultSet resultSet) {
        boolean success = true;
        try {
            setId(resultSet.getInt("id"));
            setDistance(resultSet.getDouble("distance_in_km"));
            setLatitude(resultSet.getDouble("latitude"));
            setLongitude(resultSet.getDouble("longitude"));
            setName(resultSet.getString("name"));
            setCity(resultSet.getString("city"));
        } catch (SQLException e) {
            e.printStackTrace();
            success = false;
        }
        return success;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null){
            return false;
        }
        Brewery o = (Brewery) obj;
        return Integer.compare(this.getId(), o.getId()) == 0 || this.getName().equals(o.getName());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Object getCity() {
        return city;
    }

    public String getName() {
        return name;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setName(String name) {
        this.name = name;
    }
}
