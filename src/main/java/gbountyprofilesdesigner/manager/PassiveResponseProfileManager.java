package gbountyprofilesdesigner.manager;

import gbountyprofilesdesigner.properties.PassiveResponseProfileProperties;
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


public class PassiveResponseProfileManager {
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
    private List<String> tags;

    public PassiveResponseProfileManager(String profiles_directory) {
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
        tags = new ArrayList();
    }

    public void setPassiveResponseOptions(ResponseProfile profile, PassiveResponseProfileProperties profile_property) {
        name = profile_property.getProfileName();
        author = profile_property.getAuthor();
        issuename = profile_property.getIssueName();
        issueseverity = profile_property.getIssueSeverity();
        issueconfidence = profile_property.getIssueConfidence();
        issuedetail = profile_property.getIssueDetail();
        issuebackground = profile_property.getIssueBackground();
        remediationdetail = profile_property.getRemediationDetail();
        remediationbackground = profile_property.getRemediationBackground();
        tags = profile_property.getTags();

        profile.text1.setText(name);
        profile.textauthor.setText(author);

        if (issueseverity.equals("High")) {
            profile.radio5.setSelected(true);
        } else if (issueseverity.equals("Medium")) {
            profile.radio6.setSelected(true);
        } else if (issueseverity.equals("Low")) {
            profile.radio7.setSelected(true);
        } else if (issueseverity.equals("Information")) {
            profile.radio8.setSelected(true);
        } else {
            profile.radio8.setSelected(true);  // Default to "Information"
        }

        if (issueconfidence.equals("Certain")) {
            profile.radio9.setSelected(true);
        } else if (issueconfidence.equals("Firm")) {
            profile.radio10.setSelected(true);
        } else if (issueconfidence.equals("Tentative")) {
            profile.radio11.setSelected(true);
        } else {
            profile.radio11.setSelected(true);  // Default to "Tentative"
        }

        profile.text4.setText(issuename);
        profile.textarea1.setText(issuedetail);
        profile.textarea2.setText(issuebackground);
        profile.textarea3.setText(remediationdetail);
        profile.textarea4.setText(remediationbackground);

        if (tags != null) {
            for (String t : tags) {
                profile.tag.addElement(t);
            }
        }

        profile.showGreps(profile_property.getGrep());
        profile.textgreps.setText(profile_property.getGrepsFile());


        String showAlert = profile_property.isShowAlert();

        if (showAlert.equals("one")) {
            profile.first_match.setSelected(true);
        } else if (showAlert.equals("always")) {
            profile.all_matches.setSelected(true);
        }

        profile.text4.setText(issuename);
        profile.textarea1.setText(issuedetail);
        profile.textarea2.setText(issuebackground);
        profile.textarea3.setText(remediationdetail);
        profile.textarea4.setText(remediationbackground);
    }

    public PassiveResponseProfileProperties savePassiveResponseOptions(ResponseProfile profile) {
        //Save attack with fields values
        PassiveResponseProfileProperties newfile = new PassiveResponseProfileProperties();

        newfile.setProfileName(profile.text1.getText());

        newfile.setAuthor(profile.textauthor.getText());

        newfile.setScanner("passive_response");

        newfile.setEnabled(true);
        List greps = new ArrayList();
        List tags = new ArrayList();

        newfile.setGrepsFile(profile.textgreps.getText());
        for (int i = 0; i < profile.modelgrep.getRowCount(); i++) {
            if (!profile.modelgrep.getValueAt(i, 4).toString().isEmpty()) {
                greps.add(profile.modelgrep.getValueAt(i, 0).toString() + "," + profile.modelgrep.getValueAt(i, 1).toString() + "," + profile.modelgrep.getValueAt(i, 2).toString() + "," + profile.modelgrep.getValueAt(i, 3).toString() + "," + profile.modelgrep.getValueAt(i, 4).toString());
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

//            if (profile.radio4.isSelected()) {
//                newfile.setMatchType("simple_string");
//            } else if (profile.radio3.isSelected()) {
//                newfile.setMatchType("regex");
//            } else {
//                newfile.setMatchType("simple_string");
//            }

        if (profile.first_match.isSelected()) {
            newfile.setShowAlert("one");
        } else if (profile.all_matches.isSelected()) {
            newfile.setShowAlert("always");
        }


        newfile.setIssueName(profile.text4.getText());
        newfile.setIssueDetail(profile.textarea1.getText());
        newfile.setIssueBackground(profile.textarea2.getText());
        newfile.setRemediationDetail(profile.textarea3.getText());
        newfile.setRemediationBackground(profile.textarea4.getText());

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
