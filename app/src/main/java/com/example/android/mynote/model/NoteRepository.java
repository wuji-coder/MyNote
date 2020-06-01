package com.example.android.mynote.model;

import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.example.android.mynote.database.Note;
import com.example.android.mynote.dao.NoteDao;
import com.example.android.mynote.database.NoteDatabase;

import java.util.List;

/**
 * 数据仓库，处理数据库交互逻辑,将操作放入副线程
 * @author 98578
 * @create 2020-05-29 16:55
 */
public class NoteRepository {

    private NoteDao noteDao;

    private LiveData<List<Note>> allNoteLive;

    public NoteRepository(Context context){
        NoteDatabase database = NoteDatabase.getINSTANCE(context);
        noteDao = database.getNoteDao();
        allNoteLive = noteDao.queryAllNotes();
    }

    public LiveData<List<Note>> getAllWordLive() {
        return allNoteLive;
    }

    public LiveData<List<Note>> queryNotesWithPattern(String pattern){
        //模糊匹配注意百分号
        return noteDao.queryNotesWithPattern("%"+pattern+"%");
    }

    public void insertNotes(Note... notes){
        new InsertAsyncTask(noteDao).execute(notes);
    }

    public void updateNotes(Note... notes){
        new UpdateAsyncTask(noteDao).execute(notes);
    }

    public void deleteNotes(Note... notes){
        new DeleteAsyncTask(noteDao).execute(notes);
    }

    public void deleteAllNotes(){
        new DeleteAllAsyncTask(noteDao).execute();
    }


    //创建副线程类，继承AsyncTask实现

    static class InsertAsyncTask extends AsyncTask<Note, Void, Void>{

        private NoteDao noteDao;

        InsertAsyncTask(NoteDao noteDao) {
            this.noteDao = noteDao;
        }

        @Override
        protected Void doInBackground(Note... notes) {
            noteDao.insertNotes(notes);
            return null;
        }
    }

    static class UpdateAsyncTask extends AsyncTask<Note, Void, Void>{

        private NoteDao noteDao;

        UpdateAsyncTask(NoteDao noteDao) {
            this.noteDao = noteDao;
        }

        @Override
        protected Void doInBackground(Note... notes) {
            noteDao.updateNotes(notes);
            return null;
        }
    }

    static class DeleteAllAsyncTask extends AsyncTask<Note, Void, Void>{

        private NoteDao noteDao;

        DeleteAllAsyncTask(NoteDao noteDao) {
            this.noteDao = noteDao;
        }

        @Override
        protected Void doInBackground(Note... notes) {
            noteDao.deleteAllNotes();
            return null;
        }
    }

    static class DeleteAsyncTask extends AsyncTask<Note, Void, Void>{

        private NoteDao noteDao;

        DeleteAsyncTask(NoteDao noteDao) {
            this.noteDao = noteDao;
        }

        @Override
        protected Void doInBackground(Note... notes) {
            noteDao.deleteNotes(notes);
            return null;
        }
    }





}
