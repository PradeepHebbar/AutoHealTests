package com.example.libs;

import com.example.models.CommitResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class GitAPILibrary {

    private static final String GITHUB_TOKEN = "*******";
    private static final String repoOwner = "pv-platforma";
    private static final String devRepoName = "sampleLoginApplication";
    private static final String automationRepoName = "demoTestProject";
    private static final String locatorClassesFilePath = "/src/test/java/com/example/locator";

    /**
     * Method to get the file content in string format based on file path and commit sha
     *
     * @param filePath
     * @param commitSha
     * @return
     */
    public String getFileContentInCommit(String filePath, String commitSha) {
        String decodedContent = null;
        try {
            // Build the URL for the commit API
            String urlString = String.format("https://api.github.com/repos/%s/%s/contents/%s?ref=%s",
                    repoOwner, devRepoName, filePath,commitSha);
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "token " + GITHUB_TOKEN);
            connection.setRequestProperty("Accept", "application/json");

            // Read the response
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            ObjectMapper objectMapper = new ObjectMapper();
            CommitResponse response = objectMapper.readValue(content.toString(), CommitResponse.class);
            decodedContent  =  this.decodeBase64(response.getContent());

            connection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return decodedContent;
    }

    /**
     * Method to get the file content from the master branch
     *
     * @param filePath
     * @return
     */
    public String getFileContentFromMaster(String filePath) {
        String decodedContent = null;
        try {
            // Build the URL for the commit API
            String urlString = String.format("https://api.github.com/repos/%s/%s/contents/%s",
                    repoOwner, devRepoName, filePath);
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "token " + GITHUB_TOKEN);
            connection.setRequestProperty("Accept", "application/json");

            // Read the response
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            ObjectMapper objectMapper = new ObjectMapper();
            CommitResponse response = objectMapper.readValue(content.toString(), CommitResponse.class);

            decodedContent  =  this.decodeBase64(response.getContent());

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return decodedContent;
    }


    /**
     * Method to get list of files from the given path
     *
     * @return
     */
    public List<String> getListOfLocatorFiles() {
        List<String> files = new ArrayList<>();
        try {
            // Build the URL for the commit API
            String urlString = String.format("https://api.github.com/repos/%s/%s/contents/%s?recursive=true",
                    repoOwner, automationRepoName, locatorClassesFilePath);
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "token " + GITHUB_TOKEN);
            connection.setRequestProperty("Accept", "application/json");

            // Read the response
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            JSONArray jsonArray = new JSONArray(content.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject fileObject = jsonArray.getJSONObject(i);
                String filePath = fileObject.getString("path");
                files.add(filePath);
            }
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return files;
    }

    /**
     * Method to get the file content from the automation repo master branch
     *
     * @param filePath
     * @return
     */
    public String getFileContentFromMasterAutomation(String filePath) {
        String decodedContent = null;
        try {
            // Build the URL for the commit API
            String urlString = String.format("https://api.github.com/repos/%s/%s/contents/%s",
                    repoOwner, automationRepoName, filePath);
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "token " + GITHUB_TOKEN);
            connection.setRequestProperty("Accept", "application/json");

            // Read the response
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            ObjectMapper objectMapper = new ObjectMapper();
            CommitResponse response = objectMapper.readValue(content.toString(), CommitResponse.class);

            decodedContent  =  this.decodeBase64(response.getContent());

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return decodedContent;
    }

    /**
     * Method to create new branch from master branch
     *
     * @param newBranchName
     * @return
     */
    public String createBranchFromMaster(String newBranchName) {
        try {
            // Get the SHA of the master branch
            String sha = getBranchSHA("main");

            // Now create a new branch with this SHA
            String urlString = String.format("https://api.github.com/repos/%s/%s/git/refs", repoOwner, automationRepoName);
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "token " + GITHUB_TOKEN);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");

            // Request body to create a new branch
            String payload = String.format("{\"ref\":\"refs/heads/%s\",\"sha\":\"%s\"}", newBranchName, sha);

            connection.setDoOutput(true);
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = payload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Read the response
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = in.readLine()) != null) {
                response.append(responseLine.trim());
            }

            System.out.println("Branch Created: " + response.toString());
            connection.disconnect();
            return newBranchName;

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }

    /**
     * Method to get the sha of branch by name
     * @param branchName
     * @return
     * @throws IOException
     */
    public String getBranchSHA(String branchName) throws IOException {
        String urlString = String.format("https://api.github.com/repos/%s/%s/git/refs/heads/%s", repoOwner, automationRepoName, branchName);
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "token " + GITHUB_TOKEN);
        connection.setRequestProperty("Accept", "application/json");

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
        StringBuilder response = new StringBuilder();
        String responseLine;
        while ((responseLine = in.readLine()) != null) {
            response.append(responseLine.trim());
        }

        // Parse the response to extract the SHA
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(response.toString());
        return jsonNode.get("object").get("sha").asText();
    }


    /**
     * Update the locator file with new change and commit the changes
     *
     * @param filePath
     * @param updatedContent
     * @param branchName
     * @param sha
     */
    public void updateFileAndCommit(String filePath, String updatedContent, String branchName, String sha) {
        try {
            String urlString = String.format("https://api.github.com/repos/%s/%s/contents/%s", repoOwner, automationRepoName, filePath);
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Authorization", "token " + GITHUB_TOKEN);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");

            String commitMessage = "Updated file: " + filePath;

            // Create the request payload
            String payload = String.format("{\"message\":\"%s\",\"content\":\"%s\",\"sha\":\"%s\",\"branch\":\"%s\"}",
                    commitMessage, encodeBase64(updatedContent.getBytes(StandardCharsets.UTF_8)), sha, branchName);

            connection.setDoOutput(true);
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = payload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Read the response
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = in.readLine()) != null) {
                response.append(responseLine.trim());
            }

            System.out.println("File updated: " + response.toString());
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }


    /**
     * Method to create pull request - automation project
     *
     * @param title
     * @param headBranch
     * @param baseBranch
     * @param body
     */
    public void createPullRequest(String title, String headBranch, String baseBranch, String body) {
        try {
            String urlString = String.format("https://api.github.com/repos/%s/%s/pulls", repoOwner, automationRepoName);
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "token " + GITHUB_TOKEN);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");

            // Create the request payload
            String payload = String.format("{\"title\":\"%s\",\"head\":\"%s\",\"base\":\"%s\",\"body\":\"%s\"}",
                    title, headBranch, baseBranch, body);

            connection.setDoOutput(true);
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = payload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Read the response
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = in.readLine()) != null) {
                response.append(responseLine.trim());
            }

            System.out.println("Pull Request Created: " + response.toString());
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }


    /**
     * Method to get the file sha based on file path - automation project
     *
     * @param filePath
     * @return
     */
    public String getFileSha(String filePath) {
        String fileSha = null;
        try {
            // Build the URL for the commit API
            String urlString = String.format("https://api.github.com/repos/%s/%s/contents/%s",
                    repoOwner, automationRepoName,filePath);
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "token " + GITHUB_TOKEN);
            connection.setRequestProperty("Accept", "application/json");

            // Read the response
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            JSONObject fileObject = new JSONObject(content.toString());
            fileSha = fileObject.getString("sha");
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return fileSha;
    }

    private String encodeBase64(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    public String decodeBase64(String encodedString) {
        byte[] decodedBytes = Base64.getDecoder().decode(encodedString.replaceAll("\n",""));
        return new String(decodedBytes,
                java.nio.charset.StandardCharsets.UTF_8);

    }

}

