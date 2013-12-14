package com.armenia.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.armenia.activity.MyHistoryActivity;
import com.armenia.activity.R;
import com.armenia.beans.OpenedProgramBean;
import com.armenia.utils.VideoHistoryUtil;

public class HistoryAdapter extends ArrayAdapter<OpenedProgramBean>{

	private Context mContext = null;
	private VideoHistoryUtil historyUtil = null;
	
	ArrayList<View> viewList = new ArrayList<View>();
	
	public HistoryAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		this.mContext = context;
		historyUtil = new VideoHistoryUtil(context);
	}
	
	
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if(viewList.size()>position){
			if(viewList.get(position)!=null){
				return viewList.get(position);
			}
		}
		View itemView = null;
		final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		itemView = inflater.inflate(R.layout.history_video_item_layout, null);
		((TextView)itemView.findViewById(R.id.history_name)).setText(getItem(position).getName());
		String imageInnerHTML = "<html><head></head><body style = \"padding:0px\" marginwidth=\"0\" marginheight=\"0\" leftmargin=\"0\" topmargin=\"0\">";
		imageInnerHTML+="<center><img src=\""+getItem(position).getImageURL()+/*+getItem(position).getImageURL()+*/"\" .com\"  width=\"180\" /></center></body></html>";
		((WebView)itemView.findViewById(R.id.history_video_image)).setFocusable(false);
		((WebView)itemView.findViewById(R.id.history_video_image)).setClickable(false);
		((WebView)itemView.findViewById(R.id.history_video_image)).loadData(imageInnerHTML, "text/html", "utf-8");
		
		itemView.findViewById(R.id.history_close_btn).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try{
					historyUtil.removeFromHistory("{\"video_path\":\""+getItem(position).getImageURL()+"\",\"video_name\":\""+getItem(position).getName()+"\",\"video_id\":\""+getItem(position).getId()+"\"}");
					viewList.clear();
//					get
					((MyHistoryActivity)mContext).createListOfHistory();
//					notifyDataSetChanged();
				}catch(Exception e){
					
				}
				
				
			}
		});
		viewList.add(position,itemView);
		return itemView;
	}

}



/*package com.armenia.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.armenia.activity.R;
import com.armenia.beans.NewsListBean;

public class AllShowsAdapter extends ArrayAdapter<NewsListBean> {
	
	private Context mContext = null;

	public AllShowsAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		mContext = context;
	}
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View itemView = null;
		final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		itemView = inflater.inflate(R.layout.list_item, null);
		((TextView)itemView.findViewById(R.id.list_item_text)).setText(getItem(position).getText());
		String imageInnerHTML = "<html><head></head><body style = \"padding:0px\" marginwidth=\"0\" marginheight=\"0\" leftmargin=\"0\" topmargin=\"0\">";
		imageInnerHTML+="<center><img src=\""+getItem(position).getImageURL()+"\" .com\"  width=\"100\" /></center></body></html>";
		((WebView)itemView.findViewById(R.id.list_item_webview)).setFocusable(false);
		((WebView)itemView.findViewById(R.id.list_item_webview)).setClickable(false);
		((WebView)itemView.findViewById(R.id.list_item_webview)).loadData(imageInnerHTML, "text/html", "utf-8");
		return itemView;
	}

}
*/