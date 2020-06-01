package com.example.android.mynote.fragment;


import android.app.AlertDialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.mynote.R;
import com.example.android.mynote.view.MyAdapt;
import com.example.android.mynote.database.Note;
import com.example.android.mynote.viewmodel.NoteViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * A simple {@link Fragment} subclass.
 * 注意观察的生命周期
 */
public class NotesFragment extends Fragment {

    //final String TAG = "mainTag";
    //视图层
    private NoteViewModel noteViewModel;
    private RecyclerView recyclerView;
    private MyAdapt myAdapt;
    //数据层
    private LiveData<List<Note>> noteLive;
    private FragmentActivity fragmentActivity;
    //操作标识,只有更新时候才上移。更新删除保持不动
    private boolean undoAction;

    /**
     * 实时保存数据列表，防止通过liveData时直接获取元素时因为异步获取，发生空指针异常
     * 主要用于标记滑动删除中的撤销
     */
    private List<Note> allNotes;

    public NotesFragment() {
        // 显示菜单栏目
        setHasOptionsMenu(true);
    }

    /**
     * 当复合的选项菜单被选中，其监听在此处处理。如：清空数据功能
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        //多个选项菜单，根据不同菜单项的R.id进行匹配操作
        if (item.getItemId() == R.id.clear_data) {//清空数据前需要弹窗确认
            AlertDialog.Builder builder = new AlertDialog.Builder(fragmentActivity);
            builder.setTitle("清空数据");
            builder.setPositiveButton("确定", (dialog, which) -> noteViewModel.deleteAllNotes());
            builder.setNegativeButton("取消", (dialog, which) -> {

            });
            builder.create();
            builder.show();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 初始化菜单栏，并实现显式菜单项功能show
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_menu, menu);

        //搜索
        SearchView searchView = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
        //控制搜索框长度
        int maxWidth = searchView.getMaxWidth();
        searchView.setMaxWidth((int) (0.5 * maxWidth));
        //设置搜索框的实时监听
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //去除多余前后空格
                String pattern = newText.trim();
                noteLive = noteViewModel.queryNotesWithPattern(pattern);
                /*
                  注意：重新赋予LiveData后最好先移除之前的观察。
                  大坑：观察的移除和注入都必须是getViewLifecycleOwner获取的LifecycleOwner。其对应fragment的生命周期
                 */
                noteLive.removeObservers(getViewLifecycleOwner());
                //对LiveData重新进行观察,注意Owner的生命周期，需要注入fragment的owner
                noteLive.observe(getViewLifecycleOwner(), notes -> {
                    //备份列表
                    allNotes = notes;
                    //将观察的数据注入RecycleAdapt中
                    myAdapt.submitList(notes);
                });
                //修改为返回true后事件不会再向下传递，默认false会继续传递
                return true;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notes, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fragmentActivity = requireActivity();
        //初始化当前页面所用ViewModel,注入activity
        noteViewModel = new ViewModelProvider(fragmentActivity).get(NoteViewModel.class);
        //初始化recyclerView
        recyclerView = fragmentActivity.findViewById(R.id.recyclerView);
        myAdapt = new MyAdapt();
        //recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));//大坑
        recyclerView.setLayoutManager(new LinearLayoutManager(fragmentActivity));//大坑,不设置布局不显示
        recyclerView.setAdapter(myAdapt);

        //观察数据列表
        noteLive = noteViewModel.getAllNoteLive();
        //需要注入fragment的owner
        noteLive.observe(getViewLifecycleOwner(), notes -> {
            //Log.d(TAG, "onChanged: " + notes);
            //读取当前显示列表的个数
            int temp = myAdapt.getItemCount();
            //备份列表
            allNotes = notes;
            //如果数据变化后的元素 > 变化前的个数  说明是添加操作，进行
            if (notes.size() > temp && !undoAction) {
                /*
                   滚动到首部，增强视觉效果
                   注意定时任务，否则太快会定位到第二行
                 */
                new Timer().schedule(new TimerTask() {
                    public void run() {
                        recyclerView.smoothScrollToPosition(0);
                    }
                }, 300);
            }
            //如果是撤销删除任务调用观察，撤销后需要恢复undoAction状态
            if (undoAction) {
                undoAction = false;
            }
            //将观察的数据注入RecycleAdapt中
            myAdapt.submitList(notes);
        });

        //初始化floatingActionButton浮动按钮
        FloatingActionButton floatingActionButton = fragmentActivity.findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_notesFragment_to_addFragment);
        });

        //滑动删除
        /*
          参数简介:
          @param dragDirs  Binary OR of direction flags in which the Views can be dragged.
          （上下拖动 ItemTouchHelper.UP | ItemTouchHelper.DOWN）
          @param swipeDirs Binary OR of direction flags in which the Views can be swiped.
          （左右滑动 ItemTouchHelper.START | ItemTouchHelper.END）
         */
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.START | ItemTouchHelper.END) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                //处理上下拖动
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                //处理左右滑动
                //通过提前备份note处理空指针异常，通过viewHolder.getAdapterPosition()定位元素位置
                Note deleteNote = allNotes.get(viewHolder.getAdapterPosition());
                //删除滑出元素
                noteViewModel.deleteNotes(deleteNote);
                //提供撤销操作,此处需要更改fragment中的布局为CoordinatorLayout，否则会被浮动按钮遮挡
                Snackbar.make(fragmentActivity.findViewById(R.id.mainFragment), "删除了一个笔记", Snackbar.LENGTH_SHORT)
                        .setAction("撤销", v -> {
                            //与添加进行区分，防止添加的上移动作混入
                            undoAction = true;
                            //添加回删除的元素
                            noteViewModel.insertNotes(deleteNote);
                        }).show();
            }

            //在滑动的时候，画出浅灰色背景和垃圾桶图标，增强删除的视觉效果
            Drawable icon = ContextCompat.getDrawable(requireActivity(), R.drawable.ic_delete_black_24dp);
            Drawable background = new ColorDrawable(Color.LTGRAY);

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                View itemView = viewHolder.itemView;
                int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;

                int iconLeft, iconRight, iconTop, iconBottom;
                int backTop, backBottom, backLeft, backRight;
                backTop = itemView.getTop();
                backBottom = itemView.getBottom();
                iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                iconBottom = iconTop + icon.getIntrinsicHeight();
                if (dX > 0) {
                    backLeft = itemView.getLeft();
                    backRight = itemView.getLeft() + (int) dX;
                    background.setBounds(backLeft, backTop, backRight, backBottom);
                    iconLeft = itemView.getLeft() + iconMargin;
                    iconRight = iconLeft + icon.getIntrinsicWidth();
                    icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                } else if (dX < 0) {
                    backRight = itemView.getRight();
                    backLeft = itemView.getRight() + (int) dX;
                    background.setBounds(backLeft, backTop, backRight, backBottom);
                    iconRight = itemView.getRight() - iconMargin;
                    iconLeft = iconRight - icon.getIntrinsicWidth();
                    icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                } else {
                    background.setBounds(0, 0, 0, 0);
                    icon.setBounds(0, 0, 0, 0);
                }
                background.draw(c);
                icon.draw(c);
            }
        }).attachToRecyclerView(recyclerView);

    }
}
