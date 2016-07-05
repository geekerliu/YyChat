package com.dasudian.chat;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.dasudian.chat.adapter.MessageAdapter;

public class ChatActivity extends Activity implements OnClickListener {

	public static final int REQUEST_CODE_LOCAL = 2;

	private final String TAG = "ChatActivity";
	private RelativeLayout edittextLayout;
	private LinearLayout moreLayout;
	private Button buttonSetModeKeyboard;
	private Button buttonSend;
	private Button buttonMore;
	private LinearLayout linearLayoutPressToSpeak;
	private EditText editTextContent;
	private Button buttonSetModeVoice;
	private ListView listView;
	private MessageAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		initView();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == REQUEST_CODE_LOCAL && data != null) {
				Uri selectedImage = data.getData();
				if (selectedImage != null) {
					sendImageByUri(selectedImage);
				}
			}
		}
	}

	private void initView() {
		edittextLayout = (RelativeLayout) findViewById(R.id.edittext_layout);
		editTextContent = (EditText) findViewById(R.id.et_sendmessage);
		editTextContent.requestFocus();
		moreLayout = (LinearLayout) findViewById(R.id.ll_more_container);
		buttonSetModeKeyboard = (Button) findViewById(R.id.btn_set_mode_keyboard);
		buttonSend = (Button) findViewById(R.id.btn_send);
		buttonMore = (Button) findViewById(R.id.btn_more);
		linearLayoutPressToSpeak = (LinearLayout) findViewById(R.id.ll_press_to_speak);
		buttonSetModeVoice = (Button) findViewById(R.id.btn_set_mode_voice);
		listView = (ListView) findViewById(R.id.lv_chat);
		adapter = new MessageAdapter(this);
		listView.setAdapter(adapter);

		editTextContent.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				moreLayout.setVisibility(View.GONE);
			}
		});
		editTextContent.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// 当内容不是空时，显示发送按钮，隐藏更多按钮
				if (!TextUtils.isEmpty(s)) {
					buttonMore.setVisibility(View.GONE);
					buttonSend.setVisibility(View.VISIBLE);
				} else {
					buttonMore.setVisibility(View.VISIBLE);
					buttonSend.setVisibility(View.GONE);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		// 设置监听事件
		findViewById(R.id.view_photo).setOnClickListener(this);
		findViewById(R.id.view_camera).setOnClickListener(this);
		findViewById(R.id.view_file).setOnClickListener(this);
	}

	/**
	 * 设置当前输入模式为语音输入模式
	 * 
	 * @param view
	 */
	public void setModeVoice(View view) {
		// 隐藏软键盘
		hideKeyboard();
		// 隐藏输入文字布局
		edittextLayout.setVisibility(View.GONE);
		// 隐藏更多布局
		moreLayout.setVisibility(View.GONE);
		// 隐藏当前点击的按钮
		view.setVisibility(View.GONE);
		// 显示设置为键盘输入的按钮
		buttonSetModeKeyboard.setVisibility(View.VISIBLE);
		// 隐藏发送按钮
		buttonSend.setVisibility(View.GONE);
		// 显示+号按钮
		buttonMore.setVisibility(View.VISIBLE);
		// 显示按住说话的布局
		linearLayoutPressToSpeak.setVisibility(View.VISIBLE);
	}

	/**
	 * 设置当前输入模式为文字输入模式
	 * 
	 * @param view
	 */
	public void setModeKeyboard(View view) {
		edittextLayout.setVisibility(View.VISIBLE);
		moreLayout.setVisibility(View.GONE);
		view.setVisibility(View.GONE);
		buttonSetModeVoice.setVisibility(View.VISIBLE);
		editTextContent.requestFocus();
		linearLayoutPressToSpeak.setVisibility(View.GONE);
		// 如果输入框中有文字，显示发送按钮，隐藏+号按钮
		// 如果里面没有文字，显示+号按钮，隐藏发送按钮
		if (TextUtils.isEmpty(editTextContent.getText())) {
			buttonMore.setVisibility(View.VISIBLE);
			buttonSend.setVisibility(View.GONE);
		} else {
			buttonMore.setVisibility(View.GONE);
			buttonSend.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 显示或隐藏更多
	 * 
	 * @param view
	 */
	public void more(View view) {
		if (moreLayout.getVisibility() == View.GONE) {
			hideKeyboard();
			moreLayout.setVisibility(View.VISIBLE);
		} else {
			moreLayout.setVisibility(View.GONE);
		}
	}

	/**
	 * 发送按钮点击事件
	 * 
	 * @param view
	 */
	public void onClickSendText(View view) {
		String content = editTextContent.getText().toString();
		DsdMessage msg = DsdMessage.createTxtMessage(
				DsdMessage.MESSAGE_TYPE_SEND_TXT, content + "发送");
		adapter.msgList.add(msg);
		DsdMessage msg1 = DsdMessage.createTxtMessage(
				DsdMessage.MESSAGE_TYPE_RECV_TXT, content + "接收");
		adapter.msgList.add(msg1);
		editTextContent.setText("");
		adapter.notifyDataSetChanged();
		listView.setSelection(listView.getCount() - 1);
	}

	/**
	 * 从图库获取图片
	 */
	public void selectImageFromLocal() {
		Intent intent;
		if (Build.VERSION.SDK_INT < 19) {
			intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");
		} else {
			intent = new Intent(
					Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		}
		startActivityForResult(intent, REQUEST_CODE_LOCAL);
	}

	/**
	 * 根据图库图片uri发送图片
	 * 
	 * @param selectedImage
	 */
	private void sendImageByUri(Uri selectedImage) {
		Cursor cursor = getContentResolver().query(selectedImage, null, null,
				null, null);
		if (cursor != null) {
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex("_data");
			String imagePath = cursor.getString(columnIndex);
			cursor.close();
			cursor = null;
			if (imagePath == null || imagePath.equals("null")) {
				Toast toast = Toast.makeText(this, "找不到图片", Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				return;
			}
			sendImage(imagePath);
		} else {
			File file = new File(selectedImage.getPath());
			if (!file.exists()) {
				Toast toast = Toast.makeText(this, "找不到图片", Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				return;

			}
			sendImage(file.getAbsolutePath());
		}
	}

	/**
	 * 发送图片
	 * 
	 * @param filePath
	 */
	private void sendImage(final String filePath) {
		Log.d(TAG, "sendImage");
		DsdMessage message = DsdMessage.createImageMessage(
				DsdMessage.MESSAGE_TYPE_SEND_IMAGE, filePath);
		adapter.msgList.add(message);
		DsdMessage message1 = DsdMessage.createImageMessage(
				DsdMessage.MESSAGE_TYPE_RECV_IMAGE, filePath);
		adapter.msgList.add(message1);
		listView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		listView.setSelection(listView.getCount() - 1);
	}

	/**
	 * 隐藏软键盘
	 */
	private void hideKeyboard() {
		if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
			if (getCurrentFocus() != null) {
				InputMethodManager manager;
				manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				manager.hideSoftInputFromWindow(getCurrentFocus()
						.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}
	}

	@Override
	public void onClick(View v) {
		hideKeyboard();
		int id = v.getId();

		switch (id) {
		case R.id.view_photo:
			selectImageFromLocal();
			break;
		default:
			break;
		}
	}
}
