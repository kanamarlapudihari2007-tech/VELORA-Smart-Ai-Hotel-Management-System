package com.hotel.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Review Model - Represents guest ratings, written feedback, and AI sentiment analysis.
 * Core entity for Stage 18: AI Guest Reviews & Sentiment Analysis Engine.
 */
public class Review implements Serializable {

    private int id;
    private int bookingId;
    private int guestId;
    private int rating; // 1 to 5 stars
    private String reviewText;

    // AI Sentiment Analysis Fields
    private String sentiment; // 'POSITIVE', 'NEUTRAL', 'NEGATIVE', 'CRITICAL'
    private double sentimentScore; // -1.0 to 1.0 (or normalized scale)
    private String aspectCleanliness; // 'POSITIVE', 'NEUTRAL', 'NEGATIVE', 'N/A'
    private String aspectStaff;
    private String aspectRoom;
    private String aspectFood;
    private String aspectValue;
    private String keyHighlights;
    private String aiReplyDraft;
    private String managerReply;
    private boolean isEscalated;
    private Timestamp createdAt;

    // Joined fields for view rendering
    private String guestName;
    private String bookingCode;
    private String roomNumber;
    private String typeName;

    public Review() {
    }

    public Review(int bookingId, int guestId, int rating, String reviewText) {
        this.bookingId = bookingId;
        this.guestId = guestId;
        this.rating = rating;
        this.reviewText = reviewText;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getGuestId() {
        return guestId;
    }

    public void setGuestId(int guestId) {
        this.guestId = guestId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }

    public double getSentimentScore() {
        return sentimentScore;
    }

    public void setSentimentScore(double sentimentScore) {
        this.sentimentScore = sentimentScore;
    }

    public String getAspectCleanliness() {
        return aspectCleanliness;
    }

    public void setAspectCleanliness(String aspectCleanliness) {
        this.aspectCleanliness = aspectCleanliness;
    }

    public String getAspectStaff() {
        return aspectStaff;
    }

    public void setAspectStaff(String aspectStaff) {
        this.aspectStaff = aspectStaff;
    }

    public String getAspectRoom() {
        return aspectRoom;
    }

    public void setAspectRoom(String aspectRoom) {
        this.aspectRoom = aspectRoom;
    }

    public String getAspectFood() {
        return aspectFood;
    }

    public void setAspectFood(String aspectFood) {
        this.aspectFood = aspectFood;
    }

    public String getAspectValue() {
        return aspectValue;
    }

    public void setAspectValue(String aspectValue) {
        this.aspectValue = aspectValue;
    }

    public String getKeyHighlights() {
        return keyHighlights;
    }

    public void setKeyHighlights(String keyHighlights) {
        this.keyHighlights = keyHighlights;
    }

    public String getAiReplyDraft() {
        return aiReplyDraft;
    }

    public void setAiReplyDraft(String aiReplyDraft) {
        this.aiReplyDraft = aiReplyDraft;
    }

    public String getManagerReply() {
        return managerReply;
    }

    public void setManagerReply(String managerReply) {
        this.managerReply = managerReply;
    }

    public boolean isEscalated() {
        return isEscalated;
    }

    public void setEscalated(boolean escalated) {
        isEscalated = escalated;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public void setBookingCode(String bookingCode) {
        this.bookingCode = bookingCode;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}
