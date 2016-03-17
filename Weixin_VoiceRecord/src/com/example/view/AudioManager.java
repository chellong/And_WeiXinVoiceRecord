package com.example.view;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import android.media.MediaRecorder;

public class AudioManager {

	private MediaRecorder mRecorder;
	private String mDirString;
	private String mCurrentFilePathString;

	private boolean isPrepared;

	private static AudioManager mInstance;

	private AudioManager(String dir) {
		mDirString=dir;
	}

	public static AudioManager getInstance(String dir) {
		if (mInstance == null) {
			synchronized (AudioManager.class) {
				if (mInstance == null) {
					mInstance = new AudioManager(dir);
				
				}
			}
		}
		return mInstance;

	}
	public interface AudioStageListener {
		void wellPrepared();
	}

	public AudioStageListener mListener;

	public void setOnAudioStageListener(AudioStageListener listener) {
		mListener = listener;
	}

	@SuppressWarnings("deprecation")
	public void prepareAudio() {
		try {
			isPrepared = false;

			File dir = new File(mDirString);
			if (!dir.exists()) {
				dir.mkdirs();
			}

			String fileNameString = generalFileName();
			File file = new File(dir, fileNameString);

			mCurrentFilePathString = file.getAbsolutePath();

			mRecorder = new MediaRecorder();
			mRecorder.setOutputFile(file.getAbsolutePath());
			mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
			mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			mRecorder.prepare();

			mRecorder.start();
			isPrepared = true;
			if (mListener != null) {
				mListener.wellPrepared();
			}

		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	private String generalFileName() {
		return UUID.randomUUID().toString() + ".amr";
	}

	public int getVoiceLevel(int maxLevel) {
		if (isPrepared) {
			try {
				return maxLevel * mRecorder.getMaxAmplitude() / 32768 + 1;
			} catch (Exception e) {

			}
		}

		return 1;
	}

	public void release() {
		mRecorder.stop();
		mRecorder.release();
		mRecorder = null;

	}
	public void cancel() {
		release();
		if (mCurrentFilePathString != null) {
			File file = new File(mCurrentFilePathString);
			file.delete();
			mCurrentFilePathString = null;
		}

	}

	public String getCurrentFilePath() {
		return mCurrentFilePathString;
	}

}
