package at.variabilityanalysisgui.controller;

import at.variabilityanalysisgui.model.Difference;
import at.variabilityanalysisgui.model.Element;
import at.variabilityanalysisgui.model.Group;
import at.variabilityanalysisgui.view.DifferenceDirectory;
import at.variabilityanalysisgui.view.FeatureTreeCell;
import at.variabilityanalysisgui.view.FeatureTreeNode;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.util.*;

import static at.variabilityanalysisgui.controller.Controller.findGroupContainingElement;
import static at.variabilityanalysisgui.parser.InputParser.ExtractionType.IEC61499;
import static at.variabilityanalysisgui.parser.InputParser.ExtractionType.JAVA;

public class TreeViewController {

    enum ViewMode {
        TREE, FLAT, JAVAFILE
    }

    private TreeView<FeatureTreeNode> featureTreeView;

    private TreeItem<FeatureTreeNode> rootNode;

    public HBox hierarchyButtonHBox;

    ViewMode viewMode = ViewMode.FLAT;

    Controller controller;

    private TreeItem<FeatureTreeNode> draggedItem = null;

    public TreeViewController(Controller controller, TreeView<FeatureTreeNode> featureTreeView, HBox hierarchyButtonHBox) {
        this.featureTreeView = featureTreeView;
        this.controller = controller;
        this.hierarchyButtonHBox = hierarchyButtonHBox;

        // Setup TreeView with custom cell factory
        featureTreeView.setCellFactory(tv -> new FeatureTreeCell(this));

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
                controller.showDetailsPane(node.getData(), newValue);
            }
        });
    }

    public void populateTreeView(List<Group> groups, List<Element> visibleElements) {
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

    public void refreshGroup(TreeItem<FeatureTreeNode> groupItem) {
        Group group = (Group) groupItem.getValue().getData();
        int index = rootNode.getChildren().indexOf(groupItem);
        rootNode.getChildren().remove(index);
        Set<FeatureTreeNode> expandedNodes = getExpandedElement(groupItem);
        System.out.println("Expanded Nodes: " + expandedNodes);
        groupItem = populateGroup(group, controller.getFilteredElements(), index);
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

    public void setRootNode(TreeItem<FeatureTreeNode> rootNode) {
        this.rootNode = rootNode;
        featureTreeView.setRoot(rootNode);
    }

    public TreeItem<FeatureTreeNode> getRootNode() {
        return rootNode;
    }

    public TreeView<FeatureTreeNode> getFeatureTreeView() {
        return featureTreeView;
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
            String[] pathParts = element.getLocation().split(controller.seperatorMap.get(controller.getParserType()));
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
                    String name = String.join(controller.seperatorMap.get(controller.getParserType()), Arrays.asList(pathParts).subList(0, i + 1));
                    if(viewMode == ViewMode.JAVAFILE) name = name.split(":")[0];
                    DifferenceDirectory dir = new DifferenceDirectory(name, controller.seperatorMap.get(controller.getParserType()));
                    TreeItem<FeatureTreeNode> conTreeItem = new TreeItem<>(new FeatureTreeNode(dir, true));
                    children.add(conTreeItem);
                    children = conTreeItem.getChildren();
                }
            }
        }
    }

    public static TreeItem<FeatureTreeNode> findTreeItemByPath(TreeItem<FeatureTreeNode> rootNode, String location) {
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

    public void initializeHierarchyButtons() {
        hierarchyButtonHBox.getChildren().clear();
        Button hierarchyTreeButton = new Button("Tree");
        hierarchyButtonHBox.getChildren().add(hierarchyTreeButton);
        hierarchyTreeButton.setOnAction(event -> {
            this.viewMode = ViewMode.TREE;
            populateTreeView(controller.getFilteredGroups(true), null);
        });
        if (controller.getParserType() == JAVA) {
            Button hierarchyJavaFileButton = new Button("File");
            hierarchyButtonHBox.getChildren().add(hierarchyJavaFileButton);
            hierarchyJavaFileButton.setOnAction(event -> {
                this.viewMode = ViewMode.JAVAFILE;
                populateTreeView(controller.getFilteredGroups(true), null);
            });
        } else if (controller.getParserType() == IEC61499) {
            // IEC61499 specific hierarchy button here
        }
        Button hierarchyFlatButton = new Button("Flat");
        hierarchyButtonHBox.getChildren().add(hierarchyFlatButton);
        hierarchyFlatButton.setOnAction(event -> {
            this.viewMode = ViewMode.FLAT;
            populateTreeView(controller.getFilteredGroups(true), null);
        });
    }

    // Actions
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
                Group group = findGroupContainingElement(controller.getOriginalGroups(), element);
                if (group != null) {
                    group.getElements().remove(element);
                }
                deleteItem(item);
            }
            if (item.getValue().getType() == FeatureTreeNode.DataType.ELEMENT) {
                Element element = (Element) item.getValue().getData();
                Group group = findGroupContainingElement(controller.getOriginalGroups(), element);
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
                Group group = findGroupContainingElement(controller.getOriginalGroups(), element);
                if (group != null) {
                    group.getElements().remove(element);
                    TreeItem<FeatureTreeNode> groupItem = findTreeItemByPath(rootNode, group.getName().get());
                    refreshGroup(groupItem);
                }
            }
        }
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

        Group sourceGroup = findGroupContainingElement(controller.getOriginalGroups(), element);

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
}
