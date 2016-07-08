package com.dasudian.chat.adapter;

import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dasudian.chat.DsdChatManager;
import com.dasudian.chat.DsdMessage;
import com.dasudian.chat.R;
import com.dasudian.utils.ImageCache;
import com.dasudian.utils.ImageSizeUtil;
import com.dasudian.utils.voice.AudioPlayer;

public class MessageAdapter extends BaseAdapter {

	private final static String TAG = "MessageAdapter";
	public List<DsdMessage> msgList;
	private LayoutInflater inflater;
	private Activity activity;

	public MessageAdapter(Context context, Activity activity, String toChatName) {
		this.inflater = LayoutInflater.from(context);
		this.activity = activity;
		this.msgList = DsdChatManager.getInstance().getMsgList(toChatName);
	}

	@Override
	public int getCount() {
		return msgList.size();
	}

	@Override
	public DsdMessage getItem(int position) {
		return msgList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		return getItem(position).getType();
	}

	@Override
	public int getViewTypeCount() {
		return DsdMessage.MESSAGE_TYPE_COUNT;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		DsdMessage message = getItem(position);
		ViewHolder holder;
		if (convertView == null) {
			convertView = createViewByMessage(message, position);
			holder = getHolder(message, convertView);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		setHolder(message, holder);

		return convertView;
	}

	/**
	 * 根据消息类型创建对应的布局
	 * 
	 * @param message
	 * @param position
	 * @return
	 */
	private View createViewByMessage(DsdMessage message, int position) {
		switch (message.getType()) {
		case DsdMessage.MESSAGE_TYPE_RECV_TXT:
			return inflater.inflate(R.layout.row_recive_message, null);
		case DsdMessage.MESSAGE_TYPE_SEND_TXT:
			return inflater.inflate(R.layout.row_send_message, null);
		case DsdMessage.MESSAGE_TYPE_RECV_IMAGE:
			return inflater.inflate(R.layout.row_recive_image, null);
		case DsdMessage.MESSAGE_TYPE_SEND_IMAGE:
			return inflater.inflate(R.layout.row_send_image, null);
		case DsdMessage.MESSAGE_TYPE_RECV_VOICE:
			return inflater.inflate(R.layout.row_recive_voice, null);
		case DsdMessage.MESSAGE_TYPE_SEND_VOICE:
			return inflater.inflate(R.layout.row_send_voice, null);
		default:
			return null;
		}
	}

	/**
	 * 根据消息类型返回对应的holder
	 * 
	 * @param message
	 * @param convertView
	 * @return
	 */
	private ViewHolder getHolder(DsdMessage message, View convertView) {
		ViewHolder holder = new ViewHolder();

		switch (message.getType()) {
		case DsdMessage.MESSAGE_TYPE_RECV_TXT:
		case DsdMessage.MESSAGE_TYPE_SEND_TXT:
			holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
			holder.tv_content = (TextView) convertView
					.findViewById(R.id.tv_content);
			holder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
			break;
		case DsdMessage.MESSAGE_TYPE_SEND_IMAGE:
		case DsdMessage.MESSAGE_TYPE_RECV_IMAGE:
			holder.iv_image = (ImageView) convertView
					.findViewById(R.id.iv_image);
			break;
		case DsdMessage.MESSAGE_TYPE_SEND_VOICE:
		case DsdMessage.MESSAGE_TYPE_RECV_VOICE:
			holder.iv_voice = (ImageView) convertView
					.findViewById(R.id.iv_voice);
			break;
		default:
			break;
		}

		return holder;
	}

	/**
	 * 根据消息类型设置holder
	 * 
	 * @param holder
	 */
	private void setHolder(final DsdMessage message, final ViewHolder holder) {
		switch (message.getType()) {
		case DsdMessage.MESSAGE_TYPE_RECV_TXT:
		case DsdMessage.MESSAGE_TYPE_SEND_TXT:
			holder.tv_content.setText(message.getContent());
			break;
		case DsdMessage.MESSAGE_TYPE_RECV_IMAGE:
		case DsdMessage.MESSAGE_TYPE_SEND_IMAGE:
			// 这里需要异步加载图片，不然会很卡
			// 需要压缩和cache，不然会用尽内存
			Bitmap bitmap = ImageCache.getInstance()
					.get(message.getContent());
			if (bitmap != null) {
				Log.d(TAG, "从cache中读取图片");
				holder.iv_image.setImageBitmap(bitmap);
				clickToShowBigImage(holder.iv_image, message);
			} else {
				new AsyncTask<Object, Void, Bitmap>() {

					@Override
					protected Bitmap doInBackground(Object... params) {
						Log.d(TAG, "重新获取图片");
						return ImageSizeUtil.decodeSampledBitmapFromPath(
								message.getContent(), 160, 160);
					}

					@Override
					protected void onPostExecute(Bitmap image) {
						if (image != null) {
							ImageCache.getInstance().put(
									message.getContent(), image);
							holder.iv_image.setImageBitmap(image);
							clickToShowBigImage(holder.iv_image, message);
						}
					}

				}.execute();
			}
			break;
		case DsdMessage.MESSAGE_TYPE_RECV_VOICE:
		case DsdMessage.MESSAGE_TYPE_SEND_VOICE:
			holder.iv_voice.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Log.d(TAG, "voicePath = " + message.getContent());
					new AudioPlayer().play(activity, message.getContent());
				}
			});
			break;
		default:
			break;
		}
	}

	/**
	 * 点击图片查看大图
	 * 
	 * @param image
	 * @param message
	 */
	private void clickToShowBigImage(ImageView image, final DsdMessage message) {
		image.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ImageView image = new ImageView(activity);
				final Dialog dialog = new Dialog(activity,
						android.R.style.Theme_NoTitleBar_Fullscreen);
				image.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
				image.setImageDrawable(ImageSizeUtil.getScaledBitmapDrawable(
						activity, message.getContent()));
				dialog.addContentView(image, new RelativeLayout.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT));
				dialog.show();
			}
		});
	}

	class ViewHolder {
		TextView tv_time;
		ImageView iv_icon;
		TextView tv_content;
		// 发送接收图片的ImageView
		ImageView iv_image;
		// 发送语音消息的ImageView
		ImageView iv_voice;
	}
}
