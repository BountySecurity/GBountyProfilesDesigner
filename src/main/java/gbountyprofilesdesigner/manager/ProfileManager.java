package gbountyprofilesdesigner.manager;

import gbountyprofilesdesigner.properties.ActiveProfileProperties;
import gbountyprofilesdesigner.properties.PassiveRequestProfileProperties;
import gbountyprofilesdesigner.properties.PassiveResponseProfileProperties;
import gbountyprofilesdesigner.data.Tuple;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProfileManager {
    private String path;

    private final HashMap<String, ActiveProfileProperties> actives;
    private final HashMap<String, PassiveRequestProfileProperties> requests;
    private final HashMap<String, PassiveResponseProfileProperties> responses;

    private final ArrayList<ProfileManager.ProfileListener> profileListeners;
    private final ArrayList<ProfileManager.ActiveListener> activeListeners;
    private final ArrayList<ProfileManager.RequestListener> requestListeners;
    private final ArrayList<ProfileManager.ResponseListener> responseListeners;

    public ProfileManager(String profilesDirectory) {

        // Initialize empty sets
        this.actives = new HashMap<>();
        this.requests = new HashMap<>();
        this.responses = new HashMap<>();

        // Listeners
        this.profileListeners = new ArrayList<>();
        this.activeListeners = new ArrayList<>();
        this.requestListeners = new ArrayList<>();
        this.responseListeners = new ArrayList<>();

        // Load profiles from the default path
        String basePath = profilesDirectory;
        if (basePath != null && !basePath.isEmpty()) {
            this.loadFrom(basePath);
        }
    }

    public void loadFrom(String path) {
        this.path = path;

        // Prepare the gson builder
        GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
        Gson gson = builder.create();

        // Load all profiles from the given path
        Tuple<File[], Boolean> result = this.listFiles();
        if (!result.right) {
            return;
        }

        // For each file in the directory...
        for (File file : result.left) {
            // ...if it's not a profile, skip it.
            if (!file.getName().endsWith(".bb2")) {
                continue;
            }

            // Otherwise, try to read it.
            FileReader fr;
            try {
                fr = new FileReader(file.getAbsolutePath());
            } catch (Exception e) {
                String errMsg = String.format("Error while reading profile '%s': %s", file.getName(), e.getMessage());
                System.out.println(errMsg);
                continue;
            }

            // Parse the profile file as a JSON document
            try {
                JsonArray elems = new JsonArray();
                elems.addAll(JsonParser.parseReader(new JsonReader((fr))).getAsJsonArray());

                for (int i = 0; i < elems.size(); i++) {
                    JsonObject obj = elems.get(i).getAsJsonObject();

                    String scanner = obj.get("scanner").getAsString();
                    if (scanner.contains("active")) {
                        ActiveProfileProperties pp = gson.fromJson(obj.toString(), ActiveProfileProperties.class);
                        this.addToActives(pp);
                    } else if (scanner.contains("passive_request")) {
                        PassiveRequestProfileProperties pp = gson.fromJson(obj.toString(), PassiveRequestProfileProperties.class);
                        this.addToRequests(pp);
                    } else if (scanner.contains("passive_response")) {
                        PassiveResponseProfileProperties pp = gson.fromJson(obj.toString(), PassiveResponseProfileProperties.class);
                        this.addToResponses(pp);
                    } else {
                        String errMsg = String.format("Error with profile '%s': unknown scanner type", file.getName());
                        System.out.println(errMsg);
                    }
                }
            } catch (Exception e) {
                String errMsg = String.format("Error while reading profile '%s': %s", file.getName(), e.getMessage());
                System.out.println(errMsg);
            }
        }
        this.profileListeners.forEach(ProfileListener::onProfilesRefreshed);
    }

    public String getPath() {
        return this.path;
    }

    public List<ActiveProfileProperties> getActives() {
        return new ArrayList<>(actives.values());
    }

    public List<ActiveProfileProperties> getActives(String tag) {
        return actives.values().stream().filter(pp -> pp.getTags().contains(tag)).collect(Collectors.toList());
    }

    public ActiveProfileProperties getActiveByName(String name) {
        return actives.get(name);
    }

    public List<ActiveProfileProperties> getActivesEnabled() {
        return actives.values()
                .stream()
                .filter(ActiveProfileProperties::getEnabled)
                .collect(Collectors.toList());
    }

    public List<ActiveProfileProperties> getActivesEnabled(String tag) {
        return actives.values()
                .stream()
                .filter(pp -> pp.getEnabled() && pp.getTags().contains(tag))
                .collect(Collectors.toList());
    }

    public List<PassiveRequestProfileProperties> getRequests() {
        return new ArrayList<>(requests.values());
    }

    public List<PassiveRequestProfileProperties> getRequests(String tag) {
        return requests.values().stream().filter(pp -> pp.getTags().contains(tag)).collect(Collectors.toList());
    }

    public PassiveRequestProfileProperties getRequestsByName(String name) {
        return requests.get(name);
    }

    public List<PassiveRequestProfileProperties> getRequestsEnabled() {
        return requests.values()
                .stream()
                .filter(PassiveRequestProfileProperties::isEnabled)
                .collect(Collectors.toList());
    }

    public List<PassiveRequestProfileProperties> getRequestsEnabled(String tag) {
        return requests.values()
                .stream()
                .filter(pp -> pp.isEnabled() && pp.getTags().contains(tag))
                .collect(Collectors.toList());
    }

    public List<PassiveResponseProfileProperties> getResponses() {
        return new ArrayList<>(responses.values());
    }

    public List<PassiveResponseProfileProperties> getResponses(String tag) {
        return responses.values().stream().filter(pp -> pp.getTags().contains(tag)).collect(Collectors.toList());
    }

    public PassiveResponseProfileProperties getResponsesByName(String name) {
        return responses.get(name);
    }

    public List<PassiveResponseProfileProperties> getResponsesEnabled() {
        return responses.values()
                .stream()
                .filter(PassiveResponseProfileProperties::isEnabled)
                .collect(Collectors.toList());
    }

    public List<PassiveResponseProfileProperties> getResponsesEnabled(String tag) {
        return responses.values()
                .stream()
                .filter(pp -> pp.isEnabled() && pp.getTags().contains(tag))
                .collect(Collectors.toList());
    }

    public void deleteProfiles(String[] profileNames) {
        // First we collect the list of existing files
        // We'll use it later to determine which files exist
        // thus we need to delete them.
        Tuple<File[], Boolean> result = this.listFiles();
        if (!result.right) {
            return;
        }

        // For each file in the directory...
        for (File file : result.left) {
            // ...if it's not a profile, skip it.
            if (!file.getName().endsWith(".bb2")) {
                continue;
            }

            for (String name : profileNames) {
                try {
                    JsonArray data = new JsonArray();
                    JsonReader reader = new JsonReader(new FileReader(file.getAbsolutePath()));
                    data.addAll(JsonParser.parseReader(reader).getAsJsonArray());
                    reader.close();

                    String profileName = data.get(0).getAsJsonObject().get("profile_name").getAsString();
                    if (name.equals(profileName)) {
                        Files.delete(file.toPath());
                        this.deleteProfile(name);
                        break;
                    }
                } catch (Exception e) {
                    String errMsg = String.format("Error while reading profile '%s': %s", file.getName(), e.getMessage());
                    System.out.println(errMsg);
                }
            }
        }
    }

    public void enableActiveProfiles(String[] profileNames) {
        this.setEnableDisableActiveProfiles(profileNames, true);
    }

    public void enablePassiveRequestProfiles(String[] profileNames) {
        this.setEnableDisablePassiveRequestProfiles(profileNames, true);
    }

    public void enablePassiveResponseProfiles(String[] profileNames) {
        this.setEnableDisablePassiveResponseProfiles(profileNames, true);
    }

    public void disableActiveProfiles(String[] profileNames) {
        this.setEnableDisableActiveProfiles(profileNames, false);
    }

    public void disablePassiveRequestProfiles(String[] profileNames) {
        this.setEnableDisablePassiveRequestProfiles(profileNames, false);
    }

    public void disablePassiveResponseProfiles(String[] profileNames) {
        this.setEnableDisablePassiveResponseProfiles(profileNames, false);
    }

    public void setEnableDisableActiveProfiles(String[] profileNames, boolean enabled) {
        this.updateActiveProfiles(profileNames, pp -> {
            ActiveProfileProperties clone = new ActiveProfileProperties(pp);
            clone.setEnabled(enabled);
            return clone;
        });
    }

    public void setEnableDisablePassiveRequestProfiles(String[] profileNames, boolean enabled) {
        this.updatePassiveRequestProfiles(profileNames, pp -> {
            PassiveRequestProfileProperties clone = new PassiveRequestProfileProperties(pp);
            clone.setEnabled(enabled);
            return clone;
        });
    }

    public void setEnableDisablePassiveResponseProfiles(String[] profileNames, boolean enabled) {
        this.updatePassiveResponseProfiles(profileNames, pp -> {
            PassiveResponseProfileProperties clone = new PassiveResponseProfileProperties(pp);
            clone.setEnabled(enabled);
            return clone;
        });
    }

    public void setTagToActiveProfiles(String[] profileNames, String tag) {
        this.updateActiveProfiles(profileNames, pp -> {
            ActiveProfileProperties clone = new ActiveProfileProperties(pp);
            clone.addTag(tag);
            return clone;
        });
    }

    public void setTagToPassiveRequestProfiles(String[] profileNames, String tag) {
        this.updatePassiveRequestProfiles(profileNames, pp -> {
            PassiveRequestProfileProperties clone = new PassiveRequestProfileProperties(pp);
            clone.addTag(tag);
            return clone;
        });
    }

    public void setTagToPassiveResponseProfiles(String[] profileNames, String tag) {
        this.updatePassiveResponseProfiles(profileNames, pp -> {
            PassiveResponseProfileProperties clone = new PassiveResponseProfileProperties(pp);
            clone.addTag(tag);
            return clone;
        });
    }

    public void updateActiveProfiles(String[] profileNames, Function<ActiveProfileProperties, ActiveProfileProperties> func) {
        for (String profileName : profileNames) {
            this.updateActiveProfile(profileName, func);
        }
    }

    public void updatePassiveRequestProfiles(String[] profileNames, Function<PassiveRequestProfileProperties, PassiveRequestProfileProperties> func) {
        for (String profileName : profileNames) {
            this.updatePassiveRequestProfile(profileName, func);
        }
    }

    public void updatePassiveResponseProfiles(String[] profileNames, Function<PassiveResponseProfileProperties, PassiveResponseProfileProperties> func) {
        for (String profileName : profileNames) {
            this.updatePassiveResponseProfile(profileName, func);
        }
    }

    public void updateActiveProfile(String oldName, Function<ActiveProfileProperties, ActiveProfileProperties> func) {
        try {
            updateActiveProfileFile(oldName, func);
            updateActiveProfileInMemory(oldName, func);
        } catch (Exception e) {
            String errMsg = String.format("Error while updating profile '%s': %s", oldName, e.getMessage());
            System.out.println(errMsg);
        }
    }

    public void updatePassiveRequestProfile(String oldName, Function<PassiveRequestProfileProperties, PassiveRequestProfileProperties> func) {
        try {
            updatePassiveRequestProfileFile(oldName, func);
            updatePassiveRequestProfileInMemory(oldName, func);
        } catch (Exception e) {
            String errMsg = String.format("Error while updating profile '%s': %s", oldName, e.getMessage());
            System.out.println(errMsg);
        }
    }

    public void updatePassiveResponseProfile(String oldName, Function<PassiveResponseProfileProperties, PassiveResponseProfileProperties> func) {
        try {
            updatePassiveResponseProfileFile(oldName, func);
            updatePassiveResponseProfileInMemory(oldName, func);
        } catch (Exception e) {
            String errMsg = String.format("Error while updating profile '%s': %s", oldName, e.getMessage());
            System.out.println(errMsg);
        }
    }

    public void updateProfile(String oldName, ActiveProfileProperties pp) {
        try {
            updateProfileFile(oldName, pp);
            updateProfileInMemory(oldName, pp);
        } catch (Exception e) {
            String errMsg = String.format("Error while updating profile '%s': %s", oldName, e.getMessage());
            System.out.println(errMsg);
        }
    }

    public void updateProfile(String oldName, PassiveRequestProfileProperties pp) {
        try {
            updateProfileFile(oldName, pp);
            updateProfileInMemory(oldName, pp);
        } catch (Exception e) {
            String errMsg = String.format("Error while updating profile '%s': %s", oldName, e.getMessage());
            System.out.println(errMsg);
        }
    }

    public void updateProfile(String oldName, PassiveResponseProfileProperties pp) {
        try {
            updateProfileFile(oldName, pp);
            updateProfileInMemory(oldName, pp);
        } catch (Exception e) {
            String errMsg = String.format("Error while updating profile '%s': %s", oldName, e.getMessage());
            System.out.println(errMsg);
        }
    }

    public void removeTags(ArrayList<String> tags) {
        if (tags.isEmpty()) {
            return;
        }

        tags.forEach(tag -> {
            this.actives.values().forEach(pp -> pp.removeTag(tag));
            this.requests.values().forEach(pp -> pp.removeTag(tag));
            this.responses.values().forEach(pp -> pp.removeTag(tag));
        });
    }

    private void updateActiveProfileFile(String name, Function<ActiveProfileProperties, ActiveProfileProperties> func) throws IOException {
        GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
        Gson gson = builder.create();

        String fileName = path + File.separator + name.concat(".bb2");

        // Read the profile file
        JsonReader reader = new JsonReader(new FileReader(fileName));
        JsonArray elems = JsonParser.parseReader(reader).getAsJsonArray();
        reader.close();

        // We get the first one, because there should only be one
        // Although the file is an array, we only use one element
        ActiveProfileProperties pp = gson.fromJson(elems.get(0).toString(), ActiveProfileProperties.class);

        // We update the profile
        ActiveProfileProperties updated = func.apply(pp);

        this.updateProfileFile(name, updated);
    }

    private void updatePassiveRequestProfileFile(String name, Function<PassiveRequestProfileProperties, PassiveRequestProfileProperties> func) throws IOException {
        GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
        Gson gson = builder.create();

        String fileName = path + File.separator + name.concat(".bb2");

        // Read the profile file
        JsonReader reader = new JsonReader(new FileReader(fileName));
        JsonArray elems = JsonParser.parseReader(reader).getAsJsonArray();
        reader.close();

        // We get the first one, because there should only be one
        // Although the file is an array, we only use one element
        PassiveRequestProfileProperties pp = gson.fromJson(elems.get(0).toString(), PassiveRequestProfileProperties.class);

        // We update the profile
        PassiveRequestProfileProperties updated = func.apply(pp);

        this.updateProfileFile(name, updated);
    }

    private void updatePassiveResponseProfileFile(String name, Function<PassiveResponseProfileProperties, PassiveResponseProfileProperties> func) throws IOException {
        GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
        Gson gson = builder.create();

        String fileName = path + File.separator + name.concat(".bb2");

        // Read the profile file
        JsonReader reader = new JsonReader(new FileReader(fileName));
        JsonArray elems = JsonParser.parseReader(reader).getAsJsonArray();
        reader.close();

        // We get the first one, because there should only be one
        // Although the file is an array, we only use one element
        PassiveResponseProfileProperties pp = gson.fromJson(elems.get(0).toString(), PassiveResponseProfileProperties.class);

        // We update the profile
        PassiveResponseProfileProperties updated = func.apply(pp);

        this.updateProfileFile(name, updated);
    }

    private void updateProfileFile(String oldName, ActiveProfileProperties pp) throws IOException {
        // If the name has changed, we remove the old file
        if (!oldName.equals("") && !oldName.equals(pp.getProfileName())) {
            String fileName = path + File.separator + oldName.concat(".bb2");
            File file = new File(fileName);
            Files.delete(file.toPath());
        }

        GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
        Gson gson = builder.create();

        String fileName = path + File.separator + pp.getProfileName().concat(".bb2");

        // We build the new JSON object
        List<ActiveProfileProperties> out = gson.fromJson(new JsonArray(), new TypeToken<List<ActiveProfileProperties>>() {
        }.getType());

        out.clear();
        out.add(pp);

        // We write it back to the profile file
        FileOutputStream fileStream = new FileOutputStream(fileName);
        OutputStreamWriter writer = new OutputStreamWriter(fileStream, StandardCharsets.UTF_8);
        writer.write(gson.toJson(out));
        writer.close();
        
    }

    private void updateProfileFile(String oldName, PassiveRequestProfileProperties pp) throws IOException {
        // If the name has changed, we remove the old file
        if (!oldName.equals("") && !oldName.equals(pp.getProfileName())) {
            String fileName = path + File.separator + oldName.concat(".bb2");
            File file = new File(fileName);
            Files.delete(file.toPath());
        }

        GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
        Gson gson = builder.create();

        String fileName = path + File.separator + pp.getProfileName().concat(".bb2");

        // We build the new JSON object
        List<PassiveRequestProfileProperties> out = gson.fromJson(new JsonArray(), new TypeToken<List<PassiveRequestProfileProperties>>() {
        }.getType());

        out.clear();
        out.add(pp);

        // We write it back to the profile file
        FileOutputStream fileStream = new FileOutputStream(fileName);
        OutputStreamWriter writer = new OutputStreamWriter(fileStream, StandardCharsets.UTF_8);
        writer.write(gson.toJson(out));
        writer.close();
    }

    private void updateProfileFile(String oldName, PassiveResponseProfileProperties pp) throws IOException {
        // If the name has changed, we remove the old file
        if (!oldName.equals("") && !oldName.equals(pp.getProfileName())) {
            String fileName = path + File.separator + oldName.concat(".bb2");
            File file = new File(fileName);
            Files.delete(file.toPath());
        }

        GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
        Gson gson = builder.create();

        String fileName = path + File.separator + pp.getProfileName().concat(".bb2");

        // We build the new JSON object
        List<PassiveResponseProfileProperties> out = gson.fromJson(new JsonArray(), new TypeToken<List<PassiveResponseProfileProperties>>() {
        }.getType());

        out.clear();
        out.add(pp);

        // We write it back to the profile file
        FileOutputStream fileStream = new FileOutputStream(fileName);
        OutputStreamWriter writer = new OutputStreamWriter(fileStream, StandardCharsets.UTF_8);
        writer.write(gson.toJson(out));
        writer.close();
    }

    private void updateActiveProfileInMemory(String name, Function<ActiveProfileProperties, ActiveProfileProperties> func) {
        if (this.actives.containsKey(name)) {
            // Retrieve, update and save
            ActiveProfileProperties toUpdate = this.actives.get(name);
            ActiveProfileProperties updated = func.apply(toUpdate);
            this.actives.put(updated.getProfileName(), updated);
            this.activeListeners.forEach(l -> l.onActiveUpdated(updated));

            // Mark old as delete, if name is different
            if (!name.equals(updated.getProfileName())) {
                this.actives.remove(name);
                this.activeListeners.forEach(l -> l.onActiveRemoved(name));
            }
        }
    }

    private void updatePassiveRequestProfileInMemory(String name, Function<PassiveRequestProfileProperties, PassiveRequestProfileProperties> func) {
        if (this.requests.containsKey(name)) {
            // Retrieve, update and save
            PassiveRequestProfileProperties toUpdate = this.requests.get(name);
            PassiveRequestProfileProperties updated = func.apply(toUpdate);
            this.requests.put(updated.getProfileName(), updated);
            this.requestListeners.forEach(l -> l.onRequestUpdated(updated));

            // Mark old as delete, if name is different
            if (!name.equals(updated.getProfileName())) {
                this.requests.remove(name);
                this.requestListeners.forEach(l -> l.onRequestRemoved(name));
            }
        }
    }

    private void updatePassiveResponseProfileInMemory(String name, Function<PassiveResponseProfileProperties, PassiveResponseProfileProperties> func) {
        if (this.responses.containsKey(name)) {
            // Retrieve, update and save
            PassiveResponseProfileProperties toUpdate = this.responses.get(name);
            PassiveResponseProfileProperties updated = func.apply(toUpdate);
            this.responses.put(updated.getProfileName(), updated);
            this.responseListeners.forEach(l -> l.onResponseUpdated(updated));

            // Mark old as delete, if name is different
            if (!name.equals(updated.getProfileName())) {
                this.responses.remove(name);
                this.responseListeners.forEach(l -> l.onResponseRemoved(name));
            }
        }
    }

    private void updateProfileInMemory(String name, ActiveProfileProperties pp) {
        // Exists and name hasn't changed, thus is an update
        if (this.actives.containsKey(name) && name.equals(pp.getProfileName())) {
            this.addToActives(pp);
        }
        // Either doesn't exist or name has changed, thus is a remove and add
        else {
            if (this.actives.containsKey(name)) {
                this.actives.remove(name);
                this.activeListeners.forEach(l -> l.onActiveRemoved(name));
            }
            this.addToActives(pp);
        }
    }

    private void updateProfileInMemory(String name, PassiveRequestProfileProperties pp) {
        // Exists and name hasn't changed, thus is an update
        if (this.requests.containsKey(name) && name.equals(pp.getProfileName())) {
            this.addToRequests(pp);
        }
        // Either doesn't exist or name has changed, thus is a remove and add
        else {
            if (this.requests.containsKey(name)) {
                this.requests.remove(name);
                this.requestListeners.forEach(l -> l.onRequestRemoved(name));
            }
            this.addToRequests(pp);
        }
    }

    private void updateProfileInMemory(String name, PassiveResponseProfileProperties pp) {
        // Exists and name hasn't changed, thus is an update
        if (this.responses.containsKey(name) && name.equals(pp.getProfileName())) {
            this.addToResponses(pp);
        }
        // Either doesn't exist or name has changed, thus is a remove and add
        else {
            if (this.responses.containsKey(name)) {
                this.responses.remove(name);
                this.responseListeners.forEach(l -> l.onResponseRemoved(name));
            }
            this.addToResponses(pp);
        }
    }

    public void registerListener(ProfileManager.ProfileListener listener) {
        this.profileListeners.add(listener);
    }

    public void registerListener(ProfileManager.ActiveListener listener) {
        this.activeListeners.add(listener);
    }

    public void registerListener(ProfileManager.RequestListener listener) {
        this.requestListeners.add(listener);
    }

    public void registerListener(ProfileManager.ResponseListener listener) {
        this.responseListeners.add(listener);
    }

    private void addToActives(ActiveProfileProperties pp) {
        if (this.actives.containsKey(pp.getProfileName())) {
            this.putToActives(pp);
            return;
        }

        this.actives.put(pp.getProfileName(), pp);
        this.activeListeners.forEach(l -> l.onActiveAdded(pp));
    }

    private void putToActives(ActiveProfileProperties pp) {
        this.actives.put(pp.getProfileName(), pp);
        this.activeListeners.forEach(l -> l.onActiveUpdated(pp));
    }

    private void addToRequests(PassiveRequestProfileProperties pp) {
        if (this.requests.containsKey(pp.getProfileName())) {
            this.putToRequests(pp);
            return;
        }

        this.requests.put(pp.getProfileName(), pp);
        this.requestListeners.forEach(l -> l.onRequestAdded(pp));
    }

    private void putToRequests(PassiveRequestProfileProperties pp) {
        this.requests.put(pp.getProfileName(), pp);
        this.requestListeners.forEach(l -> l.onRequestUpdated(pp));
    }

    private void addToResponses(PassiveResponseProfileProperties pp) {
        if (this.responses.containsKey(pp.getProfileName())) {
            this.putToResponses(pp);
            return;
        }

        this.responses.put(pp.getProfileName(), pp);
        this.responseListeners.forEach(l -> l.onResponseAdded(pp));
    }

    private void putToResponses(PassiveResponseProfileProperties pp) {
        this.responses.put(pp.getProfileName(), pp);
        this.responseListeners.forEach(l -> l.onResponseUpdated(pp));
    }

    private Tuple<File[], Boolean> listFiles() {
        File f = new File(path);

        if (!f.exists() || !f.isDirectory()) {
            String errMsg = String.format("Profiles path '%s': Does not exist or is not directory", path);
            System.out.println(errMsg);
            return new Tuple<>(null, false);
        }

        // For each file in the directory...
        File[] files = f.listFiles();
        if (files == null) {
            String errMsg = String.format("Profiles path '%s': Is empty", path);
            System.out.println(errMsg);
            return new Tuple<>(null, false);
        }

        return new Tuple<>(files, true);
    }

    private void deleteProfile(String name) {
        if (this.actives.containsKey(name)) {
            this.actives.remove(name);
            this.activeListeners.forEach(l -> l.onActiveRemoved(name));
        }

        if (this.requests.containsKey(name)) {
            this.requests.remove(name);
            this.requestListeners.forEach(l -> l.onRequestRemoved(name));
        }

        if (this.responses.containsKey(name)) {
            this.responses.remove(name);
            this.responseListeners.forEach(l -> l.onResponseRemoved(name));
        }
    }

    public interface ProfileListener {
        void onProfilesRefreshed();
    }

    public interface ActiveListener {
        void onActiveAdded(ActiveProfileProperties profile);

        void onActiveUpdated(ActiveProfileProperties profile);

        void onActiveRemoved(String profileName);
    }

    public interface RequestListener {
        void onRequestAdded(PassiveRequestProfileProperties profile);

        void onRequestUpdated(PassiveRequestProfileProperties profile);

        void onRequestRemoved(String profileName);
    }

    public interface ResponseListener {
        void onResponseAdded(PassiveResponseProfileProperties profile);

        void onResponseUpdated(PassiveResponseProfileProperties profile);

        void onResponseRemoved(String profileName);
    }
}
