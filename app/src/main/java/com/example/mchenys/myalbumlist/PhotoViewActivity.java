package com.example.mchenys.myalbumlist;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.mchenys.myalbumlist.widget.ZoomImageView;

/**
 * Created by mChenys on 2016/1/24.
 */
public class PhotoViewActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        String photoPath = getIntent().getStringExtra("photo");
        RelativeLayout rootView = new RelativeLayout(this);

        //创建TextView显示照片的路径
        TextView textView = new TextView(this);
        RelativeLayout.LayoutParams tvLp = new RelativeLayout.LayoutParams(-1, -2);
        tvLp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        textView.setLayoutParams(tvLp);
        textView.setText(photoPath);
        textView.setTextColor(Color.WHITE);

        //创建支持缩放的ImageView
        RelativeLayout.LayoutParams ivLp = new RelativeLayout.LayoutParams(-1, -1);
        ZoomImageView zoomImageView = new ZoomImageView(this);
        zoomImageView.setLayoutParams(ivLp);
        zoomImageView.setImageBitmap(BitmapFactory.decodeFile(photoPath));

        //添加ImageView
        rootView.addView(zoomImageView);
        //添加TextView
        rootView.addView(textView);
        setContentView(rootView);
    }
}
