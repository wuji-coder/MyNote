package com.example.android.mynote;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {

    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View viewById = findViewById(R.id.fragment);

        //使用NavigationUI对导航栏回退进行显示，无功能
        navController = Navigation.findNavController(viewById);
        NavigationUI.setupActionBarWithNavController(this,navController);
    }

    /**
     * 实现navigation的回退
     */
    @Override
    public boolean onSupportNavigateUp() {
        //处理回退键盘显示问题
        InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(findViewById(R.id.fragment).getWindowToken(),0);
        }

        navController.navigateUp();
        return super.onSupportNavigateUp();
    }

}
