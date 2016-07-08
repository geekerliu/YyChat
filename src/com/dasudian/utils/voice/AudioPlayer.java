package com.dasudian.utils.voice;

import java.io.File;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;

public class AudioPlayer {

	private MediaPlayer mPlayer;
	private boolean isPlaying = false;

	public void stop() {
		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
		}
	}

	public void play(Context c, String filePath) {
		// 避免多次单击重复创建MediaPlayer实例
		stop();

		mPlayer = MediaPlayer.create(c, Uri.fromFile(new File(filePath)));
		mPlayer.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				stop();
			}
		});
		mPlayer.start();
	}

}
