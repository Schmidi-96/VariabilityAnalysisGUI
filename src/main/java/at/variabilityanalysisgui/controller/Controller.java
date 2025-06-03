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

import static at.variabilityanalysisgui.parser.InputParser.ExtractionType.IEC61499;
import static at.variabilityanalysisgui.parser.InputParser.ExtractionType.JAVA;

public class Controller {

    enum ViewMode {
        TREE, FLAT, JAVAFILE
    }

    public HBox hierarchyButtonHBox;
    @FXML
    private TreeView<FeatureTreeNode> featureTreeView;
    @FXML
    private TextField searchTextField;
    @FXML
    private Button filterButton;
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
    private Label detailElementLabel;
    @FXML
    private TextArea detailElementData;
    @FXML
    private Label detailSubElementLabel;
    @FXML
    private ListView<Element> detailSubElementListView;


    private InputParser parser = new InputParser();
    private List<Group> originalGroups;
    private TreeItem<FeatureTreeNode> rootNode;

    private TreeItem<FeatureTreeNode> currentDetailItem = null;

    private TreeItem<FeatureTreeNode> draggedItem = null;

    Map<InputParser.ExtractionType, String> seperatorMap = Map.of(JAVA, "/", IEC61499, ";");

    private UndoManager undoManager = new UndoManager();

    ViewMode viewMode = ViewMode.FLAT;


    @FXML
    public void initialize() {
        // Setup TreeView with custom cell factory
        featureTreeView.setCellFactory(tv -> new FeatureTreeCell(this)); // Pass controller reference

        // Create an invisible root item
        rootNode = new TreeItem<>(new FeatureTreeNode(new Difference(), false)); // Dummy root node
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
    }

    private void populateTreeView(List<Group> groups, List<Element> visibleElements) {
        Set<FeatureTreeNode> expandedNodes = new HashSet<>();
        for(TreeItem<FeatureTreeNode> groupItem: rootNode.getChildren()) {
            expandedNodes.addAll(getExpandedElement(groupItem));
            System.out.println("Expanded Nodes: " + expandedNodes);
        }
        rootNode.getChildren().clear();

        if (groups == null) return;

        for (Group group : groups) {
            populateGroup(group, visibleElements, null);
            for (FeatureTreeNode node : expandedNodes) {
                TreeItem<FeatureTreeNode> foundItem = findTreeItemByPath(rootNode, node.getPath());
                if (foundItem != null) {
                    foundItem.setExpanded(true);
                }
            }
        }
        featureTreeView.getSelectionModel().clearSelection();
    }

    private void refreshGroup(TreeItem<FeatureTreeNode> groupItem) {
        Group group = (Group) groupItem.getValue().getData();
        int index = rootNode.getChildren().indexOf(groupItem);
        rootNode.getChildren().remove(index);
        Set<FeatureTreeNode> expandedNodes = getExpandedElement(groupItem);
        System.out.println("Expanded Nodes: " + expandedNodes);
        groupItem = populateGroup(group, getFilteredElements(), index);
        for (FeatureTreeNode node : expandedNodes) {
            TreeItem<FeatureTreeNode> foundItem = findTreeItemByPath(groupItem, node.getPath());
            if (foundItem != null) {
                foundItem.setExpanded(true);
            }
        }
        groupItem.setExpanded(true);
    }

    private Set<FeatureTreeNode> getExpandedElement(TreeItem<FeatureTreeNode> groupItem) {
        Set<FeatureTreeNode> expandedElements = new HashSet<>();
        for (TreeItem<FeatureTreeNode> child : groupItem.getChildren()) {
            if (child.getValue().getType() == FeatureTreeNode.DataType.ELEMENT) {
                expandedElements.add(child.getValue());
            }
            expandedElements.addAll(getExpandedElement(child));
        }
        if (groupItem.getValue().getType() == FeatureTreeNode.DataType.GROUP && groupItem.isExpanded()) {
            expandedElements.add(groupItem.getValue());
        }
        return expandedElements;
    }


