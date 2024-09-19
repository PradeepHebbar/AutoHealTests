package com.example.ngrokjava;
import com.example.libs.GitAPILibrary;
import com.example.libs.HTMLComparator;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
public class WebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);
    Format formatter = new SimpleDateFormat("yyyy_MM_ddHHmmss");
    private final String branchName = "testCommitBranch_"+formatter.format(new java.util.Date());
    GitAPILibrary gitApiLibrary = new GitAPILibrary();
    HTMLComparator htmlComparator = new HTMLComparator();
    static String commitUrl = null;


    /**
     * Receive any post request made by github webhook based on configuration
     * @param headers
     * @param payload
     * @return
     */
    @PostMapping("/webhook")
    public String handleWebhook(
            @RequestHeader Map<String, String> headers,
            @RequestBody Map<String, Object> payload) {

        Map<String, Map<String, String>> elementToReplaceInFileMap;
        Map<String, Map<Element, Element>> filePathWithElementChangeMap = new HashMap<>();

        // Log the received payload (JSON data)
        logger.info("Received GitHub Webhook: {}", payload);
        List<String> changedFiles = getListOfChangedFiles(payload);
        System.out.println("Number of files changed are: "+ changedFiles.size());

        if(!changedFiles.isEmpty()){

            elementToReplaceInFileMap = getLocatorToUpdateMap(payload, changedFiles, filePathWithElementChangeMap);

            gitApiLibrary.createBranchFromMaster(branchName);
            Map<String, String> updateFileContentMap = htmlComparator.updateFilesWithLocatorsChange_Map(elementToReplaceInFileMap);
            for(String fileName : updateFileContentMap.keySet()){
                String fileSha = gitApiLibrary.getFileSha(fileName);
                gitApiLibrary.updateFileAndCommit(fileName,updateFileContentMap.get(fileName),branchName,fileSha);
            }
            gitApiLibrary.createPullRequest("Automated pull request based on dev UI change "+formatter.format(new java.util.Date()),branchName,"main",String.format("This PR contains changes in locator class based on the changes in dev html files. Dev changes can be found here - %s",commitUrl));
        }
        return "Webhook received";
    }

    /**
     * Method to get the list of modified files in the commit from webhook payload
     * @param payload
     * @return
     */
    public static List<String> getListOfChangedFiles(Map<String, Object> payload) {
        List<String> changedFiles = new ArrayList<>();
        // Extract the commits list from the payload
        List<Map<String, Object>> commits = (List<Map<String, Object>>) payload.get("commits");

        if (commits != null) {
            for (Map<String, Object> commit : commits) {
                // Extract the list of modified files
                List<String> modifiedFiles = (List<String>) commit.get("modified");
                commitUrl = (String) commit.get("url");

                if (modifiedFiles != null && !modifiedFiles.isEmpty()) {
                    System.out.println("Modified files in the commit:");
                    for (String file : modifiedFiles) {
                        System.out.println("- " + file);
                        changedFiles.add(file);
                    }
                } else {
                    System.out.println("No files modified in this commit.");
                }
            }
        } else {
            System.out.println("No commits found in the payload.");
        }

        return  changedFiles;
    }

    /**
     * Method to read all html files in the commit and create a map of file path with map of element changes in it.
     *
     * @param payload
     * @param changedFiles
     * @param filePathWithElementChangeMap
     * @return
     */
    private Map<String, Map<String, String>> getLocatorToUpdateMap(Map<String, Object> payload, List<String> changedFiles, Map<String, Map<Element, Element>> filePathWithElementChangeMap) {
        Map<String, Map<String, String>> elementToReplaceInFileMap;
        String gitCommitId = getCommitSha(payload);
        System.out.println("Git commit id is: "+ gitCommitId);

        for(String path: changedFiles){
            if (path.contains(".html")) {
                String commitFileContent = gitApiLibrary.getFileContentInCommit(path, gitCommitId);
                String masterFileContent = gitApiLibrary.getFileContentFromMaster(path);
                System.out.println("content read");

                // Compare HTML and get changed elements
                Map<Element, Element> changedElements = htmlComparator.compareHtml(masterFileContent, commitFileContent);
                System.out.println("changedElements: "+changedElements);

                filePathWithElementChangeMap.put(path,changedElements);

                for (Map.Entry<Element, Element> entry : changedElements.entrySet()) {
                    System.out.println("Changed element: " + entry.getKey());
                    System.out.println("New element: " + entry.getValue().outerHtml());
                }

            }

        }
        elementToReplaceInFileMap = htmlComparator.generateAttributeMapping(filePathWithElementChangeMap);
        return elementToReplaceInFileMap;
    }

    /**
     * Method to get the commit sha from the webhook payload
     *
     * @param payload
     * @return
     */
    public static String getCommitSha(Map<String, Object> payload) {
        String commitSha;
        List<Map<String, Object>> commits = (List<Map<String, Object>>) payload.get("commits");
        commitSha = commits.get(0).get("id").toString();
        return  commitSha;
    }

}

