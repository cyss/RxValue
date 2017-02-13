package com.cyss.rxvalue.demo;

import android.app.Application;
import android.content.Context;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cyss.rxvalue.CustomFillAction;
import com.cyss.rxvalue.RxValue;
import com.cyss.rxvalue.RxValueBuilder;
import com.cyss.rxvalue.RxValueList;

/**
 * Created by chenyang on 2017/2/8.
 */

public class RxValueApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        RxValue.init(this);
        RxValue.registerGlobalAction(ImageView.class, new CustomFillAction<ImageView>() {

            @Override
            public void action1(Context context, ImageView view, Object obj, RxValueBuilder builder) {
                Glide.with(context).load(obj).crossFade().centerCrop().into(view);
            }

            @Override
            public Object action2(Context context, ImageView view, RxValueBuilder builder) {
                return view.getTag();
            }
        });
        RxValue.registerGlobalAction(RecyclerView.class, RxValueList.create()
                .withMode(RxValueList.MODE_SIMPLE));
        RxValue.registerGlobalAction(ListView.class, RxValueList.create()
                .withMode(RxValueList.MODE_SIMPLE));
    }
}
