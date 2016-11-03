package de.fehngarten.fhemswitch;

import org.joda.time.LocalDate;

class VersionCheck {
    String type;
    String latest;
    String installed;
    private String suppress;
    private LocalDate rememberDate;
    private Boolean suppressed;
    private Boolean isLatest;

    VersionCheck(String type) {
        this.type = type;
        this.latest = "";
        this.installed = "";
        this.suppress = "";
        this.rememberDate = LocalDate.now().minusDays(1);
        this.suppressed = false;
        this.isLatest = true;
    }

    private boolean isLatest() {
        return isLatest;
    }

    private void setSuppressed() {
        this.suppressed = this.latest.equals(this.suppress);
    }

    void setSuppress(String suppress) {
        this.suppress = suppress;
        this.setSuppressed();
    }

    void setInstalled(String installed) {
        this.installed = installed;
        isLatest = latest.equals(installed);
    }

    void setLatest(String latest) {
        this.latest = latest;
        this.setSuppressed();
        isLatest = latest.equals(installed);
    }

    void setDateShown() {
        this.rememberDate = LocalDate.now();
    }

    boolean showVersionHint() {
        if (this.isLatest) return false;
        if (this.suppressed) return false;
        return !this.rememberDate.equals(LocalDate.now());

    }

    public String toString() {
        return "Type: " + this.type + ", installed: " + this.installed + ", latest: " + this.latest + ", suppressed: " + this.suppress + ", last view: " + rememberDate.toString() + ", is suppressed: " + this.suppressed.toString();
    }
}
