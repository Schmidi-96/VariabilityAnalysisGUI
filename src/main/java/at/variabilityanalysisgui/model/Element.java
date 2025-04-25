package at.variabilityanalysisgui.model;

import javafx.beans.property.SimpleStringProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Element extends Difference {

    public enum ElementType {
        JAVA, IEC61499
    }

    private ElementType type;
    private String location;
    private String description;

    public Element(String name, ElementType type,  String location, String description) {
        this.name = new SimpleStringProperty(name);
        this.type = type;
        if (type == ElementType.JAVA) {
            this.location = location.replaceAll("[()]", "");
        } else {
            this.location = location;
        }
        this.description = description;
    }

    public ElementType getType() {
        return type;
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