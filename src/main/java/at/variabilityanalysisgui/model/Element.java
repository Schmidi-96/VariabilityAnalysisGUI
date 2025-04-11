package at.variabilityanalysisgui.model;

import javafx.beans.property.SimpleStringProperty;

public class Element extends Difference {
    private String location;
    private String description;

    public Element(String name, String location, String description) {
        this.name = new SimpleStringProperty(name);
        this.location = location;
        this.description = description;
    }

    public String getLocation() {
        return location;
    }
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return location + ":\n" + description;
    }
}