package com.mrrun.lib.pageenabledslideproject.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.mrrun.lib.pageenabledslideproject.R;
import com.mrrun.lib.pageenabledslideproject.view.PageEnabledSlidingPaneLayout;

/**
 * @author lipin
 * @version 1.0
 * @date 2019.07.01
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getBaseContext(), MainActivity.class));
            }
        });

        PageEnabledSlidingPaneLayout page = new PageEnabledSlidingPaneLayout(this);
        page.setSupportSwipeBack(true);
        page.setOnSildingFinishListener(new PageEnabledSlidingPaneLayout.OnSlidingFinishListener() {
            @Override
            public void onSildingFinish() {
                finish();
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
            }

            @Override
            public void onSildeLeftFinish() {
                //TODO
            }
        });
    }
}
