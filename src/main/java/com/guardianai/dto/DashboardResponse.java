package com.guardianai.dto;

public class DashboardResponse {

    private String userName;
    private String safetyStatus;
    private JourneyResponse activeJourney;
    private long pendingCheckIns;
    private long trustedContactsCount;
    private EmergencyResponse latestEmergency;

    public DashboardResponse() {
    }

    public DashboardResponse(String userName,
                             String safetyStatus,
                             JourneyResponse activeJourney,
                             long pendingCheckIns,
                             long trustedContactsCount,
                             EmergencyResponse latestEmergency) {
        this.userName = userName;
        this.safetyStatus = safetyStatus;
        this.activeJourney = activeJourney;
        this.pendingCheckIns = pendingCheckIns;
        this.trustedContactsCount = trustedContactsCount;
        this.latestEmergency = latestEmergency;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSafetyStatus() {
        return safetyStatus;
    }

    public void setSafetyStatus(String safetyStatus) {
        this.safetyStatus = safetyStatus;
    }

    public JourneyResponse getActiveJourney() {
        return activeJourney;
    }

    public void setActiveJourney(JourneyResponse activeJourney) {
        this.activeJourney = activeJourney;
    }

    public long getPendingCheckIns() {
        return pendingCheckIns;
    }

    public void setPendingCheckIns(long pendingCheckIns) {
        this.pendingCheckIns = pendingCheckIns;
    }

    public long getTrustedContactsCount() {
        return trustedContactsCount;
    }

    public void setTrustedContactsCount(long trustedContactsCount) {
        this.trustedContactsCount = trustedContactsCount;
    }

    public EmergencyResponse getLatestEmergency() {
        return latestEmergency;
    }

    public void setLatestEmergency(EmergencyResponse latestEmergency) {
        this.latestEmergency = latestEmergency;
    }
}
