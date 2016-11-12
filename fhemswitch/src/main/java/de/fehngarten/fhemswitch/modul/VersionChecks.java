package de.fehngarten.fhemswitch.modul;

//import com.google.gson.Gson;

import org.joda.time.LocalDate;

import java.util.HashMap;

public class VersionChecks {

    private HashMap<String, VersionCheck> types = new HashMap<>();

    public void newType(String type) {
        types.put(type, new VersionCheck());
    }

    public void setSuppressedVersion(String type, String version) {
        if (types == null || !types.containsKey(type)) {
            newType(type);
        }
        types.get(type).setSuppressedVersion(version);
    }

    public void setSuppressedToLatest(String type) {
        types.get(type).setSuppressedToLatest();
    }

    public void setVersions (String type, String installedVersion, String latestVersion) {
        types.get(type).setVersions (installedVersion, latestVersion);
    }

    public void setDateShown(String type) {
        types.get(type).setDateShown();
    }

    public String showVersionHint() {
        for (String type : types.keySet()) {
            if (types.get(type).showVersionHint()) {
                return type;
            }
        }
        return null;
    }
/*
    public String typesToString() {
        Gson gson = new Gson();
        String json = gson.toJson(types);
        return json;
    }
*/
    public String getInstalledVersion(String type) {
        return types.get(type).installedVersion;
    }

    public String getLatestVersion(String type) {
        return types.get(type).latestVersion;
    }

    public String getSuppressedVersion(String type) {
        return types.get(type).suppressedVersion;
    }

    private class VersionCheck {
        public String installedVersion;
        public String latestVersion;
        private String suppressedVersion;
        private LocalDate lastDateRemembered;
        private Boolean isSuppressed;
        private Boolean isLatest;

        private VersionCheck() {
            installedVersion = "";
            latestVersion = "";
            suppressedVersion = "";
            lastDateRemembered = LocalDate.now().minusDays(1);
            isSuppressed = false;
            isLatest = true;
        }

        public void setVersions (String installedVersion, String latestVersion) {
            this.installedVersion = installedVersion;
            this.latestVersion = latestVersion;
            compareVersions();
        }

        private void setIsSuppressed() {
            isSuppressed = latestVersion.equals(suppressedVersion);
        }

        public void setSuppressedVersion(String version) {
            suppressedVersion = version;
            setIsSuppressed();
        }

        public void setSuppressedToLatest() {
            suppressedVersion = latestVersion;
            setIsSuppressed();
        }

        public void compareVersions() {
            isLatest = latestVersion.equals(installedVersion);
            setIsSuppressed();
        }

        public void setDateShown() {
            lastDateRemembered = LocalDate.now();
        }

        public boolean showVersionHint() {
            if (this.isLatest) return false;
            if (this.isSuppressed) return false;
            return !this.lastDateRemembered.equals(LocalDate.now());
        }
    }
}