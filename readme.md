# Variability Analyser GUI

[![Language](https://img.shields.io/badge/Language-Java%2017-blue.svg)](https://www.java.com)
[![Framework](https://img.shields.io/badge/UI-JavaFX-orange.svg)](https://openjfx.io/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](https://opensource.org/licenses/MIT)

A desktop application for loading, visualizing, and managing difference reports generated from variability analysis tools. It provides an interactive interface to explore, filter, and restructure variability data before saving the changes.

## Key Features

-   **Load Difference Reports**: Parses custom `.txt` files containing variability data, with support for both Java and IEC 61499 formats.
-   **Hierarchical Visualization**: Displays differences in a structured tree view, allowing for different organizational perspectives (Flat, Tree, or by File).
-   **Interactive Filtering**:
    -   A real-time search bar to quickly find specific elements or groups.
    -   An filter menu to narrow down the view based on properties like "Occurrences" or "Element Type".
-   **Detailed Information Pane**: Click any item in the tree to view its complete details, including name, location, code snippets, and sub-elements.
-   **Data Manipulation**:
    -   **Modify Group Names**: Edit group names directly from the details pane.
    -   **Drag-and-Drop**: Easily move elements between different groups to restructure the data.
    -   **Delete Items**: Remove unwanted elements or entire directory structures.
-   **Save Changes**: Persist all modifications back to a `.txt` file in the original format.


## Architecture

The project follows a Model-View-Controller (MVC) pattern to ensure a clear separation of concerns.

-   **`model`**: Contains the plain Java objects that represent the application's data (`Group`, `Element`, `Difference`). These classes use `JavaFX Properties` to allow for easy UI binding.
-   **`view`**: Holds UI-specific components.
    -   `MainView.fxml`: Defines the primary layout of the application.
    -   Custom view classes (`FeatureTreeCell`, `FilterItem`, `FeatureTreeNode`) handle the rendering and interactive logic for complex UI controls.
-   **`controller`**: Contains the core application logic.
    -   `Controller.java`: The main controller that initializes the application and orchestrates communication between the other controllers.
    -   **Sub-controllers**: Responsibilities are delegated to specialized controllers for better organization:
        -   `TreeViewController`: Manages the main tree view, including population, view modes, and drag-and-drop logic.
        -   `DetailsController`: Manages the dynamic details pane on the right.
        -   `FilterController`: Manages all filtering logic from the search bar and filter menu.
-   **`parser`**: A dedicated `InputParser` is responsible for reading and parsing the proprietary `.txt` file format into the application's model objects.

## Technology Stack

-   **Language**: Java (developed with JDK 17)
-   **Framework**: JavaFX 17
-   **External Libraries**:
    -   [ControlsFX](https://github.com/controlsfx/controlsfx): Used for advanced UI controls like the `CheckComboBox` in the filter menu.


## How to Use

1.  Launch the application.
2.  Go to **File -> Load** to open a `.txt` difference report.
3.  The data will be displayed in the central tree view.
4.  **Explore the data**:
    -   Click on any item to see its details in the right-hand pane.
    -   Use the **Search** bar at the top to filter items by name, location, or description.
    -   Click the **Filter** button to apply advanced filters.
    -   Use the **Flat/Tree/File** toggle buttons at the bottom to change the hierarchy view.
5.  **Modify the data**:
    -   To move an element, click and drag it from the tree onto another group.
    -   To delete an item, hover over it in the tree and click the `X` button.
6.  Once you are done with your changes, go to **File -> Save** to save the modified data to a new `.txt` file.

---

## License

This project is licensed under the MPL-2.0 License - see the [LICENSE.md](LICENSE.md) file for details.