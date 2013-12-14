package com.armenia.activity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.armenia.adapter.AllShowsAdapter;
import com.armenia.adapter.AllShowsDialogListAdapter;
import com.armenia.beans.AllShowsDialogListBean;
import com.armenia.beans.OpenedProgramBean;
import com.armenia.listeners.FooterOnClickListener;
import com.armenia.utils.NetworkUtils;

public class AllShowsActivity extends Activity{
	
	private AllShowsAdapter programsAdapter = null;
	private String imagePrefix = "";
	private ProgressDialog progressDialog = null;
	
	private final String TYPE_SELECT_LAST = "/pages/20";
	private final String TYPE_SELECT_ALL = "";
	
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                                 WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.programs_layout);
		
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("Loading, please wait.");
		progressDialog.setCancelable(true);
		
		initFooter();
		
		
		GridView programsList = (GridView)findViewById(R.id.programs_list);
		
		programsAdapter = new AllShowsAdapter(this, 0);
		
		programsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
//				Intent i = new Intent(ProgramsActivity.this,OpenedProgramActivity.class);
//				startActivity(i);
				try {
					openVideosDialog(""+programsAdapter.getItem(position).getId(),TYPE_SELECT_LAST,programsAdapter.getItem(position).getName());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			}
		});
		
		imagePrefix = getString(R.string.all_shows_image_prefix);
		programsList.setAdapter(programsAdapter);
		
		Thread tr = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					getProgramsList();
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
	
	
	private void openVideosDialog(final String id,String type,String title) throws Exception{
		if(!NetworkUtils.isNetworkEnabled(getApplicationContext())){
			Toast.makeText(getApplicationContext(), R.string.no_network, Toast.LENGTH_SHORT).show();
			return;
		}
		//get json from server here;
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
			AllShowsDialogListBean tempItem = new AllShowsDialogListBean(jsonArray.getJSONObject(i).getString("post_title"), jsonArray.getJSONObject(i).getString("id"));
			adapter.add(tempItem);
		}
		adapter.notifyDataSetChanged();
		final Dialog allShowsDialog = new Dialog(this);
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
						tempItem = new AllShowsDialogListBean(jsonArray.getJSONObject(i).getString("post_title"),jsonArray.getJSONObject(i).getString("id"));
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
				if(!NetworkUtils.isNetworkEnabled(getApplicationContext())){
					Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show();
					return;
				}
				progressDialog.show();
				Intent i = new Intent(AllShowsActivity.this,PlayVideoActivity.class);
				i.putExtra("video_id",adapter.getItem(position).getId());
				i.putExtra("name", adapter.getItem(position).getName());
				i.putExtra("selected_id",R.id.featured_all_shows_button);
				i.putExtra("live",false);
				startActivity(i);
//				progressDialog.dismiss();
				finish();
			}
		});
		
	}
	
	
	private void getProgramsList() throws Exception{
		URL url = new URL("http://armeniatv.com/android/programs/");
		URLConnection connection = url.openConnection();
		String line;
		StringBuilder builder = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));
		while ((line = reader.readLine()) != null) {
			builder.append(line);
		}
		String jsonString = builder.toString();
		
		final JSONArray newsJsonArray = new JSONArray(jsonString);
		
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				int count = newsJsonArray.length();
				for(int i = 0; i<count; i++){
					try {
						OpenedProgramBean bean = new OpenedProgramBean(imagePrefix+newsJsonArray.getJSONObject(i).getString("image_title")+".jpg",newsJsonArray.getJSONObject(i).getString("name_en"),newsJsonArray.getJSONObject(i).getString("id"));
						programsAdapter.add(bean);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				programsAdapter.notifyDataSetChanged();
				(findViewById(R.id.all_shows_progress_bar)).setVisibility(View.INVISIBLE);
			}
		});
	
	}
	
	
	 private void initFooter(){
	    	FooterOnClickListener listener = new FooterOnClickListener(this);
	    	findViewById(R.id.featured_all_shows_button).setSelected(true);
	    	findViewById(R.id.featured_schedule_button).setOnClickListener(listener);
	    	findViewById(R.id.featured_featured_button).setOnClickListener(listener);
	    	findViewById(R.id.featured_all_shows_button).setOnClickListener(listener);
	    	findViewById(R.id.featured_me_button).setOnClickListener(listener);
	    	findViewById(R.id.featured_info_button).setOnClickListener(listener);
	    }
}
