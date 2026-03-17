/***
 
    This Source Code Form is subject to the terms of the Mozilla
    Public License, v. 2.0. If a copy of the MPL was not distributed
    with this file, You can obtain one at
    https://mozilla.org/MPL/2.0/.*
    Contributors:
    Michael Schmidhammer
**/

package at.variabilityanalysisgui.view;

import at.variabilityanalysisgui.model.Difference;
import at.variabilityanalysisgui.model.Group;
import at.variabilityanalysisgui.model.Element;

import static at.variabilityanalysisgui.parser.InputParser.ExtractionType.JAVA;

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

    public String getTechnicalName() {
        return data.getName().get();
    }

    public String getDisplayName() {
        if (data instanceof Element) {
            if (((Element) data).getExtractionType() == JAVA) {
                return getTechnicalName() + ": " + ((Element) data).getDescription().split("\n")[0] + "...";
            }
        } else if (data instanceof Group) {
            return getTechnicalName();
        }
        return getTechnicalName();
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
            return ((Element) data).getLocation() + ((Element) data).getSeparateSymbol() + data.getName().get();
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