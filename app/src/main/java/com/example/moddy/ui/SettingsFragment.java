package com.example.moddy.ui;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.moddy.databinding.FragmentSettingsBinding;

import java.util.Calendar;
import java.util.Locale;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private String selectedTime = "20:00";

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

        binding.tvReminderTimeValue.setText(selectedTime);

        binding.switchDailyReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String msg = isChecked ? "Lembretes diários ativados" : "Lembretes diários desativados";
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            binding.layoutReminderTime.setAlpha(isChecked ? 1.0f : 0.4f);
            binding.layoutReminderTime.setEnabled(isChecked);
        });

        binding.layoutReminderTime.setOnClickListener(v -> showTimePicker());
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(requireContext(), (view, hourOfDay, minuteOfHour) -> {
            selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
            binding.tvReminderTimeValue.setText(selectedTime);
        }, hour, minute, true);
        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}