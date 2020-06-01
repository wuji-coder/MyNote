package com.example.android.mynote.database;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.android.mynote.util.Converters;
import com.example.android.mynote.dao.NoteDao;

/**
 * 数据库管理
 * @author 98578
 * @create 2020-05-29 11:25
 */

@Database(entities = {Note.class},version = 1,exportSchema = false)
@TypeConverters({Converters.class})
public abstract class NoteDatabase extends RoomDatabase {

    private static NoteDatabase INSTANCE;

    public synchronized static NoteDatabase getINSTANCE(Context context) {
        if (INSTANCE==null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),NoteDatabase.class,"note_datebase")
                    .build();
        }
        return INSTANCE;
    }

    /**
     * 在@Database中 多个entity则写多个Dao
     */
    public abstract NoteDao getNoteDao();
}