    private TreeItem<FeatureTreeNode> populateGroup(Group group, List<Element> visibleElements, Integer index) {
        FeatureTreeNode groupNodeData = new FeatureTreeNode(group, false);
        TreeItem<FeatureTreeNode> groupItem = new TreeItem<>(groupNodeData);
        groupItem.setExpanded(false);

        organizeHierarchy(groupItem, groupNodeData);

        // Add elements as children
        if (group.getElements() != null) {
            for (Element element : group.getElements()) {
                if(visibleElements != null && !visibleElements.isEmpty() && !visibleElements.contains(element)) {
                    continue;
                }
                FeatureTreeNode elementNodeData = new FeatureTreeNode(element, false);

                // Check if the element is already in the tree
                TreeItem<FeatureTreeNode> existingItem = findTreeItemByPath(groupItem, element.getLocation() + element.getSeperaterSymbol() + element.getName().get());
                if (existingItem != null && existingItem.getValue().isDirectory()) {
                    elementNodeData.setDirectory(true);
                    existingItem.setValue(elementNodeData);
                } else {
                    TreeItem<FeatureTreeNode> elementItem = new TreeItem<>(elementNodeData);
                    ObservableList<TreeItem<FeatureTreeNode>> children = groupItem.getChildren();
                    TreeItem<FeatureTreeNode> parent = null;

                    while (true) {
                        Optional<TreeItem<FeatureTreeNode>> c = children.stream()
                                .filter(child -> child.getValue().isDirectory())
                                .filter(child -> {
                                    String path = child.getValue().getPath();
                                    if( viewMode == ViewMode.JAVAFILE) {
                                        path = path.split(":")[0];
                                        return ((Element) elementItem.getValue().getData()).getLocation().split(":")[0].startsWith(path);
                                    }
                                    return ((Element) elementItem.getValue().getData()).getLocation().startsWith(path);
                                })
                                .findFirst();
                        if (c.isPresent()) {
                            children = c.get().getChildren();
                            parent = c.get();
                        } else {
                            break;
                        }
                    }
                    children.add(elementItem);
                }
            }
        }
        collapseTillMultipleElements(groupItem);
        if (index != null) {
            rootNode.getChildren().add(index, groupItem);
        } else {
            rootNode.getChildren().add(groupItem);
        }
        return groupItem;
    }

