package com.example.android.mynote.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.mynote.R;
import com.example.android.mynote.database.Note;

import java.text.SimpleDateFormat;

/**
 * RecycleView's Adapt
 * 比较数据差异，将适配器原有的extends RecyclerView.Adapter优化为
 *
 * @author 98578
 * @create 2020-05-29 11:25
 */
public class MyAdapt extends ListAdapter<Note, MyAdapt.MyViewHolder> {

    public MyAdapt() {
        super(new DiffUtil.ItemCallback<Note>() {
            @Override
            public boolean areItemsTheSame(@NonNull Note oldItem, @NonNull Note newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull Note oldItem, @NonNull Note newItem) {
                return oldItem.getContent().equals(newItem.getContent()) &&
                        oldItem.getLastUpdateTime().equals(newItem.getLastUpdateTime());
            }
        });
    }

    /**
     * 在适配器中创建 ViewHolder,选择item注入
     */
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View itemView = layoutInflater.inflate(R.layout.cell_card, parent, false);

        return new MyViewHolder(itemView);
    }

    /**
     * 对每条item进行数据绑定
     * 经常被呼叫，每次滚入滚出都会调用,所以监听绑定放入onCreateViewHolder中
     */
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Note note = getItem(position);

        holder.textView_title.setText(note.getTitle());
        //对日期格式化再输出
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        holder.textView_time.setText(simpleDateFormat.format(note.getLastUpdateTime()));

        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("note", note);
            //传递参数
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_notesFragment_to_addFragment, bundle);
        });
    }


    /**
     * 自定义 holder对应 item
     * 内部类最好使用static修饰，防止内存泄漏
     */
    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textView_title, textView_time;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textView_title = itemView.findViewById(R.id.textView_title);
            textView_time = itemView.findViewById(R.id.textView_time);
        }
    }

}
