package gbountyprofilesdesigner.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PassiveResponseProfileProperties {

    private String profile_name = "";
    private boolean enabled = false;
    private String scanner = "";
    private String author = "";
    private String grepsFile = "";
    private List<String> grep = new ArrayList();
    private String show_alert = "";
    private String redir_type = "";
    private int max_redir = 0;
    private String issue_name = "";
    private String issue_severity = "";
    private String issue_confidence = "";
    private String issue_detail = "";
    private String remediation_detail = "";
    private String issue_background = "";
    private String remediation_background = "";
    private List<String> Tags = new ArrayList();


    public PassiveResponseProfileProperties() {
        super();
    }

    public PassiveResponseProfileProperties(PassiveResponseProfileProperties pp) {
        this.profile_name = pp.getProfileName();
        this.enabled = pp.isEnabled();
        this.scanner = pp.getScanner();
        this.author = pp.getAuthor();
        this.grepsFile = pp.getGrepsFile();
        this.grep = pp.getGrep();
        this.show_alert = pp.isShowAlert();
        this.redir_type = pp.getRedirType();
        this.max_redir = pp.getMaxRedir();
        this.issue_name = pp.getIssueName();
        this.issue_severity = pp.getIssueSeverity();
        this.issue_confidence = pp.getIssueConfidence();
        this.issue_detail = pp.getIssueDetail();
        this.remediation_detail = pp.getRemediationDetail();
        this.issue_background = pp.getIssueBackground();
        this.remediation_background = pp.getRemediationBackground();
        this.Tags = pp.getTags();
    }

    public String getProfileName() {
        return profile_name;
    }

    public void setProfileName(String profile_name) {
        this.profile_name = profile_name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getScanner() {
        return scanner;
    }

    public void setScanner(String scanner) {
        this.scanner = scanner;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getGrepsFile() {
        return grepsFile;
    }

    public void setGrepsFile(String grepsFile) {
        this.grepsFile = grepsFile;
    }

    public List<String> getGrep() {
        return grep;
    }

    public void setGrep(List<String> Grep) {
        this.grep = Grep;
    }

    public String isShowAlert() {
        return show_alert;
    }

    public void setShowAlert(String show_alert) {
        this.show_alert = show_alert;
    }

    public String getRedirType() {
        return redir_type;
    }

    public void setRedirType(String redir_type) {
        this.redir_type = redir_type;
    }

    public int getMaxRedir() {
        return max_redir;
    }

    public void setMaxRedir(int max_redir) {
        this.max_redir = max_redir;
    }

    public String getIssueName() {
        return issue_name;
    }

    public void setIssueName(String issue_name) {
        this.issue_name = issue_name;
    }

    public String getIssueSeverity() {
        return issue_severity;
    }

    public void setIssueSeverity(String issue_severity) {
        this.issue_severity = issue_severity;
    }

    public String getIssueConfidence() {
        return issue_confidence;
    }

    public void setIssueConfidence(String issue_confidence) {
        this.issue_confidence = issue_confidence;
    }

    public String getIssueDetail() {
        return issue_detail;
    }

    public void setIssueDetail(String issue_detail) {
        this.issue_detail = issue_detail;
    }

    public String getRemediationDetail() {
        return remediation_detail;
    }

    public void setRemediationDetail(String remediation_detail) {
        this.remediation_detail = remediation_detail;
    }

    public String getIssueBackground() {
        return issue_background;
    }

    public void setIssueBackground(String issue_background) {
        this.issue_background = issue_background;
    }

    public String getRemediationBackground() {
        return remediation_background;
    }

    public void setRemediationBackground(String remediation_background) {
        this.remediation_background = remediation_background;
    }

    public void setTags(List<String> Tags) {
        this.Tags = Tags;
    }

    public void addTag(String tag) {
        if (Tags == null) {
            Tags = new ArrayList();
        }

        if (Tags.contains(tag)) {
            return;
        }

        Tags.add(tag);
    }

    public void removeTag(String tag) {
        if (Tags == null || tag == null) {
            return;
        }
        Tags.remove(tag);
    }

    public List<String> getTags() {
        return Tags.stream().distinct().collect(Collectors.toList());
    }
}
