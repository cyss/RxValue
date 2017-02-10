package com.cyss.rxvalue.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.cyss.rxvalue.RxValue;
import com.cyss.rxvalue.RxValueList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Created by chenyang on 2017/2/9.
 */

public class RVSimpleRecyclerViewAdapter<T> extends RVBaseRecyclerViewAdapter<T, RVSimpleRecyclerViewAdapter.RVSimpleViewHolder> {

    private int itemLayout;

    public RVSimpleRecyclerViewAdapter(Context context, int itemLayout, List<T> dataSource) {
        super(context, dataSource);
        this.itemLayout = itemLayout;
    }

    @Override
    public RVSimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(this.itemLayout, parent, false);
        RVSimpleRecyclerViewAdapter.RVSimpleViewHolder holder = new RVSimpleRecyclerViewAdapter.RVSimpleViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(final RVSimpleRecyclerViewAdapter.RVSimpleViewHolder holder, final int position) {
        final T param = getDataSource().get(position);
        if (rxValueList != null) {
            RxValueList.OnFillItemViewListener beforeListener = rxValueList.getBeforeFillView();
            if (beforeListener != null) beforeListener.action(holder, position, param);
        }
        holder.rxValue.withFillObj(param).fillView(holder.holderViews.values());
        if (rxValueList != null) {
            RxValueList.OnFillItemViewListener afterListener = rxValueList.getBeforeFillView();
            if (afterListener != null) afterListener.action(holder, position, param);
            final RxValueList.OnItemClickListener itemClick = rxValueList.getItemClick();
            if (itemClick != null) {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        itemClick.click(RVSimpleRecyclerViewAdapter.this, holder, position, param);
                    }
                });
            }
            Map<Integer, RxValueList.OnViewClickListener> map = rxValueList.getViewClickMap();
            for (final Map.Entry<Integer, RxValueList.OnViewClickListener> item : map.entrySet()) {
                View v = (View) holder.holderViews.get(item.getKey());
                if (v != null) v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        item.getValue().click(RVSimpleRecyclerViewAdapter.this, holder, view, param);
                    }
                });
            }
        }
    }

    public class RVSimpleViewHolder extends RecyclerView.ViewHolder {

//        public List<View> holderViews = new ArrayList<>();
        public Map<Integer, View> holderViews = new HashMap<>();
        public RxValue<T> rxValue;

        public RVSimpleViewHolder(View itemView) {
            super(itemView);
            initView();
        }

        private void initView() {
            findView(itemView).subscribe(new Subscriber<View>() {
                @Override
                public void onCompleted() {
                    rxValue = RxValue.create(mContext);
                    if (rxValueList != null) rxValue.setBuilder(rxValueList);
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
}
