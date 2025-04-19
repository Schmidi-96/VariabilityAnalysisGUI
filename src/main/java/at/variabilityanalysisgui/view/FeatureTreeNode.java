package at.variabilityanalysisgui.view;

import at.variabilityanalysisgui.model.Difference;
import at.variabilityanalysisgui.model.Group;
import at.variabilityanalysisgui.model.Element;

public class FeatureTreeNode {

    public enum DataType {
        GROUP, CONTAINER, ELEMENT
    }

    private final Difference data;
    private final DataType type;

    public FeatureTreeNode(Group group) {
        this.data = group;
        this.type = DataType.GROUP;
    }

    public FeatureTreeNode(Element element) {
        this.data = element;
        this.type = DataType.ELEMENT;
    }

    public FeatureTreeNode(Difference data) {
        this.data = data;
        this.type = DataType.CONTAINER;
    }

    public FeatureTreeNode(DifferenceDirectory dir) {
        this.data = dir;
        this.type = DataType.CONTAINER;
    }


    public Difference getData() {
        return data;
    }

    public String getDisplayName() {
        return data.getName().get();
    }

    public DataType getType() {
        return type;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}