package com.example.moddy.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.moddy.db.entity.MoodRecordEntity;

import java.util.List;

@Dao
public interface MoodDao {

    @Query("SELECT * FROM mood_records WHERE dateString = :dateString LIMIT 1")
    LiveData<MoodRecordEntity> getMoodForDateLive(String dateString);

    @Query("SELECT * FROM mood_records WHERE dateString = :dateString LIMIT 1")
    MoodRecordEntity getMoodForDateSync(String dateString);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdateMood(MoodRecordEntity moodRecord);

    @Query("SELECT * FROM mood_records ORDER BY dateString DESC")
    LiveData<List<MoodRecordEntity>> getAllMoodRecords();
}