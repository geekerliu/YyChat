package com.dasudian.utils.voice;

import java.io.File;
import java.util.UUID;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class VoiceRecoder {
	private final String TAG = "VoiceRecoder";
	private RecMicToMp3 recMicToMp3 = null;
	String voiceFilePath;

	public VoiceRecoder(String toChatUserName, Context context) {
		// String filePath = context.getFilesDir().toString() + toChatUserName + "/test.mp3";
		String folder = Environment.getExternalStorageDirectory()
				+ File.separator + toChatUserName;
		File file = new File(folder);
		if (!file.exists()) {
			file.mkdir();
		}
		voiceFilePath = folder + File.separator + UUID.randomUUID().toString() + ".mp3";
		Log.d(TAG, "voiceFilePath = " + voiceFilePath);
		recMicToMp3 = new RecMicToMp3(voiceFilePath, 8000);
	}

	public void start() {
		if (recMicToMp3 != null) {
			recMicToMp3.start();
		}
	}

	public void stop() {
		if (recMicToMp3 != null) {
			recMicToMp3.stop();
		}
	}

	/**
	 * 取消本次录音
	 */
	public void discard() {
		if (recMicToMp3 != null) {
			recMicToMp3.discard();
		}
	}
	
	public String getVoiceFilePath(String toChatUserName) {
		return voiceFilePath;
	}
}
