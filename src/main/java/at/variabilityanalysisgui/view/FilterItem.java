package at.variabilityanalysisgui.view;

import at.variabilityanalysisgui.controller.FilterController;
import at.variabilityanalysisgui.controller.Filter.Filter;
import at.variabilityanalysisgui.controller.Filter.MultipleChoiceFilter;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.List;

public class FilterItem extends HBox {
    CheckBox checkBox;
    List<ToggleButton> toggleButtons = new ArrayList<>();

    public FilterItem(Filter filter) {
        this.checkBox = new CheckBox(filter.getName());
        filter.enabledProperty().bindBidirectional(checkBox.selectedProperty());
        this.getChildren().add(checkBox);
        if (filter instanceof MultipleChoiceFilter multipleChoiceFilter) {
            for(String value : multipleChoiceFilter.getSelectableValues()) {
                ToggleButton button = new ToggleButton(value);
                button.setOnAction(event -> {
                    StringBuilder sb = new StringBuilder();
                    for (ToggleButton toggleButton : toggleButtons) {
                        if (toggleButton.isSelected()) {
                            sb.append(toggleButton.getText()).append(";");
                        }
                    }
                    filter.valueProperty().set(sb.toString());
                });
                toggleButtons.add(button);
                this.getChildren().add(button);
            }
        } // add other types of filters if needed

        checkBox.setPadding(new javafx.geometry.Insets(5, 5, 0, 0));
        this.setSpacing(5);
    }
}
