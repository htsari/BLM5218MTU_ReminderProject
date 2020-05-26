package com.project.reminder;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

// singleton
public class DAO {

    private static final String TAG = "DAO";
    private final String DATA_FILE_NAME = "reminder1.txt";

    private static DAO dao = null;

    private Context context = null;
    private ArrayList<Task> list = null;
    private long nextId; // we need it to assign id to tasks

    private static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

    protected DAO() {}

    public static synchronized DAO getInstance(Context context)
    {
        Log.d(TAG, "getting DAO instance");
        if (dao == null)
        {
            dao = new DAO();
            dao.context = context.getApplicationContext();
            dao.load();
        }
        return dao;
    }

    public static synchronized DAO getInstance(Context context, String category, int timeID) {
        Log.d(TAG, "getting DAO instance cat");

        dao = new DAO();
        dao.context = context.getApplicationContext();
        dao.load(category, timeID);

        Log.e("dao list size ", String.valueOf(dao.list.size()));

        return dao;
    }

    //load without parameter
    private void load(){
        list = new ArrayList<Task>(); // creating empty list
        nextId = 1;
        try
        {
            Log.d(TAG, "reading file");

            FileInputStream fileInputStream = context.openFileInput(DATA_FILE_NAME);
            DataInputStream dis = new DataInputStream(fileInputStream);

            nextId = dis.readLong();
            int size = dis.readInt();

            for (int i = 0; i < size; i++)
            {
                Task task = new Task();
                task.deserialize(dis);
                list.add(task);

            }
            dis.close();
        }
        catch (IOException e)
        {
            Log.d(TAG, "Cant load tasks");
        }
    }

    //load by category and time
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void load(String category, int timeID){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MM yyyy");

        list = new ArrayList<Task>(); // creating empty list
        nextId = 1;
        try
        {
            Log.d(TAG, "reading file");

            FileInputStream fileInputStream = context.openFileInput(DATA_FILE_NAME);
            DataInputStream dis = new DataInputStream(fileInputStream);

            nextId = dis.readLong();
            int size = dis.readInt();

            //get current time
            Date dateNow = new Date();
            LocalDate nowDate = LocalDate.parse(new SimpleDateFormat("dd MM yyyy").format(dateNow), dtf);

            for (int i = 0; i < size; i++)
            {
                Task task = new Task();
                task.deserialize(dis);
                Date date = new Date(task.getDate());
                LocalDate taskDate = LocalDate.parse(new SimpleDateFormat("dd MM yyyy").format(date), dtf);

                //days between now and task date
                Period p = Period.between(nowDate, taskDate);
                long duration = p.getDays();

                if(category.equals("") || category.equals(task.getCategory())){
                    if(timeID == 0){
                        //all time
                        list.add(task);
                    } else if(timeID == 1 && duration == 0) {
                        //daily
                        list.add(task);
                    } else if(timeID == 2 && duration >= 0 && duration < 7) {
                        //weekly
                        list.add(task);
                    } else if(timeID == 3 && duration >= 0 && duration <= 30) {
                        //monthly
                        list.add(task);
                    }
                }
            }
            dis.close();
        }
        catch (IOException e)
        {
            Log.d(TAG, "Cant load tasks");
        }
    }

    //call save when date updated
    public void save(){
        try
        {
            Log.d(TAG, "saving file");

            DataOutputStream dos = new DataOutputStream(context.openFileOutput(DATA_FILE_NAME, Context.MODE_PRIVATE));

            dos.writeLong(nextId);
            dos.writeInt(list.size());

            for (int i = 0; i < list.size(); i++)
                list.get(i).serialize(dos);

            dos.close();
        } catch (IOException e)
        {
            Log.d(TAG, "Cant save tasks");
        }
    }

    //get list size
    public int size()
    {
        return list.size();
    }

    //get the the task at position
    public Task get(int position)
    {
        return list.get(position);
    }

    //add new task
    public void add(Task Task)
    {
        Task.setId(nextId++);
        list.add(Task);
        Collections.sort(list);
        save();
    }

    //update task
    public void update(Task Task)
    {
        Task.update();
        Collections.sort(list);
        save();
    }

    //remove task
    public void remove(int index)
    {
        list.remove(index);
        save();
    }

    public static String formatTime(Task Task)
    {
        return timeFormat.format(new Date(Task.getDate()));
    }

    public static String formatDate(Task Task)
    {
        return dateFormat.format(new Date(Task.getDate()));
    }
}
