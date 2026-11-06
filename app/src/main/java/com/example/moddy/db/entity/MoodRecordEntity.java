package com.example.moddy.db.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "mood_records", indices = {@Index(value = {"dateString"}, unique = true)})
public class MoodRecordEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private String dateString;
    private String moodName;
    private String moodEmoji;
    private long createdAt;

    public MoodRecordEntity(String dateString, String moodName, String moodEmoji) {
        this.dateString = dateString;
        this.moodName = moodName;
        this.moodEmoji = moodEmoji;
        this.createdAt = System.currentTimeMillis();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDateString() {
        return dateString;
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }

    public String getMoodName() {
        return moodName;
    }

    public void setMoodName(String moodName) {
        this.moodName = moodName;
    }

    public String getMoodEmoji() {
        return moodEmoji;
    }

    public void setMoodEmoji(String moodEmoji) {
        this.moodEmoji = moodEmoji;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}