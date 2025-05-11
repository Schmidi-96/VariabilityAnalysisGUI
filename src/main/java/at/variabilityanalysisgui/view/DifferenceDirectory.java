package at.variabilityanalysisgui.view;

import at.variabilityanalysisgui.model.Difference;
import javafx.beans.property.SimpleStringProperty;

public class DifferenceDirectory extends Difference {
    String seperator;
    String path;

    public DifferenceDirectory(String path, String separator) {
        this.name = new SimpleStringProperty(path.split(separator)[path.split(separator).length - 1]);
        this.path = path;
        this.seperator = separator;
    }

    public String getPath() {
        return path;
    }
}
