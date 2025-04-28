package at.variabilityanalysisgui.view;

import at.variabilityanalysisgui.controller.Controller;
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

        setupDragAndDrop();
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

    private void setupDragAndDrop() {
        setOnDragDetected(event -> {
            if (getItem() != null && getItem().getType() == FeatureTreeNode.DataType.ELEMENT) {
                Dragboard db = startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(getItem().getDisplayName());
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
