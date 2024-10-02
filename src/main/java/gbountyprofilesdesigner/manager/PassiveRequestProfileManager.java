/*
Copyright 2018 Eduardo Garcia Melia <wagiro@gmail.com>

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package gbountyprofilesdesigner.manager;

import gbountyprofilesdesigner.properties.PassiveRequestProfileProperties;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gbountyprofilesdesigner.data.Step;
import gbountyprofilesdesigner.properties.ActiveProfileProperties;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.DefaultListModel;
import java.util.ArrayList;
import java.util.List;

public class PassiveRequestProfileManager {
    private String name;
    private String issuename;
    private String issuedetail;
    private String issuebackground;
    private String remediationdetail;
    private String remediationbackground;
    private String matchtype;
    private String issueseverity;
    private String issueconfidence;
    private boolean showIssue;
    private String grepsfile;
    private String author;
    private String profiles_directory;

    public PassiveRequestProfileManager(String profiles_directory) {
        this.profiles_directory = profiles_directory;

        name = "";
        issuename = "";
        issuedetail = "";
        issuebackground = "";
        remediationdetail = "";
        remediationbackground = "";
        matchtype = "";
        issueseverity = "";
        issueconfidence = "";
        showIssue = true;
        grepsfile = "";
        author = "";
    }

    public PassiveRequestProfileProperties savePassiveRequestOptions(RequestProfile profile) {
        //Save attack with fields values
        PassiveRequestProfileProperties newfile = new PassiveRequestProfileProperties();

        newfile.setProfileName(profile.text1.getText());

        newfile.setAuthor(profile.textauthor.getText());

        newfile.setScanner("passive_request");

        newfile.setEnabled(true);
        List greps = new ArrayList();
        List tags = new ArrayList();

        newfile.setGrepsFile(profile.textgreps.getText());

        for (int i = 0; i < profile.modelgrep.getRowCount(); i++) {
            if (!profile.modelgrep.getValueAt(i, 5).toString().isEmpty()) {
                greps.add(profile.modelgrep.getValueAt(i, 0).toString() + "," + profile.modelgrep.getValueAt(i, 1).toString() + "," + profile.modelgrep.getValueAt(i, 2).toString() + "," + profile.modelgrep.getValueAt(i, 3).toString() + "," + profile.modelgrep.getValueAt(i, 4).toString() + "," + profile.modelgrep.getValueAt(i, 5).toString());
            }
        }
        newfile.setGrep(greps);

        for (int i = 0; i < profile.listtag.getModel().getSize(); i++) {
            Object item = profile.listtag.getModel().getElementAt(i);
            if (!item.toString().isEmpty()) {
                tags.add(item.toString().replaceAll("\r", "").replaceAll("\n", ""));
            }
        }
        if (!tags.contains("All")) {
            tags.add("All");
            newfile.setTags(tags);
        } else {
            newfile.setTags(tags);
        }


        newfile.setIssueName(profile.text4.getText());
        newfile.setIssueDetail(profile.textarea1.getText());
        newfile.setIssueBackground(profile.textarea2.getText());
        newfile.setRemediationDetail(profile.textarea3.getText());
        newfile.setRemediationBackground(profile.textarea4.getText());

        if (profile.txt_showissue.isSelected()) {
            newfile.setNotShowIssue(true);
        } else {
            newfile.setNotShowIssue(false);
        }

        if (profile.radio5.isSelected()) {
            newfile.setIssueSeverity("High");
        } else if (profile.radio6.isSelected()) {
            newfile.setIssueSeverity("Medium");
        } else if (profile.radio7.isSelected()) {
            newfile.setIssueSeverity("Low");
        } else if (profile.radio8.isSelected()) {
            newfile.setIssueSeverity("Information");
        } else {
            newfile.setIssueSeverity("");
        }

        if (profile.radio9.isSelected()) {
            newfile.setIssueConfidence("Certain");
        } else if (profile.radio10.isSelected()) {
            newfile.setIssueConfidence("Firm");
        } else if (profile.radio11.isSelected()) {
            newfile.setIssueConfidence("Tentative");
        } else {
            newfile.setIssueConfidence("");
        }

        return newfile;
    }

    public void setPassiveRequestOptions(RequestProfile profile, PassiveRequestProfileProperties profile_property) {
        name = profile_property.getProfileName();
        matchtype = profile_property.getMatchType();
        author = profile_property.getAuthor();
        grepsfile = profile_property.getGrepsFile();

        issuename = profile_property.getIssueName();
        issueseverity = profile_property.getIssueSeverity();
        issueconfidence = profile_property.getIssueConfidence();
        issuedetail = profile_property.getIssueDetail();
        issuebackground = profile_property.getIssueBackground();
        remediationdetail = profile_property.getRemediationDetail();
        remediationbackground = profile_property.getRemediationBackground();

        showIssue = profile_property.getNotShowIssue();

        profile.textauthor.setText(author);
        profile.text1.setText(name);

        profile.grep.removeAllElements();
        profile.tag.removeAllElements();

        profile.textgreps.setText(grepsfile);

        profile.showGreps(profile_property.getGrep());

        if (profile_property.getTags() != null) {
            for (String t : profile_property.getTags()) {
                profile.tag.addElement(t);
            }
        }


        profile.text4.setText(issuename);
        profile.textarea1.setText(issuedetail);
        profile.textarea2.setText(issuebackground);
        profile.textarea3.setText(remediationdetail);
        profile.textarea4.setText(remediationbackground);
        profile.txt_showissue.setSelected(showIssue);

        switch (issueseverity) {
            case "High":
                profile.buttonGroup5.setSelected(profile.radio5.getModel(), true);
                break;
            case "Medium":
                profile.buttonGroup5.setSelected(profile.radio6.getModel(), true);
                break;
            case "Low":
                profile.buttonGroup5.setSelected(profile.radio7.getModel(), true);
                break;
            case "Information":
                profile.buttonGroup5.setSelected(profile.radio8.getModel(), true);
                break;
            default:
                break;
        }

        switch (issueconfidence) {
            case "Certain":
                profile.buttonGroup6.setSelected(profile.radio9.getModel(), true);
                break;
            case "Firm":
                profile.buttonGroup6.setSelected(profile.radio10.getModel(), true);
                break;
            case "Tentative":
                profile.buttonGroup6.setSelected(profile.radio11.getModel(), true);
                break;
            default:
                break;
        }
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
            System.out.println("ProfilesManager line 1912:" + ex.getMessage());
            for (StackTraceElement element : ex.getStackTrace()) {
                System.out.println(element);
            }
        } catch (IOException ex) {
            System.out.println("ProfilesManager line 1815:" + ex.getMessage());
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
            System.out.println("ProfilesManager line 1639:");
            for (StackTraceElement element : ex.getStackTrace()) {
                System.out.println(element);
            }
        } catch (IOException ex) {
            System.out.println("ProfilesManager line 1042:");
            for (StackTraceElement element : ex.getStackTrace()) {
                System.out.println(element);
            }
        }
    }
}
