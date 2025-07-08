/***
 The MIT License (MIT)

 Copyright (c) 2025 Michael Schmidhammer
 */

package at.variabilityanalysisgui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        URL fxmlLocation = getClass().getResource("/at/variabilityanalysisgui/MainView.fxml");
        if (fxmlLocation == null) {
            System.err.println("Cannot find FXML file.");
            return;
        }
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
        Scene scene = new Scene(fxmlLoader.load(), 900, 700);
        primaryStage.setTitle("Variability Analyser");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}