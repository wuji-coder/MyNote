package com.example.android.mynote.fragment;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.android.mynote.R;
import com.example.android.mynote.database.Note;
import com.example.android.mynote.viewmodel.NoteViewModel;

import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 * 返回直接保存进数据库
 * 完成增加/修改操作
 */
public class EditorFragment extends Fragment {

    private final String TAG = "addTag";

    private EditText editText;
    private NoteViewModel noteViewModel;
    private InputMethodManager inputMethodManager;//键盘

    private Note note = null;//是否是更新
    private String oldContent = null;//更新状态下记录原有的内容

    public EditorFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FragmentActivity fragmentActivity = requireActivity();

        editText = fragmentActivity.findViewById(R.id.note);

        //获取note，判断本次是修改还是增加.注意添加默认值，否则无法以添加方式进入编辑页面
        note = (Note) (getArguments() != null ? getArguments().get("note") : null);
        //如果note非空，说明本次是修改
        if (note != null) {
            Log.d(TAG, "onActivityCreated: " + note);
            oldContent = note.getContent();
            editText.setText(oldContent);
        }


        //初始化ViewModel
        noteViewModel = new ViewModelProvider(fragmentActivity).get(NoteViewModel.class);

        /*
          弹出键盘
          1. 需要在manifest中的activity处添加android:windowSoftInputMode="adjustNothing",防止页面被压缩
          2. 大坑：必须先获取光标，再取键盘。否则先加载键盘会导致键盘无法弹出.
              解决：延迟一秒等绘制界面结束后再弹出
         */
        //获取光标
        editText.requestFocus();
        //初始化键盘
        inputMethodManager = (InputMethodManager) fragmentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);

        //设定光标所在位置，大坑
        /*Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                inputMethodManager.showSoftInput(editText, 0);
            }
        }, 1000);*/
    }

    @Override
    public void onPause() {
        super.onPause();

        //更新状态下记录提交前的内容
        String newContent = editText.getText().toString();
        String title;
        Log.d(TAG, "onPause: " + newContent);

        if (note != null) {
            //修改状态
            //1.先进行新旧内容比较
            if (oldContent.equals(newContent)) {
                Log.d(TAG, "onPause: 没有发生变化");
                return;
            }
            //2.发生变化，进行空值判断
            if (newContent.equals("")) {
                //如果文本内容已被清空,直接删除改id
                noteViewModel.deleteNotes(note);
                return;
            }
            //3. 进行修改
            //进行长度验证，选取合适部分给title赋值
            if (newContent.length() <= 16) {
                title = newContent;
            } else {
                title = newContent.substring(0, 16) + "...";
            }
            //设值修改
            note.setTitle(title);
            note.setContent(newContent);
            note.setLastUpdateTime(new Date());
            noteViewModel.updateNotes(note);

        } else {
            //添加状态
            //对本次编辑内容进行验证，做出相应操作
            //如果为空直接退出不进行任何操作,
            if (!newContent.equals("")) {
                //进行长度验证，选取合适部分给title赋值
                if (newContent.length() <= 16) {
                    title = newContent;
                } else {
                    title = newContent.substring(0, 16) + "...";
                }

                //注入数据库note对象
                Note note = new Note();
                note.setTitle(title);
                note.setContent(newContent);
                note.setLastUpdateTime(new Date());

                Log.d(TAG, "onPause: " + note);
                noteViewModel.insertNotes(note);
            }
        }


    }


    @Override
    public void onResume() {
        super.onResume();
        inputMethodManager.showSoftInput(editText, 0);
    }
}
