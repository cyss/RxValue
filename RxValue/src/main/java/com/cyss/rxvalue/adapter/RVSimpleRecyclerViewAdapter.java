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

public class RVSimpleRecyclerViewAdapter<T> extends RVBaseRecyclerViewAdapter<T, RVSimpleViewHolder> {

    private Map<Integer, Integer> itemLayout;
    private int defaultItemLayoutId = -1;

    public RVSimpleRecyclerViewAdapter(Context context, RxValueList rxValueList, List<T> dataSource) {
        super(context, dataSource);
        setRxValueList(rxValueList);
        this.itemLayout = rxValueList.getItemLayouts();
    }

    @Override
    public RVSimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int itemLayoutId = getItemLayout(viewType);
        if (itemLayoutId == -1) {
            throw new RuntimeException("====>RxValue Error: can't find item layout");
        }
        View v = LayoutInflater.from(mContext).inflate(itemLayoutId, parent, false);
        RVSimpleViewHolder holder = new RVSimpleViewHolder(v, mContext, rxValueList);
        return holder;
    }

    @Override
    public void onBindViewHolder(final RVSimpleViewHolder holder, final int position) {
        final T param = getDataSource().get(position);
        int viewType = getItemViewType(position);
        RxValueList.OnFillItemViewListener beforeListener = rxValueList.getBeforeFillView();
        if (beforeListener != null) beforeListener.action(holder, position, param);
        holder.rxValue.withFillObj(param).layoutId(getItemLayout(viewType));
        holder.rxValue.fillView(holder.holderViews.values());
        RxValueList.OnFillItemViewListener afterListener = rxValueList.getAfterFillView();
        if (afterListener != null) afterListener.action(holder, position, param);
        final RxValueList.OnItemClickListener itemClick = rxValueList.getItemClick();
        if (itemClick != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemClick.click(holder, position, param);
                }
            });
        }
        Map<Integer, RxValueList.OnViewClickListener> map = rxValueList.getViewClickMap();
        for (final Map.Entry<Integer, RxValueList.OnViewClickListener> item : map.entrySet()) {
            View v = (View) holder.holderViews.get(item.getKey());
            if (v != null) v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    item.getValue().click(holder, view, param);
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        final T param = getDataSource().get(position);
        RxValueList.OnViewTypeListener viewTypeListener = rxValueList.getViewTypeListener();
        if (viewTypeListener != null) return viewTypeListener.viewType(position, param);
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

    private int getItemLayout(int viewType) {
        int itemLayoutId = -1;
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
        return itemLayoutId;
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
}
