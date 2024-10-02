package gbountyprofilesdesigner.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PassiveRequestProfileProperties {

    private String profile_name = "";
    private boolean enabled = false;
    private String scanner = "";
    private String author = "";
    private String match_type = "";
    private String grepsFile = "";
    private List<String> Grep = new ArrayList();
    private Boolean not_show_issue = false;
    private String issue_name = "";
    private String issue_severity = "";
    private String issue_confidence = "";
    private String issue_detail = "";
    private String remediation_detail = "";
    private String issue_background = "";
    private String remediation_background = "";
    private List<String> Tags = new ArrayList();

    public PassiveRequestProfileProperties() {
        super();
    }

    public PassiveRequestProfileProperties(PassiveRequestProfileProperties pp) {
        this.profile_name = pp.getProfileName();
        this.enabled = pp.isEnabled();
        this.scanner = pp.getScanner();
        this.author = pp.getAuthor();
        this.match_type = pp.getMatchType();
        this.grepsFile = pp.getGrepsFile();
        this.Grep = pp.getGrep();
        this.not_show_issue = pp.getNotShowIssue();
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

    public String getMatchType() {
        return match_type;
    }

    public void setMatchType(String match_type) {
        this.match_type = match_type;
    }

    public String getGrepsFile() {
        return grepsFile;
    }

    public void setGrepsFile(String grepsFile) {
        this.grepsFile = grepsFile;
    }

    public List<String> getGrep() {
        return Grep;
    }

    public void setGrep(List<String> Grep) {
        this.Grep = Grep;
    }

    public Boolean getNotShowIssue() {
        return not_show_issue;
    }

    public void setNotShowIssue(Boolean show_issue) {
        this.not_show_issue = show_issue;
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
