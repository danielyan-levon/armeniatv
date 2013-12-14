package com.armenia.activity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.armenia.listeners.FooterOnClickListener;
import com.armenia.utils.VideoHistoryUtil;

public class PlayVideoActivity extends Activity implements         OnBufferingUpdateListener, OnCompletionListener,
OnPreparedListener, OnVideoSizeChangedListener, SurfaceHolder.Callback{

	
	 private static final String TAG = "MediaPlayerDemo";
	 private int mVideoWidth;
	 private int mVideoHeight;
	 private MediaPlayer mMediaPlayer;
	 private SurfaceView mPreview;
	 private SurfaceHolder holder;
	 private String path;
	 private Bundle extras;
	 private boolean mIsVideoSizeKnown = false;
	 private boolean mIsVideoReadyToBePlayed = false;
	 
	 private String videoPath = "";
	 private String videoName = "";
	 private String videoImage = ""; 
	 private String videoId = "";
	 private Bitmap videoBitmap = null;
	 
	 private int fromId = 0;
	 
	 private ScheduledExecutorService seekScheduler = null;
	 private SeekBar seek = null;

	 private boolean isToolbarVisible = true;
	 
	 private RelativeLayout playerToolbar  = null;
	 private Runnable seekChanger = null;
	    
	 private boolean isPaused = false;
	 
	 private VideoHistoryUtil historyUtil = null;
	 
	 private boolean isLive = false;
	 private int logo = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                                 WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.play_video_featured);
		
		historyUtil = new VideoHistoryUtil(this);
		
		seekScheduler = Executors.newSingleThreadScheduledExecutor();
		seekChanger = new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(!isPaused)
					seek.setProgress(mMediaPlayer.getCurrentPosition());
			}
		};
		seek = (SeekBar)findViewById(R.id.play_video_seekbar);
        mPreview = (SurfaceView) findViewById(R.id.media_player_surface_view);
        holder = mPreview.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        extras = getIntent().getExtras();
        isLive = extras.getBoolean("live");
        if(!isLive){
	        videoName = extras.getString("name");
	        videoId = extras.getString("video_id");
        }else{
        	logo = extras.getInt("logo");
        }
        if(extras.containsKey("selected_id")){
        	fromId = extras.getInt("selected_id");
        }else{
        	throw new IllegalStateException("No fromId found, send extra with key = selected_id");
        }
        
        playerToolbar = (RelativeLayout)findViewById(R.id.play_video_player_toolbar);
        if(isLive){
        	playerToolbar.setVisibility(View.INVISIBLE);
        }
        mPreview.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(isLive){
					return;
				}
				if(isToolbarVisible){
					Animation fadeOutAnim = AnimationUtils.loadAnimation(PlayVideoActivity.this, R.animator.fade_out_animation);
					playerToolbar.startAnimation(fadeOutAnim);
					playerToolbar.setVisibility(View.INVISIBLE);
					isToolbarVisible = false;
				}else{
					Animation fadeInAnim = AnimationUtils.loadAnimation(PlayVideoActivity.this, R.animator.fade_in_animation);
					playerToolbar.startAnimation(fadeInAnim);
					playerToolbar.setVisibility(View.VISIBLE);
					isToolbarVisible = true;
				}
			}
		});
        
        ((RelativeLayout)findViewById(R.id.play_video_pause_button)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(!isPaused){
					mMediaPlayer.pause();
					((ImageView)findViewById(R.id.play_video_play_pause_image)).setImageResource(R.drawable.play_video);
					isPaused = true;
				}else{
					mMediaPlayer.start();
					((ImageView)findViewById(R.id.play_video_play_pause_image)).setImageResource(R.drawable.pause);
					isPaused = false;
				}
			}
		});
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				mMediaPlayer.seekTo(seekBar.getProgress());
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				
			}
		});
        
        initFooter();

	}
	
	 private void startLastActivity(int id){
		 Intent i = null;
     	switch (id) {
		case R.id.featured_featured_button:
			i = new Intent(getApplicationContext(), FeaturedActivity.class );
			break;
		case R.id.featured_schedule_button:
			i = new Intent(getApplicationContext(), ScheduleDailyActivity.class);
			break;
		case R.id.featured_me_button:
			i = new Intent(getApplicationContext(), MyHistoryActivity.class);
			break;
		case R.id.featured_all_shows_button:
			i = new Intent(getApplicationContext(), AllShowsActivity.class);
			break;
     	}

		startActivity(i);
     }
	
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		try {
			 seekScheduler.scheduleWithFixedDelay(seekChanger, 0, 1, TimeUnit.SECONDS);
			 mMediaPlayer.start();
		} catch (Exception e) {
			// TODO: handle exception
			seekScheduler = Executors.newSingleThreadScheduledExecutor();
			seekScheduler.scheduleWithFixedDelay(seekChanger, 0, 1, TimeUnit.SECONDS);
			
		}
	}
	
	 private void initFooter(){
	    	FooterOnClickListener listener = new FooterOnClickListener(this);
	    	findViewById(fromId).setSelected(true);
	    	findViewById(R.id.featured_schedule_button).setOnClickListener(listener);
	    	findViewById(R.id.featured_featured_button).setOnClickListener(listener);
	    	findViewById(R.id.featured_all_shows_button).setOnClickListener(listener);
	    	findViewById(R.id.featured_me_button).setOnClickListener(listener);
	    	findViewById(R.id.featured_info_button).setOnClickListener(listener);
	    }

    private void playVideo() {
        doCleanUp();
        try {

        	path = videoPath;
        	mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.setDisplay(holder);
            mMediaPlayer.prepare();
            mMediaPlayer.setOnBufferingUpdateListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnVideoSizeChangedListener(this);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

             seek.setMax(mMediaPlayer.getDuration());
             seekScheduler.scheduleWithFixedDelay(seekChanger, 0, 1, TimeUnit.SECONDS);
             ((TextView)findViewById(R.id.play_video_name)).setText(videoName);

        } catch (Exception e) {
            Log.e(TAG, "error: " + e.getMessage(), e);
            Toast.makeText(getApplicationContext(), R.string.low_internet, Toast.LENGTH_LONG).show();
        }
        
    }

    public void onBufferingUpdate(MediaPlayer arg0, int percent) {
        Log.d(TAG, "onBufferingUpdate percent:" + percent);

    }

    public void onCompletion(MediaPlayer arg0) {
        Log.d(TAG, "onCompletion called");
    }

    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        Log.v(TAG, "onVideoSizeChanged called");
        if (width == 0 || height == 0) {
            Log.e(TAG, "invalid video width(" + width + ") or height(" + height + ")");
            return;
        }
        mIsVideoSizeKnown = true;
        mVideoWidth = width;
        mVideoHeight = height;
        if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
            startVideoPlayback();
        }
    }

    public void onPrepared(MediaPlayer mediaplayer) {
        Log.d(TAG, "onPrepared called");
        mIsVideoReadyToBePlayed = true;
        if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
            startVideoPlayback();
        }
    }

    public void surfaceChanged(SurfaceHolder surfaceholder, int i, int j, int k) {
        Log.d(TAG, "surfaceChanged called");

    }

    public void surfaceDestroyed(SurfaceHolder surfaceholder) {
        Log.d(TAG, "surfaceDestroyed called");
    }


    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated called");
        Thread tr = new Thread(new Runnable() {
			
			public void run() {
				try {
					getVideoPropertys(videoId);
					historyUtil.addToHistory("{\"video_path\":\""+videoImage+"\",\"video_name\":\""+videoName+"\",\"video_id\":\""+videoId+"\"}");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
        
//        playVideo(5);
        if(isLive){
        	videoPath = "rtsp://streamer.istreamlive.net:1935/144_192/ArmeniaTV";
        	((ImageView)findViewById(R.id.play_video_image)).setImageResource(logo);
        	playVideo();
        }else{
        	tr.run();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaPlayer();
        if(mMediaPlayer!=null && mMediaPlayer.isPlaying())
        	mMediaPlayer.pause();
        doCleanUp();
        seekScheduler.shutdownNow();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mMediaPlayer!=null){
        	mMediaPlayer.release();
        }
        seekScheduler.shutdownNow();
        releaseMediaPlayer();
        doCleanUp();
//        Intent
    }

    
    private void releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private void doCleanUp() {
        mVideoWidth = 0;
        mVideoHeight = 0;
        mIsVideoReadyToBePlayed = false;
        mIsVideoSizeKnown = false;
    }

    private void startVideoPlayback() {
        Log.v(TAG, "startVideoPlayback");
        holder.setFixedSize(mVideoWidth, mVideoHeight);
        mMediaPlayer.start();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	// TODO Auto-generated method stub
    	if(keyCode== KeyEvent.KEYCODE_BACK){
    		startLastActivity(fromId);
    		seekScheduler.shutdownNow();
    		finish();
    	}
    	return super.onKeyDown(keyCode, event);
    }
    
    
    private void getVideoPropertys(String id) throws Exception{
		URL url = new URL("http://armeniatv.com/android/videos/"+id);
		URLConnection connection = url.openConnection();
		String line;
		StringBuilder builder = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));
		while ((line = reader.readLine()) != null) {
			builder.append(line);
		}
		String jsonString = builder.toString();
		
		
		JSONObject jsonObject = new JSONObject(jsonString);
		
		videoImage = jsonObject.getString("image_url");
		videoPath = jsonObject.getString("video_url");
		
		URL bitmapUrl = new URL(videoImage);
		URLConnection bitmapConnection = bitmapUrl.openConnection();
		videoBitmap = BitmapFactory.decodeStream(bitmapConnection.getInputStream());
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				((ImageView)findViewById(R.id.play_video_image)).setImageBitmap(videoBitmap);
			}
		});

		playVideo();
    }
    
}
