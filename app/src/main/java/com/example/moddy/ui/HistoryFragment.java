package com.example.moddy.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.moddy.databinding.FragmentHistoryBinding;
import com.example.moddy.databinding.ItemWeeklyDayBinding;
import com.example.moddy.model.WeeklyDaySummary;
import com.example.moddy.viewmodel.ModdyViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HistoryFragment extends Fragment {

    private FragmentHistoryBinding binding;
    private ModdyViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(ModdyViewModel.class);

        setupCalendar();
        observeViewModel();
    }

    private void setupCalendar() {
        SimpleDateFormat dateDisplayFormat = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", new Locale("pt", "BR"));
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        binding.calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth);

            String isoDate = isoFormat.format(selected.getTime());
            String displayDate = dateDisplayFormat.format(selected.getTime());

            viewModel.setSelectedDate(isoDate);
            binding.tvSelectedDateTitle.setText("Detalhes de " + displayDate);

            boolean isWithin7Days = viewModel.isDateWithin7Days(isoDate);
            if (!isWithin7Days) {
                binding.tvDateMoodSummary.setText("Data selecionada possui mais de 7 dias (RN001: Somente leitura).");
            }
        });
    }

    private void observeViewModel() {
        viewModel.getMoodForSelectedDate().observe(getViewLifecycleOwner(), moodRecord -> {
            if (moodRecord != null) {
                binding.tvDateMoodSummary.setText("Humor registrado: " + moodRecord.getMoodName() + " " + moodRecord.getMoodEmoji());
            } else {
                String selectedDate = viewModel.getSelectedDate();
                boolean isWithin7Days = viewModel.isDateWithin7Days(selectedDate);
                if (isWithin7Days) {
                    binding.tvDateMoodSummary.setText("Sem registro de humor para esta data. (Dentro do limite de 7 dias para registro).");
                } else {
                    binding.tvDateMoodSummary.setText("Sem registro de humor. (Data anterior ao limite de 7 dias).");
                }
            }
        });

        viewModel.getWeeklySummary().observe(getViewLifecycleOwner(), this::updateWeeklyChart);
    }

    private void updateWeeklyChart(List<WeeklyDaySummary> weeklySummaries) {
        binding.layoutWeeklyBars.removeAllViews();
        if (weeklySummaries == null || weeklySummaries.isEmpty()) return;

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (WeeklyDaySummary day : weeklySummaries) {
            ItemWeeklyDayBinding dayBinding = ItemWeeklyDayBinding.inflate(inflater, binding.layoutWeeklyBars, false);

            dayBinding.tvDayMood.setText(day.getMoodEmoji().isEmpty() ? "•" : day.getMoodEmoji());
            dayBinding.tvDayPercent.setText(day.getCompletionPercentage() + "%");
            dayBinding.tvDayLabel.setText(day.getDayLabel());

            // Dynamically set bar height relative to completion percentage (max 60dp)
            int barMaxHeightPx = (int) (60 * getResources().getDisplayMetrics().density);
            int calculatedHeight = (int) (barMaxHeightPx * (day.getCompletionPercentage() / 100.0f));
            if (day.getCompletionPercentage() > 0 && calculatedHeight < 8) {
                calculatedHeight = 8; // minimum visible indicator
            }

            ViewGroup.LayoutParams params = dayBinding.viewDayBarFill.getLayoutParams();
            params.height = calculatedHeight;
            dayBinding.viewDayBarFill.setLayoutParams(params);

            binding.layoutWeeklyBars.addView(dayBinding.getRoot());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}