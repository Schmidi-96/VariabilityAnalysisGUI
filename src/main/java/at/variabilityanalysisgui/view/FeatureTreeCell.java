/***
 The MIT License (MIT)

 Copyright (c) 2025 Michael Schmidhammer
 */

package at.variabilityanalysisgui.view;

import at.variabilityanalysisgui.controller.TreeViewController;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class FeatureTreeCell extends TreeCell<FeatureTreeNode> {

    private final HBox hbox;
    private final Label nameLabel = new Label();
    private final Label pathLabel = new Label();
    private final Label typeLabel = new Label();
    private final Button deleteButton = new Button("X");

    private final TreeViewController controller;

    public FeatureTreeCell(TreeViewController controller) {
        this.controller = controller;

        // Buttons
        deleteButton.setStyle("-fx-padding: 2 5 2 5;");
        deleteButton.setTooltip(new Tooltip("Remove this group/element"));

        // Layout
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        hbox = new HBox(5);
        hbox.setAlignment(Pos.CENTER_LEFT);

        deleteButton.setOnAction(event -> {
            if (getItem() != null) {
                controller.handleDeleteAction(getTreeItem());
            }
        });

        setupDragAndDrop();
    }

    @Override
    protected void updateItem(FeatureTreeNode item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            nameLabel.setText(item.getDisplayName());
            pathLabel.setText(item.getPath());
            pathLabel.setStyle("-fx-text-fill: #A0A0A0; -fx-font-size: 10px;");
            if (item.getType() == FeatureTreeNode.DataType.ELEMENT) {
                typeLabel.setText("E");
                typeLabel.setStyle("-fx-text-fill: #00AA00;");
            } else if (item.getType() == FeatureTreeNode.DataType.CONTAINER) {
                typeLabel.setText("D");
                typeLabel.setStyle("-fx-text-fill: #0000FF;");
            } else if (item.getType() == FeatureTreeNode.DataType.GROUP) {
                typeLabel.setText("G");
                typeLabel.setStyle("-fx-text-fill: #A0A0A0;");
            }
            hbox.getChildren().setAll(nameLabel, typeLabel, pathLabel);

            if (item.getData() != null) {
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                hbox.getChildren().add(spacer);
                hbox.getChildren().add(deleteButton);
                pathLabel.setMinWidth(0);
                hbox.setMinWidth(0);
                hbox.setPrefWidth(100);
            }

            setGraphic(hbox);
            setText(null);
        }
    }

    private void setupDragAndDrop() {
        setOnDragDetected(event -> {
            if (getItem() != null && getItem().getType() == FeatureTreeNode.DataType.ELEMENT) {
                Dragboard db = startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(getItem().getTechnicalName());
                db.setContent(content);
                controller.setDraggedItem(getTreeItem());
                event.consume();
            }
        });

        setOnDragOver(event -> {
            TreeItem<FeatureTreeNode> targetItem = getTreeItem();
            TreeItem<FeatureTreeNode> sourceItem = controller.getDraggedItem();


            boolean canDrop = false;
            if (targetItem != null && targetItem.getValue() != null && sourceItem != null && sourceItem.getValue() != null) {
                FeatureTreeNode targetNode = targetItem.getValue();
                FeatureTreeNode sourceNode = sourceItem.getValue();

                if (sourceNode.getType() == FeatureTreeNode.DataType.ELEMENT &&
                        (targetNode.getType() == FeatureTreeNode.DataType.GROUP || targetNode.getType() == FeatureTreeNode.DataType.ELEMENT)) {
                    canDrop = true;
                }

                if (targetItem == sourceItem || targetItem == sourceItem.getParent() // Prevent dropping onto self or parent
                        ||  sourceNode.getType() == FeatureTreeNode.DataType.ELEMENT && // Prevent dropping element into the same group it's already in (directly)
                            targetNode.getType() == FeatureTreeNode.DataType.GROUP &&
                            sourceItem.getParent() == targetItem) {
                    canDrop = false;
                }
            }

            if (canDrop) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        setOnDragDropped(event -> {
            if (controller.getDraggedItem() != null) {
                TreeItem<FeatureTreeNode> sourceItem = controller.getDraggedItem();
                TreeItem<FeatureTreeNode> targetItem = getTreeItem();
                if (sourceItem.getValue().getType() == FeatureTreeNode.DataType.ELEMENT
                        && (targetItem.getValue().getType() == FeatureTreeNode.DataType.GROUP || targetItem.getValue().getType() == FeatureTreeNode.DataType.ELEMENT)) {
                    controller.moveElementToGroup(sourceItem, targetItem);
                }
                event.setDropCompleted(true);
                event.consume();
            }
        });
    }
}
