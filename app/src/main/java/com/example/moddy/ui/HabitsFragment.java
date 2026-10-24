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
import com.example.moddy.databinding.FragmentHabitsBinding;
import com.example.moddy.model.Habit;

import java.util.ArrayList;
import java.util.List;

public class HabitsFragment extends Fragment implements HabitsAdapter.HabitActionListener {

    private FragmentHabitsBinding binding;
    private HabitsAdapter adapter;
    private final List<Habit> allHabits = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHabitsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupFilters();
        setupButtons();
        updateUI();
    }

    private void setupRecyclerView() {
        adapter = new HabitsAdapter(this);
        binding.rvAllHabits.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAllHabits.setAdapter(adapter);
    }

    private void setupFilters() {
        binding.chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> updateUI());
    }

    private void setupButtons() {
        View.OnClickListener openAddDialog = v -> showAddHabitDialog();
        binding.btnCreateHabit.setOnClickListener(openAddDialog);
        binding.fabAddHabitMain.setOnClickListener(openAddDialog);
    }

    private void showAddHabitDialog() {
        CreateHabitBottomSheetDialog dialog = CreateHabitBottomSheetDialog.newInstance(null);
        dialog.setOnHabitSaveListener(newHabit -> {
            allHabits.add(newHabit);
            updateUI();
        });
        dialog.show(getChildFragmentManager(), "AddHabitMain");
    }

    private void updateUI() {
        List<Habit> filteredList = getFilteredHabits();

        if (filteredList.isEmpty()) {
            binding.layoutEmptyHabits.setVisibility(View.VISIBLE);
            binding.rvAllHabits.setVisibility(View.GONE);
        } else {
            binding.layoutEmptyHabits.setVisibility(View.GONE);
            binding.rvAllHabits.setVisibility(View.VISIBLE);
            adapter.setHabits(filteredList);
        }
    }

    private List<Habit> getFilteredHabits() {
        int checkedChipId = binding.chipGroupFilters.getCheckedChipId();
        List<Habit> filtered = new ArrayList<>();

        if (checkedChipId == R.id.chip_active) {
            for (Habit h : allHabits) {
                if (!h.isArchived()) filtered.add(h);
            }
        } else if (checkedChipId == R.id.chip_archived) {
            for (Habit h : allHabits) {
                if (h.isArchived()) filtered.add(h);
            }
        } else {
            filtered.addAll(allHabits);
        }
        return filtered;
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
        dialog.show(getChildFragmentManager(), "EditHabitMain");
    }

    @Override
    public void onDeleteHabit(Habit habit, int position) {
        allHabits.remove(habit);
        updateUI();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}