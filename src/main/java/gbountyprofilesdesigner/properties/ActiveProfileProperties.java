package gbountyprofilesdesigner.properties;

import gbountyprofilesdesigner.data.Step;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ActiveProfileProperties {

    private String profile_name;
    private boolean enabled = false;
    private String scanner = "";
    private String author = "";
    List<Step> steps = new ArrayList<>();

    private List<String> Tags = new ArrayList();

    public ActiveProfileProperties() {
        super();
    }

    public ActiveProfileProperties(ActiveProfileProperties pp) {
        this.profile_name = pp.getProfileName();
        this.enabled = pp.getEnabled();
        this.scanner = pp.getScanner();
        this.author = pp.getAuthor();
        this.steps = pp.getStep();
        this.Tags = pp.getTags();
    }

    public String getProfileName() {
        return profile_name;
    }

    public List<Step> getStep() {
        return steps;
    }

    public void setStep(List<Step> steps) {
        this.steps = steps;
    }

    public String getAuthor() {
        return author;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public String getScanner() {
        return scanner;
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

    //Set functions
    public void setProfileName(String profilename) {
        profile_name = profilename;
    }

    public void setAuthor(String Author) {
        author = Author;
    }

    public void setEnabled(boolean Enabled) {
        enabled = Enabled;
    }

    public void setScanner(String Scanner) {
        scanner = Scanner;
    }

    public void setTags(List<String> tags) {
        Tags = tags;
    }
}
