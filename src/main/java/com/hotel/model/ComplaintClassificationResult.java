package com.hotel.model;

import java.io.Serializable;

/**
 * ComplaintClassificationResult - DTO holding AI-classified metadata for guest complaints.
 */
public class ComplaintClassificationResult implements Serializable {

    private String category;
    private String priority;
    private String shortDescription;
    private String reassuranceMessage;
    private boolean classifiedByAI;

    public ComplaintClassificationResult() {
    }

    public ComplaintClassificationResult(String category, String priority, String shortDescription, String reassuranceMessage, boolean classifiedByAI) {
        this.category = category;
        this.priority = priority;
        this.shortDescription = shortDescription;
        this.reassuranceMessage = reassuranceMessage;
        this.classifiedByAI = classifiedByAI;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getReassuranceMessage() {
        return reassuranceMessage;
    }

    public void setReassuranceMessage(String reassuranceMessage) {
        this.reassuranceMessage = reassuranceMessage;
    }

    public boolean isClassifiedByAI() {
        return classifiedByAI;
    }

    public void setClassifiedByAI(boolean classifiedByAI) {
        this.classifiedByAI = classifiedByAI;
    }
}
