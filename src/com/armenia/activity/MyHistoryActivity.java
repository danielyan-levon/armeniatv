package com.armenia.activity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.armenia.activity.R.id;
import com.armenia.adapter.HistoryAdapter;
import com.armenia.beans.OpenedProgramBean;
import com.armenia.listeners.FooterOnClickListener;
import com.armenia.utils.VideoHistoryUtil;

public class MyHistoryActivity extends Activity {

	private ProgressDialog progressDialog = null;
	private HistoryAdapter historyAdapter ;
	private VideoHistoryUtil historyUtil = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                                 WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.programs_layout);
		
		((TextView)findViewById(id.all_programms_layout_title)).setText("Me");
		initFooter();
		
		GridView historyLayout = (GridView)findViewById(R.id.programs_list);
		historyAdapter = new HistoryAdapter(this, 0);
		historyUtil = new VideoHistoryUtil(this);
		
		historyLayout.setAdapter(historyAdapter);
		
		historyLayout.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				progressDialog.show();
				Intent i = new Intent(MyHistoryActivity.this,PlayVideoActivity.class);
				i.putExtra("video_id",historyAdapter.getItem(position).getId());
				i.putExtra("name", historyAdapter.getItem(position).getName());
				i.putExtra("selected_id",R.id.featured_me_button);
				i.putExtra("live",false);
				startActivity(i);
				finish();
			}
		});
		
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("Loading, please wait.");
		progressDialog.setCancelable(true);
		
		createListOfHistory();
		
	}
	
	public void createListOfHistory(){
		historyAdapter.clear();
		JSONArray historyJsonArray = historyUtil.getVideoHistory();
		if(historyJsonArray == null){
			return;
		}
		if(historyJsonArray.length() == 0){
			return;
		}
		int historySize = historyJsonArray.length();
		for(int i = 0;i<historySize; i++){
			try {
				JSONObject curItem = historyJsonArray.getJSONObject(i);
				OpenedProgramBean bean = new OpenedProgramBean(curItem.getString("video_path"),curItem.getString("video_name") , curItem.getString("video_id"));
				historyAdapter.add(bean);
			} catch (JSONException e) {
				continue;
			}
			
		}
		
		
		historyAdapter.notifyDataSetChanged();
		
		(findViewById(R.id.all_shows_progress_bar)).setVisibility(View.INVISIBLE);
	}
	
	 private void initFooter(){
	    	FooterOnClickListener listener = new FooterOnClickListener(this);
	    	((RelativeLayout)findViewById(R.id.featured_me_button)).setSelected(true);
	    	findViewById(R.id.featured_schedule_button).setOnClickListener(listener);
	    	findViewById(R.id.featured_featured_button).setOnClickListener(listener);
	    	findViewById(R.id.featured_all_shows_button).setOnClickListener(listener);
	    	findViewById(R.id.featured_me_button).setOnClickListener(listener);
	    	findViewById(R.id.featured_info_button).setOnClickListener(listener);
	    }
	 
	 
}
