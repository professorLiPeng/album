package com.example.mchenys.myalbumlist.model;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 相册bean
 * Created by mChenys on 2016/1/24.
 */
public class AlbumBean implements Parcelable {
    public String albumName; //相册的名称
    public String albumPath; //相册的路径
    public String firstPic; //相册封面图
    public String photoNum; //相片的个数
    public List<String> albumPhotoList;//每个相册内的相片集合
    public List<String> allPhotoList; //所有相片

    public AlbumBean() {
    }

    protected AlbumBean(Parcel in) {
        albumName = in.readString();
        albumPath = in.readString();
        firstPic = in.readString();
        photoNum = in.readString();
        albumPhotoList = in.createStringArrayList();
        allPhotoList = in.createStringArrayList();
    }

    public static final Creator<AlbumBean> CREATOR = new Creator<AlbumBean>() {
        @Override
        public AlbumBean createFromParcel(Parcel in) {
            return new AlbumBean(in);
        }

        @Override
        public AlbumBean[] newArray(int size) {
            return new AlbumBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(albumName);
        dest.writeString(albumPath);
        dest.writeString(firstPic);
        dest.writeString(photoNum);
        dest.writeStringList(albumPhotoList);
        dest.writeStringList(allPhotoList);
    }

    /**
     * 获取相册的所有相片
     *
     * @param albumPath
     * @return
     */
    public static List<String> parserPhotoList(String albumPath) {
        if (!TextUtils.isEmpty(albumPath)) {
            //相册
            File albumFile = new File(albumPath);
            if (null != albumFile && albumFile.exists() && albumFile.isDirectory()) {
                List<File> fileList = new ArrayList<>();//每个相册内的相片集合
                List<String> albumPhotoList = new ArrayList<>();
                //遍历整个相册所在的目录,拿到符合条件的相片
                for (File photo : albumFile.listFiles(mFileFilter)) {
                    if (null != photo && photo.exists() && photo.isFile()) {
                        fileList.add(photo);
                    }
                }
                if (fileList.size() > 0) {
                    //对相册内的相片进行按修改时间倒序排序
                    Collections.sort(fileList, new Comparator<File>() {
                        @Override
                        public int compare(File lhs, File rhs) {
                            if (lhs.lastModified() > rhs.lastModified()) {
                                return -1;//最后修改的照片放在集合前面
                            } else if (lhs.lastModified() < rhs.lastModified()) {
                                return 1;
                            } else {
                                return 0;
                            }
                        }
                    });
                    //获取文件的绝对路径保存到集合中
                    for (File file : fileList) {
                        albumPhotoList.add(file.getAbsolutePath());
                    }
                }
                return albumPhotoList;
            }
        }
        return null;
    }

    /**
     * 根据cursor解析所有的相册
     *
     * @param cursor
     * @return
     */
    public static List<AlbumBean> parserList(Cursor cursor) {
        if (null != cursor && cursor.getCount() > 0) {
            cursor.moveToFirst();
            Set<String> albumPathSet = new HashSet<>();//保存所有相册路径的集合,通过HashSet集合可以过滤掉同名的路径
            List<String> allPhotoList = new ArrayList<>();//保存所有相片的集合
            do {
                String photoPath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                if (!TextUtils.isEmpty(photoPath)) {
                    File photoFile = new File(photoPath);
                    if (null != photoFile && photoFile.exists() && photoFile.isFile()) {
                        //保存所有的相片
                        allPhotoList.add(photoFile.getAbsolutePath());
                        //保存所有的相册路径
                        albumPathSet.add(photoFile.getParentFile().getAbsolutePath());
                    }
                }
            } while (cursor.moveToNext());

            //遍历所有的相册,解析到bean中保存
            if (null != albumPathSet && albumPathSet.size() > 0) {
                List<AlbumBean> albumBeanList = new ArrayList<>();
                for (String albumPaht : albumPathSet) {
                    AlbumBean albumBean = new AlbumBean();
                    albumBean.albumPath = albumPaht;//相册的路径
                    albumBean.albumName = albumPaht.substring(albumPaht.lastIndexOf("/") + 1);//相册的名字
                    albumBean.albumPhotoList = parserPhotoList(albumPaht);//相册内相片的集合
                    albumBean.photoNum = String.valueOf(albumBean.albumPhotoList.size());//相册内相片的个数
                    albumBean.allPhotoList = allPhotoList;//手机内所有的相片集合
                    albumBean.firstPic = albumBean.albumPhotoList == null ? "" : albumBean.albumPhotoList.get(0);//相册封面图
                    albumBeanList.add(albumBean);
                }
                //根据相册名字排序
                Collections.sort(albumBeanList, new Comparator<AlbumBean>() {
                    @Override
                    public int compare(AlbumBean lhs, AlbumBean rhs) {
                        return lhs.albumName.compareTo(rhs.albumName);
                    }
                });
                return albumBeanList;
            }
        }
        return null;
    }

    //过滤非png,jpg的相片
    private static FileFilter mFileFilter = new FileFilter() {
        @Override
        public boolean accept(File f) {
            String fileName = f.getName().toLowerCase();
            if (fileName.endsWith(".png") || fileName.endsWith("jpg") || fileName.endsWith("jpeg")) {
                return true;
            }
            return false;
        }
    };


}
