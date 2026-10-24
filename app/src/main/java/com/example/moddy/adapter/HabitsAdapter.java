package com.example.moddy.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moddy.R;
import com.example.moddy.databinding.ItemHabitBinding;
import com.example.moddy.model.Habit;

import java.util.ArrayList;
import java.util.List;

public class HabitsAdapter extends RecyclerView.Adapter<HabitsAdapter.HabitViewHolder> {

    public interface HabitActionListener {
        void onToggleCheck(Habit habit, int position);
        void onEditHabit(Habit habit, int position);
        void onDeleteHabit(Habit habit, int position);
    }

    private final List<Habit> habitsList = new ArrayList<>();
    private final HabitActionListener listener;

    public HabitsAdapter(HabitActionListener listener) {
        this.listener = listener;
    }

    public void setHabits(List<Habit> newHabits) {
        habitsList.clear();
        if (newHabits != null) {
            habitsList.addAll(newHabits);
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

        public void bind(Habit habit) {
            Context context = binding.getRoot().getContext();
            binding.tvHabitName.setText(habit.getTitle());
            binding.tvHabitCategory.setText(habit.getCategory());
            binding.tvHabitTime.setText(habit.getTime());

            if (habit.isCompleted()) {
                binding.btnToggleCheck.setBackgroundResource(R.drawable.bg_mood_selected);
                binding.btnToggleCheck.setImageTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.brown_primary)));
                binding.tvHabitName.setPaintFlags(
                        binding.tvHabitName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                binding.btnToggleCheck.setBackgroundResource(R.drawable.bg_mood_unselected);
                binding.btnToggleCheck.setImageTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.brown_secondary)));
                binding.tvHabitName.setPaintFlags(
                        binding.tvHabitName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
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