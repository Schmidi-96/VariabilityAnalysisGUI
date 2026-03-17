/***
 
    This Source Code Form is subject to the terms of the Mozilla
    Public License, v. 2.0. If a copy of the MPL was not distributed
    with this file, You can obtain one at
    https://mozilla.org/MPL/2.0/.*
    Contributors:
    Michael Schmidhammer
**/
package at.variabilityanalysisgui.model;

import javafx.beans.property.SimpleStringProperty;

import java.util.ArrayList;
import java.util.List;

public class Group extends Difference {
    private final List<String> occurrences;
    private final List<Element> elements;

    public Group(String name) {
        this.name = new SimpleStringProperty(name);
        this.occurrences = new ArrayList<>();
        this.elements = new ArrayList<>();
    }

    public List<String> getOccurrences() {
        return occurrences;
    }

    public List<Element> getElements() {
        return elements;
    }

    public void addOccurrence(String occurrence) {
        this.occurrences.add(occurrence);
    }

    public void addElement(Element element) {
        this.elements.add(element);
    }

    @Override
    public String toString() {
        return name.get();
    }
}
