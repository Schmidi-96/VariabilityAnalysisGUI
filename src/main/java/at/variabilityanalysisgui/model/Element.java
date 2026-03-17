/***
 
    This Source Code Form is subject to the terms of the Mozilla
    Public License, v. 2.0. If a copy of the MPL was not distributed
    with this file, You can obtain one at
    https://mozilla.org/MPL/2.0/.*
    Contributors:
    Michael Schmidhammer
**/

package at.variabilityanalysisgui.model;

import at.variabilityanalysisgui.parser.InputParser.ExtractionType;
import javafx.beans.property.SimpleStringProperty;

import static at.variabilityanalysisgui.parser.InputParser.ExtractionType.JAVA;

public class Element extends Difference {

    private final String location;
    private final String description;
    private final String separatorSymbol;
    private final ExtractionType extractionType;

    public Element(String name, ExtractionType type, String location, String description) {
        this.name = new SimpleStringProperty(name);
        this.extractionType = type;
        if (extractionType == JAVA) {
            this.location = location.replaceAll("[()]", "");
            this.separatorSymbol = "/";
        } else {
            this.location = location;
            this.separatorSymbol = ";";
        }
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public String getSeparateSymbol() {
        return separatorSymbol;
    }

    public ExtractionType getExtractionType() {
        return extractionType;
    }

    @Override
    public String toString() {
        return location + ":\n" + description;
    }
}