package com.xy.wanandroid.util.glide;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.xy.wanandroid.base.app.MyApplication;
import com.youth.banner.loader.ImageLoader;

/**
 * Created by jxy on 2018/6/12.
 */

public class GlideImageLoader extends ImageLoader {
    @Override
    public void displayImage(Context context, Object path, ImageView imageView) {
        Glide.with(MyApplication.getInstance()).load(path).into(imageView);
    }
}
