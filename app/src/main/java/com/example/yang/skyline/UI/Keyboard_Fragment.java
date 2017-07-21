package com.example.yang.skyline.UI;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.yang.skyline.Control.ClientConnectToServerThread;
import com.example.yang.skyline.Control.SkylineClient;
import com.example.yang.skyline.R;

import java.io.IOException;
import java.io.ObjectOutputStream;

import Model.Message;
import Model.MessageType;

public class Keyboard_Fragment extends Fragment{
	private EditText editText;
	private ImageButton send_button;

	private String client_info = SkylineClient.myself_information;
	private String info[]  = client_info.split("_");
	private String sender_id = info[0];
	private String sender_nickName = info[1];


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_keyboard_layout, container, false);

		editText = (EditText) view.findViewById(R.id.edittext_in_conversation);
		send_button = (ImageButton) view.findViewById(R.id.send_icon_in_conversation);

		//为输入信息的edittext添加监听器
		editText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			//监听EditText内容是否为空，来决定发送按钮是否变为蓝色
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.length() != 0) {
					send_button.setImageResource(R.mipmap.send_icon_in_conversation_on);
				} else {
					send_button.setImageResource(R.mipmap.send_icon_in_conversation_close);
				}
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		send_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (editText.getText().length() == 0) {
					Toast.makeText(getActivity(), "内容为空不可以发送", Toast.LENGTH_SHORT).show();
				} else {
					String message_content = editText.getText().toString();

					Intent send_message = new Intent();
					send_message.setAction("send_common_message");
					send_message.putExtra("message",message_content);
					getActivity().sendBroadcast(send_message);

					editText.setText("");
				}
			}
		});


		return view;
	}

}
