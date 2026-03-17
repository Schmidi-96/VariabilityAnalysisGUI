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
import javafx.beans.property.SimpleStringProperty;

public class DifferenceDirectory extends Difference {
    String separator;
    String path;

    public DifferenceDirectory(String path, String separator) {
        this.name = new SimpleStringProperty(path.split(separator)[path.split(separator).length - 1]);
        this.path = path;
        this.separator = separator;
    }

    public String getPath() {
        return path;
    }
}
