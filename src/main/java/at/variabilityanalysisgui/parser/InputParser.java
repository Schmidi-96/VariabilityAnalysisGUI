package at.variabilityanalysisgui.parser;

import at.variabilityanalysisgui.model.Element;
import at.variabilityanalysisgui.model.Group;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static at.variabilityanalysisgui.parser.InputParser.ExtractionType.*;

public class InputParser {

    public ExtractionType getType() {
        return type;
    }

    public enum ExtractionType {
        JAVA, IEC61499, UNKNOWN
    }
    private ExtractionType type = ExtractionType.UNKNOWN;

    private static final Pattern JAVA_ELEMENT_PATTERN = Pattern.compile("^\\((.*?):(\\d+)\\)$");

    public List<Group> parse(String filePath) throws IOException {
        List<Group> groups = new ArrayList<>();
        Group currentGroup = null;
        ParseState currentState = null;
        StringBuilder codeSnippet = new StringBuilder();
        this.type = UNKNOWN;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String fullLine;
            String line;
            while ((fullLine = reader.readLine()) != null) {
                line = fullLine.trim();

                if (line.startsWith("Group") || line.startsWith("Core")) { // New Group
                    if (currentGroup != null && !codeSnippet.isEmpty()) {
                        // Assuming any leftover snippet must be a Java element
                        Element previousElement = parseJavaElement(codeSnippet.toString().trim());
                        if (previousElement != null) {
                            currentGroup.addElement(previousElement);
                        }
                        codeSnippet = new StringBuilder();
                    }

                    String groupName = line.replace(":", "").trim();
                    currentGroup = new Group(groupName);
                    groups.add(currentGroup);
                    currentState = null; // Reset state for the new group


                } else if (line.startsWith("Occurrences:") || line.startsWith("Variants:")) { // Occurrences/Variants
                    if (currentGroup != null) currentState = ParseState.READING_OCCURRENCES;

                } else if (line.startsWith("Elements:")) { // Element
                    if (currentGroup != null) currentState = ParseState.READING_ELEMENTS;

                } else if (!line.isEmpty() && currentGroup != null && currentState != null) { // Content line

                    switch (currentState) {

                        case READING_OCCURRENCES:
                            currentGroup.addOccurrence(line);
                            break;

                        case READING_ELEMENTS:
                            if(this.type == ExtractionType.UNKNOWN) {
                                if(JAVA_ELEMENT_PATTERN.matcher(line).matches()) this.type = JAVA;
                                else if (line.contains(";")) this.type = IEC61499;
                                else this.type = ExtractionType.UNKNOWN;
                            }

                            if(this.type == JAVA) {
                                if (JAVA_ELEMENT_PATTERN.matcher(line).matches()) { // Java location
                                    if (!codeSnippet.isEmpty()) {
                                        currentGroup.addElement(parseJavaElement(codeSnippet.toString().trim()));
                                        codeSnippet = new StringBuilder();
                                    }
                                    codeSnippet.append(line).append("\n");
                                } else { // Java code
                                    if (!codeSnippet.isEmpty()) {
                                        codeSnippet.append(fullLine).append("\n");
                                    }
                                }

                            } else if (this.type == IEC61499) { // IEC Element
                                if (!codeSnippet.isEmpty()) {
                                    Element previousElement = parseJavaElement(codeSnippet.toString().trim());
                                    if (previousElement != null) {
                                        currentGroup.addElement(previousElement);
                                    }
                                    codeSnippet = new StringBuilder();
                                }
                                currentGroup.addElement(parseIECElement(line, line));

                            } else {
                                System.err.println("Unexpected line format in Elements: " + line);
                            }

                            break;

                        default:
                            break;
                    }
                }
            }
        }
        if (currentGroup != null && !codeSnippet.isEmpty()) {
            Element lastElement = parseJavaElement(codeSnippet.toString().trim());
            if (lastElement != null) {
                currentGroup.addElement(lastElement);
            }
        }
        return groups;
    }

    private Element parseJavaElement(String snippet) {
        String[] lines = snippet.split("\n");
        if (lines.length > 0) {
            String line = lines[0].trim();
            StringBuilder description = new StringBuilder();
            for (int i = 1; i < lines.length; i++) {
                description.append(lines[i]).append("\n");
            }
            String name = line;
            int lastSlash = line.lastIndexOf('/');
            int colon = line.indexOf(':', Math.max(lastSlash, 0));
            String substring = line.substring(colon + 1, line.length() - 1);
            if (lastSlash != -1 && colon > lastSlash) {
                name = line.substring(lastSlash + 1, colon) + ":" + substring;
            } else if (colon > 0){
                name = "Line " + substring;
            }
            return new Element(name, JAVA, line, description.toString());
        }
        return null;
    }

    private Element parseIECElement(String line, String description) {
        String name = line;
        String location = line;
        if (line.contains("->")) {
            String[] connection = line.split("->");
            int indexOld = connection[0].lastIndexOf(';');
            int indexNew = connection[1].lastIndexOf(';');
            if (indexOld != -1 && indexNew != -1) {
                location = connection[0].substring(0, indexOld);
                name = connection[0].substring(indexOld+1) + " -> " + connection[1].substring(indexNew+1);
            }
        } else {
            int index = line.lastIndexOf(';');
            if (index != -1) {
                location = line.substring(0, index);
                name = line.substring(index+1);
            }
        }
        return new Element(name, IEC61499, location, description);
    }


    private enum ParseState {
        READING_OCCURRENCES,
        READING_ELEMENTS
    }
}