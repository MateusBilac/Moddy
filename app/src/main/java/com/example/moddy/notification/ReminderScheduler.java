package com.example.moddy.notification;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ReminderScheduler {

    @SuppressLint("ScheduleExactAlarm")
    public static String scheduleDailyReminder(Context context, String timeString) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return "Erro ao acessar o gerenciador de alarmes.";

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

        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.setAction("com.example.moddy.DAILY_REMINDER");
        intent.setData(Uri.parse("moddy://daily_reminder"));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 100, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        int hour = 20;
        int minute = 0;
        try {
            if (timeString != null && timeString.contains(":")) {
                String[] parts = timeString.split(":");
                hour = Integer.parseInt(parts[0]);
                minute = Integer.parseInt(parts[1]);
            }
        } catch (Exception ignored) {}

        Calendar now = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        boolean isToday = true;
        if (calendar.getTimeInMillis() < now.getTimeInMillis() - 60000) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            isToday = false;
        } else if (calendar.getTimeInMillis() <= now.getTimeInMillis()) {
            calendar.setTimeInMillis(now.getTimeInMillis() + 5000);
            isToday = true;
        }

        try {
            AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), pendingIntent);
            alarmManager.setAlarmClock(clockInfo, pendingIntent);
        } catch (Exception e) {
            try {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            } catch (Exception ex) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        }

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String formattedTime = timeFormat.format(calendar.getTime());
        return (isToday ? "Hoje às " : "Amanhã às ") + formattedTime;
    }

    public static void cancelReminder(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.setAction("com.example.moddy.DAILY_REMINDER");
        intent.setData(Uri.parse("moddy://daily_reminder"));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 100, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
    }
}