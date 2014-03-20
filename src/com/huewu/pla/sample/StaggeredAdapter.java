package com.huewu.pla.sample;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huewu.pla.R;
import com.huewu.pla.sample.entity.ImgInfo;
import com.huewu.pla.sample.loader.ImageLoader;
import com.huewu.pla.sample.util.DeviceUtil;

public class StaggeredAdapter extends BaseAdapter {

	private ImageLoader mLoader;
	private Context mContext;
	private List<ImgInfo> mObjects;

	public StaggeredAdapter(Context context) {
		mContext = context;
		mLoader = new ImageLoader(context);
		mObjects = new ArrayList<ImgInfo>();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;
		ImgInfo data = (ImgInfo) getItem(position);

		if (convertView == null) {
			LayoutInflater layoutInflator = LayoutInflater.from(mContext);
			convertView = layoutInflator.inflate(R.layout.sample_item, null);
			holder = new ViewHolder();
			holder.imageView = (ImageView) convertView
					.findViewById(R.id.ptr_id_image);
			holder.textView = (TextView) convertView.findViewById(R.id.text1);

			holder.imageView.setBackgroundColor(mContext.getResources()
					.getColor(android.R.color.white));
			convertView.setTag(holder);
		}

		holder = (ViewHolder) convertView.getTag();
		int width = data.getWidth();
		int height = data.getHeight();
		String url = data.getUrl();

		System.out.println("oops...position = " + position + "url = " + url
				+ ", w = " + width + ", h = " + height);
		int picWidth = DeviceUtil.getScreenWidth(mContext) / 2;
		float rate = width * 1.0f / picWidth;
		int picHeight = (int) (height / rate);
		holder.imageView.setLayoutParams(new LinearLayout.LayoutParams(
				picWidth, picHeight));
		holder.imageView.setBackgroundColor(mContext.getResources().getColor(
				android.R.color.black));
		holder.imageView.setScaleType(ScaleType.FIT_XY);
		holder.textView.setText(data.getMsg());
		mLoader.DisplayImage(url, holder.imageView);

		return convertView;
	}

	static class ViewHolder {
		ImageView imageView;
		TextView textView;
	}

	@Override
	public int getCount() {
		if (mObjects != null) {
			return mObjects.size();
		}
		return 0;
	}

	@Override
	public ImgInfo getItem(int position) {
		if (mObjects != null) {
			return mObjects.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void addData(List<ImgInfo> data) {
		mObjects.addAll(data);
		notifyDataSetChanged();
	}
}
