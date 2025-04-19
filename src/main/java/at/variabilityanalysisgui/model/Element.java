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
    private List<String> location;
    private String description;

    public Element(String name, ElementType type,  String location, String description) {
        this.name = new SimpleStringProperty(name);
        this.type = type;
        if (type == ElementType.JAVA) {
            this.location = Arrays.stream(location.replaceAll("[()]", "").split("/")).collect(Collectors.toList());
        } else {
            this.location = Arrays.stream(location.split(";")).collect(Collectors.toList());
        }
        this.description = description;
    }

    public ElementType getType() {
        return type;
    }

    public List<String> getLocation() {
        return location;
    }

    public String getLocationDisplayString() {
        StringBuilder sb = new StringBuilder();
        this.location.forEach(s -> {
            sb.append(s);
            if (!s.equals(location.get(location.size() - 1))) {
                if (type.equals(ElementType.JAVA)) {
                    sb.append("/");
                } else {
                    sb.append(";");
                }
            }
        });
        return sb.toString();
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return location + ":\n" + description;
    }
}