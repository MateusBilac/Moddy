package com.example.moddy.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.moddy.R;
import com.example.moddy.adapter.HabitsAdapter;
import com.example.moddy.databinding.FragmentTodayBinding;
import com.example.moddy.db.entity.HabitEntity;
import com.example.moddy.db.entity.HabitRecordEntity;
import com.example.moddy.db.entity.MoodRecordEntity;
import com.example.moddy.viewmodel.ModdyViewModel;

import java.util.ArrayList;
import java.util.List;

public class TodayFragment extends Fragment implements HabitsAdapter.HabitActionListener {

    private FragmentTodayBinding binding;
    private ModdyViewModel viewModel;
    private HabitsAdapter adapter;

    private List<HabitEntity> currentHabits = new ArrayList<>();
    private final List<Long> completedHabitIds = new ArrayList<>();

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

        viewModel = new ViewModelProvider(requireActivity()).get(ModdyViewModel.class);

        setupRecyclerView();
        setupMoodSelectors();
        setupButtons();
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new HabitsAdapter(this);
        binding.rvTodayHabits.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTodayHabits.setAdapter(adapter);
    }

    private void setupMoodSelectors() {
        binding.btnMoodExcellent.setOnClickListener(v -> viewModel.setTodayMood("Excelente", "😊"));
        binding.btnMoodGood.setOnClickListener(v -> viewModel.setTodayMood("Bem", "🙂"));
        binding.btnMoodNeutral.setOnClickListener(v -> viewModel.setTodayMood("Neutro", "😐"));
        binding.btnMoodTired.setOnClickListener(v -> viewModel.setTodayMood("Cansado", "😴"));
        binding.btnMoodSad.setOnClickListener(v -> viewModel.setTodayMood("Chateado", "😔"));
    }

    private void setupButtons() {
        View.OnClickListener openAddDialog = v -> showAddHabitDialog(null);
        binding.btnAddFirstHabit.setOnClickListener(openAddDialog);
        binding.fabAddHabit.setOnClickListener(openAddDialog);
    }

    private void observeViewModel() {
        viewModel.getActiveHabits().observe(getViewLifecycleOwner(), habits -> {
            currentHabits = habits != null ? habits : new ArrayList<>();
            updateHabitsUI();
        });

        viewModel.getTodayHabitRecords().observe(getViewLifecycleOwner(), records -> {
            completedHabitIds.clear();
            if (records != null) {
                for (HabitRecordEntity r : records) {
                    if (r.isCompleted()) {
                        completedHabitIds.add(r.getHabitId());
                    }
                }
            }
            updateHabitsUI();
        });

        viewModel.getTodayMood().observe(getViewLifecycleOwner(), this::updateMoodUI);

        viewModel.getStatusMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateMoodUI(MoodRecordEntity moodRecord) {
        resetMoodSelection();
        if (moodRecord != null) {
            String name = moodRecord.getMoodName();
            String emoji = moodRecord.getMoodEmoji();
            binding.tvMoodSelectedStatus.setText("Humor hoje: " + name + " " + emoji);

            if ("Excelente".equals(name)) {
                binding.btnMoodExcellent.setBackgroundResource(R.drawable.bg_mood_selected);
            } else if ("Bem".equals(name)) {
                binding.btnMoodGood.setBackgroundResource(R.drawable.bg_mood_selected);
            } else if ("Neutro".equals(name)) {
                binding.btnMoodNeutral.setBackgroundResource(R.drawable.bg_mood_selected);
            } else if ("Cansado".equals(name)) {
                binding.btnMoodTired.setBackgroundResource(R.drawable.bg_mood_selected);
            } else if ("Chateado".equals(name)) {
                binding.btnMoodSad.setBackgroundResource(R.drawable.bg_mood_selected);
            }
        } else {
            binding.tvMoodSelectedStatus.setText(getString(R.string.mood_select_prompt));
        }
    }

    private void resetMoodSelection() {
        binding.btnMoodExcellent.setBackgroundResource(R.drawable.bg_mood_unselected);
        binding.btnMoodGood.setBackgroundResource(R.drawable.bg_mood_unselected);
        binding.btnMoodNeutral.setBackgroundResource(R.drawable.bg_mood_unselected);
        binding.btnMoodTired.setBackgroundResource(R.drawable.bg_mood_unselected);
        binding.btnMoodSad.setBackgroundResource(R.drawable.bg_mood_unselected);
    }

    private void updateHabitsUI() {
        if (currentHabits.isEmpty()) {
            binding.layoutEmptyTodayHabits.setVisibility(View.VISIBLE);
            binding.rvTodayHabits.setVisibility(View.GONE);
            binding.progressIndicator.setProgress(0);
            binding.tvProgressText.setText(getString(R.string.progress_empty));
        } else {
            binding.layoutEmptyTodayHabits.setVisibility(View.GONE);
            binding.rvTodayHabits.setVisibility(View.VISIBLE);
            adapter.setHabits(currentHabits, completedHabitIds);

            int total = currentHabits.size();
            int completedCount = 0;
            for (HabitEntity h : currentHabits) {
                if (completedHabitIds.contains(h.getId())) {
                    completedCount++;
                }
            }

            int percent = (int) (((float) completedCount / total) * 100);
            binding.progressIndicator.setProgress(percent);
            binding.tvProgressText.setText(completedCount + " de " + total + " hábitos concluídos hoje (" + percent + "%)");
        }
    }

    private void showAddHabitDialog(HabitEntity habitToEdit) {
        CreateHabitBottomSheetDialog dialog = CreateHabitBottomSheetDialog.newInstance(habitToEdit);
        dialog.setOnHabitSaveListener((title, category, time, editEntity) -> {
            if (editEntity != null) {
                editEntity.setTitle(title);
                editEntity.setCategory(category);
                editEntity.setTime(time);
                viewModel.updateHabit(editEntity);
            } else {
                viewModel.addHabit(title, category, time);
            }
        });
        dialog.show(getChildFragmentManager(), "AddHabitToday");
    }

    @Override
    public void onToggleCheck(HabitEntity habit, int position) {
        viewModel.toggleHabitRecord(habit.getId());
    }

    @Override
    public void onEditHabit(HabitEntity habit, int position) {
        showAddHabitDialog(habit);
    }

    @Override
    public void onDeleteHabit(HabitEntity habit, int position) {
        viewModel.deleteHabit(habit);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}