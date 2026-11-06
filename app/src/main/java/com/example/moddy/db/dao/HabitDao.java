package com.example.moddy.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.moddy.db.entity.HabitEntity;
import com.example.moddy.db.entity.HabitRecordEntity;

import java.util.List;

@Dao
public interface HabitDao {

    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    LiveData<List<HabitEntity>> getAllHabits();

    @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY createdAt DESC")
    LiveData<List<HabitEntity>> getActiveHabits();

    @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY createdAt DESC")
    List<HabitEntity> getActiveHabitsSync();

    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    HabitEntity getHabitByIdSync(long id);

    @Query("SELECT * FROM habits WHERE isArchived = 1 ORDER BY createdAt DESC")
    LiveData<List<HabitEntity>> getArchivedHabits();

    @Insert
    long insertHabit(HabitEntity habit);

    @Update
    void updateHabit(HabitEntity habit);

    @Delete
    void deleteHabit(HabitEntity habit);

    @Query("SELECT * FROM habit_records WHERE dateString = :dateString")
    LiveData<List<HabitRecordEntity>> getHabitRecordsForDate(String dateString);

    @Query("SELECT * FROM habit_records WHERE dateString = :dateString")
    List<HabitRecordEntity> getRecordsForDateSync(String dateString);

    @Query("SELECT * FROM habit_records WHERE habitId = :habitId AND dateString = :dateString LIMIT 1")
    HabitRecordEntity getRecordSync(long habitId, String dateString);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdateRecord(HabitRecordEntity record);

    @Query("DELETE FROM habit_records WHERE habitId = :habitId")
    void deleteRecordsForHabit(long habitId);
}