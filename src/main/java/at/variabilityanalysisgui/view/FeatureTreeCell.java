package at.variabilityanalysisgui.view;

import at.variabilityanalysisgui.controller.Controller;
import at.variabilityanalysisgui.model.Element;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class FeatureTreeCell extends TreeCell<FeatureTreeNode> {

    private final HBox hbox;
    private final Label label = new Label();
    private final Button editButton = new Button("Edit");
    private final Button deleteButton = new Button("X");
    private final Button moreButton = new Button("...");

    private final Controller controller;

    public FeatureTreeCell(Controller controller) {
        this.controller = controller;

        // Buttons
        editButton.setStyle("-fx-padding: 2 5 2 5;");
        deleteButton.setStyle("-fx-padding: 2 5 2 5;");
        moreButton.setStyle("-fx-padding: 2 5 2 5;");
        deleteButton.setTooltip(new Tooltip("Remove this group/element"));
        editButton.setTooltip(new Tooltip("Edit group"));
        moreButton.setTooltip(new Tooltip("More options / Show details"));

        // Layout
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        hbox = new HBox(5);
        hbox.setAlignment(Pos.CENTER_LEFT);

        // Action handlers
        editButton.setOnAction(event -> {
            if (getItem() != null && getItem().getType() == FeatureTreeNode.DataType.GROUP) {
                controller.handleEditAction(getTreeItem());
            }
        });

        deleteButton.setOnAction(event -> {
            if (getItem() != null) {
                controller.handleDeleteAction(getTreeItem());
            }
        });

        moreButton.setOnAction(event -> {
            if (getItem() != null) {
                controller.handleMoreAction(getTreeItem(), moreButton);
            }
        });
    }

    @Override
    protected void updateItem(FeatureTreeNode item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            label.setText(item.getDisplayName());
            hbox.getChildren().setAll(label);

            if (item.getData() != null) {
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                hbox.getChildren().addAll(spacer);
                hbox.getChildren().addAll(deleteButton, moreButton);
            }

            setGraphic(hbox);
            setText(null);
        }
    }
}
