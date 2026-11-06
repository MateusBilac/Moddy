package com.example.moddy.db.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "habit_records", indices = {@Index(value = {"habitId", "dateString"}, unique = true)})
public class HabitRecordEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private long habitId;
    private String dateString;
    private boolean isCompleted;

    public HabitRecordEntity(long habitId, String dateString, boolean isCompleted) {
        this.habitId = habitId;
        this.dateString = dateString;
        this.isCompleted = isCompleted;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getHabitId() {
        return habitId;
    }

    public void setHabitId(long habitId) {
        this.habitId = habitId;
    }

    public String getDateString() {
        return dateString;
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}