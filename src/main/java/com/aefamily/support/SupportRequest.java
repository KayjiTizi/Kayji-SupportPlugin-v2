package com.aefamily.support;

public class SupportRequest {
    private int id;
    private final String player;
    private final String message;
    private final String time;
    private Status status;
    private String category;
    private Priority priority;
    private String assignedStaff;
    private String lastUpdated;

    public SupportRequest(int id,
                          String player,
                          String message,
                          String time,
                          Status status,
                          String category,
                          Priority priority,
                          String assignedStaff,
                          String lastUpdated) {
        this.id = id;
        this.player = player;
        this.message = message;
        this.time = time;
        this.status = status;
        this.category = category;
        this.priority = priority;
        this.assignedStaff = assignedStaff;
        this.lastUpdated = lastUpdated;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPlayer() {
        return player;
    }

    public String getMessage() {
        return message;
    }

    public String getTime() {
        return time;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public String getAssignedStaff() {
        return assignedStaff;
    }

    public void setAssignedStaff(String assignedStaff) {
        this.assignedStaff = assignedStaff;
    }

    public boolean isAssigned() {
        return assignedStaff != null && !assignedStaff.trim().isEmpty();
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
