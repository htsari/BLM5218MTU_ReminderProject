package com.project.reminder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Comparable;

import android.content.Intent;
import android.provider.Settings;

public class Task implements Comparable<Task>{

    private long id;
    private String name;
    private String description;
    private String category;
    private long date; // start date
    private boolean enabled; //alarm enabled
    private boolean done;
    private long occurence;
    private String ringtoneUri; //alarm specific ringtone

    // used while populating intent
    private String prefix = "com.project.task";

    public Task()
    {
        id = 0;
        name = "";
        description = "";
        category = "";
        date = System.currentTimeMillis();
        enabled = false;
        done = false;
        ringtoneUri = Settings.System.DEFAULT_RINGTONE_URI.toString();
        update();
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public long getDate()
    {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
        update();
    }

    public boolean getEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public boolean getDone()
    {
        return done;
    }

    public void setDone(boolean done)
    {
        this.done = done;
    }

    public String getRingtoneUri()
    {
        return ringtoneUri;
    }

    public void setRingtoneUri(String ringtoneUri)
    {
        this.ringtoneUri = ringtoneUri;
    }

    public void toIntent(Intent intent)
    {
        intent.putExtra(prefix + ".id", id);
        intent.putExtra(prefix + ".name", name);
        intent.putExtra(prefix + ".description", description);
        intent.putExtra(prefix + ".category", category);
        intent.putExtra(prefix + ".date", date);
        intent.putExtra(prefix + ".enabled", enabled);
        intent.putExtra(prefix + ".done", done);
        intent.putExtra(prefix + ".occurence", occurence);
        intent.putExtra(prefix + ".ringtoneUri", ringtoneUri);
    }

    // reserved for the case if there are multiple alarms for the same task
    public void update()
    {
        occurence = date;
    }

    public void fromIntent(Intent intent)
    {
        id = intent.getLongExtra(prefix + ".id", 0);
        name = intent.getStringExtra(prefix + ".name");
        description = intent.getStringExtra(prefix + ".description");
        category = intent.getStringExtra(prefix + ".category");
        date = intent.getLongExtra(prefix + ".date", 0);
        enabled = intent.getBooleanExtra(prefix + ".enabled", true);
        done = intent.getBooleanExtra(prefix + ".done", true);
        occurence = intent.getLongExtra(prefix + ".occurence", 0);
        ringtoneUri = intent.getStringExtra(prefix + ".ringtoneUri");
        update();
    }

    // reserved for multiple alarms for the same task
    public long getNextOccurence()
    {
        return occurence;
    }

    public boolean getOutdated()
    {
        return occurence < System.currentTimeMillis();
    }

    // used to sort while displaying tasks
    public int compareTo(Task other)
    {
        final long thisNext = getNextOccurence();
        final long thatNext = other.getNextOccurence();
        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;

        if (this == other)
            return EQUAL;

        if (thisNext > thatNext)
            return AFTER;
        else if (thisNext < thatNext)
            return BEFORE;
        else
            return EQUAL;
    }

    public void serialize(DataOutputStream dos) throws IOException
    {
        dos.writeLong(id);
        dos.writeUTF(name);
        dos.writeUTF(description);
        dos.writeUTF(category);
        dos.writeLong(date);
        dos.writeBoolean(enabled);
        dos.writeBoolean(done);
        dos.writeLong(occurence);
    }

    public void deserialize(DataInputStream dis) throws IOException
    {
        id = dis.readLong();
        name = dis.readUTF();
        description = dis.readUTF();
        category = dis.readUTF();
        date = dis.readLong();
        enabled = dis.readBoolean();
        done = dis.readBoolean();
        occurence = dis.readLong();
        update();
    }
}
