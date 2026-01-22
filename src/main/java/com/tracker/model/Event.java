package com.tracker.model;

import java.util.Date;

public class Event {
    private int id;
    private String title;
    private String description;
    private Date eventDate;
    private String location;
    private int departmentId;
    private String status; // SCHEDULED, COMPLETED, CANCELLED

    // Constructors
    public Event() {}

    public Event(int id, String title, String description, Date eventDate, String location, int departmentId, String status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.eventDate = eventDate;
        this.location = location;
        this.departmentId = departmentId;
        this.status = status;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Date getEventDate() { return eventDate; }
    public void setEventDate(Date eventDate) { this.eventDate = eventDate; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public int getDepartmentId() { return departmentId; }
    public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
