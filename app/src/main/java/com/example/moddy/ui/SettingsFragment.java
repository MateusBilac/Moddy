package com.example.moddy.ui;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.moddy.databinding.FragmentSettingsBinding;
import com.example.moddy.viewmodel.ModdyViewModel;

import java.util.Calendar;
import java.util.Locale;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private ModdyViewModel viewModel;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(requireContext(), "Permissão de notificação concedida!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Permissão necessária para receber lembretes no horário configurado.", Toast.LENGTH_LONG).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(ModdyViewModel.class);

        boolean enabled = viewModel.isReminderEnabled();
        String time = viewModel.getReminderTime();

        binding.switchDailyReminder.setChecked(enabled);
        binding.tvReminderTimeValue.setText(time);
        binding.layoutReminderTime.setAlpha(enabled ? 1.0f : 0.4f);
        binding.layoutReminderTime.setEnabled(enabled);

        binding.switchDailyReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String scheduleInfo = viewModel.setReminderEnabled(isChecked);
            if (isChecked) {
                checkAndRequestNotificationPermission();
                Toast.makeText(requireContext(), "Lembrete ativado: " + scheduleInfo, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Lembretes desativados.", Toast.LENGTH_SHORT).show();
            }
            binding.layoutReminderTime.setAlpha(isChecked ? 1.0f : 0.4f);
            binding.layoutReminderTime.setEnabled(isChecked);
        });

        binding.layoutReminderTime.setOnClickListener(v -> showTimePicker());
    }

    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(requireContext(), (view, hourOfDay, minuteOfHour) -> {
            String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
            String scheduleInfo = viewModel.setReminderTime(selectedTime);
            binding.tvReminderTimeValue.setText(selectedTime);
            Toast.makeText(requireContext(), "Agendado para " + scheduleInfo, Toast.LENGTH_SHORT).show();
        }, hour, minute, true);
        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}