package com.example.mchenys.myalbumlist.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mchenys.myalbumlist.R;
import com.example.mchenys.myalbumlist.model.AlbumBean;
import com.lidroid.xutils.BitmapUtils;

import java.util.List;

/**
 * 相册适配器
 * Created by mChenys on 2016/1/24.
 */
public class AlbumAdapter extends BaseAdapter {
    List<AlbumBean> mAlbumBeanList;
    Context mContext;
    BitmapUtils mBitmapUtils;

    public AlbumAdapter(Context context, List<AlbumBean> albumBeanList) {
        mAlbumBeanList = albumBeanList;
        mContext = context;
        mBitmapUtils = new BitmapUtils(context);
        mBitmapUtils.configDefaultLoadingImage(R.drawable.app_default);
    }

    @Override
    public int getCount() {
        return mAlbumBeanList == null ? 0 : mAlbumBeanList.size();
    }

    @Override
    public AlbumBean getItem(int position) {
        return mAlbumBeanList == null ? null : mAlbumBeanList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (null == convertView) {
            holder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.item_album_list, null);
            holder.albumIv = (ImageView) convertView.findViewById(R.id.iv_album);
            holder.nameTv = (TextView) convertView.findViewById(R.id.tv_name);
            holder.numTv = (TextView) convertView.findViewById(R.id.tv_num);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        System.out.println("firstPic=" + getItem(position).firstPic +
                " ,num=" + getItem(position).photoNum +
                " ,name=" + getItem(position).albumName);

        if (position == 0) {
            //显示所有相片的相册
            List<String> allPhotoList = getItem(position).allPhotoList;
            if (null != allPhotoList && allPhotoList.size() > 0 && !TextUtils.isEmpty(allPhotoList.get(0))) {
                mBitmapUtils.display(holder.albumIv, allPhotoList.get(0));
                holder.numTv.setText(String.valueOf(allPhotoList.size()) + "张");
                holder.nameTv.setText("所有相片");
            }
        } else {
            //显示其他相册
            mBitmapUtils.display(holder.albumIv, getItem(position).firstPic);
            holder.numTv.setText(getItem(position).photoNum + "张");
            holder.nameTv.setText(getItem(position).albumName);
        }
        return convertView;
    }

    class ViewHolder {
        TextView nameTv, numTv;
        ImageView albumIv;
    }
}
