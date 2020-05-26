package com.project.reminder;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.project.reminder.R;

public class StatisticsFragment extends Fragment {

    TextView taskCountTextView;
    TextView doneCountTextView;
    TextView undoneCountTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        if (getArguments() != null) {
            taskCountTextView = view.findViewById(R.id.textView_taskCount);
            doneCountTextView = view.findViewById(R.id.textView_doneCount);
            undoneCountTextView = view.findViewById(R.id.textView_undoneCount);

            //task count, completed and uncompleted task counts
            int taskCount = getArguments().getInt("taskCount");
            int doneCount = getArguments().getInt("doneCount");
            int undoneCount = getArguments().getInt("undoneCount");

            Log.e("taskcount", String.valueOf(taskCount));
            Log.e("donecount", String.valueOf(doneCount));
            Log.e("undonecount", String.valueOf(undoneCount));

            taskCountTextView.setText(String.valueOf(taskCount));
            doneCountTextView.setText(String.valueOf(doneCount));
            undoneCountTextView.setText(String.valueOf(undoneCount));
        }

        return view;
    }
}
