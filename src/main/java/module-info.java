/***
 
    This Source Code Form is subject to the terms of the Mozilla
    Public License, v. 2.0. If a copy of the MPL was not distributed
    with this file, You can obtain one at
    https://mozilla.org/MPL/2.0/.*
    Contributors:
    Michael Schmidhammer
**/

module at.variabilityanalysisgui {
    requires javafx.fxml;
    requires org.controlsfx.controls;


    opens at.variabilityanalysisgui to javafx.fxml;
    exports at.variabilityanalysisgui;
    exports at.variabilityanalysisgui.parser;
    opens at.variabilityanalysisgui.parser to javafx.fxml;
    exports at.variabilityanalysisgui.model;
    opens at.variabilityanalysisgui.model to javafx.fxml;
    exports at.variabilityanalysisgui.controller;
    opens at.variabilityanalysisgui.controller to javafx.fxml;
    exports at.variabilityanalysisgui.view;
    opens at.variabilityanalysisgui.view to javafx.fxml;
    exports at.variabilityanalysisgui.controller.Filter;
    opens at.variabilityanalysisgui.controller.Filter to javafx.fxml;
}