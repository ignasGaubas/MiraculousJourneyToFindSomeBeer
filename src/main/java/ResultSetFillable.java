import java.sql.ResultSet;

/**
 * Created by Ignas on 2017-05-03.
 */
public interface ResultSetFillable {
    /**
     * Fill data fields from ResultSet
     *
     * @param resultSet
     */
    public void fillData(ResultSet resultSet);
}
