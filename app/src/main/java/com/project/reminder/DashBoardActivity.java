package com.project.reminder;

import android.app.PendingIntent;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentManager;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.navigation.NavigationView;
import com.project.reminder.R;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

class TaskListAdapter extends BaseAdapter{

    private final String TAG = "TaskListAdapter";

    //list view holder
    static class ViewHolder
    {
        TextView name;
        TextView time;
        TextView date;
        ImageView subMenu;
    }

    private Context context;
    private DAO dao; // access to data
    private LayoutInflater inflater;

    private AlarmManager alarmManager;

    public TaskListAdapter (Context context)
    {
        Log.d(TAG, "calling constructor, context is " + (context!=null ? "not null" : "null"));

        this.context = context;
        dao = DAO.getInstance(context);

        inflater = LayoutInflater.from(context);

        alarmManager = (AlarmManager)context.getSystemService(context.ALARM_SERVICE);
    }

    public void updateAdapter (String category, int timeID)
    {
        Log.d(TAG, "calling constructor, context is " + (context!=null ? "not null" : "null"));
        dao = DAO.getInstance(this.context, category, timeID);

        inflater = LayoutInflater.from(this.context);

        notifyDataSetChanged();
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;
        Task task = dao.get(position);

        if (convertView == null)
        {
            convertView = inflater.inflate(R.layout.record, null);

            holder = new ViewHolder();
            holder.name = (TextView)convertView.findViewById(R.id.record_name);
            holder.time = (TextView)convertView.findViewById(R.id.record_time);
            holder.date = (TextView)convertView.findViewById(R.id.record_date);
            holder.subMenu = (ImageView)convertView.findViewById(R.id.sub_menu);

            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)convertView.getTag();
        }

        holder.name.setText(task.getName());
        holder.time.setText(DAO.formatTime(task));
        holder.date.setText(DAO.formatDate(task));

        if (task.getOutdated() || task.getDone())
            holder.name.setTextColor(context.getResources().getColor(R.color.outdated));
        else
            holder.name.setTextColor(context.getResources().getColor(R.color.active));

        return convertView;
    }

    public int getCount()
    {
        return dao.size();
    }

    public Task getItem(int position)
    {
        return dao.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    public void add(Task Task)
    {
        dao.add(Task);
        update();
    }

    public void delete(int index)
    {
        cancelTask(dao.get(index));
        dao.remove(index);
        update();
    }

    public void update(Task Task)
    {
        dao.update(Task);
        update();
    }

    private void update()
    {
        for (int i = 0; i < dao.size(); i++)
            setTask(dao.get(i));

        notifyDataSetChanged();
    }

    private void setTask(Task task){
        PendingIntent sender;
        Intent intent;

        Log.d(TAG, "Setting task to alarm at " + task.getDate() + (task.getEnabled() ? " enabled" : "") + (task.getOutdated() ? " outdated" : ""));

        if (task.getEnabled() && !task.getOutdated() && !task.getDone() )
        {
            intent = new Intent(context, TaskReceiver.class);
            task.toIntent(intent);
            sender = PendingIntent.getBroadcast(context, (int)task.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, task.getDate(), sender);
        }
    }

    private void cancelTask(Task task){

    }
}


public class DashBoardActivity extends AppCompatActivity implements TasksFragment.OnFragmentInteractionListener{

    private final String TAG = "DashBoardActivity";

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;

    public TaskListAdapter taskListAdapter;
    private Task currentTask;
    private ListView taskList;

    private final int NEW_ACTIVITY = 0;
    private final int EDIT_ACTIVITY = 1;

    private final int CONTEXT_MENU_EDIT = 0;
    private final int CONTEXT_MENU_DELETE = 1;

    private static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

    private Spinner categorySpinner;

    private String category = "";
    private int timeID = 0;
    private boolean nightMode = false;

