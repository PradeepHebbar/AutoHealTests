package com.example.models;


import lombok.Getter;

import java.util.List;

@Getter
public class ListOfFiles{

    private List<FileDetails> data;

    // Getters and setters for the data field
    public List<FileDetails> getData() {
        return data;
    }

    public void setData(List<FileDetails> data) {
        this.data = data;
    }

    public static class FileDetails {
        private String name;
        private String path;
        private String sha;
        private int size;
        private String url;
        private String html_url;
        private String git_url;
        private String download_url;
        private String type;

        private Links _links;

        // Getters and setters for all fields
        // ... (omitted for brevity)

        public static class Links {
            private String self;
            private String git;
            private String html;

            // Getters and setters for all fields
            // ... (omitted for brevity)

            // Getters and setters omitted for brevity
        }
    }

}

