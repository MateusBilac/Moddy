package com.example.moddy.ui;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.moddy.databinding.DialogCreateHabitBinding;
import com.example.moddy.db.entity.HabitEntity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Calendar;
import java.util.Locale;

public class CreateHabitBottomSheetDialog extends BottomSheetDialogFragment {

    public interface OnHabitSaveListener {
        void onHabitSaved(String title, String category, String time, HabitEntity editEntity);
    }

    private DialogCreateHabitBinding binding;
    private OnHabitSaveListener listener;
    private String selectedTime = "08:00";
    private HabitEntity habitToEdit = null;

    public static CreateHabitBottomSheetDialog newInstance(HabitEntity habitToEdit) {
        CreateHabitBottomSheetDialog dialog = new CreateHabitBottomSheetDialog();
        dialog.habitToEdit = habitToEdit;
        return dialog;
    }

    public void setOnHabitSaveListener(OnHabitSaveListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DialogCreateHabitBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnSelectTime.setText(selectedTime);

        if (habitToEdit != null) {
            binding.tvDialogTitle.setText("Editar Hábito");
            binding.etHabitName.setText(habitToEdit.getTitle());
            binding.etHabitCategory.setText(habitToEdit.getCategory());
            selectedTime = habitToEdit.getTime();
            binding.btnSelectTime.setText(selectedTime);
        }

        binding.btnSelectTime.setOnClickListener(v -> showTimePicker());
        binding.btnCancelHabit.setOnClickListener(v -> dismiss());

        binding.btnSaveHabit.setOnClickListener(v -> {
            String title = binding.etHabitName.getText() != null ? binding.etHabitName.getText().toString().trim() : "";
            String category = binding.etHabitCategory.getText() != null ? binding.etHabitCategory.getText().toString().trim() : "";

            if (TextUtils.isEmpty(title)) {
                binding.etHabitName.setError("Informe o nome do hábito");
                return;
            }

            if (TextUtils.isEmpty(category)) {
                category = "Geral";
            }

            if (listener != null) {
                listener.onHabitSaved(title, category, selectedTime, habitToEdit);
            }
            dismiss();
        });
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(requireContext(), (view, hourOfDay, minuteOfHour) -> {
            selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
            binding.btnSelectTime.setText(selectedTime);
        }, hour, minute, true);
        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}