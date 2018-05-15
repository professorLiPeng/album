package com.example.mchenys.myalbumlist;

import android.content.AsyncQueryHandler;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.mchenys.myalbumlist.adapter.AlbumAdapter;
import com.example.mchenys.myalbumlist.model.AlbumBean;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView mListView;
    private List<AlbumBean> mAlbumBeanList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        mListView = new ListView(this);
        mListView.setCacheColorHint(Color.TRANSPARENT);
        mListView.setSelector(new ColorDrawable());
        setContentView(mListView);
        queryAlbum();
        initListener();
    }

    /**
     * 查询相片
     */
    private void queryAlbum() {
        AsyncQueryHandler queryHandler = new AsyncQueryHandler(getContentResolver()) {
            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                //查询结束时回调,这里回调的时候是UI线程
                mAlbumBeanList = AlbumBean.parserList(cursor);
                AlbumAdapter adapter = new AlbumAdapter(MainActivity.this, mAlbumBeanList);
                mListView.setAdapter(adapter);
            }
        };
        int token = 0; //相当于message的what
        Object cookie = null;//相当于message的obj
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;//查询的uri
        String[] projection = new String[]{ //查询的列
                MediaStore.Images.Media._ID, //如果要使用CursorAdapter,那么就必须查询此字段
                MediaStore.Images.Media.DATA //相片的路径
        };
        String selection = MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?";//查询的条件
        String[] selectionArgs = new String[]{ //查询条件?号后面的参数
                "image/jpeg", "image/png"
        };
        String orderBy = MediaStore.Images.Media.DATE_MODIFIED + " DESC";//根据修改日期降序排序
        // 这个方法会运行在子线程
        queryHandler.startQuery(token, cookie, uri, projection, selection, selectionArgs, orderBy);
    }

    /**
     * 初始化监听
     */
    private void initListener() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlbumBean albumBean = mAlbumBeanList.get(position);
                Intent intent = new Intent(MainActivity.this, PhotoListActivity.class);
                Bundle bundle = new Bundle();
                if (position == 0) {
                    //当前点击的是所有相册
                    bundle.putStringArrayList("photoList", (ArrayList<String>) albumBean.allPhotoList);
                } else {
                    //点击的是其他相册
                    bundle.putStringArrayList("photoList", (ArrayList<String>) albumBean.albumPhotoList);
                }
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }
}
