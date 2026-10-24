package com.example.moddy.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.moddy.R;
import com.example.moddy.adapter.HabitsAdapter;
import com.example.moddy.databinding.FragmentTodayBinding;
import com.example.moddy.model.Habit;

import java.util.ArrayList;
import java.util.List;

public class TodayFragment extends Fragment implements HabitsAdapter.HabitActionListener {

    private FragmentTodayBinding binding;
    private HabitsAdapter adapter;
    private final List<Habit> habits = new ArrayList<>();
    private String selectedMood = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTodayBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupMoodSelectors();
        setupButtons();
        updateUI();
    }

    private void setupRecyclerView() {
        adapter = new HabitsAdapter(this);
        binding.rvTodayHabits.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTodayHabits.setAdapter(adapter);
    }

    private void setupMoodSelectors() {
        binding.btnMoodExcellent.setOnClickListener(v -> selectMood("Excelente 😊", binding.btnMoodExcellent));
        binding.btnMoodGood.setOnClickListener(v -> selectMood("Bem 🙂", binding.btnMoodGood));
        binding.btnMoodNeutral.setOnClickListener(v -> selectMood("Neutro 😐", binding.btnMoodNeutral));
        binding.btnMoodTired.setOnClickListener(v -> selectMood("Cansado 😴", binding.btnMoodTired));
        binding.btnMoodSad.setOnClickListener(v -> selectMood("Chateado 😔", binding.btnMoodSad));
    }

    private void selectMood(String moodName, View selectedView) {
        selectedMood = moodName;
        resetMoodSelection();
        selectedView.setBackgroundResource(R.drawable.bg_mood_selected);
        binding.tvMoodSelectedStatus.setText("Humor hoje: " + selectedMood);
    }

    private void resetMoodSelection() {
        binding.btnMoodExcellent.setBackgroundResource(R.drawable.bg_mood_unselected);
        binding.btnMoodGood.setBackgroundResource(R.drawable.bg_mood_unselected);
        binding.btnMoodNeutral.setBackgroundResource(R.drawable.bg_mood_unselected);
        binding.btnMoodTired.setBackgroundResource(R.drawable.bg_mood_unselected);
        binding.btnMoodSad.setBackgroundResource(R.drawable.bg_mood_unselected);
    }

    private void setupButtons() {
        View.OnClickListener openAddDialog = v -> showAddHabitDialog();
        binding.btnAddFirstHabit.setOnClickListener(openAddDialog);
        binding.fabAddHabit.setOnClickListener(openAddDialog);
    }

    private void showAddHabitDialog() {
        CreateHabitBottomSheetDialog dialog = CreateHabitBottomSheetDialog.newInstance(null);
        dialog.setOnHabitSaveListener(newHabit -> {
            habits.add(newHabit);
            updateUI();
        });
        dialog.show(getChildFragmentManager(), "AddHabitToday");
    }

    private void updateUI() {
        if (habits.isEmpty()) {
            binding.layoutEmptyTodayHabits.setVisibility(View.VISIBLE);
            binding.rvTodayHabits.setVisibility(View.GONE);
            binding.progressIndicator.setProgress(0);
            binding.tvProgressText.setText(getString(R.string.progress_empty));
        } else {
            binding.layoutEmptyTodayHabits.setVisibility(View.GONE);
            binding.rvTodayHabits.setVisibility(View.VISIBLE);
            adapter.setHabits(habits);

            int completed = 0;
            for (Habit h : habits) {
                if (h.isCompleted()) completed++;
            }
            int total = habits.size();
            int percent = (int) (((float) completed / total) * 100);

            binding.progressIndicator.setProgress(percent);
            binding.tvProgressText.setText(completed + " de " + total + " hábitos concluídos hoje (" + percent + "%)");
        }
    }

    @Override
    public void onToggleCheck(Habit habit, int position) {
        habit.setCompleted(!habit.isCompleted());
        updateUI();
    }

    @Override
    public void onEditHabit(Habit habit, int position) {
        CreateHabitBottomSheetDialog dialog = CreateHabitBottomSheetDialog.newInstance(habit);
        dialog.setOnHabitSaveListener(updated -> updateUI());
        dialog.show(getChildFragmentManager(), "EditHabitToday");
    }

    @Override
    public void onDeleteHabit(Habit habit, int position) {
        habits.remove(habit);
        updateUI();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}