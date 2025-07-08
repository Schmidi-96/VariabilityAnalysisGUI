/***
 The MIT License (MIT)

 Copyright (c) 2025 Michael Schmidhammer
 */

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