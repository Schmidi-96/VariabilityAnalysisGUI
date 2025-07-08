/***
 Copyright (c) 2025 Michael Schmidhammer

 License MIT
 */

package at.variabilityanalysisgui.controller.Filter;

import at.variabilityanalysisgui.model.Element;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

public class MultipleChoiceFilter extends Filter{
    private final List<String> selectableValues;
    private final ObservableSet<String> selectedValues;
    private final BiFunction<Set<String>, Element, Element> filterFunction;

    public MultipleChoiceFilter(String name, List<String> selectableValues, BiFunction<Set<String>, Element, Element> filterFunction) {
        super(name);
        this.selectableValues = selectableValues;
        this.filterFunction = filterFunction;
        this.selectedValues = FXCollections.observableSet();
    }

    public List<String> getSelectableValues() {
        return selectableValues;
    }

    public ObservableSet<String> getSelectedValues() {
        return selectedValues;
    }

    public void addSelectedValue(String value) {
        selectedValues.add(value);
    }

    public void removeSelectedValue(String value) {
        selectedValues.remove(value);
    }

    @Override
    public Element filter(Element element) {
        return filterFunction.apply(selectedValues, element);
    }
}
