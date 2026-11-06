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
import com.example.moddy.repository.ModdyRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class ReminderReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID = "moddy_active_notifications_v5";

    @Override
    public void onReceive(Context context, Intent intent) {
        ModdyRepository repository = new ModdyRepository(context);

        // Schedule next alarm for TOMORROW at the same time
        if (repository.isReminderEnabled()) {
            ReminderScheduler.scheduleDailyReminder(context, repository.getReminderTime());
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            if (!repository.isReminderEnabled()) return;

            AppDatabase db = AppDatabase.getInstance(context);
            List<HabitEntity> activeHabits = db.habitDao().getActiveHabitsSync();

            String title = "Moddy - Lembrete Diário";
            String content;

            if (activeHabits == null || activeHabits.isEmpty()) {
                content = "Hora de registrar seu humor e criar seus novos hábitos no Moddy! ✨";
            } else {
                String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
                List<HabitRecordEntity> todayRecords = db.habitDao().getRecordsForDateSync(todayDate);

                int completedCount = 0;
                if (todayRecords != null) {
                    for (HabitRecordEntity r : todayRecords) {
                        if (r.isCompleted()) completedCount++;
                    }
                }

                int pending = activeHabits.size() - completedCount;
                if (pending > 0) {
                    content = "Você possui " + pending + " hábito(s) pendente(s) hoje. Que tal registrar sua rotina?";
                } else {
                    content = "Parabéns! Todos os seus hábitos de hoje já foram concluídos. Como está seu humor agora?";
                }
            }

            sendNotification(context, title, content);
        });
    }

    @SuppressLint("MissingPermission")
    private static void sendNotification(Context context, String title, String contentText) {
        createNotificationChannel(context);

        Intent openAppIntent = new Intent(context, MainActivity.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, openAppIntent,
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
            notificationManager.notify(1001, builder.build());
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