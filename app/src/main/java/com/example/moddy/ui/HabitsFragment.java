package com.example.moddy.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.moddy.R;
import com.example.moddy.adapter.HabitsAdapter;
import com.example.moddy.databinding.FragmentHabitsBinding;
import com.example.moddy.db.entity.HabitEntity;
import com.example.moddy.db.entity.HabitRecordEntity;
import com.example.moddy.viewmodel.ModdyViewModel;

import java.util.ArrayList;
import java.util.List;

public class HabitsFragment extends Fragment implements HabitsAdapter.HabitActionListener {

    private FragmentHabitsBinding binding;
    private ModdyViewModel viewModel;
    private HabitsAdapter adapter;

    private List<HabitEntity> allHabitsList = new ArrayList<>();
    private List<HabitEntity> activeHabitsList = new ArrayList<>();
    private List<HabitEntity> archivedHabitsList = new ArrayList<>();
    private final List<Long> todayCompletedIds = new ArrayList<>();

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

        viewModel = new ViewModelProvider(requireActivity()).get(ModdyViewModel.class);

        setupRecyclerView();
        setupFilters();
        setupButtons();
        observeViewModel();
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
        View.OnClickListener openAddDialog = v -> showAddHabitDialog(null);
        binding.btnCreateHabit.setOnClickListener(openAddDialog);
        binding.fabAddHabitMain.setOnClickListener(openAddDialog);
    }

    private void observeViewModel() {
        viewModel.getAllHabits().observe(getViewLifecycleOwner(), habits -> {
            allHabitsList = habits != null ? habits : new ArrayList<>();
            updateHabitCategoryLists();
            updateUI();
        });

        viewModel.getTodayHabitRecords().observe(getViewLifecycleOwner(), records -> {
            todayCompletedIds.clear();
            if (records != null) {
                for (HabitRecordEntity r : records) {
                    if (r.isCompleted()) {
                        todayCompletedIds.add(r.getHabitId());
                    }
                }
            }
            updateUI();
        });
    }

    private void updateHabitCategoryLists() {
        activeHabitsList.clear();
        archivedHabitsList.clear();
        for (HabitEntity h : allHabitsList) {
            if (h.isArchived()) {
                archivedHabitsList.add(h);
            } else {
                activeHabitsList.add(h);
            }
        }
    }

    private void updateUI() {
        List<HabitEntity> filtered = getFilteredList();

        if (filtered.isEmpty()) {
            binding.layoutEmptyHabits.setVisibility(View.VISIBLE);
            binding.rvAllHabits.setVisibility(View.GONE);
        } else {
            binding.layoutEmptyHabits.setVisibility(View.GONE);
            binding.rvAllHabits.setVisibility(View.VISIBLE);
            adapter.setHabits(filtered, todayCompletedIds);
        }
    }

    private List<HabitEntity> getFilteredList() {
        int checkedChipId = binding.chipGroupFilters.getCheckedChipId();
        if (checkedChipId == R.id.chip_active) {
            return activeHabitsList;
        } else if (checkedChipId == R.id.chip_archived) {
            return archivedHabitsList;
        } else {
            return allHabitsList;
        }
    }

    private void showAddHabitDialog(HabitEntity editEntity) {
        CreateHabitBottomSheetDialog dialog = CreateHabitBottomSheetDialog.newInstance(editEntity);
        dialog.setOnHabitSaveListener((title, category, time, entity) -> {
            if (entity != null) {
                entity.setTitle(title);
                entity.setCategory(category);
                entity.setTime(time);
                viewModel.updateHabit(entity);
            } else {
                viewModel.addHabit(title, category, time);
            }
        });
        dialog.show(getChildFragmentManager(), "AddHabitMain");
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