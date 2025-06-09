package at.variabilityanalysisgui.model;

import at.variabilityanalysisgui.parser.InputParser.ExtractionType;
import javafx.beans.property.SimpleStringProperty;

import static at.variabilityanalysisgui.parser.InputParser.ExtractionType.JAVA;

public class Element extends Difference {

    private String location;
    private String description;
    private String seperatorSymbol;
    private ExtractionType extractionType;

    public Element(String name, ExtractionType type, String location, String description) {
        this.name = new SimpleStringProperty(name);
        this.extractionType = type;
        if (extractionType == JAVA) {
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

    public ExtractionType getExtractionType() {
        return extractionType;
    }

    @Override
    public String toString() {
        return location + ":\n" + description;
    }
}