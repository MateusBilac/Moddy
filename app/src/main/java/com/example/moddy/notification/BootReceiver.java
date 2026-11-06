package com.example.moddy.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.moddy.repository.ModdyRepository;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) ||
            Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())) {

            ModdyRepository repository = new ModdyRepository(context);
            if (repository.isReminderEnabled()) {
                ReminderScheduler.scheduleDailyReminder(context, repository.getReminderTime());
            }
        }
    }
}