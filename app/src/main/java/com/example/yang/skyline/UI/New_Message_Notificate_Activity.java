package com.example.yang.skyline.UI;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yang.skyline.R;

/**
 * Created by yang on 2016/4/19.
 */
public class New_Message_Notificate_Activity extends Activity implements View.OnClickListener {
    private ImageView back_button;
    private TextView back_textview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.new_message_notificate_layout);

        back_button = (ImageView) findViewById(R.id.back_to_about);
        back_textview = (TextView) findViewById(R.id.exit_to_about);

        back_button.setOnClickListener(this);
        back_textview.setOnClickListener(this);


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed(); //super.onBackPressed()会自动调用finish()方法,关闭当前Activity
        overridePendingTransition(R.anim.new_enter_from_left, R.anim.old_exit_to_right);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_to_about:
            case R.id.exit_to_about:
                finish();
                overridePendingTransition(R.anim.new_enter_from_left, R.anim.old_exit_to_right);

                break;
        }
    }
}
