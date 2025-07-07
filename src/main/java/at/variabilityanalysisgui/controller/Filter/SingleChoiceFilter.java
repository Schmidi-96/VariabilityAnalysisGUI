package at.variabilityanalysisgui.controller.Filter;

import at.variabilityanalysisgui.model.Element;

import java.util.List;
import java.util.function.BiFunction;

public class SingleChoiceFilter extends Filter {
    private final List<String> selectableValues;
    private final BiFunction<String, Element, Element> filterFunction;

    public SingleChoiceFilter(String name, List<String> selectableValues, BiFunction<String, Element, Element> filterFunction) {
        super(name);
        this.selectableValues = selectableValues;
        this.filterFunction = filterFunction;
    }

    public List<String> getSelectableValues() {
        return selectableValues;
    }

    @Override
    public Element filter(Element element) {
        return filterFunction.apply(this.getValue(), element);
    }
}
