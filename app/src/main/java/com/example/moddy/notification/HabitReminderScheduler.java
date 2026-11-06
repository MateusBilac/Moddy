package com.example.moddy.notification;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import com.example.moddy.db.entity.HabitEntity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class HabitReminderScheduler {

    public static final String EXTRA_HABIT_ID = "extra_habit_id";
    public static final String EXTRA_HABIT_TITLE = "extra_habit_title";
    public static final String EXTRA_HABIT_CATEGORY = "extra_habit_category";
    public static final String EXTRA_HABIT_TIME = "extra_habit_time";

    @SuppressLint("ScheduleExactAlarm")
    public static String scheduleHabitReminder(Context context, HabitEntity habit) {
        if (habit == null || habit.isArchived()) return "Hábito inativo.";

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return "Erro no sistema de alarmes.";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                try {
                    Intent permIntent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    permIntent.setData(Uri.parse("package:" + context.getPackageName()));
                    permIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(permIntent);
                } catch (Exception ignored) {}
            }
        }

        Intent intent = new Intent(context, HabitReminderReceiver.class);
        intent.setAction("com.example.moddy.HABIT_REMINDER_" + habit.getId());
        intent.setData(Uri.parse("moddy://habit/" + habit.getId()));
        intent.putExtra(EXTRA_HABIT_ID, habit.getId());
        intent.putExtra(EXTRA_HABIT_TITLE, habit.getTitle());
        intent.putExtra(EXTRA_HABIT_CATEGORY, habit.getCategory());
        intent.putExtra(EXTRA_HABIT_TIME, habit.getTime());

        int requestCode = (int) (habit.getId() % Integer.MAX_VALUE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        int hour = 8;
        int minute = 0;
        try {
            String timeString = habit.getTime();
            if (timeString != null && timeString.contains(":")) {
                String[] parts = timeString.split(":");
                hour = Integer.parseInt(parts[0]);
                minute = Integer.parseInt(parts[1]);
            }
        } catch (Exception ignored) {}

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long triggerTime = calendar.getTimeInMillis();
        long currentTime = System.currentTimeMillis();
        boolean isToday = true;

        if (triggerTime < currentTime - 10000) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            triggerTime = calendar.getTimeInMillis();
            isToday = false;
        } else if (triggerTime <= currentTime) {
            triggerTime = currentTime + 10000;
            isToday = true;
        }

        try {
            AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(triggerTime, pendingIntent);
            alarmManager.setAlarmClock(clockInfo, pendingIntent);
        } catch (Exception e) {
            try {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            } catch (Exception ex) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            }
        }

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String formattedTime = timeFormat.format(calendar.getTime());
        return (isToday ? "Hoje às " : "Amanhã às ") + formattedTime;
    }

    public static void cancelHabitReminder(Context context, long habitId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, HabitReminderReceiver.class);
        intent.setAction("com.example.moddy.HABIT_REMINDER_" + habitId);
        intent.setData(Uri.parse("moddy://habit/" + habitId));

        int requestCode = (int) (habitId % Integer.MAX_VALUE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
    }
}