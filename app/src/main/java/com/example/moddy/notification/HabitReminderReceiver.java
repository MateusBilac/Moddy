package com.example.moddy.notification;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.moddy.MainActivity;
import com.example.moddy.R;
import com.example.moddy.db.AppDatabase;
import com.example.moddy.db.entity.HabitEntity;
import com.example.moddy.db.entity.HabitRecordEntity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;

public class HabitReminderReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID = "moddy_active_notifications_v5";

    @Override
    public void onReceive(Context context, Intent intent) {
        long habitId = intent.getLongExtra(HabitReminderScheduler.EXTRA_HABIT_ID, -1);
        String title = intent.getStringExtra(HabitReminderScheduler.EXTRA_HABIT_TITLE);
        String category = intent.getStringExtra(HabitReminderScheduler.EXTRA_HABIT_CATEGORY);
        String time = intent.getStringExtra(HabitReminderScheduler.EXTRA_HABIT_TIME);

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);

            HabitEntity targetHabit = null;
            if (habitId != -1) {
                targetHabit = db.habitDao().getHabitByIdSync(habitId);
            }

            if (targetHabit != null && targetHabit.isArchived()) return;

            String habitTitle = (targetHabit != null && targetHabit.getTitle() != null) ? targetHabit.getTitle() : (title != null ? title : "Hábito");
            String habitCategory = (targetHabit != null && targetHabit.getCategory() != null) ? targetHabit.getCategory() : (category != null ? category : "");
            String habitTime = (targetHabit != null && targetHabit.getTime() != null) ? targetHabit.getTime() : (time != null ? time : "");

            // Check if completed for today
            String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
            boolean isCompleted = false;
            if (habitId != -1) {
                HabitRecordEntity record = db.habitDao().getRecordSync(habitId, todayDate);
                isCompleted = (record != null && record.isCompleted());
            }

            // Re-schedule for tomorrow
            if (targetHabit != null) {
                HabitReminderScheduler.scheduleHabitReminder(context, targetHabit);
            }

            // If not completed today, send notification!
            if (!isCompleted) {
                String notificationTitle = "Moddy • " + habitTitle;
                String categoryText = (!habitCategory.isEmpty()) ? " [" + habitCategory + "]" : "";
                String notificationContent = "Lembrete das " + habitTime + categoryText + ": Hora de praticar '" + habitTitle + "'!";

                int notifyId = habitId != -1 ? (int) (habitId % Integer.MAX_VALUE) : (int) System.currentTimeMillis();
                sendNotification(context, notifyId, notificationTitle, notificationContent);
            }
        });
    }

    @SuppressLint("MissingPermission")
    private static void sendNotification(Context context, int notificationId, String title, String contentText) {
        createNotificationChannel(context);

        Intent openAppIntent = new Intent(context, MainActivity.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, notificationId, openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Uri defaultNotificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_nav_today)
                .setContentTitle(title)
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSound(defaultNotificationSound)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(notificationId, builder.build());
        }
    }

    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager == null) return;

            NotificationChannel existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID);
            if (existingChannel == null) {
                CharSequence name = "Lembretes do Moddy";
                String description = "Notificações ativas de hábitos e humor";
                int importance = NotificationManager.IMPORTANCE_HIGH;

                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
                channel.setDescription(description);

                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build();

                Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                channel.setSound(defaultSoundUri, audioAttributes);
                channel.enableVibration(true);
                channel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);

                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}