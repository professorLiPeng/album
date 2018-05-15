package com.example.mchenys.myalbumlist;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.mchenys.myalbumlist.utils.SizeUtils;
import com.lidroid.xutils.BitmapUtils;

import java.util.List;

/**
 * Created by mChenys on 2016/1/24.
 */
public class PhotoListActivity extends Activity {

    private GridView mGridView;
    private List<String> mPhotoList;
    private BitmapUtils mBitmapUtils;
    private int mWidth, mHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        initView();
        showPhotoList();
        initListener();
    }

    private void initListener() {
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(PhotoListActivity.this, PhotoViewActivity.class);
                intent.putExtra("photo", mPhotoList.get(position));
                startActivity(intent);
            }
        });
    }

    private void initData() {
        Intent intent = getIntent();
        if (null != intent) {
            Bundle bundle = intent.getExtras();
            if (null != bundle) {
                mPhotoList = bundle.getStringArrayList("photoList");
                System.out.println("mPhotoList:" + mPhotoList);
            }
        }
        mBitmapUtils = new BitmapUtils(this);
        mBitmapUtils.configDefaultLoadingImage(R.drawable.app_default);
        mWidth = (int) ((SizeUtils.getStreenWidth(this) - 2 * SizeUtils.px2dp(this, 1)) / 3.0f);
        mHeight = mWidth;
        System.out.println("mWidth:" + mWidth + " mHeight:" + mHeight + " SizeUtils.getStreenWidth(this):" +
                SizeUtils.getStreenWidth(this));
    }

    private void initView() {
        mGridView = new GridView(this);
        mGridView.setNumColumns(3);
        mGridView.setVerticalSpacing(SizeUtils.px2dp(this, 1));
        mGridView.setHorizontalSpacing(SizeUtils.px2dp(this, 1));
        mGridView.setCacheColorHint(Color.TRANSPARENT);
        mGridView.setBackgroundColor(Color.parseColor("#262424"));
        mGridView.setSelector(new ColorDrawable());
        setContentView(mGridView);
    }

    private void showPhotoList() {
        mGridView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return mPhotoList == null ? 0 : mPhotoList.size();
            }

            @Override
            public String getItem(int position) {
                return mPhotoList == null ? "" : mPhotoList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (null == convertView) {
                    ImageView imageView = new ImageView(PhotoListActivity.this);
                    imageView.setLayoutParams(new AbsListView.LayoutParams(mWidth, mHeight));
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    convertView = imageView;
                }
                ImageView imageView = (ImageView) convertView;
                mBitmapUtils.display(imageView, getItem(position));
                return convertView;
            }
        });
    }
}
