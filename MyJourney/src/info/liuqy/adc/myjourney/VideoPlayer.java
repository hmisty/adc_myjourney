package info.liuqy.adc.myjourney;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class VideoPlayer extends Activity implements Callback,
		OnCompletionListener {
	
    MediaPlayer mediaPlayer;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    String uriString;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        this.setContentView(R.layout.videoplayer);

        getWindow().setFormat(PixelFormat.UNKNOWN);
        
        surfaceView = (SurfaceView) findViewById(R.id.surfaceview);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setFixedSize(176, 144);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setDisplay(surfaceHolder);
        mediaPlayer.setOnCompletionListener(this);
        
        Intent i = this.getIntent();
        uriString = i.getStringExtra("uri");        
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
        mediaPlayer.release();
        this.finish();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
        // play the video. don't try to play when onResume() which is too early.
        try {
            mediaPlayer.setDataSource(uriString);
            mediaPlayer.prepare();
        } catch (Exception e) {
            // TODO handle various exceptions
            e.printStackTrace();
        }
        mediaPlayer.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub

	}

}
