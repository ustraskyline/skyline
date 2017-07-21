package com.example.yang.skyline.UI;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.yang.skyline.R;

public class Microphone_Fragment extends Fragment {
	private TextView audio_btn;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_microphone_layout, container,false);
		audio_btn = (TextView) view.findViewById(R.id.audio);
		audio_btn.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN){
					audio_btn.setBackgroundResource(R.drawable.color_have_focus);
					audio_btn.setText("正在录音中...");

					Intent record_audio = new Intent();
					record_audio.setAction("record_audio");
					getActivity().sendBroadcast(record_audio);
				}else if(event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL){ //ACTION_UP处理的是在Button原位置弹起;//ACTION_CANCEL处理的是焦点离开Button
					audio_btn.setBackgroundResource(R.drawable.color_no_focus);
					audio_btn.setText("按下说话");

					Intent stop_record = new Intent();
					stop_record.setAction("stop_record_audio");
					getActivity().sendBroadcast(stop_record);
				}
				return true;
			}
		});
		return view;
	}

}
