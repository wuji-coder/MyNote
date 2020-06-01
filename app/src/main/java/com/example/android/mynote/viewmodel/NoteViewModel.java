package com.example.android.mynote.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.android.mynote.database.Note;
import com.example.android.mynote.model.NoteRepository;

import java.util.List;

/**
 * 数据处理类
 * 继承AndroidViewModel，原因是为了引入application，将其注入数据仓库中
 * @author 98578
 * @create 2020-05-29 17:41
 */
public class NoteViewModel extends AndroidViewModel {
    /**
     * 使用数据仓库处理好的数据库交互逻辑
     */
    private NoteRepository repository;

    public NoteViewModel(@NonNull Application application) {
        super(application);
        repository = new NoteRepository(application);
    }

    public LiveData<List<Note>> getAllNoteLive() {
        return repository.getAllWordLive();
    }

    public LiveData<List<Note>> queryNotesWithPattern(String pattern){
        return repository.queryNotesWithPattern(pattern);
    }

    public void insertNotes(Note... notes){
        repository.insertNotes(notes);
    }

    public void updateNotes(Note... notes){
        repository.updateNotes(notes);
    }

    public void deleteNotes(Note... notes){
        repository.deleteNotes(notes);
    }

    public void deleteAllNotes(){
        repository.deleteAllNotes();
    }
}
