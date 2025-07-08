/***
 The MIT License (MIT)

 Copyright (c) 2025 Michael Schmidhammer
 */

package at.variabilityanalysisgui.view;

import at.variabilityanalysisgui.controller.Filter.Filter;
import at.variabilityanalysisgui.controller.Filter.MultipleChoiceFilter;
import at.variabilityanalysisgui.controller.Filter.SingleChoiceFilter;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.controlsfx.control.CheckComboBox;

public class FilterItem extends HBox {
    CheckBox checkBox;

    public FilterItem(Filter filter) {
        this.checkBox = new CheckBox(filter.getName());
        filter.enabledProperty().bindBidirectional(checkBox.selectedProperty());
        this.checkBox.setWrapText(true);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        this.getChildren().add(checkBox);
        this.getChildren().add(spacer);

        if (filter instanceof MultipleChoiceFilter multipleChoiceFilter) {
            CheckComboBox<String> checkComboBox = new CheckComboBox<>();
            checkComboBox.getItems().addAll(multipleChoiceFilter.getSelectableValues());
            checkComboBox.setMinWidth(100);
            checkComboBox.setPrefWidth(100);
            checkComboBox.setMaxWidth(100);
            checkComboBox.getCheckModel().getCheckedItems().addListener((ListChangeListener<String>) c -> {
                while (c.next()) {
                    if (c.wasAdded()) {
                        for (String item : c.getAddedSubList()) {
                            multipleChoiceFilter.addSelectedValue(item);
                        }
                    }
                    if (c.wasRemoved()) {
                        for (String item : c.getRemoved()) {
                            multipleChoiceFilter.removeSelectedValue(item);
                        }
                    }
                }
            });
            this.getChildren().add(checkComboBox);

        } else if (filter instanceof SingleChoiceFilter singleChoiceFilter) {
            ChoiceBox<String> choiceBox = new ChoiceBox<>();
            choiceBox.setMinWidth(100);
            choiceBox.setPrefWidth(100);
            choiceBox.setMaxWidth(100);
            choiceBox.getItems().addAll(singleChoiceFilter.getSelectableValues());
            choiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    singleChoiceFilter.setValue(newValue);
                }
            });
            this.getChildren().add(choiceBox);
        }

        this.setPrefWidth(250);
        this.setMaxWidth(250);
        this.setSpacing(5);
        this.setAlignment(Pos.CENTER_LEFT);
        checkBox.setPadding(new javafx.geometry.Insets(5, 5, 5, 5));
    }
}