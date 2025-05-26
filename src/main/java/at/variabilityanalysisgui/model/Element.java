package at.variabilityanalysisgui.model;

import at.variabilityanalysisgui.parser.InputParser.ExtractionType;
import javafx.beans.property.SimpleStringProperty;

import static at.variabilityanalysisgui.parser.InputParser.ExtractionType.JAVA;

public class Element extends Difference {

    private String location;
    private String description;
    private String seperatorSymbol;

    public Element(String name, ExtractionType type, String location, String description) {
        this.name = new SimpleStringProperty(name);
        if (type == JAVA) {
            this.location = location.replaceAll("[()]", "");
            this.seperatorSymbol = "/";
        } else {
            this.location = location;
            this.seperatorSymbol = ";";
        }
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public String getSeperaterSymbol() {
        return seperatorSymbol;
    }

    @Override
    public String toString() {
        return location + ":\n" + description;
    }
}