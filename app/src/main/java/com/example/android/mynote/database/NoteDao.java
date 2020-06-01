package com.example.android.mynote.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * 数据库操作的接口
 * @author 98578
 * @create 2020-05-29 11:25
 */
@Dao
public interface NoteDao {

    @Insert
    void insertNotes(Note... notes);

    @Update
    void updateNotes(Note... notes);

    @Delete
    void deleteNotes(Note... notes);

    @Query("delete from note")
    void deleteAllNotes();

    @Query("select * from note order by last_update_time desc")
    LiveData<List<Note>> queryAllNotes();

    @Query("select * from note where content like :pattern order by last_update_time desc")
    LiveData<List<Note>> queryNotesWithPattern(String pattern);

}
