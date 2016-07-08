package com.dasudian.chat;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dasudian.chat.adapter.MessageAdapter;
import com.dasudian.utils.CommonUtils;
import com.dasudian.utils.voice.AudioPlayer;
import com.dasudian.utils.voice.VoiceRecoder;

public class ChatActivity extends Activity implements OnClickListener {
	// 从图库选择图片
	public static final int REQUEST_CODE_LOCAL = 2;

	private final String TAG = "ChatActivity";
	private RelativeLayout edittextLayout;
	private LinearLayout moreLayout;
	private Button buttonSetModeKeyboard;
	// 发送消息按钮
	private Button buttonSend;
	// +号按钮
	private Button buttonMore;
	private LinearLayout linearLayoutPressToSpeak;
	// 输入聊天内容的EditText
	private EditText editTextContent;
	private Button buttonSetModeVoice;
	private ListView listView;
	private MessageAdapter adapter;
	// 麦克风
	private ImageView micImage;
	private AnimationDrawable animationDrawable;
	// 语音输入按钮上的提示文字
	private TextView recordingHint;
	// 语音显示布局
	private View recordingContainer;
	private AudioPlayer audioPlayer = new AudioPlayer();
	private VoiceRecoder voiceRecoder;
	private String toChatName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		toChatName = getIntent().getStringExtra("name");
		initView();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == REQUEST_CODE_LOCAL && data != null) {
				// 选完图片后隐藏选择布局
				moreLayout.setVisibility(View.GONE);
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
		micImage = (ImageView) findViewById(R.id.mic_image);
		animationDrawable = (AnimationDrawable) micImage.getBackground();
		animationDrawable.setOneShot(false);
		linearLayoutPressToSpeak = (LinearLayout) findViewById(R.id.ll_press_to_speak);
		linearLayoutPressToSpeak.setOnTouchListener(new PressToSpeakListener());
		recordingHint = (TextView) findViewById(R.id.recording_hint);
		buttonSetModeVoice = (Button) findViewById(R.id.btn_set_mode_voice);
		recordingContainer = findViewById(R.id.view_talk);
		listView = (ListView) findViewById(R.id.lv_chat);
		Log.d(TAG, "toChatName = " + toChatName);
		adapter = new MessageAdapter(this, this, toChatName);
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
	 * 按住说话listener
	 */
	class PressToSpeakListener implements View.OnTouchListener {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				animationDrawable.start();
				if (!CommonUtils.isExitsSdcard()) {
					Toast.makeText(ChatActivity.this, "发送语音需要sdcard支持",
							Toast.LENGTH_SHORT).show();
					return false;
				}
				v.setPressed(true);

				// 如果此时正在播放语音，先停止语音播放
				if (audioPlayer != null) {
					audioPlayer.stop();
				}

				// 显示说话的布局
				recordingContainer.setVisibility(View.VISIBLE);
				recordingHint.setText("手指上滑，取消发送");
				recordingHint.setBackgroundColor(Color.TRANSPARENT);

				// 开始录音
				Log.d(TAG, "voiceRecoder.start()");
				voiceRecoder = new VoiceRecoder("dasudian", ChatActivity.this);
				voiceRecoder.start();

				return true;
			case MotionEvent.ACTION_MOVE:
				if (event.getY() < 0) {
					recordingHint.setText("松开手指，取消发送");
					recordingHint
							.setBackgroundResource(R.drawable.recording_text_hint_bg);
				} else {
					recordingHint.setText("手指上滑，取消发送");
					recordingHint.setBackgroundColor(Color.TRANSPARENT);
					animationDrawable.start();
				}
				return true;
			case MotionEvent.ACTION_UP:
				if (animationDrawable.isRunning()) {
					animationDrawable.stop();
				}
				v.setPressed(false);
				recordingContainer.setVisibility(View.INVISIBLE);
				if (event.getY() < 0) {
					// 丢弃这次录音
					if (voiceRecoder != null) {
						Log.d(TAG, "voiceRecoder.discard()");
						voiceRecoder.discard();
					}
				} else {
					// 停止录音并发送录音文件
					if (voiceRecoder != null) {
						Log.d(TAG, "voiceRecoder.stop()");
						voiceRecoder.stop();
						sendVoice(voiceRecoder.getVoiceFilePath("name"));
					}
				}
				return true;
			default:
				recordingContainer.setVisibility(View.INVISIBLE);
				// 丢弃这次录音
				if (voiceRecoder != null) {
					Log.d(TAG, "voiceRecoder.discard()");
					voiceRecoder.discard();
				}
				return false;
			}
		}
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
	 * 发送语音
	 * @param filePath
	 */
	private void sendVoice(String filePath) {
		Log.d(TAG, "sendVoice :" + filePath);
		DsdMessage message = DsdMessage.createVoiceMessage(
				DsdMessage.MESSAGE_TYPE_SEND_VOICE, filePath);
		adapter.msgList.add(message);
		DsdMessage message1 = DsdMessage.createVoiceMessage(
				DsdMessage.MESSAGE_TYPE_RECV_VOICE, filePath);
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
