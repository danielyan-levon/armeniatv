package com.armenia.activity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.armenia.adapter.AllShowsDialogListAdapter;
import com.armenia.adapter.RecentVideosAdapter;
import com.armenia.beans.AllShowsDialogListBean;
import com.armenia.beans.ResentVideosBean;
import com.armenia.beans.SliderBean;
import com.armenia.listeners.FooterOnClickListener;
import com.armenia.utils.NetworkUtils;

public class FeaturedActivity extends Activity {
    /** Called when the activity is first created. */
	private RecentVideosAdapter recentAdapter = null;
	private final String sliderURL = "http://armeniatv.com/android/programs/slider";
	private ProgressDialog progress  = null;
	private ArrayList<SliderBean> sliderObjects = new ArrayList<SliderBean>();
	private ScheduledExecutorService sliderScheduler = null;
	private Runnable sliderRunnable = null;
	private final String TYPE_SELECT_ALL = "";
	private final String TYPE_SELECT_LAST = "/pages/20";
	private int curImage = 0;
	
	private Bitmap[] sliderBitmaps = new Bitmap[10];
	Bitmap bmp = null;
	Drawable d = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                                 WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.featured);
        findViewById(R.id.featured_most_recent_videos).setVisibility(View.VISIBLE);
		findViewById(R.id.featured_channels_layout).setVisibility(View.INVISIBLE);
        initFooter();
        
        for(int i= 0;i<10;i++){
        	sliderBitmaps[i]= null;
        }
        progress = new ProgressDialog(this);
        progress.setCancelable(true);
        progress.setMessage("Loading, please wait...");
        
        recentAdapter = new RecentVideosAdapter(this, 0);
        
        
        sliderScheduler = Executors.newSingleThreadScheduledExecutor();
        sliderRunnable = new Runnable() {
			
			@Override
			public void run() {
				curImage++;
				if(curImage>= sliderObjects.size()){
					curImage = 0;
				}
					try{
						if(sliderBitmaps[curImage]==null){
							URL url = new URL(sliderObjects.get(curImage).getImage_title());
							URLConnection connection = url.openConnection();
							sliderBitmaps[curImage] = BitmapFactory.decodeStream(connection.getInputStream());
						}
						
					}catch(Exception e){
					}
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						try{
							((ImageView)findViewById(R.id.featured_slider_image_view)).setImageBitmap(sliderBitmaps[curImage]);
							((RelativeLayout)findViewById(R.id.featured_show_relative_layout)).invalidate();
							((TextView)findViewById(R.id.featured_slider_programm_name)).setText(sliderObjects.get(curImage).getName());
							((TextView)findViewById(R.id.featured_slider_programm_day)).setText(sliderObjects.get(curImage).getDay());
						}catch(Exception e){
							
						}
					}
				});
			}
		};
        
        ((Gallery)findViewById(R.id.featured_most_recent_videos)).setAdapter(recentAdapter);
        ((Gallery)findViewById(R.id.featured_most_recent_videos)).setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				Intent i = new Intent(getApplicationContext(), PlayVideoActivity.class);
				sliderScheduler.shutdownNow();
				i.putExtra("video_id",recentAdapter.getItem(position).getId());
				i.putExtra("name",recentAdapter.getItem(position).getName());
				i.putExtra("selected_id",R.id.featured_featured_button);
				i.putExtra("live",false);
				progress.show();
				startActivity(i);
				finish();
				
			}
		});
        
        ((RelativeLayout)findViewById(R.id.featured_button_most_recent)).setSelected(true);
        ((RelativeLayout)findViewById(R.id.featured_button_most_recent)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				((ViewFlipper)findViewById(R.id.featured_menu_flipper)).setDisplayedChild(0);
				findViewById(R.id.featured_most_recent_videos).setVisibility(View.VISIBLE);
				findViewById(R.id.featured_channels_layout).setVisibility(View.INVISIBLE);
				((RelativeLayout)findViewById(R.id.featured_button_most_recent)).setSelected(true);
				((RelativeLayout)findViewById(R.id.featured_button_watch_live)).setSelected(false);
			}
		});
        
        ((RelativeLayout)findViewById(R.id.featured_button_watch_live)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				findViewById(R.id.featured_most_recent_videos).setVisibility(View.INVISIBLE);
				findViewById(R.id.featured_channels_layout).setVisibility(View.VISIBLE);
				((RelativeLayout)findViewById(R.id.featured_button_watch_live)).setSelected(true);
				((RelativeLayout)findViewById(R.id.featured_button_most_recent)).setSelected(false);
				Thread tr = new Thread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						try {
							getRecent();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
		    	if(!NetworkUtils.isNetworkEnabled(getApplicationContext())){
					Toast.makeText(getApplicationContext(), R.string.no_network, Toast.LENGTH_SHORT).show();
					return;
				}else{
					tr.start();
				}
		        
			}
		});
        
        
        findViewById(R.id.featured_slider_toolbar).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(sliderObjects==null || sliderObjects.size()==0){
					return;
				}
					Thread tr = new Thread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							try {
								openVideosDialog(sliderObjects.get(curImage).getId(),  TYPE_SELECT_LAST, sliderObjects.get(curImage).getName());
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					});
					tr.start();
			}
		});
        
        ((RelativeLayout)findViewById(R.id.featured_armenia_live_tv)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(getApplicationContext(),PlayVideoActivity.class);
				sliderScheduler.shutdown();
				i.putExtra("selected_id",R.id.featured_featured_button);
				i.putExtra("live",true);
				i.putExtra("logo",R.drawable.logo);
				progress.show();
				startActivity(i);
				finish();
			}
		});
        
        ((RelativeLayout)findViewById(R.id.featured_usa_armenia_live_tv)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				Intent i = new Intent(getApplicationContext(),PlayVideoActivity.class);
//				i.putExtra("selected_id",R.id.featured_featured_button);
//				i.putExtra("live",true);
//				i.putExtra("logo",R.drawable.logo);
//				startActivity(i);
				Toast.makeText(getApplicationContext(), "Comming soon .", Toast.LENGTH_SHORT).show();
				
			}
		});
        
        Thread tr = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					getRecent();
					getSliderObjects();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
        if(!NetworkUtils.isNetworkEnabled(getApplicationContext())){
			Toast.makeText(getApplicationContext(), R.string.no_network, Toast.LENGTH_SHORT).show();
			return;
		}else{
			tr.start();
		}
       
        
    }
    
    private void getRecent() throws Exception{
    	URL url = new URL("http://armeniatv.com/android/videos/");
		URLConnection connection = url.openConnection();
		String line;
		StringBuilder builder = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));
		while ((line = reader.readLine()) != null) {
			builder.append(line);
		}
		String serverResponse = builder.toString();
		
		final JSONArray jsonArray = new JSONArray(serverResponse);
		
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				recentAdapter.clear();
				int count = jsonArray.length();
		    	for(int i = 0;i<count ; i++){
		    		ResentVideosBean bean = null;
					try {
						JSONObject curObject = jsonArray.getJSONObject(i);
						bean = new ResentVideosBean(curObject.getString("title"),curObject.getString("image_path"),curObject.getString("id"));
					} catch (JSONException e) {
						e.printStackTrace();
					}
		    		recentAdapter.add(bean);
		    	}
		    	recentAdapter.notifyDataSetChanged();
			}
		});
		
    }
    private void initFooter(){
    	FooterOnClickListener listener = new FooterOnClickListener(this);
    	((RelativeLayout)findViewById(R.id.featured_featured_button)).setSelected(true);
    	findViewById(R.id.featured_schedule_button).setOnClickListener(listener);
    	findViewById(R.id.featured_featured_button).setOnClickListener(listener);
    	findViewById(R.id.featured_all_shows_button).setOnClickListener(listener);
    	findViewById(R.id.featured_me_button).setOnClickListener(listener);
    	findViewById(R.id.featured_info_button).setOnClickListener(listener);
    }
    
    
    private void getSliderObjects() throws Exception{
		 URL url = new URL(sliderURL);
			URLConnection connection = url.openConnection();
			String line;
			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			String response = builder.toString();
			
			JSONArray jsonArray = new JSONArray(response);
			
			int count = jsonArray.length();
			
			for(int i= 0;i<count; i++){
				JSONObject obj = jsonArray.getJSONObject(i);
				SliderBean tempObj = new SliderBean(obj.getString("id"), getString(R.string.slider_image_prefix)+obj.getString("image_title")+".jpg", obj.getString("name"), obj.getString("day"));
				sliderObjects.add(tempObj);
			}
			
			
			sliderScheduler.scheduleWithFixedDelay(sliderRunnable, 0, 5, TimeUnit.SECONDS);
			
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	
    	if(bmp!=null){
    		bmp.recycle();
    		bmp = null;
    	}
    	
    	if(sliderScheduler!=null){
    		sliderScheduler.shutdown();
    	}
    }
    
    @Override
    protected void onPause() {
    	sliderScheduler.shutdownNow();
    	super.onPause();
    }
    
    @Override
    protected void onResume() {
    	try{
    		sliderScheduler.schedule(sliderRunnable, 5, TimeUnit.SECONDS);
    	}catch (Exception e) {
    		sliderScheduler = Executors.newSingleThreadScheduledExecutor();
    		sliderScheduler.schedule(sliderRunnable, 5, TimeUnit.SECONDS);
		}
    	super.onResume();
    }
    private void openVideosDialog(final String id,String type,final String title) throws Exception{
		if(!NetworkUtils.isNetworkEnabled(getApplicationContext())){
			Toast.makeText(getApplicationContext(), R.string.no_network, Toast.LENGTH_SHORT).show();
			return;
		}
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				progress.show();
			}
		});
		URL url = new URL("http://armeniatv.com/android/programs/"+id+type);
		URLConnection connection = url.openConnection();
		String line;
		StringBuilder builder = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));
		while ((line = reader.readLine()) != null) {
			builder.append(line);
		}
		String serverResponse = builder.toString();
		
		JSONArray jsonArray = new JSONArray(serverResponse);
		
		final AllShowsDialogListAdapter adapter = new AllShowsDialogListAdapter(this, 0);
		
		int count = jsonArray.length();
		for(int i=0;i<count;i++){
			AllShowsDialogListBean tempItem = new AllShowsDialogListBean(jsonArray.getJSONObject(i).getString("post_title"),jsonArray.getJSONObject(i).getString("id"));
			adapter.add(tempItem);
		}
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				progress.dismiss();
				adapter.notifyDataSetChanged();
				final Dialog allShowsDialog = new Dialog(FeaturedActivity.this);
				allShowsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				final View dialogContentView = getLayoutInflater().inflate(R.layout.all_shows_on_click_dialog_layout, null);
				((TextView)dialogContentView.findViewById(R.id.all_shows_dialog_title)).setText(title);
				((ProgressBar)dialogContentView.findViewById(R.id.all_shows_dialog_progress)).setVisibility(View.INVISIBLE);
				dialogContentView.findViewById(R.id.all_shows_dialog_button).setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(!NetworkUtils.isNetworkEnabled(getApplicationContext())){
							Toast.makeText(getApplicationContext(), R.string.no_network, Toast.LENGTH_SHORT).show();
							return;
						}
					try{	
						adapter.clear();
						dialogContentView.findViewById(R.id.all_shows_dialog_progress).setVisibility(View.VISIBLE);
						URL url = null;
						url = new URL("http://armeniatv.com/android/programs/"+id+TYPE_SELECT_ALL);
						URLConnection connection = null;
						connection = url.openConnection();
						String line;
						StringBuilder builder = new StringBuilder();
						BufferedReader reader = null;
						reader = new BufferedReader(new InputStreamReader(
								connection.getInputStream()));
						while ((line = reader.readLine()) != null) {
							builder.append(line);
						}
						String serverResponse = builder.toString();
						
						JSONArray jsonArray = null;
						jsonArray = new JSONArray(serverResponse);
						
						int count = jsonArray.length();
						for(int i=0;i<count;i++){
							AllShowsDialogListBean tempItem = null;
								tempItem = new AllShowsDialogListBean(jsonArray.getJSONObject(i).getString("post_title"), jsonArray.getJSONObject(i).getString("id"));
							adapter.add(tempItem);
						}
						adapter.notifyDataSetChanged();
						dialogContentView.findViewById(R.id.all_shows_dialog_progress).setVisibility(View.INVISIBLE);
					}catch(Exception e){
						
					}
					}
				});
				allShowsDialog.setContentView(dialogContentView);
				allShowsDialog.show();
				((ListView)dialogContentView.findViewById(R.id.all_shows_dialog_list_view)).setAdapter(adapter);
				((ListView)dialogContentView.findViewById(R.id.all_shows_dialog_list_view)).setOnItemClickListener(new AdapterView.OnItemClickListener() {

					public void onItemClick(AdapterView<?> arg0, View arg1, int position,
							long arg3) {
						progress.show();
						Intent i = new Intent(getApplicationContext(),PlayVideoActivity.class);
						sliderScheduler.shutdown();
						i.putExtra("video_id", adapter.getItem(position).getId());
						i.putExtra("name", adapter.getItem(position).getName());
						i.putExtra("selected_id",R.id.featured_featured_button);
						i.putExtra("live",false);
						startActivity(i);
						finish();
					}
				});
			}
		});

		
	}
}