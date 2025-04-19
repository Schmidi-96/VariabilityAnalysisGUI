package at.variabilityanalysisgui.view;

import at.variabilityanalysisgui.model.Difference;
import javafx.beans.property.SimpleStringProperty;

public class DifferenceDirectory extends Difference {
    String seperator;
    String path;

    public DifferenceDirectory(String path, String seperator) {
        this.name = new SimpleStringProperty(path.split(seperator)[path.split(seperator).length - 1]);
        this.path = path;
        this.seperator = seperator;
    }

    public String getPath() {
        return path;
    }
}