    private int taskCount = 0;
    private int doneCount = 0;
    private int undoneCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "starting dashboard activity");
        Log.e("loc", "onCreate");

        super.onCreate(savedInstanceState);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        //get night mode status from shared preferences
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        nightMode = mPrefs.getBoolean("NIGHT_MODE", true);

        //update night mode
        if (nightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        //set title
        setTitle(getString(R.string.reminder_list_name));

        taskListAdapter = new TaskListAdapter(this);
        currentTask = null;

        setContentView(R.layout.activity_dash_board);

        drawerLayout = (DrawerLayout)findViewById(R.id.activity_main);
        toggle = new ActionBarDrawerToggle(this, drawerLayout,R.string.open, R.string.close);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //menu
        navigationView = (NavigationView)findViewById(R.id.nv);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int id = item.getItemId();
                Bundle bundle = null;
                Class fragmentClass = TasksFragment.class;
                switch(id)
                {
                    case R.id.dashboard:
                        setTitle(getString(R.string.reminder_list_name));
                        Log.d(TAG, "Choosing tasks");
                        break;
                    case R.id.statistics:
                        setTitle(getString(R.string.statistics));
                        category = "";
                        timeID = 0;
                        taskListAdapter.updateAdapter(category, timeID);
                        fragmentClass = StatisticsFragment.class;

                        Fragment fragment = new StatisticsFragment();
                        FragmentManager fragmentManager = getSupportFragmentManager();

                        getStatisticsValues();

                        //put values to bundle
                        bundle = new Bundle();
                        bundle.putInt("taskCount", taskCount);
                        bundle.putInt("doneCount", doneCount);
                        bundle.putInt("undoneCount", undoneCount);

                        break;
                    case R.id.settings:
                        setTitle(getString(R.string.settings));
                        fragmentClass = SettingsFragment.class;
                        Log.d(TAG, "Choosing settings");
                        //Intent intent = new Intent(getBaseContext(), HelpActivity.class);
                        //DashBoardActivity.this.startActivity(intent);
                        break;
                    case R.id.exit:
                        finish();
                        moveTaskToBack(true);
                        break;
                }
                switchFragment(fragmentClass, bundle);

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        switchFragment(TasksFragment.class, null);

    }

    void switchFragment(Class fragmentClass, Bundle bundle){
        Fragment fragment = null;
        try {
            fragment = (Fragment) fragmentClass.newInstance();
            if(bundle != null) {
                fragment.setArguments(bundle);
            }
        }
        catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.tasksfragment, fragment).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(toggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

    public TaskListAdapter getTaskListAdapter(){
        return taskListAdapter;
    }

    public void setTaskListAdapter(TaskListAdapter taskListAdapter){
        this.taskListAdapter = taskListAdapter;
        //switchFragment(TasksFragment.class);
    }

    public void onFragmentInteraction(int position){
        Intent intent = new Intent(getBaseContext(), EditActivity.class);

        currentTask = taskListAdapter.getItem(position);
        currentTask.toIntent(intent);
        DashBoardActivity.this.startActivityForResult(intent, EDIT_ACTIVITY);
    }

    public void onSubMenuClick(final View view)
    {
        Log.e("loc","onSubMenuClick");

        PopupMenu popupMenu =  new PopupMenu(getBaseContext(), view);
        try {
            Field[] fields = popupMenu.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popupMenu);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        MenuInflater mInflater = popupMenu.getMenuInflater();
        mInflater.inflate(R.menu.task_menu, popupMenu.getMenu());
        popupMenu.show();

        //list item menu
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()){
                    case R.id.menu_edit:
                        onEditClick(view);
                        return true;
                    case R.id.menu_delete:
                        onDeleteClick(view);
                        return true;
                    case R.id.menu_done:
                        onDoneClick(view);
                        return true;
                    case R.id.menu_share:
                        onShareClick(view);
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    //create reminder
    public void onAddClick(View view)
    {
        Intent intent = new Intent(getBaseContext(), EditActivity.class);

        currentTask = new Task();
        currentTask.toIntent(intent);
        intent.putExtra("page-title", getString(R.string.reminder_create_name));

        Log.e("loc","onAddClick");

        DashBoardActivity.this.startActivityForResult(intent, NEW_ACTIVITY);
    }

    //edit reminder
    public void onEditClick(View view){

        View parentRow = (View) view.getParent();
        ListView listView = (ListView) parentRow.getParent().getParent();
        final int position = listView.getPositionForView(parentRow);

        Intent intent = new Intent(getBaseContext(), EditActivity.class);
        currentTask = taskListAdapter.getItem(position);
        currentTask.toIntent(intent);
        intent.putExtra("page-title", getString(R.string.reminder_edit_name));

        startActivityForResult(intent, EDIT_ACTIVITY);
    }

    //delete reminder
    public void onDeleteClick(View view){
        View parentRow = (View) view.getParent();
        ListView listView = (ListView) parentRow.getParent().getParent();
        final int position = listView.getPositionForView(parentRow);
        taskListAdapter.delete(position);
        Toast toast = Toast.makeText(this, getString(R.string.delete_message), Toast.LENGTH_LONG);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        v.setTextColor(Color.RED);
        toast.show();
    }

    //set reminder as done
    public void onDoneClick(View view){
        View parentRow = (View) view.getParent();
        ListView listView = (ListView) parentRow.getParent().getParent();
        final int position = listView.getPositionForView(parentRow);

        currentTask = taskListAdapter.getItem(position);
        currentTask.setDone(true);

        taskListAdapter.update(currentTask);

        Toast toast = Toast.makeText(this, getString(R.string.done_message), Toast.LENGTH_LONG);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        v.setTextColor(Color.GREEN);
        toast.show();
    }

    //share reminder
    public void onShareClick(View view){
        View parentRow = (View) view.getParent();
        ListView listView = (ListView) parentRow.getParent().getParent();
        final int position = listView.getPositionForView(parentRow);

        currentTask = taskListAdapter.getItem(position);

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, currentTask.getName() + " - " + dateFormat.format(new Date(currentTask.getDate())) + " " + timeFormat.format(new Date(currentTask.getDate())));

        startActivity(Intent.createChooser(share, "Title of the dialog the system will open"));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.e("HomeFragment", "onActivityResult");
        if (requestCode == NEW_ACTIVITY || requestCode == EDIT_ACTIVITY)
        {
            if (resultCode == RESULT_OK)
            {
                currentTask.fromIntent(data);
                if(requestCode == NEW_ACTIVITY)
                    taskListAdapter.add(currentTask);
                else
                    taskListAdapter.update(currentTask);
            }
            currentTask = null;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
        if (v.getId() == R.id.task_list)
        {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;

            menu.setHeaderTitle(taskListAdapter.getItem(info.position).getName());
            menu.add(Menu.NONE, CONTEXT_MENU_EDIT, Menu.NONE, "Edit");
            menu.add(Menu.NONE, CONTEXT_MENU_DELETE, Menu.NONE, "Delete");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int index = item.getItemId();

        if (index == CONTEXT_MENU_EDIT)
        {
            Intent intent = new Intent(getBaseContext(), EditActivity.class);

            currentTask = taskListAdapter.getItem(info.position);
            currentTask.toIntent(intent);
            startActivityForResult(intent, EDIT_ACTIVITY);
        }
        else if (index == CONTEXT_MENU_DELETE)
        {
            taskListAdapter.delete(info.position);
        }

        return true;
    }

    private AdapterView.OnItemClickListener listOnItemClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            Intent intent = new Intent(getBaseContext(), EditActivity.class);

            currentTask = taskListAdapter.getItem(position);
            currentTask.toIntent(intent);
            DashBoardActivity.this.startActivityForResult(intent, EDIT_ACTIVITY);
        }
    };

    //filter list by category
    public void updateListByCategory(String category){
        this.category = category;
        taskListAdapter.updateAdapter(category, timeID);
    }

    //filter list by time
    public void updateListByTime(int timeID){
        this.timeID = timeID;
        taskListAdapter.updateAdapter(category, timeID);
    }

    //save night mode status to shared preferences
    public void setNightMode(boolean mode){
        Log.d("night", String.valueOf(mode));
        nightMode = mode;
        if(mode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putBoolean("NIGHT_MODE", mode);
            editor.commit();
            startActivity(new Intent(getApplicationContext(), DashBoardActivity.class));
            finish();
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putBoolean("NIGHT_MODE", mode);
            editor.commit();
            startActivity(new Intent(getApplicationContext(), DashBoardActivity.class));
            finish();
        }
    }

    //statistic values
    public void getStatisticsValues(){
        taskCount = taskListAdapter.getCount();
        doneCount = 0;
        undoneCount = 0;
        for(int i = 0; i < taskCount; i++) {
            if(taskListAdapter.getItem(i).getDone()) {
                doneCount++;
            } else {
                undoneCount++;
            }
        }
    }

}
