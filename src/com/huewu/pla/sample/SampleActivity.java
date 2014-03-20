package com.huewu.pla.sample;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.huewu.pla.R;
import com.huewu.pla.lib.MultiColumnListView;
import com.huewu.pla.lib.MultiColumnPullToRefreshListView;
import com.huewu.pla.lib.MultiColumnPullToRefreshListView.OnRefreshListener;
import com.huewu.pla.lib.internal.PLA_AbsListView;
import com.huewu.pla.sample.entity.ImgInfo;
import com.huewu.pla.sample.util.Helper;

public class SampleActivity extends Activity implements OnRefreshListener {

	private MultiColumnPullToRefreshListView mAdapterView = null;
	private StaggeredAdapter mAdapter = null;
	private ContentTask mTask = new ContentTask(this);
	private String pushTime = "0";
	private List<String> list;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sample_act);
		// mAdapterView = (PLA_AdapterView<Adapter>) findViewById(R.id.list);
		list = new ArrayList<String>();
		mAdapterView = (MultiColumnPullToRefreshListView) findViewById(R.id.list);

		initAdapter();
		// {
		// for (int i = 0; i < 3; ++i) {
		// // add header view.
		// TextView tv = new TextView(this);
		// tv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
		// LayoutParams.WRAP_CONTENT));
		// tv.setText("Hello Header!! ........................................................................");
		// mAdapterView.addHeaderView(tv);
		// }
		// // }
		// // {
		// for (int i = 0; i < 3; ++i) {
		// // add footer view.
		// TextView tv = new TextView(this);
		// tv.setLayoutParams(new PLA_AbsListView.LayoutParams(
		// LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		// tv.setText("Hello Footer!! ........................................................................");
		// mAdapterView.addFooterView(tv);
		// }
		mAdapterView.setAdapter(mAdapter);
		addItemToContainer();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// mAdapterView.setAdapter(mAdapter);
	}

	private Random mRand = new Random();

	private void initAdapter() {
		mAdapter = new StaggeredAdapter(this);
		mAdapterView.setOnRefreshListener(this);
	}

	@SuppressWarnings("deprecation")
	private void addItemToContainer() {
		if (mTask.getStatus() != Status.RUNNING && !list.contains(pushTime)
				&& list.size() < 5) {
			list.add(pushTime);
			pushTime = URLEncoder.encode(pushTime);
			String refresh = "2";
			if ("0".equals(pushTime)) {
				refresh = "1";
			}
			String url = "http://10.76.161.1/photo!topiclist.open?app_version=2.0.3&brand=HTC&model=HTC%2B802d&"
					+ "refresh="
					+ refresh
					+ "&push_time="
					+ pushTime
					+ "&app_code=28&os_version=REL&sign=A76BDD7998EA8F8C7227A909467BE098";
			Log.d("MainActivity", "current url:" + url);
			ContentTask task = new ContentTask(this);
			task.execute(url);
		}
	}

	private class ContentTask extends AsyncTask<String, Integer, List<ImgInfo>> {

		private Context mContext;

		public ContentTask(Context context) {
			super();
			mContext = context;
		}

		@Override
		protected List<ImgInfo> doInBackground(String... params) {
			try {
				return parseNewsJSON(params[0]);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(List<ImgInfo> result) {
			mAdapter.addData(result);
//			mAdapterView.setRefreshing();
			// addItemToContainer();
		}

		@Override
		protected void onPreExecute() {
		}

		public List<ImgInfo> parseNewsJSON(String url) throws IOException {
			List<ImgInfo> duitangs = new ArrayList<ImgInfo>();
			String json = "";
			if (Helper.checkConnection(mContext)) {
				try {
					json = Helper.getStringFromUrl(url);

				} catch (IOException e) {
					Log.e("IOException is : ", e.toString());
					e.printStackTrace();
					return duitangs;
				}
			}
			Log.d("MainActiivty", "json:" + json);

			try {
				if (null != json) {
					JSONObject newsObject = new JSONObject(json);
					JSONObject jsonObject = newsObject.getJSONObject("result");
					JSONArray blogsJson = jsonObject.getJSONArray("topiclist");

					for (int i = 0; i < blogsJson.length(); i++) {
						JSONObject newsInfoLeftObject = blogsJson
								.getJSONObject(i);
						ImgInfo newsInfo1 = new ImgInfo();
						newsInfo1.setAlbid(newsInfoLeftObject.isNull("id") ? ""
								: newsInfoLeftObject.getString("id"));
						newsInfo1
								.setUrl(newsInfoLeftObject.isNull("cover") ? ""
										: newsInfoLeftObject.getString("cover"));
						newsInfo1.setMsg(newsInfoLeftObject
								.isNull("description") ? ""
								: newsInfoLeftObject.getString("description"));
						newsInfo1.setHeight(newsInfoLeftObject
								.getInt("cover_heigh"));
						newsInfo1.setWidth(newsInfoLeftObject
								.getInt("cover_width"));
						pushTime = newsInfoLeftObject.getString("create_time");
						duitangs.add(newsInfo1);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return duitangs;
		}
	}

	@Override
	public void onRefresh() {
		System.out.println();
	}

}// end of class
