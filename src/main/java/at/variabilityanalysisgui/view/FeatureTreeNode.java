package at.variabilityanalysisgui.view;

import at.variabilityanalysisgui.model.Difference;
import at.variabilityanalysisgui.model.Group;
import at.variabilityanalysisgui.model.Element;

public class FeatureTreeNode {
    private final Difference data;
    private final boolean isGroup;

    public FeatureTreeNode(Group group) {
        this.data = group;
        this.isGroup = true;
    }

    public FeatureTreeNode(Element element) {
        this.data = element;
        this.isGroup = false;
    }

    public FeatureTreeNode(Difference data) {
        this.data = data;
        this.isGroup = false;
    }


    public Difference getData() {
        return data;
    }

    public String getDisplayName() {
        return data.getName().get();
    }

    public boolean isGroup() {
        return isGroup;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}