    private TreeItem<FeatureTreeNode> findTreeItemByPath(TreeItem<FeatureTreeNode> rootNode, String location) {
        for (TreeItem<FeatureTreeNode> child : rootNode.getChildren()) {
            if (child.getValue().getPath() != null && child.getValue().getPath().equals(location)) {
                return child;
            }
            TreeItem<FeatureTreeNode> found = findTreeItemByPath(child, location);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private void collapseTillMultipleElements(TreeItem<FeatureTreeNode> groupItem) {
        if (viewMode == ViewMode.FLAT) return;
        boolean viewCriteria = true;
        if (viewMode == ViewMode.JAVAFILE) {
            for(TreeItem<FeatureTreeNode> child : groupItem.getChildren()) {
                if (child.getValue().getDisplayName().contains(".java")) viewCriteria = false;
            }
        }

        if (groupItem.getChildren().size() == 1
                && groupItem.getChildren().get(0).getValue().isDirectory()
                && viewCriteria) {
            TreeItem<FeatureTreeNode> oldChild = groupItem.getChildren().get(0);
            collapseTillMultipleElements(oldChild);

            ObservableList<TreeItem<FeatureTreeNode>> newChildren = oldChild.getChildren();
            groupItem.getChildren().clear();
            for (TreeItem<FeatureTreeNode> childItem : newChildren) {
                groupItem.getChildren().add(childItem);
            }
        }
    }


    private void organizeHierarchy(TreeItem<FeatureTreeNode> groupItem, FeatureTreeNode groupNodeData) {
        if (viewMode == ViewMode.FLAT) return;

        List<Element> elements = ((Group) groupNodeData.getData()).getElements();
        for (Element element : elements) {
            String[] pathParts = element.getLocation().split(seperatorMap.get(this.parser.getType()));
            ObservableList<TreeItem<FeatureTreeNode>> children = groupItem.getChildren();
            for (int i = 0; i < pathParts.length; i++) {
                // Check if the path part already exists in the children
                String pathPart;
                if(viewMode == ViewMode.JAVAFILE) {
                    pathPart = pathParts[i].split(":")[0]; // For Java file view, we only want the part before the colon
                } else {
                    pathPart = pathParts[i];
                }
                Optional<TreeItem<FeatureTreeNode>> existingChild = children.stream()
                        .filter(child -> child.getValue().getDisplayName().equals(pathPart))
                        .findFirst();
                if (existingChild.isPresent()) {
                    children = existingChild.get().getChildren();
                } else {
                    String name = String.join(seperatorMap.get(this.parser.getType()), Arrays.asList(pathParts).subList(0, i + 1));
                    if(viewMode == ViewMode.JAVAFILE) name = name.split(":")[0];
                    DifferenceDirectory dir = new DifferenceDirectory(name, seperatorMap.get(this.parser.getType()));
                    TreeItem<FeatureTreeNode> conTreeItem = new TreeItem<>(new FeatureTreeNode(dir, true));
                    children.add(conTreeItem);
                    children = conTreeItem.getChildren();
                }
            }
        }
    }

    private void filterTreeView(String filterText) {
        rootNode.getChildren().clear();

        if (originalGroups == null) return;

        if (filterText == null) {
            filterText = "";
        } else {
            filterText = filterText.trim();
        }

        if (filterText.isEmpty()) {
            populateTreeView(getFilteredGroups(true), null);
            return;
        }
        populateTreeView(getFilteredGroups(true), getFilteredElements());
    }


    // Actions for Buttons

    public void handleEditAction(TreeItem<FeatureTreeNode> item) {
        System.out.println("Handle Edit Action Triggered - Not Implemented");
    }

    public void handleDeleteAction(TreeItem<FeatureTreeNode> item) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Remove '" + item.getValue().getDisplayName() + "'?");
        ButtonType yesButton = new ButtonType("Yes");
        ButtonType onlyDirectoryButton = new ButtonType("Keep Subelements");
        ButtonType noButton = new ButtonType("No");
        if (item.getValue().isDirectory() && item.getValue().getType() == FeatureTreeNode.DataType.ELEMENT) {
            alert.setContentText("Are you sure you want to remove this item and all its subelements?");
            alert.getButtonTypes().setAll(yesButton, onlyDirectoryButton, noButton);
        } else {
            alert.setContentText("Are you sure you want to remove this item from the view?");
            alert.getButtonTypes().setAll(yesButton, noButton);
        }
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == yesButton) {
            for(TreeItem<FeatureTreeNode> child: item.getChildren()) {
                Element element = (Element) child.getValue().getData();
                Group group = findGroupContainingElement(element);
                if (group != null) {
                    group.getElements().remove(element);
                }
                deleteItem(item);
            }
            if (item.getValue().getType() == FeatureTreeNode.DataType.ELEMENT) {
                Element element = (Element) item.getValue().getData();
                Group group = findGroupContainingElement(element);
                if (group != null) {
                    group.getElements().remove(element);
                    TreeItem<FeatureTreeNode> groupItem = findTreeItemByPath(rootNode, group.getName().get());
                    refreshGroup(groupItem);
                }
            }

        }
        if (result.isPresent() && result.get() == onlyDirectoryButton) {
            if (item.getValue().getType() == FeatureTreeNode.DataType.ELEMENT) {
                Element element = (Element) item.getValue().getData();
                Group group = findGroupContainingElement(element);
                if (group != null) {
                    group.getElements().remove(element);
                    TreeItem<FeatureTreeNode> groupItem = findTreeItemByPath(rootNode, group.getName().get());
                    refreshGroup(groupItem);
                }
            }
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

    private void hideDetailsPane() {
        detailScrollPane.setVisible(false);
        detailScrollPane.setManaged(false);
        currentDetailItem = null;
        detailOccurrencesListView.setItems(FXCollections.emptyObservableList());
        detailSubElementListView.setItems(FXCollections.emptyObservableList());
    }


    // Actions for Menu Buttons
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
                hierarchyButtonHBox.getChildren().clear();
                initializeHierarchyButtons(hierarchyButtonHBox);
                populateTreeView(getFilteredGroups(true), null); // Populate with parsed data
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
                if (group.getElements().isEmpty()) {
                    continue;
                }
                // Group Header
                String groupName = group.getName().get();
                writer.write(groupName + ":");
                writer.newLine();

                // Occurrences
                if (group.getOccurrences() != null && !group.getOccurrences().isEmpty()) {
                    if (group.getElements().size() > 1 && this.parser.getType() == JAVA) {
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
                writer.write("Elements:");
                writer.newLine();
                for (Element element : group.getElements()) {
                    if (this.parser.getType() == JAVA) {
                        writer.write("(" + element.getLocation() + ")");
                        writer.newLine();

                        writer.write(element.getDescription());
                        writer.newLine();

                    } else if (this.parser.getType() == IEC61499) {
                        writer.write(element.getDescription());
                        writer.newLine();
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
                targetGroupItem.getValue().isDirectory() && targetGroupItem.getValue().getType() != FeatureTreeNode.DataType.ELEMENT) {
            System.err.println("Invalid types for moveElementToGroup");
            return;
        }

        // Open dialogue if the subelements should also be moved or if the subelement should kept in the current group, if the element is a directory
        if (elementItem.getValue().isDirectory()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Move Directory");
            alert.setHeaderText("Move Directory and all its subelements?");
            alert.setContentText("Do you want to move the directory and all its subelements to the new group?");
            ButtonType yesButton = new ButtonType("Yes");
            ButtonType onlyDirectoryButton = new ButtonType("Keep Subelements");
            ButtonType noButton = new ButtonType("No");
            alert.getButtonTypes().setAll(yesButton, onlyDirectoryButton, noButton);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == noButton) {
                return;
            }
            if (result.isPresent() && result.get() == yesButton) {
                for(TreeItem<FeatureTreeNode> child: elementItem.getChildren()) {
                    moveElementToGroup(child, targetGroupItem);
                }
            }
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

            //refresh old group TreeItem
            TreeItem<FeatureTreeNode> sourceGroupItem = findTreeItemByPath(rootNode, sourceGroup.getName().get());
            if (sourceGroupItem != null) {
                refreshGroup(sourceGroupItem);
            }
            // Add new group TreeItem
            refreshGroup(targetGroupItem);
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

    private void initializeHierarchyButtons(HBox hbox) {
        hbox.getChildren().clear();
        Button hierarchyTreeButton = new Button("Tree");
        hbox.getChildren().add(hierarchyTreeButton);
        hierarchyTreeButton.setOnAction(event -> {
            this.viewMode = ViewMode.TREE;
            populateTreeView(getFilteredGroups(true), null);
        });
        if (this.parser.getType() == JAVA) {
            Button hierarchyJavaFileButton = new Button("File");
            hbox.getChildren().add(hierarchyJavaFileButton);
            hierarchyJavaFileButton.setOnAction(event -> {
                this.viewMode = ViewMode.JAVAFILE;
                populateTreeView(getFilteredGroups(true), null);
            });
        } else if (this.parser.getType() == IEC61499) {
            // IEC61499 specific hierarchy button here
        }
        Button hierarchyFlatButton = new Button("Flat");
        hbox.getChildren().add(hierarchyFlatButton);
        hierarchyFlatButton.setOnAction(event -> {
            this.viewMode = ViewMode.FLAT;
            populateTreeView(getFilteredGroups(true), null);
        });
    }

    private List<Group> getFilteredGroups(boolean includeElementGroupFilter) {

        if (originalGroups == null) return Collections.emptyList();

        String lowerCaseFilter;
        if (searchTextField.getText() == null) {
            lowerCaseFilter = "";
        } else {
            lowerCaseFilter = searchTextField.getText().toLowerCase().trim();
        }

        if (lowerCaseFilter.isEmpty()) {
            populateTreeView(originalGroups, null);
            return originalGroups;
        }

        List<Group> filteredGroups = new ArrayList<>();
        if (includeElementGroupFilter) {
            for (Element element : getFilteredElements()) {
                Group parentGroup = findGroupContainingElement(element);
                if (parentGroup != null && !filteredGroups.contains(parentGroup)) {
                    filteredGroups.add(parentGroup);
                }
            }
        }
        filteredGroups.addAll(originalGroups.stream()
                .filter(group -> group.getName().get().toLowerCase().contains(lowerCaseFilter)
                )
                .collect(Collectors.toList()));
        return filteredGroups;
    }

    private List<Element> getFilteredElements() {
        String lowerCaseFilter;
        if (searchTextField.getText() == null) {
            lowerCaseFilter = "";
        } else {
            lowerCaseFilter = searchTextField.getText().toLowerCase().trim();
        }
        return originalGroups.stream()
                .flatMap(group -> group.getElements().stream())
                .filter(element -> element.getName().get().toLowerCase().contains(lowerCaseFilter)
                        || element.getDescription().toLowerCase().contains(lowerCaseFilter)
                        || element.getLocation().toLowerCase().contains(lowerCaseFilter))
                .collect(Collectors.toList());
    }
}