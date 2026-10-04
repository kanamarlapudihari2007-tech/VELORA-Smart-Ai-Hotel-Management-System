package com.hotel.model;

import java.io.Serializable;

/**
 * ServiceRequestAIResult - DTO holding structured extraction output from Google Gemini AI
 * for guest room service and housekeeping requests.
 */
public class ServiceRequestAIResult implements Serializable {

    private String requestType;       // 'Housekeeping', 'Food Service', 'Maintenance', 'Reception'
    private String extractedItems;    // e.g. "2x Bath Towels, 1x Sparkling Water"
    private String priority;          // 'LOW', 'NORMAL', 'HIGH', 'URGENT'
    private String estimatedMinutes;   // e.g. "15-20 mins"
    private String confirmationMessage;// Personalized reassurance for the guest
    private boolean classifiedByAI;   // true if processed by Gemini, false if fallback rule

    public ServiceRequestAIResult() {
        this.requestType = "Housekeeping";
        this.extractedItems = "";
        this.priority = "NORMAL";
        this.estimatedMinutes = "15-20 mins";
        this.confirmationMessage = "Your request has been received and is being prepared.";
        this.classifiedByAI = false;
    }

    public ServiceRequestAIResult(String requestType, String extractedItems, String priority,
                                  String estimatedMinutes, String confirmationMessage, boolean classifiedByAI) {
        this.requestType = requestType;
        this.extractedItems = extractedItems;
        this.priority = priority;
        this.estimatedMinutes = estimatedMinutes;
        this.confirmationMessage = confirmationMessage;
        this.classifiedByAI = classifiedByAI;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getExtractedItems() {
        return extractedItems;
    }

    public void setExtractedItems(String extractedItems) {
        this.extractedItems = extractedItems;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public void setEstimatedMinutes(String estimatedMinutes) {
        this.estimatedMinutes = estimatedMinutes;
    }

    public String getConfirmationMessage() {
        return confirmationMessage;
    }

    public void setConfirmationMessage(String confirmationMessage) {
        this.confirmationMessage = confirmationMessage;
    }

    public boolean isClassifiedByAI() {
        return classifiedByAI;
    }

    public void setClassifiedByAI(boolean classifiedByAI) {
        this.classifiedByAI = classifiedByAI;
    }

    @Override
    public String toString() {
        return "ServiceRequestAIResult{" +
                "requestType='" + requestType + '\'' +
                ", extractedItems='" + extractedItems + '\'' +
                ", priority='" + priority + '\'' +
                ", estimatedMinutes='" + estimatedMinutes + '\'' +
                ", confirmationMessage='" + confirmationMessage + '\'' +
                ", classifiedByAI=" + classifiedByAI +
                '}';
    }
}
