package com.example.yang.skyline.UI;

import android.app.Activity;
import android.app.NotificationManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.Toast;

import com.example.yang.skyline.R;

/**
 * Created by yang on 2016/5/16.
 */
public class Test extends Activity {
    private NotificationManager manager;
    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.test_layout);

        Toast.makeText(getApplicationContext(),"已经到了Test_activity",Toast.LENGTH_SHORT).show();

        manager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(100);
    }
}
