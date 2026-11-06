package com.example.moddy.repository;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.moddy.db.AppDatabase;
import com.example.moddy.db.dao.HabitDao;
import com.example.moddy.db.dao.MoodDao;
import com.example.moddy.db.entity.HabitEntity;
import com.example.moddy.db.entity.HabitRecordEntity;
import com.example.moddy.db.entity.MoodRecordEntity;
import com.example.moddy.model.WeeklyDaySummary;
import com.example.moddy.notification.HabitReminderScheduler;
import com.example.moddy.notification.ReminderScheduler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ModdyRepository {

    private static final String PREFS_NAME = "moddy_prefs";
    private static final String KEY_REMINDER_ENABLED = "reminder_enabled";
    private static final String KEY_REMINDER_TIME = "reminder_time";

    private final HabitDao habitDao;
    private final MoodDao moodDao;
    private final SharedPreferences prefs;
    private final ExecutorService executor;
    private final Context context;

    public ModdyRepository(Context context) {
        this.context = context.getApplicationContext();
        AppDatabase db = AppDatabase.getInstance(this.context);
        this.habitDao = db.habitDao();
        this.moodDao = db.moodDao();
        this.prefs = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<HabitEntity>> getAllActiveHabits() {
        return habitDao.getActiveHabits();
    }

    public LiveData<List<HabitEntity>> getAllHabits() {
        return habitDao.getAllHabits();
    }

    public LiveData<List<HabitEntity>> getArchivedHabits() {
        return habitDao.getArchivedHabits();
    }

    public LiveData<List<HabitRecordEntity>> getHabitRecordsForDate(String dateString) {
        return habitDao.getHabitRecordsForDate(dateString);
    }

    public LiveData<MoodRecordEntity> getMoodForDate(String dateString) {
        return moodDao.getMoodForDateLive(dateString);
    }

    public LiveData<List<MoodRecordEntity>> getAllMoodRecords() {
        return moodDao.getAllMoodRecords();
    }

    public LiveData<List<WeeklyDaySummary>> getWeeklySummary() {
        MutableLiveData<List<WeeklyDaySummary>> weeklyLiveData = new MutableLiveData<>();
        executor.execute(() -> {
            List<WeeklyDaySummary> list = new ArrayList<>();
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", new Locale("pt", "BR"));

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -6);

            List<HabitEntity> activeHabits = habitDao.getActiveHabitsSync();
            int totalActive = activeHabits != null ? activeHabits.size() : 0;

            for (int i = 0; i < 7; i++) {
                String isoDate = isoFormat.format(cal.getTime());
                String dayLabel = dayFormat.format(cal.getTime()).substring(0, 3).toUpperCase();

                List<HabitRecordEntity> records = habitDao.getRecordsForDateSync(isoDate);
                int completedCount = 0;
                if (records != null) {
                    for (HabitRecordEntity r : records) {
                        if (r.isCompleted()) completedCount++;
                    }
                }

                MoodRecordEntity mood = moodDao.getMoodForDateSync(isoDate);
                String emoji = mood != null ? mood.getMoodEmoji() : "";

                list.add(new WeeklyDaySummary(dayLabel, isoDate, completedCount, totalActive, emoji));
                cal.add(Calendar.DAY_OF_YEAR, 1);
            }
            weeklyLiveData.postValue(list);
        });
        return weeklyLiveData;
    }

    public void insertHabit(String title, String category, String time, OnOperationResultListener listener) {
        executor.execute(() -> {
            HabitEntity habit = new HabitEntity(title, category, time);
            long id = habitDao.insertHabit(habit);
            habit.setId(id);
            String scheduleInfo = HabitReminderScheduler.scheduleHabitReminder(context, habit);
            if (listener != null) {
                listener.onResult(true, "Hábito criado! Lembrete: " + scheduleInfo);
            }
        });
    }

    public void updateHabit(HabitEntity habit, OnOperationResultListener listener) {
        executor.execute(() -> {
            habitDao.updateHabit(habit);
            String scheduleInfo = HabitReminderScheduler.scheduleHabitReminder(context, habit);
            if (listener != null) {
                listener.onResult(true, "Hábito atualizado! Lembrete: " + scheduleInfo);
            }
        });
    }

    public void deleteHabit(HabitEntity habit) {
        executor.execute(() -> {
            habitDao.deleteRecordsForHabit(habit.getId());
            habitDao.deleteHabit(habit);
            HabitReminderScheduler.cancelHabitReminder(context, habit.getId());
        });
    }

    /**
     * [RN001] Registo Retroativo: limite de até 7 dias anteriores à data atual.
     */
    public boolean isDateWithin7Days(String dateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date targetDate = sdf.parse(dateString);
            if (targetDate == null) return false;

            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 23);
            today.set(Calendar.MINUTE, 59);
            today.set(Calendar.SECOND, 59);

            Calendar limit7DaysAgo = Calendar.getInstance();
            limit7DaysAgo.add(Calendar.DAY_OF_YEAR, -7);
            limit7DaysAgo.set(Calendar.HOUR_OF_DAY, 0);
            limit7DaysAgo.set(Calendar.MINUTE, 0);
            limit7DaysAgo.set(Calendar.SECOND, 0);

            return !targetDate.after(today.getTime()) && !targetDate.before(limit7DaysAgo.getTime());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Toggle habit completion for a specific date, enforcing [RN001].
     */
    public void toggleHabitRecord(long habitId, String dateString, OnOperationResultListener listener) {
        executor.execute(() -> {
            if (!isDateWithin7Days(dateString)) {
                if (listener != null) {
                    listener.onResult(false, "Não é possível alterar registros com mais de 7 dias.");
                }
                return;
            }

            HabitRecordEntity record = habitDao.getRecordSync(habitId, dateString);
            boolean newStatus = (record == null) || !record.isCompleted();
            HabitRecordEntity updated = new HabitRecordEntity(habitId, dateString, newStatus);
            habitDao.insertOrUpdateRecord(updated);

            if (listener != null) {
                listener.onResult(true, "Hábito atualizado com sucesso!");
            }
        });
    }

    /**
     * [RN002] Unicidade do Registo de Humor: apenas uma avaliação por dia, atualizando se já existir.
     */
    public void setMoodForDate(String dateString, String moodName, String moodEmoji, OnOperationResultListener listener) {
        executor.execute(() -> {
            if (!isDateWithin7Days(dateString)) {
                if (listener != null) {
                    listener.onResult(false, "Não é possível alterar registros de humor com mais de 7 dias.");
                }
                return;
            }

            MoodRecordEntity moodRecord = new MoodRecordEntity(dateString, moodName, moodEmoji);
            moodDao.insertOrUpdateMood(moodRecord);

            if (listener != null) {
                listener.onResult(true, "Humor registrado com sucesso!");
            }
        });
    }

    // [RN005] Notification Settings
    public boolean isReminderEnabled() {
        return prefs.getBoolean(KEY_REMINDER_ENABLED, true);
    }

    public String setReminderEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_REMINDER_ENABLED, enabled).apply();
        if (enabled) {
            return ReminderScheduler.scheduleDailyReminder(context, getReminderTime());
        } else {
            ReminderScheduler.cancelReminder(context);
            return "Lembretes desativados.";
        }
    }

    public String getReminderTime() {
        return prefs.getString(KEY_REMINDER_TIME, "20:00");
    }

    public String setReminderTime(String time) {
        prefs.edit().putString(KEY_REMINDER_TIME, time).apply();
        if (isReminderEnabled()) {
            return ReminderScheduler.scheduleDailyReminder(context, time);
        }
        return "Horário de notificação salvo.";
    }

    public String getTodayDateString() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }

    public interface OnOperationResultListener {
        void onResult(boolean success, String message);
    }
}