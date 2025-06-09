package at.variabilityanalysisgui.controller;

import at.variabilityanalysisgui.model.Difference;
import at.variabilityanalysisgui.model.Element;
import at.variabilityanalysisgui.model.Group;
import at.variabilityanalysisgui.view.DifferenceDirectory;
import at.variabilityanalysisgui.view.FeatureTreeNode;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.stream.Collectors;

public class DetailsController {

    Controller controller;
    private ListView<Element> detailSubElementListView;
    private ScrollPane detailScrollPane;
    private VBox detailsPane;
    private HBox detailsNameHBox;
    private Label detailLocationLabel;
    private TextArea detailLocationTextArea;
    private Label detailGroupNameLabel;
    private TextField detailGroupNameTextField;
    private Label detailOccurrenceLabel;
    private ListView<String> detailOccurrencesListView;
    private Label detailElementLabel;
    private TextArea detailElementData;
    private Label detailSubElementLabel;
    private Button detailCloseButton;

    private TreeItem<FeatureTreeNode> currentDetailItem = null;

    public DetailsController(Controller controller, ListView<Element> detailSubElementListView, ScrollPane detailScrollPane, VBox detailsPane, HBox detailsNameHBox, Label detailLocationLabel, TextArea detailLocationTextArea, Label detailGroupNameLabel, TextField detailGroupNameTextField, Label detailOccurrenceLabel, ListView<String> detailOccurrencesListView, Label detailElementLabel, TextArea detailElementData, Label detailSubElementLabel, Button detailCloseButton) {
        this.controller = controller;
        this.detailSubElementListView = detailSubElementListView;
        this.detailScrollPane = detailScrollPane;
        this.detailsPane = detailsPane;
        this.detailsNameHBox = detailsNameHBox;
        this.detailLocationLabel = detailLocationLabel;
        this.detailLocationTextArea = detailLocationTextArea;
        this.detailGroupNameLabel = detailGroupNameLabel;
        this.detailGroupNameTextField = detailGroupNameTextField;
        this.detailOccurrenceLabel = detailOccurrenceLabel;
        this.detailOccurrencesListView = detailOccurrencesListView;
        this.detailElementLabel = detailElementLabel;
        this.detailElementData = detailElementData;
        this.detailSubElementLabel = detailSubElementLabel;
        this.detailCloseButton = detailCloseButton;

        // Configure the optional detail elements list view display
        detailSubElementListView.setCellFactory(lv -> new ListCell<Element>() {
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
        if (diff instanceof Group) {
            showDetailsPaneGroup((Group) diff, item);
        } else if (diff instanceof Element) {
            showDetailsPaneElement((Element) diff, item);
        } else if (diff instanceof DifferenceDirectory) {
            showDetailsPaneDifferenceDirectory((DifferenceDirectory) diff, item);
        } else {
            System.out.println("No details available for this item.");
            hideDetailsPane();
        }
    }

    private void showDetailsPaneDifferenceDirectory(DifferenceDirectory dir, TreeItem<FeatureTreeNode> item) {
        currentDetailItem = item;
        detailsNameHBox.setVisible(false);
        detailsNameHBox.setManaged(false);
        detailLocationLabel.setVisible(true);
        detailLocationLabel.setManaged(true);
        detailLocationTextArea.setText(dir.getPath());
        detailLocationTextArea.setVisible(true);
        detailLocationTextArea.setManaged(true);
        detailOccurrenceLabel.setVisible(false);
        detailOccurrenceLabel.setManaged(false);
        detailOccurrencesListView.setVisible(false);
        detailOccurrencesListView.setManaged(false);
        detailSubElementListView.setItems(item.getChildren().stream().map(i -> (Element) i.getValue().getData()).collect(Collectors.toCollection(FXCollections::observableArrayList)));

        detailElementLabel.setVisible(false);
        detailElementLabel.setManaged(false);
        detailElementData.setVisible(false);
        detailElementData.setManaged(false);
        detailElementData.setEditable(false);

        detailSubElementLabel.setVisible(true);
        detailSubElementLabel.setManaged(true);
        detailSubElementListView.setVisible(true);
        detailSubElementListView.setManaged(true);

        detailScrollPane.setVisible(true);
        detailScrollPane.setManaged(true);
    }

    private void showDetailsPaneGroup(Group group, TreeItem<FeatureTreeNode> item) {
        currentDetailItem = item;
        detailsNameHBox.setVisible(true);
        detailsNameHBox.setManaged(true);
        detailGroupNameTextField.setText(group.getName().get());
        group.getName().bind(detailGroupNameTextField.textProperty());
        detailSubElementListView.setItems(FXCollections.observableArrayList(group.getElements()));
        detailOccurrencesListView.setItems(FXCollections.observableArrayList(group.getOccurrences()));
        detailGroupNameTextField.setEditable(true);

        detailLocationLabel.setVisible(true);
        detailLocationLabel.setManaged(true);
        detailLocationTextArea.setVisible(false);
        detailLocationTextArea.setManaged(false);

        detailOccurrenceLabel.setVisible(true);
        detailOccurrenceLabel.setManaged(true);
        detailOccurrencesListView.setVisible(true);
        detailOccurrencesListView.setManaged(true);

        detailElementLabel.setVisible(false);
        detailElementLabel.setManaged(false);
        detailElementData.setVisible(false);
        detailElementData.setManaged(false);
        detailElementData.setEditable(false);

        detailSubElementLabel.setVisible(true);
        detailSubElementLabel.setManaged(true);
        detailSubElementListView.setVisible(true);
        detailSubElementListView.setManaged(true);

        detailScrollPane.setVisible(true);
        detailScrollPane.setManaged(true);
    }

    private void showDetailsPaneElement(Element element, TreeItem<FeatureTreeNode> item) {
        currentDetailItem = item;
        detailGroupNameTextField.setText(element.getName().get());
        element.getName().bind(detailGroupNameTextField.textProperty());
        detailElementData.setText(element.getDescription());
        detailSubElementListView.setItems(FXCollections.observableArrayList(element));
        detailLocationTextArea.setText(element.getLocation());
        detailGroupNameTextField.setEditable(true);

        detailsNameHBox.setVisible(true);
        detailsNameHBox.setManaged(true);

        detailLocationLabel.setVisible(true);
        detailLocationLabel.setManaged(true);
        detailLocationTextArea.setVisible(true);
        detailLocationTextArea.setManaged(true);
        detailLocationTextArea.setEditable(false);

        detailOccurrenceLabel.setVisible(false);
        detailOccurrenceLabel.setManaged(false);
        detailOccurrencesListView.setVisible(false);
        detailOccurrencesListView.setManaged(false);

        detailElementLabel.setVisible(true);
        detailElementLabel.setManaged(true);
        detailElementData.setVisible(true);
        detailElementData.setManaged(true);
        detailElementData.setEditable(false);

        detailSubElementLabel.setVisible(item.getValue().isDirectory());
        detailSubElementLabel.setManaged(item.getValue().isDirectory());
        detailSubElementListView.setVisible(item.getValue().isDirectory());
        detailSubElementListView.setManaged(item.getValue().isDirectory());

        detailScrollPane.setVisible(true);
        detailScrollPane.setManaged(true);
    }

    @FXML
    private void handleCloseDetails() {
        hideDetailsPane();
    }

    public void hideDetailsPane() {
        detailScrollPane.setVisible(false);
        detailScrollPane.setManaged(false);
        currentDetailItem = null;
        detailOccurrencesListView.setItems(FXCollections.emptyObservableList());
        detailSubElementListView.setItems(FXCollections.emptyObservableList());
    }
}
