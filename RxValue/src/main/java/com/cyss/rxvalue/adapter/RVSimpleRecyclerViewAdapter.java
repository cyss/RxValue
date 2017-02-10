package com.cyss.rxvalue.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cyss.rxvalue.RxValue;
import com.cyss.rxvalue.RxValueList;

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

    private Map<Integer, Integer> itemLayout;
    private int defaultItemLayoutId = -1;

    public RVSimpleRecyclerViewAdapter(Context context, Map<Integer, Integer> itemLayout, List<T> dataSource) {
        super(context, dataSource);
        this.itemLayout = itemLayout;
    }

    public RVSimpleRecyclerViewAdapter(Context context, List<T> dataSource) {
        super(context, dataSource);
    }

    @Override
    public RVSimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int itemLayoutId = -1;
        if (rxValueList != null) {
            if (itemLayout == null || itemLayout.isEmpty()) {
                if (defaultItemLayoutId == -1) defaultItemLayoutId = rxValueList.getDefaultListItemId();
                itemLayoutId = defaultItemLayoutId;
            } else {
                if (itemLayout.containsKey(viewType)) {
                    itemLayoutId = itemLayout.get(viewType);
                } else if (itemLayout.containsKey(RxValueList.DEFAULT_ITEM_LAYOUT)){
                    itemLayoutId = itemLayout.get(RxValueList.DEFAULT_ITEM_LAYOUT);
                }
            }
        }
        if (itemLayoutId == -1) {
            throw new RuntimeException("====>RxValue Error: can't find item layout");
        }
        View v = LayoutInflater.from(mContext).inflate(itemLayoutId, parent, false);
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

    @Override
    public int getItemViewType(int position) {
        final T param = getDataSource().get(position);
        if (rxValueList != null) {
            RxValueList.OnViewTypeListener viewTypeListener = rxValueList.getViewTypeListener();
            if (viewTypeListener != null) return viewTypeListener.viewType(position, param);
        }
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
//        Map<Integer, Integer> appendCountMap = rxValueList.getAppendCountMap();
//        if (rxValueList != null && !appendCountMap.isEmpty()) {
//            int appendCount = 0;
//            for (Integer count: appendCountMap.values()) {
//                appendCount += count;
//            }
//            return super.getItemCount() + appendCount;
//        }
        return super.getItemCount();
    }

//    private T getData(int position) {
//        if (rxValueList != null) {
//            Map<Integer, Integer> appendCountMap = rxValueList.getAppendCountMap();
//            if (!appendCountMap.isEmpty()) {
//                for (Map.Entry<Integer, Integer> item: appendCountMap.entrySet()) {
//                    int startIndex = item.getKey();
//                    if (position > startIndex && position <= startIndex + item.getValue()) {
//                        return null;
//                    }
//                }
//            }
//        }
//        return getDataSource().get(position);
//    }

    public class RVSimpleViewHolder extends RecyclerView.ViewHolder {
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
