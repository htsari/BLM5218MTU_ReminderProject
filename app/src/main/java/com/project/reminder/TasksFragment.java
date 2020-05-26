package com.project.reminder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.project.reminder.R;

public class TasksFragment extends Fragment {

    private ListView taskList;
    private Toolbar toolbar;
    private Spinner categorySpinner;
    private Spinner timeSpinner;
    private Menu categoryMenu;
    private int prevPosition = 0;

    @SuppressLint("RestrictedApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        //get category spinner
        categorySpinner = (Spinner) view.findViewById(R.id.category_spinner);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.categories_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        categorySpinner.setAdapter(adapter);
        categorySpinner.setSelection(0);
        //create listener
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                if(position == 0){
                    //all categories
                    ((DashBoardActivity)getActivity()).updateListByCategory("");
                } else {
                    //category selected
                    ((DashBoardActivity)getActivity()).updateListByCategory(categorySpinner.getSelectedItem().toString());
                }

            } // to close the onItemSelected
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });

        //get time spinner
        timeSpinner = (Spinner) view.findViewById(R.id.time_spinner);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterTime = ArrayAdapter.createFromResource(getContext(),
                R.array.time_list_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterTime.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        timeSpinner.setAdapter(adapterTime);
        timeSpinner.setSelection(0);
        //create listener
        timeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                //pass selected index
                ((DashBoardActivity)getActivity()).updateListByTime(position);

            } // to close the onItemSelected
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });

        //set list adapter
        taskList = (ListView)view.findViewById(R.id.task_list);
        TaskListAdapter taskListAdapter = listener.getTaskListAdapter();
        taskList.setAdapter(taskListAdapter);
        //taskList.setOnItemClickListener(listOnItemClickListener); // making it editable
        registerForContextMenu(taskList); // choose edit or delete

        return view;
    }

    interface OnFragmentInteractionListener {
        TaskListAdapter getTaskListAdapter();
        void onFragmentInteraction(int position);
    }

    OnFragmentInteractionListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //Log.d("!!!!!", context.getClass().getName());
        try {
            listener = (OnFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " should implement interface OnFragmentInteractionListener");
        }
    }

    private AdapterView.OnItemClickListener listOnItemClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            listener.onFragmentInteraction(position);
        }
    };
}
