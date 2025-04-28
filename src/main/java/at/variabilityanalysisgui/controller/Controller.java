package at.variabilityanalysisgui.controller;

import at.variabilityanalysisgui.model.Difference;
import at.variabilityanalysisgui.model.Group;
import at.variabilityanalysisgui.view.DifferenceDirectory;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import javafx.collections.FXCollections;

import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.application.Platform;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import javafx.scene.Node;
import javafx.stage.Window;
import at.variabilityanalysisgui.model.Element;
import at.variabilityanalysisgui.view.FeatureTreeCell;
import at.variabilityanalysisgui.view.FeatureTreeNode;
import at.variabilityanalysisgui.parser.InputParser;


import java.util.stream.Collectors;

import static at.variabilityanalysisgui.model.Element.ElementType.IEC61499;
import static at.variabilityanalysisgui.model.Element.ElementType.JAVA;

public class Controller {

    @FXML
    private TreeView<FeatureTreeNode> featureTreeView;
    @FXML
    private TextField searchTextField;
    @FXML
    private Button filterButton;
    @FXML
    private Button upButton;
    @FXML
    private Button confirmButton;
    @FXML
    private Button downButton;
    @FXML
    private MenuItem saveDecisionsMenuItem;

    // Detail Pane
    @FXML
    private ScrollPane detailScrollPane;
    @FXML
    private VBox detailsPane;
    @FXML
    private HBox detailsNameHBox;
    @FXML
    private Label detailLocationLabel;
    @FXML
    private TextArea detailLocationTextArea;
    @FXML
    private Label detailGroupNameLabel;
    @FXML
    private TextField detailGroupNameTextField;
    @FXML
    private Label detailOccurrenceLabel;
    @FXML
    private ListView<String> detailOccurrencesListView;
    @FXML
    private ListView<Element> detailElementsListView;


    private InputParser parser = new InputParser();
    private List<Group> originalGroups;
    private TreeItem<FeatureTreeNode> rootNode;

    private TreeItem<FeatureTreeNode> currentDetailItem = null;

    private TreeItem<FeatureTreeNode> draggedItem = null;

    private int hierarchyLevel = 0;



    private UndoManager undoManager = new UndoManager();


