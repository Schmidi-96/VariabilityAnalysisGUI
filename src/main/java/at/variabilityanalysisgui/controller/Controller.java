package at.variabilityanalysisgui.controller;

import at.variabilityanalysisgui.model.Difference;
import at.variabilityanalysisgui.model.Group;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import javafx.scene.control.*;
import javafx.scene.layout.HBox;
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
import at.variabilityanalysisgui.view.FeatureTreeNode;
import at.variabilityanalysisgui.parser.InputParser;
import static at.variabilityanalysisgui.parser.InputParser.ExtractionType.IEC61499;
import static at.variabilityanalysisgui.parser.InputParser.ExtractionType.JAVA;

public class Controller {

    public static final String OCCURRENCE_HEADER_JAVA = "Occurrence:";
    public static final String OCCURRENCE_HEADER_IEC = "Variants:";
    public static final String ELEMENTS_HEADER = "Elements:";
    private TreeViewController treeViewController;
    private FilterController filterController;
    private DetailsController detailsController;

    //FilterController
    @FXML private TextField searchTextField;
    @FXML private Button filterButton;

    // DetailsController
    @FXML private ListView<Element> detailSubElementListView;
    @FXML private ScrollPane detailScrollPane;
    @FXML private HBox detailsNameHBox;
    @FXML private Label detailLocationLabel;
    @FXML private TextArea detailLocationTextArea;
    @FXML private TextField detailGroupNameTextField;
    @FXML private Label detailOccurrenceLabel;
    @FXML private ListView<String> detailOccurrencesListView;
    @FXML private Label detailElementLabel;
    @FXML private TextArea detailElementData;
    @FXML private Label detailSubElementLabel;
    @FXML private Button detailCloseButton;

    //TreeViewController
    @FXML public HBox hierarchyButtonHBox;
    @FXML private TreeView<FeatureTreeNode> featureTreeView;

    private final InputParser parser = new InputParser();
    private List<Group> originalGroups;

    public Map<InputParser.ExtractionType, String> seperatorMap = Map.of(JAVA, "/", IEC61499, ";");

    private UndoManager undoManager = new UndoManager();


    @FXML
    public void initialize() {

        this.treeViewController = new TreeViewController(this, featureTreeView, hierarchyButtonHBox);
        this.filterController = new FilterController(this, searchTextField, filterButton);
        this.detailsController = new DetailsController(this, detailSubElementListView, detailScrollPane, detailsNameHBox,
                detailLocationLabel, detailLocationTextArea, detailGroupNameTextField,
                detailOccurrenceLabel, detailOccurrencesListView, detailElementLabel, detailElementData,
                detailSubElementLabel, detailCloseButton);
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
                treeViewController.initializeHierarchyButtons();
                treeViewController.populateTreeView(filterController.getFilteredGroups(), null); // Populate with parsed data
                detailsController.hideDetailsPane();
                filterController.setupFilterListener();
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
                        writer.write(OCCURRENCE_HEADER_JAVA);
                    } else {
                        writer.write(OCCURRENCE_HEADER_IEC);
                    }
                    writer.newLine();
                    for (String occurrence : group.getOccurrences()) {
                        writer.write(occurrence);
                        writer.newLine();
                    }
                }

                // Elements
                writer.write(ELEMENTS_HEADER);
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
        Node node = treeViewController.getFeatureTreeView().getScene().getRoot();
        if (node != null) {
            return node.getScene().getWindow();
        }
        return null;
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

    public static Group findGroupContainingElement(List<Group> groups, Element element) {
        if (groups == null) return null;
        for (Group group : groups) {
            if (group.getElements().contains(element)) {
                return group;
            }
        }
        return null;
    }

    public InputParser.ExtractionType getParserType() {
        return parser.getType();
    }

    public List<Group> getOriginalGroups() {
        return originalGroups;
    }

    public void populateTreeView(List<Group> groups, List<Element> elements) {
        treeViewController.populateTreeView(groups, elements);
    }

    public List<Group> getFilteredGroups() {
        return filterController.getFilteredGroups();
    }

    public List<Element> getFilteredElements() {
        return filterController.getFilteredElements();
    }

    public void showDetailsPane(Difference data, TreeItem<FeatureTreeNode> newValue) {
        detailsController.showDetailsPane(data, newValue);
    }
}