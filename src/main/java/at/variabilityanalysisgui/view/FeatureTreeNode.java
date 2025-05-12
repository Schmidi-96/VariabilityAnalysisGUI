package at.variabilityanalysisgui.view;

import at.variabilityanalysisgui.model.Difference;
import at.variabilityanalysisgui.model.Group;
import at.variabilityanalysisgui.model.Element;

public class FeatureTreeNode {

    public enum DataType {
        GROUP, CONTAINER, ELEMENT
    }

    private final Difference data;
    private boolean directory;

    public FeatureTreeNode(Difference data, boolean directory) {
        this.data = data;
        this.directory = directory;
    }

    public boolean isDirectory() {
        return directory;
    }

    public void setDirectory(boolean directory) {
        this.directory = directory;
    }

    public Difference getData() {
        return data;
    }

    public String getDisplayName() {
        return data.getName().get();
    }

    public DataType getType() {
        if (data instanceof Group) {
            return DataType.GROUP;
        } else if (data instanceof Element) {
            return DataType.ELEMENT;
        } else {
            return DataType.CONTAINER;
        }
    }

    public String getPath() {
        if (data instanceof DifferenceDirectory) {
            return ((DifferenceDirectory) data).getPath();
        } else if (data instanceof Element) {
            return ((Element) data).getLocation() + ((Element) data).getSeperaterSymbol() + data.getName().get();
        } else if (data instanceof Group) {
            return data.getName().get();
        }
        return null;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}