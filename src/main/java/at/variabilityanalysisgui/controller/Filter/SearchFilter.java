/***
 
    This Source Code Form is subject to the terms of the Mozilla
    Public License, v. 2.0. If a copy of the MPL was not distributed
    with this file, You can obtain one at
    https://mozilla.org/MPL/2.0/.*
    Contributors:
    Michael Schmidhammer
**/

package at.variabilityanalysisgui.controller.Filter;

import at.variabilityanalysisgui.model.Element;
import at.variabilityanalysisgui.model.Group;

public class SearchFilter extends Filter {
    public boolean includeElements;

    public SearchFilter(String name) {
        super(name);
        this.includeElements = true;
    }

    @Override
    public Element filter(Element element) {
        String lowerCaseFilter;
        if (this.getValue() == null) {
            lowerCaseFilter = "";
        } else {
            lowerCaseFilter = this.getValue().toLowerCase().trim();
        }
        if (lowerCaseFilter.isEmpty()) {
            return element;
        }
        if(element.getName().get().toLowerCase().contains(lowerCaseFilter)
                            || element.getDescription().toLowerCase().contains(lowerCaseFilter)
                            || element.getLocation().toLowerCase().contains(lowerCaseFilter)) return element;
        return null;
    }

    @Override
    public Group filter(Group group) {
        String lowerCaseFilter;
        if (this.getValue() == null) {
            lowerCaseFilter = "";
        } else {
            lowerCaseFilter = this.getValue().toLowerCase().trim();
        }
        if (lowerCaseFilter.isEmpty()) {
            return group;
        }
        if (includeElements && !group.getElements().stream().filter(e -> filter(e) != null).toList().isEmpty()) {
            return group;
        }
        if (group.getName().get().toLowerCase().contains(lowerCaseFilter)) return group;
        return null;
    }
}
