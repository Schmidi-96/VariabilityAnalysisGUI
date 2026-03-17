/***
 
    This Source Code Form is subject to the terms of the Mozilla
    Public License, v. 2.0. If a copy of the MPL was not distributed
    with this file, You can obtain one at
    https://mozilla.org/MPL/2.0/.*
    Contributors:
    Michael Schmidhammer
**/

package at.variabilityanalysisgui.controller;

import at.variabilityanalysisgui.model.Difference;
import at.variabilityanalysisgui.model.Element;
import at.variabilityanalysisgui.model.Group;
import at.variabilityanalysisgui.view.DifferenceDirectory;
import at.variabilityanalysisgui.view.FeatureTreeNode;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.stream.Collectors;

public class DetailsController {

    Controller controller;
    private final ListView<Element> detailSubElementListView;
    private final ScrollPane detailScrollPane;
    private final HBox detailsNameHBox;
    private final Label detailLocationLabel;
    private final TextArea detailLocationTextArea;
    private final TextField detailGroupNameTextField;
    private final Label detailOccurrenceLabel;
    private final ListView<String> detailOccurrencesListView;
    private final Label detailElementLabel;
    private final TextArea detailElementData;
    private final Label detailSubElementLabel;

    private TreeItem<FeatureTreeNode> currentDetailItem = null;

    public DetailsController(Controller controller, ListView<Element> detailSubElementListView, ScrollPane detailScrollPane,
                             HBox detailsNameHBox, Label detailLocationLabel, TextArea detailLocationTextArea,
                             TextField detailGroupNameTextField, Label detailOccurrenceLabel, ListView<String> detailOccurrencesListView,
                             Label detailElementLabel, TextArea detailElementData, Label detailSubElementLabel, Button detailCloseButton) {
        this.controller = controller;
        this.detailSubElementListView = detailSubElementListView;
        this.detailScrollPane = detailScrollPane;
        this.detailsNameHBox = detailsNameHBox;
        this.detailLocationLabel = detailLocationLabel;
        this.detailLocationTextArea = detailLocationTextArea;
        this.detailGroupNameTextField = detailGroupNameTextField;
        this.detailOccurrenceLabel = detailOccurrenceLabel;
        this.detailOccurrencesListView = detailOccurrencesListView;
        this.detailElementLabel = detailElementLabel;
        this.detailElementData = detailElementData;
        this.detailSubElementLabel = detailSubElementLabel;

        // Configure the optional detail elements list view display
        detailSubElementListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Element item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox vbox = new VBox(2);
                    Label label = new Label(item.getLocation());
                    label.setStyle("-fx-font-weight: bold;");
                    vbox.getChildren().add(label);
                    if (item.getDescription() != null && !item.getDescription().isEmpty()) {
                        Label descLabel = new Label(item.getDescription());
                        descLabel.setWrapText(true);
                        vbox.getChildren().add(descLabel);
                    }
                    setGraphic(vbox);
                    setText(null);
                }
            }
        });
        detailCloseButton.setOnAction(event -> hideDetailsPane());
    }

    // Detail Pane
    public void showDetailsPane(Difference diff, TreeItem<FeatureTreeNode> item) {
        currentDetailItem = item;
        if (diff instanceof Group) {
            showDetailsPaneGroup((Group) diff);
        } else if (diff instanceof Element) {
            showDetailsPaneElement((Element) diff);
        } else if (diff instanceof DifferenceDirectory) {
            showDetailsPaneDifferenceDirectory((DifferenceDirectory) diff);
        } else {
            System.out.println("No details available for this item.");
            hideDetailsPane();
        }
    }

    private void showDetailsPaneDifferenceDirectory(DifferenceDirectory dir) {

        setNodeVisibility(true, detailLocationLabel, detailLocationTextArea, detailSubElementLabel, detailSubElementListView, detailScrollPane);
        setNodeVisibility(false, detailsNameHBox, detailOccurrenceLabel, detailOccurrencesListView, detailElementLabel, detailElementData);

        detailElementData.setEditable(false);
        detailLocationTextArea.setText(dir.getPath());
        detailSubElementListView.setItems(currentDetailItem.getChildren().stream().map(i -> (Element) i.getValue().getData()).collect(Collectors.toCollection(FXCollections::observableArrayList)));

    }

    private void showDetailsPaneGroup(Group group) {
        detailGroupNameTextField.setText(group.getName().get());
        group.getName().bind(detailGroupNameTextField.textProperty());
        detailSubElementListView.setItems(FXCollections.observableArrayList(group.getElements()));
        detailOccurrencesListView.setItems(FXCollections.observableArrayList(group.getOccurrences()));

        setNodeVisibility(true, detailOccurrenceLabel, detailOccurrencesListView, detailSubElementLabel, detailSubElementListView, detailScrollPane, detailsNameHBox);
        setNodeVisibility(false, detailLocationLabel, detailLocationTextArea, detailElementLabel, detailElementData);

        detailGroupNameTextField.setEditable(true);
        detailElementData.setEditable(false);
    }

    private void showDetailsPaneElement(Element element) {
        detailGroupNameTextField.setText(element.getName().get());
        element.getName().bind(detailGroupNameTextField.textProperty());
        detailElementData.setText(element.getDescription());
        detailSubElementListView.setItems(FXCollections.observableArrayList(element));
        detailLocationTextArea.setText(element.getLocation());

        setNodeVisibility(false, detailOccurrenceLabel, detailOccurrencesListView);
        setNodeVisibility(true, detailsNameHBox, detailLocationLabel, detailLocationTextArea, detailElementLabel, detailElementData, detailScrollPane);
        setNodeVisibility(currentDetailItem.getValue().isDirectory(), detailSubElementLabel, detailSubElementListView);

        detailGroupNameTextField.setEditable(false);
        detailLocationTextArea.setEditable(false);
        detailElementData.setEditable(false);

    }

    private void setNodeVisibility(boolean visible, Node... node) {
        for (Node n : node) {
            if (n != null) {
                n.setVisible(visible);
                n.setManaged(visible);
            }
        }
    }

    public void hideDetailsPane() {
        detailScrollPane.setVisible(false);
        detailScrollPane.setManaged(false);
        currentDetailItem = null;
        detailOccurrencesListView.setItems(FXCollections.emptyObservableList());
        detailSubElementListView.setItems(FXCollections.emptyObservableList());
    }
}
