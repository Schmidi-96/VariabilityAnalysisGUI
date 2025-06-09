package at.variabilityanalysisgui.view;

import at.variabilityanalysisgui.controller.Filter.Filter;
import at.variabilityanalysisgui.controller.Filter.MultipleChoiceFilter;
import at.variabilityanalysisgui.controller.Filter.SingleChoiceFilter;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
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
                    if (button.isSelected()) {
                        multipleChoiceFilter.addSelectedValue(value);
                    } else {
                        multipleChoiceFilter.removeSelectedValue(value);
                    }
                });
                toggleButtons.add(button);
                this.getChildren().add(button);
            }
        } else if (filter instanceof SingleChoiceFilter singleChoiceFilter) {
            ChoiceBox<String> choiceBox = new ChoiceBox<>();
            choiceBox.getItems().addAll(singleChoiceFilter.getSelectableValues());
            choiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    singleChoiceFilter.setValue(newValue);
                }
            });
            this.getChildren().add(choiceBox);
        }

        checkBox.setPadding(new javafx.geometry.Insets(5, 5, 0, 0));
        this.setSpacing(5);
    }
}
