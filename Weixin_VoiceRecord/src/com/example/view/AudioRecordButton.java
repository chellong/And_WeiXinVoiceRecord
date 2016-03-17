package com.example.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.example.view.AudioManager.AudioStageListener;
import com.example.weixin_voicerecord.R;

@SuppressLint("HandlerLeak")
public class AudioRecordButton extends Button implements AudioStageListener {

	private static final int STATE_NORMAL = 1;
	private static final int STATE_RECORDING = 2;
	private static final int STATE_WANT_TO_CANCEL = 3;

	private static final int DISTANCE_Y_CANCEL = 50;

	private int mCurrentState = STATE_NORMAL;
	private boolean isRecording = false;

	private DialogManager mDialogManager;

	private AudioManager mAudioManager;

	private float mTime = 0;
	private boolean mReady;

	public AudioRecordButton(Context context) {
		this(context, null);
	}

	public AudioRecordButton(Context context, AttributeSet attrs) {
		super(context, attrs);

		mDialogManager = new DialogManager(getContext());

		String dir = Environment.getExternalStorageDirectory()
				+ "/nickming_recorder_audios";
		mAudioManager = AudioManager.getInstance(dir);
		mAudioManager.setOnAudioStageListener(this);

		setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				mReady = true;
				mAudioManager.prepareAudio();
				return false;
			}
		});
	}
	
	public interface AudioFinishRecorderListener{
		void onFinished(float seconds,String filePath);
	}
	
	private AudioFinishRecorderListener mListener;
	
	public void setAudioFinishRecorderListener(AudioFinishRecorderListener listener)
	{
		mListener=listener;
	}

	private Runnable mGetVoiceLevelRunnable = new Runnable() {

		@Override
		public void run() {
			while (isRecording) {
				try {
					Thread.sleep(100);
					mTime += 0.1f;
					mhandler.sendEmptyMessage(MSG_VOICE_CHANGE);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	};

	private static final int MSG_AUDIO_PREPARED = 0X110;
	private static final int MSG_VOICE_CHANGE = 0X111;
	private static final int MSG_DIALOG_DIMISS = 0X112;

	private Handler mhandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_AUDIO_PREPARED:
				mDialogManager.showRecordingDialog();
				isRecording = true;
				new Thread(mGetVoiceLevelRunnable).start();
				break;
			case MSG_VOICE_CHANGE:
				mDialogManager.updateVoiceLevel(mAudioManager.getVoiceLevel(7));
				break;
			case MSG_DIALOG_DIMISS:
				break;

			}
		};
	};

	@Override
	public void wellPrepared() {
		mhandler.sendEmptyMessage(MSG_AUDIO_PREPARED);
	}

	@Override
	@SuppressLint("ClickableViewAccessibility")
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		int x = (int) event.getX();
		int y = (int) event.getY();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			changeState(STATE_RECORDING);
			break;
		case MotionEvent.ACTION_MOVE:

			if (isRecording) {
				if (wantToCancel(x, y)) {
					changeState(STATE_WANT_TO_CANCEL);
				} else {
					changeState(STATE_RECORDING);
				}

			}

			break;
		case MotionEvent.ACTION_UP:
			// 棣栧厛鍒ゆ柇鏄惁鏈夎Е鍙憃nlongclick浜嬩欢锛屾病鏈夌殑璇濈洿鎺ヨ繑鍥瀝eset
			if (!mReady) {
				reset();
				return super.onTouchEvent(event);
			}
			// 濡傛灉鎸夌殑鏃堕棿澶煭锛岃繕娌″噯澶囧ソ鎴栬�呮椂闂村綍鍒跺お鐭紝灏辩寮�浜嗭紝鍒欐樉绀鸿繖涓猟ialog
			if (!isRecording || mTime < 0.6f) {
				mDialogManager.tooShort();
				mAudioManager.cancel();
				mhandler.sendEmptyMessageDelayed(MSG_DIALOG_DIMISS, 1300);// 鎸佺画1.3s
			} else if (mCurrentState == STATE_RECORDING) {//姝ｅ父褰曞埗缁撴潫

				mDialogManager.dimissDialog();
				
				mAudioManager.release();// release閲婃斁涓�涓猰ediarecorder
				
				if (mListener!=null) {// 骞朵笖callbackActivity锛屼繚瀛樺綍闊�
				
					mListener.onFinished(mTime, mAudioManager.getCurrentFilePath());
				}
				
				
				
				

			} else if (mCurrentState == STATE_WANT_TO_CANCEL) {
				// cancel
				mAudioManager.cancel();
				mDialogManager.dimissDialog();
			}
			reset();// 鎭㈠鏍囧織浣�

			break;

		}

		return super.onTouchEvent(event);
	}

	/**
	 * 鍥炲鏍囧織浣嶄互鍙婄姸鎬�
	 */
	private void reset() {
		// TODO Auto-generated method stub
		isRecording = false;
		changeState(STATE_NORMAL);
		mReady = false;
		mTime = 0;
	}

	private boolean wantToCancel(int x, int y) {
		// TODO Auto-generated method stub

		if (x < 0 || x > getWidth()) {// 鍒ゆ柇鏄惁鍦ㄥ乏杈癸紝鍙宠竟锛屼笂杈癸紝涓嬭竟
			return true;
		}
		if (y < -DISTANCE_Y_CANCEL || y > getHeight() + DISTANCE_Y_CANCEL) {
			return true;
		}

		return false;
	}

	private void changeState(int state) {
		// TODO Auto-generated method stub
		if (mCurrentState != state) {
			mCurrentState = state;
			switch (mCurrentState) {
			case STATE_NORMAL:
				setBackgroundResource(R.drawable.button_recordnormal);
				setText(R.string.normal);

				break;
			case STATE_RECORDING:
				setBackgroundResource(R.drawable.button_recording);
				setText(R.string.recording);
				if (isRecording) {
					mDialogManager.recording();
					// 澶嶅啓dialog.recording();
				}
				break;

			case STATE_WANT_TO_CANCEL:
				setBackgroundResource(R.drawable.button_recording);
				setText(R.string.want_to_cancle);
				// dialog want to cancel
				mDialogManager.wantToCancel();
				break;

			}
		}

	}

	@Override
	public boolean onPreDraw() {
		// TODO Auto-generated method stub
		return false;
	}

}
