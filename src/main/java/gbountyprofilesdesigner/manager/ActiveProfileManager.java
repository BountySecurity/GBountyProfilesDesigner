package gbountyprofilesdesigner.manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;

import gbountyprofilesdesigner.properties.ActiveProfileProperties;
import gbountyprofilesdesigner.data.Headers;
import gbountyprofilesdesigner.data.Step;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ActiveProfileManager {

    private String name;
    private String issuename;
    private String issuedetail;
    private String issuebackground;
    private String remediationdetail;
    private String remediationbackground;
    private String issueseverity;
    private String issueconfidence;
    private boolean isHeaderValue;
    private String payloadsfile;
    private String author;
    private String profiles_directory;
    private List<Headers> headers;
    private List<String> insertionPointType;
    private List<String> tags;

    public ActiveProfileManager(String profiles_directory) {
        this.profiles_directory = profiles_directory;

        name = "";
        issuename = "";
        issuedetail = "";
        issuebackground = "";
        remediationdetail = "";
        remediationbackground = "";
        issueseverity = "";
        issueconfidence = "";
        payloadsfile = "";
        author = "";
        isHeaderValue = false;
        headers = new ArrayList();
        insertionPointType = new ArrayList();
        tags = new ArrayList();
    }

    public void setActiveAttackValues(ActiveProfile profile, ActiveProfileProperties profile_property) {
        headers = new ArrayList();

        insertionPointType = new ArrayList();
        //Save attack with fields values
        name = profile_property.getProfileName();
        author = profile_property.getAuthor();

        tags = profile_property.getTags();

        profile.textname.setText(name);
        profile.textauthor.setText(author);

        if (tags != null) {
            for (String t : tags) {
                profile.tag.addElement(t);
            }
        }

        List<Step> step_data = profile_property.getStep();
        profile.setRequestResponseClass(step_data.size() - 1);
        List<RequestResponse> all_steps = profile.getRequestResponseClass();
        for (int i = 0; i < all_steps.size(); i++) {
            RequestResponse one_step = all_steps.get(i);
            Step step = step_data.get(i);
            headers = new ArrayList();
            insertionPointType = new ArrayList();

            issuename = step.getIssueName();
            issueseverity = step.getIssueSeverity();
            issueconfidence = step.getIssueConfidence();
            issuedetail = step.getIssueDetail();
            issuebackground = step.getIssueBackground();
            remediationdetail = step.getRemediationDetail();
            remediationbackground = step.getRemediationBackground();

            
            if (issueseverity.equals("High")) {
                one_step.jComboBox2.setSelectedIndex(0);
            } else if (issueseverity.equals("Medium")) {
                one_step.jComboBox2.setSelectedIndex(1);
            } else if (issueseverity.equals("Low")) {
                one_step.jComboBox2.setSelectedIndex(2);
            } else if (issueseverity.equals("Information")) {
                one_step.jComboBox2.setSelectedIndex(3);
            } else {
                one_step.jComboBox2.setSelectedIndex(0);  // Default to "High"
            }

            if (issueconfidence.equals("Certain")) {
                one_step.jComboBox3.setSelectedIndex(0);
            } else if (issueconfidence.equals("Firm")) {
                one_step.jComboBox3.setSelectedIndex(1);
            } else if (issueconfidence.equals("Tentative")) {
                one_step.jComboBox3.setSelectedIndex(2);
            } else {
                one_step.jComboBox3.setSelectedIndex(0);  // Default to "Certain"
            }

            one_step.text4.setText(issuename);
            one_step.textarea1.setText(issuedetail);
            one_step.textarea2.setText(issuebackground);
            one_step.textarea3.setText(remediationdetail);
            one_step.textarea4.setText(remediationbackground);

            String showAlert = step.isShowAlert();

            if (showAlert.equals("none")) {
                one_step.jComboBox1.setSelectedIndex(2);
            } else if (showAlert.equals("one")) {
                one_step.jComboBox1.setSelectedIndex(1);
            } else if (showAlert.equals("always")) {
                one_step.jComboBox1.setSelectedIndex(0);
            }else{
                one_step.jComboBox1.setSelectedIndex(0);
            }

            if (step.getRequestType().contains("original")) {
                one_step.request_position.setSelectedIndex(0);
            } else {
                one_step.request_position.setSelectedIndex(1);
            }

            one_step.txt_rawrequest.setText(step.getRawRequest());

            if (step.getInsertionPoint().contains("same")) {
                one_step.same_insertion_point.setSelected(true);
            } else {
                one_step.any_insertion_point.setSelected(true);
            }

            if (!payloadsfile.isEmpty()) {
                loadPath(payloadsfile, one_step.payload);
                updatePayloads(payloadsfile, step, profile_property);

            } else {
                for (String pay : step.getPayloads()) {
                    one_step.payload.addElement(pay);
                }
            }

            if (step.getPayloadPosition().contains("replace")) {
                one_step.payload_position_combo.setSelectedIndex(0);
            } else if (step.getPayloadPosition().contains("append")) {
                one_step.payload_position_combo.setSelectedIndex(1);
            } else if (step.getPayloadPosition().contains("insert")) {
                one_step.payload_position_combo.setSelectedIndex(2);
            }

            if (step.getchangeHttpRequestType().contains("post_to_get")) {
                one_step.combo_changeHTTP.setSelectedIndex(0);
            } else if (step.getchangeHttpRequestType().contains("get_to_post")) {
                one_step.combo_changeHTTP.setSelectedIndex(1);
            } else if (step.getchangeHttpRequestType().contains("get_post_get")) {
                one_step.combo_changeHTTP.setSelectedIndex(2);
            }

            one_step.txt_changeHTTP.setSelected(step.getchangeHttpRequest());

            if (step.getRequestType().contains("original")) {
                one_step.request_position.setSelectedIndex(0);
            } else if (step.getRequestType().contains("raw_request")) {
                one_step.request_position.setSelectedIndex(1);
            }

            one_step.grep.removeAllElements();
            one_step.payload.removeAllElements();
            one_step.encoder.removeAllElements();

            one_step.textpayloads.setText(step.getPayloadsFile());
            one_step.textgreps.setText(step.getGrepFile());

            one_step.showGreps(step.getGrep());
            one_step.showPayloads(step.getPayloads());
            one_step.showNewHeaders(step.getNewHeaders());
            one_step.showHeaders(step.getMatchReplace());

            if (!payloadsfile.isEmpty()) {
                loadPath(payloadsfile, one_step.payload);
                updatePayloads(payloadsfile, step, profile_property);

            } else {
                for (String pay : step.getPayloads()) {
                    one_step.payload.addElement(pay);
                }
            }

            for (String enc : step.getEncoder()) {
                one_step.encoder.addElement(enc);
            }

            one_step.check8.setSelected(step.getUrlEncode());
            one_step.text5.setText(step.getCharsToUrlEncode());

            String redirType = step.getRedirection();

            if (redirType.equals("never")) {
                one_step.rb1.setSelected(true);
            } else if (redirType.equals("on_site")) {
                one_step.rb2.setSelected(true);
            } else if (redirType.equals("in-scope")) {
                one_step.rb3.setSelected(true);
            } else if (redirType.equals("always")) {
                one_step.rb4.setSelected(true);
            } else {
                one_step.rb1.setSelected(true);
            }

            one_step.sp1.setValue(step.getMaxRedir());

            List<String> insertion_points = step.getInsertionPointType();

            if (insertion_points.contains("all")) {
                one_step.All.setSelected(true);
            }
            if (insertion_points.contains("single_path_discovery")) {
                one_step.single_extensionprovided.setSelected(true);
            }
            if (insertion_points.contains("extension_provice")) {
                one_step.extensionprovided.setSelected(true);
            }
            if (insertion_points.contains("user_agent")) {
                one_step.useragent.setSelected(true);
            }
            if (insertion_points.contains("entire_body")) {
                one_step.entirebody.setSelected(true);
            }
            if (insertion_points.contains("param_body")) {
                one_step.parambody.setSelected(true);
            }
            if (insertion_points.contains("param_cookie")) {
                one_step.paramcookie.setSelected(true);
            }
            if (insertion_points.contains("param_json")) {
                one_step.paramjson.setSelected(true);
            }
            if (insertion_points.contains("url_path_folder")) {
                one_step.urlpathfolder.setSelected(true);
            }
            if (insertion_points.contains("param_multipart_attr")) {
                one_step.parammultipartattr.setSelected(true);
            }
            if (insertion_points.contains("param_name_body")) {
                one_step.paramnamebody.setSelected(true);
            }
            if (insertion_points.contains("param_name_url")) {
                one_step.paramnameurl.setSelected(true);
            }
            if (insertion_points.contains("user_provided")) {
                one_step.userprovided.setSelected(true);
            }
            if (insertion_points.contains("param_url")) {
                one_step.paramurl.setSelected(true);
            }
            if (insertion_points.contains("param_xml")) {
                one_step.paramxml.setSelected(true);
            }
            if (insertion_points.contains("param_xml_attr")) {
                one_step.paramxmlattr.setSelected(true);
            }
            if (insertion_points.contains("url_path_filename")) {
                one_step.urlpathfilename.setSelected(true);
            }
            if (insertion_points.contains("param_name_cookie")) {
                one_step.paramnamecookie.setSelected(true);
            }
            if (insertion_points.contains("param_name_xml")) {
                one_step.paramnamexml.setSelected(true);
            }
            if (insertion_points.contains("param_name_xml_attr")) {
                one_step.paramnamexmlattr.setSelected(true);
            }
            if (insertion_points.contains("param_name_multi_part_attr")) {
                one_step.paramnamemultipartattr.setSelected(true);
            }
            if (insertion_points.contains("param_name_json")) {
                one_step.paramnamejson.setSelected(true);
            }
            if (insertion_points.contains("referer")) {
                one_step.referer.setSelected(true);
            }
            if (insertion_points.contains("origin")) {
                one_step.origin.setSelected(true);
            }
            if (insertion_points.contains("host")) {
                one_step.host.setSelected(true);
            }
            if (insertion_points.contains("content_type")) {
                one_step.contenttype.setSelected(true);
            }
            if (insertion_points.contains("accept")) {
                one_step.accept.setSelected(true);
            }
            if (insertion_points.contains("accept_language")) {
                one_step.acceptlanguage.setSelected(true);
            }
            if (insertion_points.contains("accept_encoding")) {
                one_step.acceptencoding.setSelected(true);
            }

            if (insertion_points.contains("entire_body_xml")) {
                one_step.entirebodyxml.setSelected(true);
            }
            if (insertion_points.contains("entire_body_json")) {
                one_step.entirebodyjson.setSelected(true);
            }
            if (insertion_points.contains("entire_body_multipart")) {
                one_step.entirebodymultipart.setSelected(true);
            }
        }
    }

    public ActiveProfileProperties saveActiveAttackValues(ActiveProfile profile) {
        headers = new ArrayList();
        insertionPointType = new ArrayList();
        //Save attack with fields values
        ActiveProfileProperties properties = new ActiveProfileProperties();

        List<Step> steps = new ArrayList<>();

        properties.setProfileName(profile.textname.getText());
        properties.setAuthor(profile.textauthor.getText());
        properties.setScanner("active");
        properties.setEnabled(true);

        List tags = new ArrayList();
        for (int i = 0; i < profile.listtag.getModel().getSize(); i++) {
            Object item = profile.listtag.getModel().getElementAt(i);
            if (!item.toString().isEmpty()) {
                tags.add(item.toString().replaceAll("\r", "").replaceAll("\n", ""));
            }
        }
        if (!tags.contains("All")) {
            tags.add("All");
            properties.setTags(tags);
        } else {
            properties.setTags(tags);
        }

        List<RequestResponse> all_steps = profile.getRequestResponseClass();
        for (RequestResponse one_step : all_steps) {
            headers = new ArrayList();
            insertionPointType = new ArrayList();
            Step step_data = new Step();

            step_data.setIssueName(one_step.text4.getText());
            step_data.setIssueDetail(one_step.textarea1.getText());
            step_data.setIssueBackground(one_step.textarea2.getText());
            step_data.setRemediationDetail(one_step.textarea3.getText());
            step_data.setRemediationBackground(one_step.textarea4.getText());

            if (one_step.jComboBox2.getSelectedIndex() == 0) {
                step_data.setIssueSeverity("High");
            } else if (one_step.jComboBox2.getSelectedIndex() == 1) {
                step_data.setIssueSeverity("Medium");
            } else if (one_step.jComboBox2.getSelectedIndex() == 2) {
                step_data.setIssueSeverity("Low");
            } else if (one_step.jComboBox2.getSelectedIndex() == 3) {
                step_data.setIssueSeverity("Information");
            } else {
                step_data.setIssueSeverity("High");
            }

            if (one_step.jComboBox3.getSelectedIndex() == 0) {
                step_data.setIssueConfidence("Certain");
            } else if (one_step.jComboBox3.getSelectedIndex() == 1) {
                step_data.setIssueConfidence("Firm");
            } else if (one_step.jComboBox3.getSelectedIndex() == 2) {
                step_data.setIssueConfidence("Tentative");
            } else {
                step_data.setIssueConfidence("Certain");
            }

            if (one_step.jComboBox1.getSelectedIndex() == 2) {
                step_data.setShowAlert("none");
            } else if (one_step.jComboBox1.getSelectedIndex() == 1) {
                step_data.setShowAlert("one");
            } else if (one_step.jComboBox1.getSelectedIndex() == 0) {
                step_data.setShowAlert("always");
            }else{
                step_data.setShowAlert("always");
            }

            if (one_step.request_position.getSelectedIndex() == 0) {
                step_data.setRequestType("original");
                step_data.setRawRequest("");
            } else if (one_step.request_position.getSelectedIndex() == 1) {
                step_data.setRequestType("raw_request");
                step_data.setRawRequest(one_step.txt_rawrequest.getText());

            }

            if (one_step.same_insertion_point.isSelected()) {
                step_data.setInsertionPoint("same");
            } else {
                step_data.setInsertionPoint("any");
            }

            if (one_step.payload_position_combo.getSelectedItem().toString().equals("Replace")) {
                step_data.setPayloadPosition("replace");
            } else if (one_step.payload_position_combo.getSelectedItem().toString().equals("Append")) {
                step_data.setPayloadPosition("append");
            } else if (one_step.payload_position_combo.getSelectedItem().toString().equals("Insert")) {
                step_data.setPayloadPosition("insert");
            }

            if (one_step.combo_changeHTTP.getSelectedItem().toString().equals("POST to GET")) {
                step_data.setchangeHttpRequestType("post_to_get");
            } else if (one_step.combo_changeHTTP.getSelectedItem().toString().equals("GET to POST")) {
                step_data.setchangeHttpRequestType("get_to_post");
            } else if (one_step.combo_changeHTTP.getSelectedItem().toString().equals("GET <-> POST")) {
                step_data.setchangeHttpRequestType("get_post_get");
            }

            List encoders = new ArrayList();
            List payloads = new ArrayList();
            List newHeaders = new ArrayList();
            List greps = new ArrayList();

            step_data.setPayloadsFile(one_step.textpayloads.getText());

            for (int i = 0; i < one_step.modelpayload.getRowCount(); i++) {
                Object column0Value = one_step.modelpayload.getValueAt(i, 0);
                Object column1Value = one_step.modelpayload.getValueAt(i, 1);

                if (column0Value != null && column1Value != null && !column1Value.toString().isEmpty()) {
                    payloads.add(column0Value.toString() + "," + column1Value.toString());
                }
            }

            step_data.setPayloads(payloads);

            if (one_step.modelnewheaders.getRowCount() > 0) {
                for (int i = 0; i < one_step.modelnewheaders.getRowCount(); i++) {
                    newHeaders.add(one_step.modelnewheaders.getValueAt(i, 0).toString());
                }
            }

            step_data.setNewHeaders(newHeaders);

            step_data.setGrepFile(one_step.textgreps.getText());
            for (int i = 0; i < one_step.modelgrep.getRowCount(); i++) {
                Object column0Value = one_step.modelgrep.getValueAt(i, 0);
                Object column1Value = one_step.modelgrep.getValueAt(i, 1);
                Object column2Value = one_step.modelgrep.getValueAt(i, 2);
                Object column3Value = one_step.modelgrep.getValueAt(i, 3);
                Object column4Value = one_step.modelgrep.getValueAt(i, 4);

                greps.add(column0Value.toString() + "," + column1Value.toString() + "," + column2Value.toString() + "," + column3Value.toString() + "," + column4Value.toString());
            }

            step_data.setGrep(greps);

            for (int row = 0; row < one_step.model4.getRowCount(); row++) {
                headers.add(new Headers((String) one_step.model4.getValueAt(row, 0), (String) one_step.model4.getValueAt(row, 1), (String) one_step.model4.getValueAt(row, 2), (String) one_step.model4.getValueAt(row, 3)));
            }

            step_data.setMatchReplace(headers);

            for (int i = 0; i < one_step.list3.getModel().getSize(); i++) {
                Object item = one_step.list3.getModel().getElementAt(i);
                if (item != null && !item.toString().isEmpty()) {
                    encoders.add(item.toString().replaceAll("\r", "").replaceAll("\n", ""));
                }
            }

            step_data.setEncoder(encoders);
            step_data.setCharsToUrlEncode(one_step.text5.getText());
            step_data.setUrlEncode(one_step.check8.isSelected());

            if (one_step.rb1.isSelected()) {
                step_data.setRedirType("never");
            } else if (one_step.rb2.isSelected()) {
                step_data.setRedirType("on_site");
            } else if (one_step.rb3.isSelected()) {
                step_data.setRedirType("in-scope");
            } else if (one_step.rb4.isSelected()) {
                step_data.setRedirType("always");
            } else {
                step_data.setRedirType("never");
            }

            if (one_step.txt_changeHTTP.isSelected()) {
                step_data.setchangeHttpRequest(true);
            } else {
                step_data.setchangeHttpRequest(false);
            }

            if (one_step.All.isSelected()) {
                insertionPointType.add("all");
            }

            if (one_step.single_extensionprovided.isSelected()) {
                insertionPointType.add("single_path_discovery");
            }

            if (one_step.extensionprovided.isSelected()) {
                insertionPointType.add("extension_provice");
            }

            if (one_step.useragent.isSelected()) {
                insertionPointType.add("user_agent");
            }
            if (one_step.entirebody.isSelected()) {
                insertionPointType.add("entire_body");
            }
            if (one_step.parambody.isSelected()) {
                insertionPointType.add("param_body");
            }
            if (one_step.paramcookie.isSelected()) {
                insertionPointType.add("param_cookie");
            }
            if (one_step.paramjson.isSelected()) {
                insertionPointType.add("param_json");
            }
            if (one_step.urlpathfolder.isSelected()) {
                insertionPointType.add("url_path_folder");
            }
            if (one_step.parammultipartattr.isSelected()) {
                insertionPointType.add("param_multipart_attr");
            }
            if (one_step.paramnamebody.isSelected()) {
                insertionPointType.add("param_name_body");
            }
            if (one_step.paramnameurl.isSelected()) {
                insertionPointType.add("param_name_url");
            }
            if (one_step.userprovided.isSelected()) {
                insertionPointType.add("user_provided");
            }
            if (one_step.paramurl.isSelected()) {
                insertionPointType.add("param_url");
            }
            if (one_step.paramxml.isSelected()) {
                insertionPointType.add("param_xml");
            }
            if (one_step.paramxmlattr.isSelected()) {
                insertionPointType.add("param_xml_attr");
            }
            if (one_step.urlpathfilename.isSelected()) {
                insertionPointType.add("url_path_filename");
            }
            if (one_step.paramnamecookie.isSelected()) {
                insertionPointType.add("param_name_cookie");
            }
            if (one_step.paramnamexml.isSelected()) {
                insertionPointType.add("param_name_xml");
            }
            if (one_step.paramnamexmlattr.isSelected()) {
                insertionPointType.add("param_name_xml_attr");
            }
            if (one_step.paramnamemultipartattr.isSelected()) {
                insertionPointType.add("param_name_multi_part_attr");
            }
            if (one_step.paramnamejson.isSelected()) {
                insertionPointType.add("param_name_json");
            }
            if (one_step.referer.isSelected()) {
                insertionPointType.add("referer");
            }
            if (one_step.origin.isSelected()) {
                insertionPointType.add("origin");
            }
            if (one_step.host.isSelected()) {
                insertionPointType.add("host");
            }
            if (one_step.contenttype.isSelected()) {
                insertionPointType.add("content_type");
            }
            if (one_step.accept.isSelected()) {
                insertionPointType.add("accept");
            }
            if (one_step.acceptlanguage.isSelected()) {
                insertionPointType.add("accept_language");
            }
            if (one_step.acceptencoding.isSelected()) {
                insertionPointType.add("accept_encoding");
            }
            if (one_step.modelnewheaders.getRowCount() > 0) {
                insertionPointType.add("new_headers");
                isHeaderValue = true;
            }

            if (one_step.entirebodyxml.isSelected()) {
                insertionPointType.add("entire_body_xml");
            }
            if (one_step.entirebodyjson.isSelected()) {
                insertionPointType.add("entire_body_json");
            }
            if (one_step.entirebodymultipart.isSelected()) {
                insertionPointType.add("entire_body_multipart");
            }

            step_data.setisHeaderValue(isHeaderValue);
            step_data.setInsertionPointType(insertionPointType);

            step_data.setMaxRedir((Integer) one_step.sp1.getValue());

            steps.add(step_data);
        }

        properties.setStep(steps);

        return properties;
    }

    public void loadPath(String file, DefaultListModel list) {
        //Load file for implement payloads
        DefaultListModel List = list;
        String line;
        File fileload = new File(file);

        try {
            BufferedReader bufferreader = new BufferedReader(new FileReader(fileload.getAbsolutePath()));
            line = bufferreader.readLine();

            while (line != null) {
                List.addElement(line);
                line = bufferreader.readLine();
            }
            bufferreader.close();
        } catch (FileNotFoundException ex) {
            System.out.println(ex.getMessage());
            for (StackTraceElement element : ex.getStackTrace()) {
                System.out.println(element);
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            for (StackTraceElement element : ex.getStackTrace()) {
                System.out.println(element);
            }
        }
    }

    public void updatePayloads(String file, Step issue, ActiveProfileProperties profile) {

        //Load file for implement payloads
        List payloads = new ArrayList();
        String line;
        File fileload = new File(file);

        try {
            BufferedReader bufferreader = new BufferedReader(new FileReader(fileload.getAbsolutePath()));
            line = bufferreader.readLine();

            while (line != null) {
                payloads.add(line);
                line = bufferreader.readLine();
            }
            bufferreader.close();

            issue.setPayloads(payloads);

            GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
            Gson gson = builder.create();
            String strJson = gson.toJson(issue);
            FileWriter writer = null;

            writer = new FileWriter(profiles_directory + File.separator + profile.getProfileName().concat(".bb2"));
            writer.write("[" + strJson + "]");

            writer.close();
        } catch (FileNotFoundException ex) {
            for (StackTraceElement element : ex.getStackTrace()) {
                System.out.println(element);
            }
        } catch (IOException ex) {
            for (StackTraceElement element : ex.getStackTrace()) {
                System.out.println(element);
            }
        }
    }
}
