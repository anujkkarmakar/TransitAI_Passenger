package com.example.navixpassanger.ticket;

public class Ticket {
    private String pnr;
    private String fromStop;
    private String toStop;
    private String journeyType;
    private String userEmail;
    private String pdfUrl;
    private long timestamp;
    private boolean isActive;
    private String status;
    private long cancellationTime;

    public Ticket() {
        // Default constructor required for Firestore
        this.status = "ACTIVE";  // Default status
    }


    // Parameterized constructor
    public Ticket(String pnr, String fromStop, String toStop, String journeyType,
                  String userEmail, String pdfUrl, long timestamp) {
        this.pnr = pnr;
        this.fromStop = fromStop;
        this.toStop = toStop;
        this.journeyType = journeyType;
        this.userEmail = userEmail;
        this.pdfUrl = pdfUrl;
        this.timestamp = timestamp;
        this.status = "ACTIVE";
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCancellationTime() {
        return cancellationTime;
    }

    public void setCancellationTime(long cancellationTime) {
        this.cancellationTime = cancellationTime;
    }

    // Getters
    public String getPnr() {
        return pnr;
    }

    public String getFromStop() {
        return fromStop;
    }

    public String getToStop() {
        return toStop;
    }

    public String getJourneyType() {
        return journeyType;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isActive() {
        return isActive;
    }

    // Setters
    public void setPnr(String pnr) {
        this.pnr = pnr;
    }

    public void setFromStop(String fromStop) {
        this.fromStop = fromStop;
    }

    public void setToStop(String toStop) {
        this.toStop = toStop;
    }

    public void setJourneyType(String journeyType) {
        this.journeyType = journeyType;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        // Update isActive based on new timestamp
        this.isActive = (System.currentTimeMillis() - timestamp) <= (4 * 60 * 60 * 1000);
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "pnr='" + pnr + '\'' +
                ", fromStop='" + fromStop + '\'' +
                ", toStop='" + toStop + '\'' +
                ", journeyType='" + journeyType + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", pdfUrl='" + pdfUrl + '\'' +
                ", timestamp=" + timestamp +
                ", isActive=" + isActive +
                '}';
    }
}