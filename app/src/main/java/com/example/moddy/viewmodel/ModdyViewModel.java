package com.example.moddy.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.moddy.db.entity.HabitEntity;
import com.example.moddy.db.entity.HabitRecordEntity;
import com.example.moddy.db.entity.MoodRecordEntity;
import com.example.moddy.model.WeeklyDaySummary;
import com.example.moddy.repository.ModdyRepository;

import java.util.List;

public class ModdyViewModel extends AndroidViewModel {

    private final ModdyRepository repository;
    private final MutableLiveData<String> selectedDate = new MutableLiveData<>();
    private final LiveData<MoodRecordEntity> selectedDateMood;
    private final LiveData<List<HabitRecordEntity>> selectedDateHabitRecords;
    private final MutableLiveData<String> statusMessage = new MutableLiveData<>();

    public ModdyViewModel(@NonNull Application application) {
        super(application);
        repository = new ModdyRepository(application);

        String today = repository.getTodayDateString();
        selectedDate.setValue(today);

        selectedDateMood = Transformations.switchMap(selectedDate, repository::getMoodForDate);
        selectedDateHabitRecords = Transformations.switchMap(selectedDate, repository::getHabitRecordsForDate);
    }

    public LiveData<List<HabitEntity>> getActiveHabits() {
        return repository.getAllActiveHabits();
    }

    public LiveData<List<HabitEntity>> getAllHabits() {
        return repository.getAllHabits();
    }

    public LiveData<List<HabitEntity>> getArchivedHabits() {
        return repository.getArchivedHabits();
    }

    public LiveData<MoodRecordEntity> getTodayMood() {
        return selectedDateMood;
    }

    public LiveData<List<HabitRecordEntity>> getTodayHabitRecords() {
        return selectedDateHabitRecords;
    }

    public LiveData<MoodRecordEntity> getMoodForSelectedDate() {
        return selectedDateMood;
    }

    public LiveData<List<MoodRecordEntity>> getAllMoodRecords() {
        return repository.getAllMoodRecords();
    }

    public LiveData<List<WeeklyDaySummary>> getWeeklySummary() {
        return repository.getWeeklySummary();
    }

    public LiveData<String> getStatusMessage() {
        return statusMessage;
    }

    public void setSelectedDate(String dateString) {
        selectedDate.setValue(dateString);
    }

    public String getSelectedDate() {
        return selectedDate.getValue() != null ? selectedDate.getValue() : repository.getTodayDateString();
    }

    public String getTodayDateString() {
        return repository.getTodayDateString();
    }

    public boolean isDateWithin7Days(String dateString) {
        return repository.isDateWithin7Days(dateString);
    }

    public void addHabit(String title, String category, String time) {
        repository.insertHabit(title, category, time, (success, message) -> statusMessage.postValue(message));
    }

    public void updateHabit(HabitEntity habit) {
        repository.updateHabit(habit, (success, message) -> statusMessage.postValue(message));
    }

    public void deleteHabit(HabitEntity habit) {
        repository.deleteHabit(habit);
        statusMessage.postValue("Hábito excluído!");
    }

    public void toggleHabitRecord(long habitId) {
        String targetDate = getSelectedDate();
        repository.toggleHabitRecord(habitId, targetDate, (success, message) -> statusMessage.postValue(message));
    }

    public void toggleHabitRecordForDate(long habitId, String dateString) {
        repository.toggleHabitRecord(habitId, dateString, (success, message) -> statusMessage.postValue(message));
    }

    public void setTodayMood(String moodName, String moodEmoji) {
        String today = repository.getTodayDateString();
        repository.setMoodForDate(today, moodName, moodEmoji, (success, message) -> statusMessage.postValue(message));
    }

    public void setMoodForDate(String dateString, String moodName, String moodEmoji) {
        repository.setMoodForDate(dateString, moodName, moodEmoji, (success, message) -> statusMessage.postValue(message));
    }

    // Reminder Settings
    public boolean isReminderEnabled() {
        return repository.isReminderEnabled();
    }

    public String setReminderEnabled(boolean enabled) {
        return repository.setReminderEnabled(enabled);
    }

    public String getReminderTime() {
        return repository.getReminderTime();
    }

    public String setReminderTime(String time) {
        return repository.setReminderTime(time);
    }
}