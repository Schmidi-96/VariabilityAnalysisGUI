/***
     The MIT License (MIT)

     Copyright (c) 2025 Michael Schmidhammer
 */

package at.variabilityanalysisgui.controller.Filter;

import at.variabilityanalysisgui.model.Element;
import at.variabilityanalysisgui.model.Group;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public abstract class Filter {
    private final String name;
    private final BooleanProperty enabled =new SimpleBooleanProperty(false);
    private final StringProperty value = new SimpleStringProperty("");

    public Filter(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled.get();
    }

    public void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
    }

    public String getValue() {
        return value.getValue();
    }

    public void setValue(String value) {
        this.value.setValue(value);
    }

    public abstract Element filter(Element element);

    public Group filter(Group group) {
        if(group.getElements().stream().anyMatch((e -> filter(e) != null))) {
            return group;
        }
        return null;
    }

    public StringProperty valueProperty() {
        return value;
    }

    public BooleanProperty enabledProperty() {
        return enabled;
    }
}
