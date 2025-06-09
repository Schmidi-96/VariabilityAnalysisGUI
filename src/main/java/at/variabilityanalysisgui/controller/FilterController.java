package at.variabilityanalysisgui.controller;

import at.variabilityanalysisgui.controller.Filter.Filter;
import at.variabilityanalysisgui.controller.Filter.MultipleChoiceFilter;
import at.variabilityanalysisgui.controller.Filter.SearchFilter;
import at.variabilityanalysisgui.model.Element;
import at.variabilityanalysisgui.model.Group;
import at.variabilityanalysisgui.view.FilterItem;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static at.variabilityanalysisgui.parser.InputParser.ExtractionType.IEC61499;
import static at.variabilityanalysisgui.parser.InputParser.ExtractionType.JAVA;

public class FilterController {

    private TextField searchTextField;
    private Button filterButton;

    private ContextMenu filterContextMenu;
    private List<Filter> filters = new ArrayList<>();
    private Controller controller;

    public FilterController(Controller controller, TextField searchTextField, Button filterButton) {
        this.controller = controller;
        this.searchTextField = searchTextField;
        this.filterButton = filterButton;
        //Filter and Search setup
        setupFilterListener();
    }

    public void setupFilterListener() {
        filters.clear();
        searchTextField.clear();

        // setup filter listener and ui elements
        if(controller.getParserType() == IEC61499) {
            this.filters.add(new MultipleChoiceFilter("Element Type", Arrays.asList("Connection", "Function Block"), (a, b) -> {
                if (a == null || a.isEmpty()) {
                    return b;
                } else if (a.contains("Connection") && b.getName().get().contains(" -> ")) {
                    return b;
                } else if (a.contains("Function Block") && !b.getName().get().contains(" -> ")) {
                    return b;
                }
                return null;
            }));
            this.filterButton.setDisable(false);
            this.searchTextField.setDisable(false);

        } else if (controller.getParserType() == JAVA) {
            this.filterButton.setDisable(true);
            this.searchTextField.setDisable(false);

        } else {
            this.filterButton.setDisable(true);
            this.searchTextField.setDisable(true);
        }

        // setup search filter
        Filter searchFilter = new SearchFilter("Search");
        searchTextField.textProperty().addListener((obs, oldVal, newVal) -> {
            searchFilter.setValue(newVal);
            if (newVal == null || newVal.trim().isEmpty()) {
                searchFilter.setEnabled(false);
            } else {
                searchFilter.setEnabled(true);
            }
            if (newVal != null && newVal.isEmpty()) {
                controller.populateTreeView(getFilteredGroups(true), null);
                return;
            }
            controller.populateTreeView(getFilteredGroups(true), getFilteredElements());
        });
        this.filters.add(searchFilter);


        // setup context menu
        filterButton.setOnAction(event -> {
            if (filterContextMenu == null) {
                filterContextMenu = new ContextMenu();
                filterContextMenu.setAutoHide(true);

                for(Filter filter : filters) {
                    filter.valueProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal == null || newVal.trim().isEmpty()) {
                            filter.setEnabled(false);
                        } else {
                            filter.setEnabled(true);
                        }
                        controller.populateTreeView(getFilteredGroups(true), getFilteredElements());
                    });
                    if(!(filter instanceof SearchFilter)) {
                        CustomMenuItem customMenuItem = new CustomMenuItem(new FilterItem(filter), false);
                        filterContextMenu.getItems().add(customMenuItem);
                    }
                }
            }

            if (filterContextMenu.isShowing()) {
                filterContextMenu.hide();
            } else {
                filterContextMenu.show(filterButton, javafx.geometry.Side.BOTTOM, 0, 0);
            }
        });
    }

    public List<Group> getFilteredGroups(boolean includeElementGroupFilter) {
        List<Group> filteredGroups = controller.getOriginalGroups();
        for(Filter filter : filters) {
            if (filter.isEnabled()) {
                filteredGroups = filteredGroups.stream().filter(group -> filter.filter(group) != null).toList();
            }
        }
        return filteredGroups;
    }

    public List<Element> getFilteredElements() {
        List<Element> elements = controller.getOriginalGroups().stream().flatMap(group -> group.getElements().stream()).toList();
        for (Filter filter : filters) {
            if (filter.isEnabled()) {
                elements = elements.stream()
                        .map(filter::filter)
                        .filter(e -> e != null)
                        .collect(Collectors.toList());
            }
        }
        return elements;
    }
}
