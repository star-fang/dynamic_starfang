package com.starfang.ui.dynamic.unitsim;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.starfang.R;
import com.starfang.realm.simulator.UnitSim;
import com.starfang.realm.source.caocao.UnitTypes;
import com.starfang.realm.source.caocao.Units;

import java.text.MessageFormat;

import javax.annotation.Nonnull;

public class GradeControlFragment extends UnitSimPlaceHolderFragment {

    private static final String TAG = "FANG_GRADE_CTRL";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_fragment_control_grade, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            int id = args.getInt(UnitSim.FIELD_ID);
            UnitSim unitSim = realm.where(UnitSim.class).equalTo(UnitSim.FIELD_ID, id).findFirst();
            if (unitSim != null) {
                Units unit = unitSim.getUnit();
                if (unit != null) {
                    UnitTypes type = unit.getType();
                    if (type != null) {
                        final GradeViewModel liveData = new ViewModelProvider(this).get(GradeViewModel.class);
                        final LifecycleOwner owner = getViewLifecycleOwner();
                        liveData.setMaxPlusStat(unitSim.getMaxPlusStat());

                        final AppCompatTextView text_stat_sum = view.findViewById(R.id.text_stat_sum);

                        final RecyclerView recycler_control_stat = view.findViewById(R.id.recycler_control_stat);
                        final StatRecyclerAdapter statRecyclerAdapter = new StatRecyclerAdapter(unitSim, liveData, owner, text_stat_sum);
                        recycler_control_stat.setLayoutManager(new LinearLayoutManager(mContext));
                        recycler_control_stat.setAdapter(statRecyclerAdapter);

                        final RecyclerView recycler_control_grade = view.findViewById(R.id.recycler_control_grade);
                        final GradeRecyclerAdapter gradeRecyclerAdapter = new GradeRecyclerAdapter(unitSim, liveData, owner);
                        recycler_control_grade.setLayoutManager(new LinearLayoutManager(mContext));
                        recycler_control_grade.setAdapter(gradeRecyclerAdapter);

                    } // if type != null
                } // if unit != null
            } // if unitSim != null
        }
    }

    private class GradeRecyclerAdapter extends RecyclerView.Adapter<GradeRecyclerAdapter.GradeViewHolder> {

        private GradeViewModel liveData;
        private LifecycleOwner owner;
        private UnitSim unitSim;

        private GradeRecyclerAdapter(UnitSim unitSim, GradeViewModel liveData, LifecycleOwner owner) {
            this.unitSim = unitSim;
            this.liveData = liveData;
            this.owner = owner;
        }

        @NonNull
        @Override
        public GradeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new GradeViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_control_value, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull GradeViewHolder holder, int position) {
            holder.bind(position);
        }

        @Override
        public int getItemCount() {
            return GradeViewModel.GRADE_NAMES.length;
        }

        private class GradeViewHolder extends RecyclerView.ViewHolder {

            private AppCompatTextView text_title;
            private AppCompatSeekBar seek_bar_value;
            private AppCompatTextView text_value;
            private AppCompatImageButton button_minus;
            private AppCompatImageButton button_plus;

            public GradeViewHolder(@NonNull View itemView) {
                super(itemView);
                text_title = itemView.findViewById(R.id.text_stat_title);
                seek_bar_value = itemView.findViewById(R.id.seek_bar_stat);
                text_value = itemView.findViewById(R.id.text_stat_val);
                button_minus = itemView.findViewById(R.id.button_stat_minus);
                button_plus = itemView.findViewById(R.id.button_stat_plus);
            }

            public void bind(int position) {
                Log.d(TAG, position + "(st|nd|rd|th) grade factor bound");
                text_title.setText(GradeViewModel.GRADE_NAMES[position]);
                MutableLiveData<Integer> dataValue = liveData.getGradeFactorByIndex(position);
                dataValue.observe(owner, value -> {
                    text_value.setText(String.valueOf(value));
                    seek_bar_value.setProgress(value - 1);
                    if (position == GradeViewModel.GradeFactorsPosition.GRADE) {
                        Log.d(TAG, "grade changed: " + value);
                        liveData.setMaxPlusStat(unitSim.calcMaxPlusStat(value));
                    }
                });

                int initMax;
                int initValue;
                switch (position) {
                    case GradeViewModel.GradeFactorsPosition.LEVEL:
                        initMax = UnitSim.MAX_LEVEL_BY_GRADE[UnitSim.MAX_LEVEL_BY_GRADE.length - 1] - 1;
                        initValue = unitSim.getLevel();
                        break;
                    case GradeViewModel.GradeFactorsPosition.REINFORCEMENT:
                        initMax = UnitSim.MIN_LEVEL_BY_REINFORCE.length - 1;
                        initValue = unitSim.getReinforcement();
                        break;
                    case GradeViewModel.GradeFactorsPosition.GRADE:
                        initMax = UnitSim.MAX_LEVEL_BY_GRADE.length - 1;
                        initValue = unitSim.getGrade().getGrade();
                        break;
                    default:
                        initMax = 0;
                        initValue = 0;
                }

                seek_bar_value.setMax(initMax);
                seek_bar_value.setOnSeekBarChangeListener(new GradeFactorChangeListener(position, liveData));
                dataValue.setValue(initValue);

                button_minus.setOnClickListener(v -> {
                    Integer value = dataValue.getValue();
                    if (value != null) {
                        dataValue.setValue(value - 1);
                    }
                });

                button_plus.setOnClickListener(v -> {
                    Integer value = dataValue.getValue();
                    if (value != null) {
                        dataValue.setValue(value + 1);
                    }
                });

            }
        }
    }

    private static class StatRecyclerAdapter extends RecyclerView.Adapter<StatRecyclerAdapter.StatViewHolder> {

        private GradeViewModel liveData;
        private LifecycleOwner owner;
        private UnitSim unitSim;
        private AppCompatTextView text_stat_sum;

        private StatRecyclerAdapter(UnitSim unitSim, GradeViewModel liveData, LifecycleOwner owner, AppCompatTextView text_stat_sum) {
            this.liveData = liveData;
            this.owner = owner;
            this.unitSim = unitSim;
            this.text_stat_sum = text_stat_sum;
        }

        @NonNull
        @Override
        public StatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new StatViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.row_control_value, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull StatViewHolder holder, int position) {
            holder.bind(
                    UnitSim.STAT_CODE.values()[position]
                    , GradeViewModel.STAT_NAMES[position]
                    , liveData.getPlusStatByIndex(position));
        }

        @Override
        public int getItemCount() {
            return GradeViewModel.STAT_NAMES.length;
        }

        private class StatViewHolder extends RecyclerView.ViewHolder {

            private static final String VALUE_PATTERN = "{0} / {1}";

            private AppCompatTextView text_title;
            private AppCompatSeekBar seek_bar_value;
            private AppCompatTextView text_value;
            private AppCompatImageButton button_minus;
            private AppCompatImageButton button_plus;

            private StatViewHolder(@NonNull View itemView) {
                super(itemView);
                text_title = itemView.findViewById(R.id.text_stat_title);
                seek_bar_value = itemView.findViewById(R.id.seek_bar_stat);
                text_value = itemView.findViewById(R.id.text_stat_val);
                button_minus = itemView.findViewById(R.id.button_stat_minus);
                button_plus = itemView.findViewById(R.id.button_stat_plus);

                seek_bar_value.setMax(unitSim.getMaxPlusStat());
            }

            private void showPlusStatSum() {
                Integer gradeValue = liveData.getGradeValue();
                Integer plusStatSum = liveData.getPlusStatSum();
                if (gradeValue != null && plusStatSum != null) {
                    text_stat_sum.setText(MessageFormat.format(VALUE_PATTERN, plusStatSum, gradeValue * 100));
                }
            }

            private void bind(
                    @Nonnull UnitSim.STAT_CODE code
                    , final int titleResId
                    , @Nonnull MutableLiveData<Integer> stat) {

                Log.d(TAG, code.name() + " stat bound");
                text_title.setText(titleResId);
                stat.observe(owner, value -> {
                    Integer maxPlusStatValue = liveData.getMaxPlusStatValue();
                    if (maxPlusStatValue != null) {
                        text_value.setText(MessageFormat.format(VALUE_PATTERN, value, maxPlusStatValue));
                        showPlusStatSum();
                    }
                });

                liveData.setMaxPlusStatObserver(owner, value -> {
                    Integer statInt = stat.getValue();
                    if (statInt != null) {
                        text_value.setText(MessageFormat.format(VALUE_PATTERN, statInt, value));
                        showPlusStatSum();
                    }
                    seek_bar_value.setMax(value);
                });

                stat.setValue(unitSim.getPlusStat(code));

                seek_bar_value.setOnSeekBarChangeListener(new StatChangeListener(code, liveData));
                button_minus.setOnClickListener(v -> {
                    Integer value = liveData.getPlusStatValue(code);
                    if (value != null) {
                        liveData.setPlusStat(code, --value);
                    }
                });

                button_plus.setOnClickListener(v -> {
                    Integer value = liveData.getPlusStatValue(code);
                    if (value != null) {
                        liveData.setPlusStat(code, ++value);
                    }
                });
            }
        }
    }

    private class GradeFactorChangeListener implements SeekBar.OnSeekBarChangeListener {
        private int factorPosition; // 0 : level, 1 : reinforcement, 2: grade
        private int originalProgress;
        private GradeViewModel liveData;

        private GradeFactorChangeListener(int position, GradeViewModel liveData) {
            this.factorPosition = position;
            this.liveData = liveData;
        }


        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                boolean change;
                switch (factorPosition) {
                    case GradeViewModel.GradeFactorsPosition.LEVEL:
                        int level = progress + 1; // progress : 0 ~ 98
                        change = updateLevel(level, liveData);
                        break;
                    case GradeViewModel.GradeFactorsPosition.REINFORCEMENT:
                        int reinforcement = progress + 1;
                        change = updateReinforcement(reinforcement, liveData);
                        break;
                    case GradeViewModel.GradeFactorsPosition.GRADE:
                        int grade = progress + 1;
                        change = updateGrade(grade, liveData);
                        break;
                    default:
                        change = false;
                }
                if (!change) {
                    seekBar.setProgress(originalProgress);
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            originalProgress = seekBar.getProgress();
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    private static class StatChangeListener implements SeekBar.OnSeekBarChangeListener {
        private UnitSim.STAT_CODE statCode;
        private int originalProgress;
        private GradeViewModel liveData;

        private StatChangeListener(UnitSim.STAT_CODE code, GradeViewModel liveData) {
            this.statCode = code;
            this.liveData = liveData;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            boolean update = liveData.setPlusStat(statCode, progress);
            if (!update) {
                seekBar.setProgress(originalProgress);
            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            originalProgress = seekBar.getProgress();
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }


    public boolean updateReinforcement(int reinforceValue, GradeViewModel liveData) {
        try {
            if (reinforceValue > 0 && reinforceValue <= UnitSim.MAX_LEVEL_BY_REINFORCE.length) {
                liveData.setReinforcement(reinforceValue);
                boolean updateLevel = updateLevelByReinforcement(liveData);
                if (updateLevel) {
                    updateGradeByLevel(liveData);
                }
                return true;
            }
        } catch( ArrayIndexOutOfBoundsException e ) {
            Log.d(TAG, Log.getStackTraceString(e));
        }
        return false;
    }

    private boolean updateLevelByReinforcement(GradeViewModel liveData) throws ArrayIndexOutOfBoundsException {
        Integer levelValue = liveData.getLevelValue();
        Integer reinforceValue = liveData.getReinforcementValue();
        if (levelValue != null && reinforceValue != null) {
            if (reinforceValue < UnitSim.MAX_LEVEL_BY_REINFORCE.length && levelValue > UnitSim.MAX_LEVEL_BY_REINFORCE[reinforceValue - 1]) {
                liveData.setLevel(UnitSim.MAX_LEVEL_BY_REINFORCE[reinforceValue - 1]);
                return true;
            } else if (reinforceValue < UnitSim.MIN_LEVEL_BY_REINFORCE.length && levelValue < UnitSim.MIN_LEVEL_BY_REINFORCE[reinforceValue - 1]) {
                liveData.setLevel(UnitSim.MIN_LEVEL_BY_REINFORCE[reinforceValue - 1]);
                return true;
            }
        }
        return false;
    }

    private boolean updateGrade(int gradeValue, GradeViewModel liveData) {
        try {
            if (gradeValue > 0 && gradeValue <= UnitSim.MAX_LEVEL_BY_GRADE.length) {
                liveData.setGrade(gradeValue);
                boolean updateLevel = updateLevelByGrade(liveData);
                if (updateLevel) {
                    updateReinforcementByLevel(liveData);
                }

                return true;
            }
        } catch( ArrayIndexOutOfBoundsException e ) {
            Log.d(TAG, Log.getStackTraceString(e));
        }
        return false;
    }

    private boolean updateLevelByGrade(GradeViewModel liveData) throws ArrayIndexOutOfBoundsException {
        Integer levelValue = liveData.getLevelValue();
        Integer gradeValue = liveData.getGradeValue();
        if (levelValue != null && gradeValue != null) {
            if (gradeValue < UnitSim.MAX_LEVEL_BY_GRADE.length && levelValue > UnitSim.MAX_LEVEL_BY_GRADE[gradeValue - 1]) {
                liveData.setLevel(UnitSim.MAX_LEVEL_BY_GRADE[gradeValue - 1]);
                return true;
            } else if (gradeValue > 1 && levelValue < UnitSim.MAX_LEVEL_BY_GRADE[gradeValue - 2]) {
                liveData.setLevel(UnitSim.MAX_LEVEL_BY_GRADE[gradeValue - 2]);
                return true;
            }
        }
        return false;
    }


    private boolean updateLevel(int levelValue, GradeViewModel liveData) {

        try {
            int maxLevel = UnitSim.MAX_LEVEL_BY_GRADE[UnitSim.MAX_LEVEL_BY_GRADE.length - 1];
            if (levelValue > 0 && levelValue <= maxLevel) {
                liveData.setLevel(levelValue);
                updateGradeByLevel(liveData);
                updateReinforcementByLevel(liveData);
                return true;
            }
        } catch( ArrayIndexOutOfBoundsException e ) {
            Log.d(TAG, Log.getStackTraceString(e));
        }
        return false;
    }

    private void updateGradeByLevel(GradeViewModel liveData) throws ArrayIndexOutOfBoundsException {
        Integer gradeValue = liveData.getGradeValue(); // 1 ~ 5
        Integer levelValue = liveData.getLevelValue();
        if (levelValue != null && gradeValue != null) {
            if (gradeValue < UnitSim.MAX_LEVEL_BY_GRADE.length && levelValue > UnitSim.MAX_LEVEL_BY_GRADE[gradeValue - 1]) {
                int gradeUp = 1;
                for (int i = gradeValue; i < UnitSim.MAX_LEVEL_BY_GRADE.length - 1; i++) { // 1, 1 < 4
                    if (levelValue > UnitSim.MAX_LEVEL_BY_GRADE[i]) { // 21 > 40
                        gradeUp++; // 2 // 3
                    }
                }
                liveData.setGrade(gradeValue + gradeUp);
                //return true;
            } else if (gradeValue > 1 && levelValue < UnitSim.MAX_LEVEL_BY_GRADE[gradeValue - 2]) {
                int gradeDown = 1;
                for (int i = gradeValue - 3; i >= 0; i--) {
                    if (levelValue < UnitSim.MAX_LEVEL_BY_GRADE[i]) {
                        gradeDown++;
                    }
                }
                liveData.setGrade(gradeValue - gradeDown);
                //return true;
            }
        }
        //return false;
    }

    private void updateReinforcementByLevel(GradeViewModel liveData) throws ArrayIndexOutOfBoundsException{
        // 1 ~ 12
        Integer levelValue = liveData.getLevelValue();
        Integer reinforceValue = liveData.getReinforcementValue();
        if (levelValue != null && reinforceValue != null) {
            if (reinforceValue < UnitSim.MAX_LEVEL_BY_REINFORCE.length && levelValue > UnitSim.MAX_LEVEL_BY_REINFORCE[reinforceValue - 1]) {
                int reinforceUp = 1;
                for (int i = reinforceValue; i < UnitSim.MAX_LEVEL_BY_REINFORCE.length - 1; i++) {
                    if (levelValue > UnitSim.MAX_LEVEL_BY_REINFORCE[i]) {
                        reinforceUp++;
                    }
                }
                liveData.setReinforcement(reinforceValue + reinforceUp);
                //return true;
            } else if (reinforceValue > 1 && levelValue < UnitSim.MIN_LEVEL_BY_REINFORCE[reinforceValue - 1]) {
                int reinforceDown = 1;
                for (int i = reinforceValue - 2; i > 0; i--) {
                    if (levelValue < UnitSim.MIN_LEVEL_BY_REINFORCE[i]) {
                        reinforceDown++;
                    }
                }
                liveData.setReinforcement(reinforceValue - reinforceDown);
                //return true;
            }
        }
        //return false;
    }

}
