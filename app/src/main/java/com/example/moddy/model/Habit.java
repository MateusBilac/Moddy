package com.example.moddy.model;

public class Habit {
    private String id;
    private String title;
    private String category;
    private String time;
    private boolean isCompleted;
    private boolean isArchived;

    public Habit(String id, String title, String category, String time) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.time = time;
        this.isCompleted = false;
        this.isArchived = false;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public boolean isArchived() {
        return isArchived;
    }

    public void setArchived(boolean archived) {
        isArchived = archived;
    }
}