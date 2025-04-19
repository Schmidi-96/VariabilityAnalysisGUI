package at.variabilityanalysisgui.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Difference {
    protected StringProperty name;

    public StringProperty getName() {
        return name;
    }

    public void setName(SimpleStringProperty simpleStringProperty) {
        this.name = simpleStringProperty; //TODO delete later again just for testing
    }
}