    @FXML
    public void initialize() {
        // Setup TreeView with custom cell factory
        featureTreeView.setCellFactory(tv -> new FeatureTreeCell(this)); // Pass controller reference

        // Create an invisible root item
        rootNode = new TreeItem<>(new FeatureTreeNode(new Difference())); // Dummy root node
        featureTreeView.setRoot(rootNode);
        featureTreeView.setShowRoot(false);

        // selection model
        featureTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != null) {
                oldValue.setValue(oldValue.getValue()); //refresh FeatureTreeCell
                oldValue.getValue().getData().getName().unbind();
            }
            if (newValue != null && newValue.getValue() != null) {
                FeatureTreeNode node = newValue.getValue();
                showDetailsPane(node.getData(), newValue);
            }
        });

        // Listener for search/filter
        searchTextField.textProperty().addListener((obs, oldVal, newVal) -> filterTreeView(newVal));

        // Configure the optional detail elements list view display
        detailElementsListView.setCellFactory(lv -> new ListCell<Element>() {
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
    }

    private void populateTreeView(List<Group> groups) {
        rootNode.getChildren().clear();

        if (groups == null) return;

        for (Group group : groups) {
            populateGroup(group, null);
        }
        featureTreeView.getSelectionModel().clearSelection();
    }

    private TreeItem<FeatureTreeNode> populateGroup(Group group, Integer index) {
        FeatureTreeNode groupNodeData = new FeatureTreeNode(group);
        TreeItem<FeatureTreeNode> groupItem = new TreeItem<>(groupNodeData);
        groupItem.setExpanded(false);

        organizeHierarchy(groupItem, groupNodeData);

        // Add elements as children
        if (group.getElements() != null) {
            for (Element element : group.getElements()) {
                FeatureTreeNode elementNodeData = new FeatureTreeNode(element);
                TreeItem<FeatureTreeNode> elementItem = new TreeItem<>(elementNodeData);
                ObservableList<TreeItem<FeatureTreeNode>> children = groupItem.getChildren();
                TreeItem<FeatureTreeNode> parent = null;

                while (true) {
                    Optional<TreeItem<FeatureTreeNode>> c = children.stream()
                            .filter(child -> child.getValue().getType() == FeatureTreeNode.DataType.CONTAINER)
                            .filter(child -> ((Element) elementItem.getValue().getData()).getLocation().startsWith(((DifferenceDirectory) child.getValue().getData()).getPath()))
                            .findFirst();
                    if (c.isPresent()){
                        children = c.get().getChildren();
                        parent = c.get();
                    } else {
                        break;
                    }
                }
                if (parent != null) {
                    int i = hierarchyLevel;
                    while (i > 0 && parent.getParent() != null && parent.getParent().getValue().getType() == FeatureTreeNode.DataType.CONTAINER) {
                        children = parent.getParent().getChildren();
                        TreeItem<FeatureTreeNode> oldParent = parent;
                        parent = parent.getParent();
                        children.remove(oldParent);
                        i--;
                    }
                }
                children.add(elementItem);
            }
        }
        collapseTillMultipleElements(groupItem, hierarchyLevel);
        if (index != null) {
            rootNode.getChildren().add(index, groupItem);
        } else {
            rootNode.getChildren().add(groupItem);
        }
        return groupItem;
    }

    private int collapseTillMultipleElements(TreeItem<FeatureTreeNode> groupItem, int depthOffset) {
        if (groupItem.getChildren().size() == 1 && groupItem.getChildren().get(0).getValue().getType() == FeatureTreeNode.DataType.CONTAINER) {
            TreeItem<FeatureTreeNode> oldChild = groupItem.getChildren().get(0);
            int offset = collapseTillMultipleElements(oldChild, depthOffset);
            if (offset > 0) {
                ObservableList<TreeItem<FeatureTreeNode>> newChildren = oldChild.getChildren();
                groupItem.getChildren().clear();
                for (TreeItem<FeatureTreeNode> childItem : newChildren) {
                    groupItem.getChildren().add(childItem);
                }
            } else {
                offset += 1;
            }
            return offset;
        } else {
            return depthOffset + 1;
        }
    }


    private void organizeHierarchy(TreeItem<FeatureTreeNode> groupItem, FeatureTreeNode groupNodeData) {
        HashMap<Element.ElementType, String> hierarchyMap = new HashMap<>();
        hierarchyMap.put(JAVA, "/");
        hierarchyMap.put(IEC61499, ";");
        List<Element> elements = ((Group) groupNodeData.getData()).getElements();
        for (Element element : elements) {
            String[] pathParts = element.getLocation().split(hierarchyMap.get(element.getType()));
            ObservableList<TreeItem<FeatureTreeNode>> children = groupItem.getChildren();
            for (int i = 0; i < pathParts.length; i++) {
                // Check if the path part already exists in the children
                int fi = i;
                Optional<TreeItem<FeatureTreeNode>> existingChild = children.stream()
                        .filter(child -> child.getValue().getDisplayName().equals(pathParts[fi]))
                        .findFirst();
                if (existingChild.isPresent()) {
                    children = existingChild.get().getChildren();
                } else {
                    DifferenceDirectory con = new DifferenceDirectory(String.join(hierarchyMap.get(element.getType()), Arrays.asList(pathParts).subList(0, i + 1)), hierarchyMap.get(element.getType()));
                    TreeItem<FeatureTreeNode> conTreeItem = new TreeItem<>(new FeatureTreeNode(con));
                    children.add(conTreeItem);
                    children = conTreeItem.getChildren();
                }
            }
        }
    }

    private void filterTreeView(String filterText) {
        rootNode.getChildren().clear();

        if (originalGroups == null) return;

        String lowerCaseFilter;
        if (filterText == null) {
            lowerCaseFilter = "";
        } else {
            lowerCaseFilter = filterText.toLowerCase().trim();
        }

        if (lowerCaseFilter.isEmpty()) {
            populateTreeView(originalGroups);
            return;
        }

        List<Group> filtered = originalGroups.stream()
                .filter(group -> group.getName().get().toLowerCase().contains(lowerCaseFilter)
                )
                .collect(Collectors.toList());

        populateTreeView(filtered);
    }


    // Actions for Buttons

    public void handleEditAction(TreeItem<FeatureTreeNode> item) {
        System.out.println("Handle Edit Action Triggered - Not Implemented");
    }

    public void handleDeleteAction(TreeItem<FeatureTreeNode> item) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Remove '" + item.getValue().getDisplayName() + "'?");
        alert.setContentText("Are you sure you want to remove this item from the view?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (item.getValue().getType() == FeatureTreeNode.DataType.ELEMENT) {
                Element element = (Element) item.getValue().getData();
                Group group = findGroupContainingElement(element);
                if (group != null) {
                    group.getElements().remove(element);
                }
            }
            deleteItem(item);
        }
    }

    public void handleMoreAction(TreeItem<FeatureTreeNode> item, Node anchorNode) {
        System.out.println("More Action Triggered - Not Implemented");
    }

    // Detail Pane
    private void showDetailsPane(Difference diff, TreeItem<FeatureTreeNode> item) {
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
        detailElementsListView.setItems(item.getChildren().stream().map(i -> (Element) i.getValue().getData()).collect(Collectors.toCollection(FXCollections::observableArrayList)));
        detailScrollPane.setVisible(true);
        detailScrollPane.setManaged(true);
    }

    private void showDetailsPaneGroup(Group group, TreeItem<FeatureTreeNode> item) {
        currentDetailItem = item;
        detailsNameHBox.setVisible(true);
        detailsNameHBox.setManaged(true);
        detailGroupNameTextField.setText(group.getName().get());
        group.getName().bind(detailGroupNameTextField.textProperty());
        detailElementsListView.setItems(FXCollections.observableArrayList(group.getElements()));

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
        detailScrollPane.setVisible(true);
        detailScrollPane.setManaged(true);
    }

    private void showDetailsPaneElement(Element element, TreeItem<FeatureTreeNode> item) {
        currentDetailItem = item;
        detailsNameHBox.setVisible(true);
        detailsNameHBox.setManaged(true);
        detailGroupNameTextField.setText(element.getName().get());
        element.getName().bind(detailGroupNameTextField.textProperty());
        detailElementsListView.setItems(FXCollections.observableArrayList(element));

        detailLocationTextArea.setText(element.getLocation());
        detailGroupNameTextField.setEditable(true);
        detailLocationLabel.setVisible(true);
        detailLocationLabel.setManaged(true);
        detailLocationTextArea.setEditable(false);
        detailLocationTextArea.setVisible(true);
        detailLocationTextArea.setManaged(true);
        detailOccurrenceLabel.setVisible(false);
        detailOccurrenceLabel.setManaged(false);
        detailOccurrencesListView.setVisible(false);
        detailOccurrencesListView.setManaged(false);
        detailScrollPane.setVisible(true);
        detailScrollPane.setManaged(true);
    }

    @FXML
    private void handleCloseDetails() {
        hideDetailsPane();
    }

    private void hideDetailsPane() {
        detailScrollPane.setVisible(false);
        detailScrollPane.setManaged(false);
        currentDetailItem = null;
        detailOccurrencesListView.setItems(FXCollections.emptyObservableList());
        detailElementsListView.setItems(FXCollections.emptyObservableList());
    }


    // Actions for Menu Buttons
    @FXML
    private void handleHierarchyUpAction() {
        System.out.println("Hierarchie Up Action Triggered - Not Implemented");
        hierarchyLevel--;
        populateTreeView(originalGroups);
    }

    @FXML
    private void handleConfirmAction() {
        System.out.println("Confirm Action Triggered - Not Implemented");
    }

    @FXML
    private void handleHierarchyDownAction() {
        System.out.println("Hierarchie Down Action Triggered - Not Implemented");
        hierarchyLevel++;
        populateTreeView(originalGroups);
    }

    @FXML
    private void handleFilterAction() {
        System.out.println("Filter Action Triggered - Not Implemented");
    }

    @FXML
    private void handleLoadAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Difference Report File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        fileChooser.setInitialDirectory(new File("./"));
        File selectedFile = fileChooser.showOpenDialog(getWindow());

        if (selectedFile != null) {
            try {
                originalGroups = parser.parse(selectedFile.getAbsolutePath());
                populateTreeView(originalGroups); // Populate with parsed data
                saveDecisionsMenuItem.setDisable(false);
                searchTextField.clear();
                hideDetailsPane(); // Hide details when new file loaded
            } catch (IOException e) {
                showErrorDialog("Error Parsing File", "Could not read or parse the file:\n" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleSaveAction() {
        System.out.println("Save Action Triggered - Not Implemented");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Differences");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        fileChooser.setInitialFileName("saved_differences.txt");
        fileChooser.setInitialDirectory(new File("./"));
        File file = fileChooser.showSaveDialog(getWindow());

        if (file == null) {
            System.out.println("Saving cancelled by user.");
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Group group : originalGroups) {
                // Group Header
                String groupName = group.getName().get();
                writer.write(groupName + ":");
                writer.newLine();

                // Occurrences
                if (group.getOccurrences() != null && !group.getOccurrences().isEmpty()) {
                    if (group.getElements().size() > 1 && group.getElements().get(0).getType() == JAVA) {
                        writer.write("Occurrence:");
                    } else {
                        writer.write("Variants:");
                    }
                    writer.newLine();
                    for (String occurrence : group.getOccurrences()) {
                        writer.write(occurrence);
                        writer.newLine();
                    }
                }

                // Elements
                if (!group.getElements().isEmpty()) {
                    writer.write("Elements:");
                    writer.newLine();
                    for (Element element : group.getElements()) {

                        if (element.getType() == Element.ElementType.JAVA) {
                            writer.write("(" + element.getLocation() + ")");
                            writer.newLine();

                            writer.write(element.getDescription());
                            writer.newLine();

                        } else if (element.getType() == Element.ElementType.IEC61499) {
                            writer.write(element.getDescription());
                            writer.newLine();
                        }
                    }
                }
                writer.newLine();
            }

        } catch (IOException e) {
            showErrorDialog("Error Saving File", "Could not save the file:\n" + e.getMessage());
            e.printStackTrace();
        }
    }


    @FXML
    private void handleExit() {
        Platform.exit();
    }

    // Main window
    private Window getWindow() {
        return featureTreeView.getScene().getWindow();
    }

    // Error dialogs
    private void showErrorDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initOwner(getWindow());
        alert.showAndWait();
    }

    public TreeItem<FeatureTreeNode> getDraggedItem() {
        return draggedItem;
    }

    public void setDraggedItem(TreeItem<FeatureTreeNode> draggedItem) {
        this.draggedItem = draggedItem;
    }

    public void moveElementToGroup(TreeItem<FeatureTreeNode> elementItem, TreeItem<FeatureTreeNode> targetGroupItem) {
        if (elementItem == null || targetGroupItem == null ||
                elementItem.getValue().getType() != FeatureTreeNode.DataType.ELEMENT ||
                targetGroupItem.getValue().getType() == FeatureTreeNode.DataType.CONTAINER) {
            System.err.println("Invalid types for moveElementToGroup");
            return;
        }

        while(targetGroupItem.getValue().getType() != FeatureTreeNode.DataType.GROUP) {
            targetGroupItem = targetGroupItem.getParent();
        }

        Element element = (Element) elementItem.getValue().getData();
        Group targetGroup = (Group) targetGroupItem.getValue().getData();

        Group sourceGroup = findGroupContainingElement(element);

        // Remove from old group's element list
        boolean removed = sourceGroup.getElements().remove(element);
        if (!removed) {
            System.err.println("Failed to remove element from source group's list in model.");
        } else {
            // Add to new group's element list
            targetGroup.getElements().add(element);

            // Add new group TreeItem
            int index = rootNode.getChildren().indexOf(targetGroupItem);
            rootNode.getChildren().remove(index);
            targetGroupItem = populateGroup(targetGroup, index);
            targetGroupItem.setExpanded(true);
            // remove old group TreeItem
            deleteItem(elementItem);

            System.out.println("Moved element '" + element.getName().get());
        }
    }

    private Group findGroupContainingElement(Element element) {
        if (originalGroups == null) return null;
        for (Group group : originalGroups) {
            if (group.getElements().contains(element)) {
                return group;
            }
        }
        return null;
    }

    private void deleteItem(TreeItem<FeatureTreeNode> item) {
        if (item != null) {
            TreeItem<FeatureTreeNode> parent = item.getParent();
            if (parent != null) {
                parent.getChildren().remove(item);
                System.out.println("Removed item: " + item.getValue().getDisplayName());
                removeEmptyContainers(parent);
            }
        }
    }

    private void removeEmptyContainers(TreeItem<FeatureTreeNode> item) {
        if (item != null && item.getChildren().isEmpty() && item.getValue().getType() == FeatureTreeNode.DataType.CONTAINER) {
            item.getParent().getChildren().remove(item);
            removeEmptyContainers(item.getParent());
        }
    }
}