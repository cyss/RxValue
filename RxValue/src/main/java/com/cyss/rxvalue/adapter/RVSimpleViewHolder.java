package com.cyss.rxvalue.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.cyss.rxvalue.RxValue;
import com.cyss.rxvalue.RxValueList;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Created by chenyang on 2017/2/13.
 */

public class RVSimpleViewHolder<T> extends RecyclerView.ViewHolder {
    public Map<Integer, View> holderViews = new HashMap<>();
    public RxValue<T> rxValue;
    public Context mContext;
    public RxValueList rxValueList;

    public RVSimpleViewHolder(View itemView, Context context, RxValueList rxValueList) {
        super(itemView);
        this.mContext = context;
        this.rxValueList = rxValueList;
        initView();
    }

    private void initView() {
        findView(itemView).subscribe(new Subscriber<View>() {
            @Override
            public void onCompleted() {
                rxValue = RxValue.create(mContext);
                rxValue.setBuilder(rxValueList);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(View view) {
                holderViews.put(view.getId(), view);
            }
        });
    }

    private Observable<View> findView(View v) {
        if (v instanceof ViewGroup) {
            final ViewGroup vp = (ViewGroup) v;
            return Observable.range(0, vp.getChildCount())
                    .filter(new Func1<Integer, Boolean>() {
                        @Override
                        public Boolean call(Integer i) {
                            View view = vp.getChildAt(i);
                            return view instanceof ViewGroup || (view.getId() != -1 && view != null);
                        }
                    })
                    .flatMap(new Func1<Integer, Observable<View>>() {
                        @Override
                        public Observable<View> call(Integer i) {
                            View view = vp.getChildAt(i);
                            if (view instanceof ViewGroup) {
                                Observable<View> childView = findView(view);
                                return Observable.from(new View[]{}).concatWith(childView);
                            } else {
                                return Observable.just(view);
                            }
                        }
                    });
        } else {
            return Observable.just(v);
        }
    }
}
