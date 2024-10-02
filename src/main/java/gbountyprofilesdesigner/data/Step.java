package gbountyprofilesdesigner.data;

import java.util.ArrayList;
import java.util.List;

public class Step {

    private String request_type = "";
    private Boolean reuse_cookie = false;
    private String insertion_point = "";
    private String raw_request = "";
    private List<String> payloads = new ArrayList();
    private String payloads_file = "";
    private String payload_position = "";
    private Boolean change_http_request = false;
    private String change_http_request_type = "";
    private List<String> insertion_points = new ArrayList();
    private boolean header_value = false;
    private List<String> new_headers = new ArrayList();
    private List<Headers> match_replace = new ArrayList();
    private List<String> encoder = new ArrayList();
    private boolean url_encode = false;
    private String chars_to_url_encode = "";
    private String greps_file = "";
    private List<String> grep = new ArrayList();
    private String redir_type = "";
    private int max_redir = 0;
    private String show_alert = "";
    private String issue_name = "";
    private String issue_severity = "";
    private String issue_confidence = "";
    private String issue_detail = "";
    private String remediation_detail = "";
    private String issue_background = "";
    private String remediation_background = "";

    public Step() {
        super();
    }

    public List<Headers> getMatchReplace() {
        return match_replace;
    }

    public boolean getReuseCokie() {
        return reuse_cookie;
    }

    public List<String> getInsertionPointType() {
        return insertion_points;
    }

    public String getRequestType() {
        return request_type;
    }

    public String getInsertionPoint() {
        return insertion_point;
    }

    public String getRawRequest() {
        return raw_request;
    }

    public String isShowAlert() {
        return show_alert;
    }

    public void setShowAlert(String show_alert) {
        this.show_alert = show_alert;
    }

    public String getIssueName() {
        return issue_name;
    }

    public String getIssueSeverity() {
        return issue_severity;
    }

    public String getIssueConfidence() {
        return issue_confidence;
    }

    public String getIssueDetail() {
        return issue_detail;
    }

    public String getIssueBackground() {
        return issue_background;
    }

    public String getRemediationDetail() {
        return remediation_detail;
    }

    public String getRemediationBackground() {
        return remediation_background;
    }

    public void setIssueName(String issuename) {
        issue_name = issuename;
    }

    public void setIssueSeverity(String issueseverity) {
        issue_severity = issueseverity;
    }

    public void setIssueConfidence(String issueconfidence) {
        issue_confidence = issueconfidence;
    }

    public void setIssueDetail(String issuedetail) {
        issue_detail = issuedetail;
    }

    public void setIssueBackground(String issuebackground) {
        issue_background = issuebackground;
    }

    public void setRemediationDetail(String remediationdetail) {
        remediation_detail = remediationdetail;
    }

    public void setRemediationBackground(String remediationbackground) {
        remediation_background = remediationbackground;
    }

    public String getPayloadPosition() {
        return payload_position;
    }

    public List<String> getPayloads() {
        return payloads;
    }

    public List<String> getNewHeaders() {
        return new_headers;
    }

    public List<String> getEncoder() {
        return encoder;
    }

    public String getCharsToUrlEncode() {
        return chars_to_url_encode;
    }

    public String getPayloadsFile() {
        return payloads_file;
    }

    public String getGrepFile() {
        return greps_file;
    }

    public List<String> getGrep() {
        return grep;
    }

    public boolean getchangeHttpRequest() {
        return change_http_request;
    }

    public String getchangeHttpRequestType() {
        return change_http_request_type;
    }

    public boolean getisHeaderValue() {
        return header_value;
    }

    public boolean getUrlEncode() {
        return url_encode;
    }

    public String getRedirection() {
        return redir_type;
    }

    public int getMaxRedir() {
        return max_redir;
    }

    //Set functions
    public void setMatchReplace(List<Headers> header) {
        match_replace = header;
    }

    public void setInsertionPointType(List<String> insertionPointType) {
        insertion_points = insertionPointType;
    }

    public void setReuseCookie(boolean reusecookie) {
        reuse_cookie = reusecookie;
    }

    public void setInsertionPoint(String insertionpoint) {
        insertion_point = insertionpoint;
    }

    public void setRequestType(String requesttype) {
        request_type = requesttype;
    }

    public void setRawRequest(String rawrequest) {
        raw_request = rawrequest;
    }

    public void setPayloadPosition(String payloadposition) {
        payload_position = payloadposition;
    }

    public void setPayloads(List<String> Payloads) {
        payloads = Payloads;
    }

    public void setNewHeaders(List<String> newheaders) {
        new_headers = newheaders;
    }

    public void setEncoder(List<String> Encoder) {
        encoder = Encoder;
    }

    public void setCharsToUrlEncode(String charstourlencode) {
        chars_to_url_encode = charstourlencode;
    }

    public void setPayloadsFile(String payloadsfile) {
        payloads_file = payloadsfile;
    }

    public void setGrepFile(String grepsfile) {
        greps_file = grepsfile;
    }

    public void setGrep(List<String> Grep) {
        grep = Grep;
    }

    public void setisHeaderValue(boolean isheadervalue) {
        header_value = isheadervalue;
    }

    public void setchangeHttpRequest(boolean changehttprequest) {
        change_http_request = changehttprequest;
    }

    public void setchangeHttpRequestType(String changehttprequesttype) {
        change_http_request_type = changehttprequesttype;
    }

    public void setUrlEncode(boolean urlencode) {
        url_encode = urlencode;
    }

    public void setRedirType(String redirtype) {
        redir_type = redirtype;
    }

    public void setMaxRedir(int maxredir) {
        max_redir = maxredir;
    }

}
