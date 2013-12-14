package com.armenia.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.armenia.activity.R;
import com.armenia.beans.OpenedProgramBean;

public class AllShowsAdapter extends ArrayAdapter<OpenedProgramBean>{

	private Context mContext = null;
	
	ArrayList<View> viewList = new ArrayList<View>();
	
	public AllShowsAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		this.mContext = context;
	}
	
	
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(viewList.size()>position){
			if(viewList.get(position)!=null){
				return viewList.get(position);
			}
		}
		View itemView = null;
		final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		itemView = inflater.inflate(R.layout.list_item, null);
//		Typeface font= Typeface.createFromAsset(getContext().getAssets(), "arlamu.ttf");
		((TextView)itemView.findViewById(R.id.list_item_text)).setText(getItem(position).getName());
		String imageInnerHTML = "<html><head></head><body style = \"padding:0px\" marginwidth=\"0\" marginheight=\"0\" leftmargin=\"0\" topmargin=\"0\">";
		imageInnerHTML+="<center><img src=\""+getItem(position).getImageURL()+"\" .com\"  width=\"100\" /></center></body></html>";
		((WebView)itemView.findViewById(R.id.list_item_webview)).setFocusable(false);
		((WebView)itemView.findViewById(R.id.list_item_webview)).setClickable(false);
		((WebView)itemView.findViewById(R.id.list_item_webview)).loadData(imageInnerHTML, "text/html", "utf-8");
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