import java.util.stream.Stream;

/**
 * Created by Ignas on 2017-05-03.
 */
public class Beer {
    private String name;
    private String breweryName;
    private String category;
    private String style;

    public Beer(String name, String breweryName, String category, String style) {
        this.name = name;
        this.breweryName = breweryName;
        this.category = category;
        this.style = style;
    }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | %s",
                name == null ? "-" : name,
                breweryName == null ? "-" : breweryName,
                category == null ? "-" : category,
                style == null ? "-" : style);
    }

    public String getName() {
        return name;
    }

    public String getBreweryName() {
        return breweryName;
    }

    public String getCategory() {
        return category;
    }

    public String getStyle() {
        return style;
    }
}