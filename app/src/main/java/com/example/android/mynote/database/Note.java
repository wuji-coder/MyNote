package com.example.android.mynote.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * 实体类
 * @author 98578
 * @create 2020-05-29 11:25
 */
@Data
@Entity(tableName = "note")
public class Note implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "content")
    private String content;

    @ColumnInfo(name = "last_update_time")
    private Date lastUpdateTime;

}
