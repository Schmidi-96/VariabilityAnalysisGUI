/***
 
    This Source Code Form is subject to the terms of the Mozilla
    Public License, v. 2.0. If a copy of the MPL was not distributed
    with this file, You can obtain one at
    https://mozilla.org/MPL/2.0/.*
    Contributors:
    Michael Schmidhammer
**/

package at.variabilityanalysisgui.controller;

import javafx.beans.property.StringProperty;

public class ChangeDiffNameCommand implements Command {
    private final StringProperty targetProperty;
    private final String oldValue;
    private final String newValue;

    public ChangeDiffNameCommand(StringProperty targetProperty, String oldValue, String newValue) {
        this.targetProperty = targetProperty;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    @Override
    public void execute() {
        targetProperty.set(newValue);
        System.out.println("ChangeDiffNameCommand executed: " + targetProperty.get());
    }

    @Override
    public void undo() {
        targetProperty.set(oldValue);
    }
}
