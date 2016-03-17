package com.example.weixin_voicerecord;

import java.io.IOException;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;

public class MediaManager {

	private static MediaPlayer mPlayer;

	private static boolean isPause;

	public static void playSound(String filePathString,
			OnCompletionListener onCompletionListener) {
		if (mPlayer == null) {
			mPlayer = new MediaPlayer();
			mPlayer.setOnErrorListener(new OnErrorListener() {
				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					mPlayer.reset();
					return false;
				}
			});
		} else {
			mPlayer.reset();
		}

		try {
			mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mPlayer.setOnCompletionListener(onCompletionListener);
			mPlayer.setDataSource(filePathString);
			mPlayer.prepare();
			mPlayer.start();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void pause() {
		if (mPlayer != null && mPlayer.isPlaying()) {
			mPlayer.pause();
			isPause = true;
		}
	}

	public static void resume() {
		if (mPlayer != null && isPause) {
			mPlayer.start();
			isPause = false;
		}
	}

	public static void release() {
		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
		}
	}
}
