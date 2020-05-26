package com.project.reminder;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.Dialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.content.Intent;
import android.widget.Toast;

import com.project.reminder.R;

public class EditActivity extends AppCompatActivity {

    private final String TAG = "EditActivity";

    private EditText name;
    private EditText description;
    private Spinner categorySpinner;
    private CheckBox enabled;
    private CheckBox done;
    private Button dateButton;
    private Button timeButton;
    private ConstraintLayout ringSelection;
    private TextView ringtoneText;
    private Button button_ringtone;

    private Task task;

    private GregorianCalendar calendar;
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private Ringtone ringTone;

    static final int DATE_DIALOG_ID = 0;
    static final int TIME_DIALOG_ID = 1;
    static final int RQS_RINGTONEPICKER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        Log.d(TAG, "getting view values");

        name = (EditText)findViewById(R.id.name);
        description = (EditText)findViewById(R.id.description);
        categorySpinner = (Spinner) findViewById(R.id.category);
        enabled = (CheckBox)findViewById(R.id.enabled);
        done = (CheckBox)findViewById(R.id.checkbox_done);
        dateButton = (Button)findViewById(R.id.date_button);
        timeButton = (Button)findViewById(R.id.time_button);
        ringSelection = (ConstraintLayout)findViewById(R.id.ringSelection);
        ringtoneText = (TextView) findViewById(R.id.ringtoneText);
        button_ringtone = (Button)findViewById(R.id.button_ringtone);

        Log.d(TAG, "got view values");

        task = new Task();
        task.fromIntent(getIntent());

        setTitle(getIntent().getStringExtra("page-title"));

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.categories_edit_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        categorySpinner.setAdapter(adapter);

        name.setText(task.getName());
        name.addTextChangedListener(nameChangedListener);

        description.setText(task.getDescription());
        description.addTextChangedListener(descriptionChangedListener);

        categorySpinner.setSelection(Arrays.asList(getResources().getStringArray(R.array.categories_array)).indexOf(task.getCategory()));
        categorySpinner.setOnItemSelectedListener(categoryClickListener);

        enabled.setChecked(task.getEnabled());
        enabled.setOnCheckedChangeListener(enabledChangeListener);

        done.setChecked(task.getDone());
        done.setOnCheckedChangeListener(doneChangeListener);

        Ringtone ringtoneCurrent = RingtoneManager.getRingtone(this, Uri.parse(task.getRingtoneUri()));
        String ringtoneTitle = ringtoneCurrent.getTitle(this);
        ringtoneText.setText(ringtoneTitle);
        if(task.getEnabled()){
            ringSelection.setVisibility(View.VISIBLE);
        }

        button_ringtone.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                startActivityForResult(intent, RQS_RINGTONEPICKER);
            }
        });

        calendar = new GregorianCalendar();
        calendar.setTimeInMillis(task.getDate());
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);

        updateButtons();
    }

    @Override
    protected Dialog onCreateDialog(int id)
    {
        if (DATE_DIALOG_ID == id)
            return new DatePickerDialog(this, dateSetListener, year, month, day);
        else if (TIME_DIALOG_ID == id)
            return new TimePickerDialog(this, timeSetListener, hour, minute, true);
        else
            return null;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog)
    {
        if (DATE_DIALOG_ID == id)
            ((DatePickerDialog)dialog).updateDate(year, month, day);
        else if (TIME_DIALOG_ID == id)
            ((TimePickerDialog)dialog).updateTime(hour, minute);
    }

    public void onDateClick(View view){
        showDialog(DATE_DIALOG_ID);
    }

    public void onTimeClick(View view)
    {
        showDialog(TIME_DIALOG_ID);
    }

    public void onDoneClick(View view)
    {
        String errorMessage = null;

        if (task.getName().isEmpty()) {
            errorMessage = "Lütfen Anımsatıcı Başlığını Girin";
        }
        else if(task.getOutdated()){
            errorMessage = "Lütfen Anımsatıcı için İleriki Bir Zaman Seçin";
        }

        if (errorMessage!=null){
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_LONG);
            TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
            v.setTextColor(Color.RED);
            toast.show();
            return;
        }

        Toast toast = Toast.makeText(this, getString(R.string.save_message), Toast.LENGTH_LONG);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        v.setTextColor(Color.GREEN);
        toast.show();

        Intent intent = new Intent();
        task.toIntent(intent);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onCancelClick(View view)
    {
        setResult(RESULT_CANCELED, null);
        finish();
    }

    private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener()
    {
        public void onDateSet(DatePicker view, int year_, int month_, int day_)
        {
            year = year_;
            month = month_;
            day = day_;

            calendar = new GregorianCalendar(year, month, day, hour, minute);
            task.setDate(calendar.getTimeInMillis());

            updateButtons();
        }
    };

    private TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener()
    {
        public void onTimeSet(TimePicker view, int hour_, int minute_)
        {
            hour = hour_;
            minute = minute_;

            calendar = new GregorianCalendar(year, month, day, hour, minute);
            task.setDate(calendar.getTimeInMillis());

            updateButtons();
        }
    };

    private TextWatcher nameChangedListener = new TextWatcher()
    {
        public void afterTextChanged(Editable s)
        {
            task.setName(name.getText().toString());
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count)
        {
        }
    };

    private TextWatcher descriptionChangedListener = new TextWatcher()
    {
        public void afterTextChanged(Editable s)
        {
            task.setDescription(description.getText().toString());
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count)
        {
        }
    };

    private Spinner.OnItemSelectedListener categoryClickListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            // your code here
            task.setCategory(categorySpinner.getSelectedItem().toString());
        }

        @Override
        public void onNothingSelected(AdapterView<?> parentView) {
            // your code here
        }

    };

    private CompoundButton.OnCheckedChangeListener enabledChangeListener = new CompoundButton.OnCheckedChangeListener()
    {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
        {
            task.setEnabled(isChecked);
            if(isChecked){
                ringSelection.setVisibility(View.VISIBLE);
            } else {
                ringSelection.setVisibility(View.INVISIBLE);
            }

        }
    };

    private CompoundButton.OnCheckedChangeListener doneChangeListener = new CompoundButton.OnCheckedChangeListener()
    {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
        {
            task.setDone(isChecked);
        }
    };

    private void updateButtons()
    {
        dateButton.setText(DAO.formatDate(task));
        Log.d(TAG, "updated date button");
        timeButton.setText(DAO.formatTime(task));
        Log.d(TAG, "updated time button");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == RQS_RINGTONEPICKER && resultCode == RESULT_OK){
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            ringTone = RingtoneManager.getRingtone(getApplicationContext(), uri);
            Toast.makeText(this,
                    ringTone.getTitle(this),
                    Toast.LENGTH_LONG).show();
            task.setRingtoneUri(uri.toString());
            Ringtone ringtoneCurrent = RingtoneManager.getRingtone(this, uri);
            String ringtoneTitle = ringtoneCurrent.getTitle(this);
            ringtoneText.setText(ringtoneTitle);
        }
    }

}
