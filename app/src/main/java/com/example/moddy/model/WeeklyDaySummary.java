package com.example.moddy.model;

public class WeeklyDaySummary {

    private final String dayLabel; // e.g., "Seg", "Ter", "Qua"
    private final String dateString; // "yyyy-MM-dd"
    private final int completedCount;
    private final int totalCount;
    private final int completionPercentage;
    private final String moodEmoji;

    public WeeklyDaySummary(String dayLabel, String dateString, int completedCount, int totalCount, String moodEmoji) {
        this.dayLabel = dayLabel;
        this.dateString = dateString;
        this.completedCount = completedCount;
        this.totalCount = totalCount;
        this.completionPercentage = totalCount > 0 ? (int) (((float) completedCount / totalCount) * 100) : 0;
        this.moodEmoji = moodEmoji != null ? moodEmoji : "";
    }

    public String getDayLabel() {
        return dayLabel;
    }

    public String getDateString() {
        return dateString;
    }

    public int getCompletedCount() {
        return completedCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getCompletionPercentage() {
        return completionPercentage;
    }

    public String getMoodEmoji() {
        return moodEmoji;
    }
}