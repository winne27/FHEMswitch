package de.fehngarten.fhemswitch.modul;

//import com.google.gson.Gson;

import org.joda.time.LocalDate;

import java.util.HashMap;

public class VersionChecks {

    private HashMap<String, VersionCheck> types = new HashMap<>();

    private void newType(String type) {
        types.put(type, new VersionCheck());
    }

    public void setSuppressedVersion(String type, String version) {
        if (types == null || !types.containsKey(type)) {
            newType(type);
        }
        types.get(type).setSuppressedVersion(version);
    }

    public void setSuppressedToLatest(String type) {
        try {
            types.get(type).setSuppressedToLatest();
        } catch (Exception e) {
            // fly Robin, fly
            return;
        }
    }

    public void setVersions (String type, String installedVersion, String latestVersion) {
        try {
            types.get(type).setVersions (installedVersion, latestVersion);
        } catch (Exception e) {
            // fly Robin, fly
            return;
        }
    }

    public void setDateShown(String type) {
        try {
            types.get(type).setDateShown();
        } catch (Exception e) {
            // fly Robin, fly
            return;
        }
    }

    public String showVersionHint() {
        for (String type : types.keySet()) {
            if (types.get(type).doShowVersionHint()) {
                return type;
            }
        }
        return null;
    }

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
        String installedVersion;
        String latestVersion;
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

        void setVersions(String installedVersion, String latestVersion) {
            this.installedVersion = installedVersion;
            this.latestVersion = latestVersion;
            compareVersions();
        }

        private void setIsSuppressed() {
            isSuppressed = latestVersion.equals(suppressedVersion);
        }

        void setSuppressedVersion(String version) {
            suppressedVersion = version;
            setIsSuppressed();
        }

        void setSuppressedToLatest() {
            suppressedVersion = latestVersion;
            setIsSuppressed();
        }

        void compareVersions() {
            isLatest = latestVersion.equals(installedVersion);
            setIsSuppressed();
        }

        void setDateShown() {
            lastDateRemembered = LocalDate.now();
        }

        boolean doShowVersionHint() {
            if (this.isLatest) return false;
            if (this.isSuppressed) return false;
            if (this.latestVersion.equals("")) return false;
            return !this.lastDateRemembered.equals(LocalDate.now());
        }
    }
}