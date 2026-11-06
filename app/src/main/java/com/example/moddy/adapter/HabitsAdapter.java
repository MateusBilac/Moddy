package com.example.moddy.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moddy.R;
import com.example.moddy.databinding.ItemHabitBinding;
import com.example.moddy.db.entity.HabitEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HabitsAdapter extends RecyclerView.Adapter<HabitsAdapter.HabitViewHolder> {

    public interface HabitActionListener {
        void onToggleCheck(HabitEntity habit, int position);
        void onEditHabit(HabitEntity habit, int position);
        void onDeleteHabit(HabitEntity habit, int position);
    }

    private final List<HabitEntity> habitsList = new ArrayList<>();
    private final Set<Long> completedHabitIds = new HashSet<>();
    private final HabitActionListener listener;

    public HabitsAdapter(HabitActionListener listener) {
        this.listener = listener;
    }

    public void setHabits(List<HabitEntity> newHabits, List<Long> completedIds) {
        habitsList.clear();
        if (newHabits != null) {
            habitsList.addAll(newHabits);
        }
        completedHabitIds.clear();
        if (completedIds != null) {
            completedHabitIds.addAll(completedIds);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHabitBinding binding = ItemHabitBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new HabitViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        holder.bind(habitsList.get(position));
    }

    @Override
    public int getItemCount() {
        return habitsList.size();
    }

    class HabitViewHolder extends RecyclerView.ViewHolder {
        private final ItemHabitBinding binding;

        public HabitViewHolder(@NonNull ItemHabitBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(HabitEntity habit) {
            Context context = binding.getRoot().getContext();
            binding.tvHabitName.setText(habit.getTitle());
            binding.tvHabitCategory.setText(habit.getCategory());
            binding.tvHabitTime.setText(habit.getTime());

            boolean isCompleted = completedHabitIds.contains(habit.getId());

            if (isCompleted) {
                binding.btnToggleCheck.setBackgroundResource(R.drawable.bg_mood_selected);
                binding.btnToggleCheck.setImageTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.brown_primary)));
                binding.tvHabitName.setPaintFlags(
                        binding.tvHabitName.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                binding.btnToggleCheck.setBackgroundResource(R.drawable.bg_mood_unselected);
                binding.btnToggleCheck.setImageTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.brown_secondary)));
                binding.tvHabitName.setPaintFlags(
                        binding.tvHabitName.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
            }

            binding.btnToggleCheck.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (listener != null && pos != RecyclerView.NO_POSITION) {
                    listener.onToggleCheck(habit, pos);
                }
            });

            binding.btnEditHabit.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (listener != null && pos != RecyclerView.NO_POSITION) {
                    listener.onEditHabit(habit, pos);
                }
            });

            binding.btnDeleteHabit.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (listener != null && pos != RecyclerView.NO_POSITION) {
                    listener.onDeleteHabit(habit, pos);
                }
            });
        }
    }
}