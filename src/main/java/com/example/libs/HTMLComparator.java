package com.example.libs;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HTMLComparator {

    GitAPILibrary apiLibrary = new GitAPILibrary();

    /**
     * Method to compare master and committed html files and create a map with change in elements between files
     *
     * @param masterFile
     * @param fileInCommit
     * @return
     */
    public Map<Element, Element> compareHtml(String masterFile, String fileInCommit) {
        Map<Element, Element> changedElements = new HashMap<>();

        Elements masterElements = this.parseHtml(masterFile).getAllElements();
        Elements committedFileElements = this.parseHtml(fileInCommit).getAllElements();

        // Compare old and new elements by attributes
        for (int i = 0; i < masterElements.size(); i++) {
            Element oldElement = masterElements.get(i);
            Element newElement = committedFileElements.get(i);

            // Check if the tag name, class, or any attribute has changed
            if (!oldElement.tagName().equals(newElement.tagName()) ||
                    !oldElement.attributes().equals(newElement.attributes())) {
                changedElements.put(oldElement, newElement);
            }
        }
        return changedElements;
    }

    public Document parseHtml(String htmlContent) {
        return Jsoup.parse(htmlContent);
    }

    /**
     * Method to create a map of file with map of old and new element in the file
     *
     * @param fileData
     * @return
     */
    public  Map<String, Map<String, String>> generateAttributeMapping(Map<String, Map<Element, Element>> fileData) {
        Map<String, Map<String, String>> output = new HashMap<>();

        for (Map.Entry<String, Map<Element, Element>> fileEntry : fileData.entrySet()) {
            String filePath = fileEntry.getKey();
            Map<Element, Element> elementChanges = fileEntry.getValue();
            Map<String, String> attributeChanges = new HashMap<>();

            for (Map.Entry<Element, Element> elementEntry : elementChanges.entrySet()) {
                Element oldElement = elementEntry.getKey();
                Element newElement = elementEntry.getValue();

                // Extract the attributes from the old and new elements
                String oldAttribute = extractAttribute(oldElement);
                String newAttribute = extractAttribute(newElement);

                // Put the mapping in the inner map
                attributeChanges.put(oldAttribute, newAttribute);
            }

            // Add the processed map for the file
            output.put(filePath, attributeChanges);
        }

        return output;
    }

    // Helper method to extract the attribute (name, id, or class) from the Element
    private String extractAttribute(Element element) {
        if (element.hasAttr("name")) {
            return "name=\"" + element.attr("name") + "\"";
        } else if (element.hasAttr("id")) {
            return "id=\"" + element.attr("id") + "\"";
        } else if (element.hasAttr("class")) {
            return "class=\"" + element.attr("class") + "\"";
        }
        return element.toString(); // Fallback to the string representation of the element
    }

    /**
     * Method to replace the locator with new changes and return map with path of the file and value as file content with changes
     *
     * @param locatorsToReplace
     * @return
     */
    public Map<String, String> updateFilesWithLocatorsChange_Map(Map<String, Map<String, String>> locatorsToReplace){
        Map<String, String> updatedContent = new HashMap<>();
        List<String> locatorFiles = apiLibrary.getListOfLocatorFiles();
        Set<String> files = locatorsToReplace.keySet();
        for(String file : files){
            String fileName = extractFileName(file);
            if(fileName!=null){
                String result = locatorFiles.stream()
                        .filter(s -> s.toLowerCase().contains(fileName.toLowerCase()))
                        .findFirst().get();

                String content = apiLibrary.getFileContentFromMasterAutomation(result);
                System.out.println("Before update \n"+ content);
                Map<String, String> changesToReplace = locatorsToReplace.get(file);
                for(String key: changesToReplace.keySet()){
                    content = content.replaceAll(key.replaceAll("\"","\'"), changesToReplace.get(key).replaceAll("\"","\'"));
                }
                updatedContent.put(result,content);
            }
        }
        return updatedContent;
    }

    public String extractFileName(String path){
        String paths[] = path.split("/");
        return paths[paths.length-1].replace(".html","");
    }
}

