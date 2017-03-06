package com.cyss.rxvalue.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cyss.rxvalue.RxValue;
import com.cyss.rxvalue.RxValueBuilder;
import com.cyss.rxvalue.RxValueList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Created by chenyang on 2017/2/13.
 */

public class RVSimpleListViewAdapter<T> extends RVBaseListViewAdapter<T> {

    private Map<Integer, Integer> itemLayout;
    private int defaultItemLayoutId = -1;
    private LayoutInflater mInflater;
    private RxValueBuilder<Object, RxValue> itemBuilder;

    public RVSimpleListViewAdapter(Context context, RxValueList rxValueList, List dataSource) {
        this(context, rxValueList, null, dataSource);
    }

    public RVSimpleListViewAdapter(Context context, RxValueList rxValueList, RxValueBuilder<Object, RxValue> itemBuilder,  List dataSource) {
        super(context, dataSource);
        setRxValueList(rxValueList);
        itemLayout = rxValueList.getItemLayouts();
        mInflater = LayoutInflater.from(context);
        this.itemBuilder = itemBuilder;
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        RVSimpleViewHolder viewHolder = null;
        if (view == null) {
            int itemLayoutId = getItemLayout(position);
            if (itemLayoutId == -1) {
                throw new RuntimeException("====>RxValue Error: can't find item layout");
            }
            view = mInflater.inflate(itemLayoutId, null);
            viewHolder = new RVSimpleViewHolder(view, mContext, rxValueList);
            view.setTag(viewHolder);
        } else {
            viewHolder = (RVSimpleViewHolder) view.getTag();
        }

        final RVSimpleViewHolder holder = viewHolder;
        final T param = getDataSource().get(position);
        RxValueList.OnFillItemViewListener beforeListener = rxValueList.getBeforeFillView();
        if (beforeListener != null) beforeListener.action(holder, position, param);
        holder.rxValue.setBuilder(itemBuilder).withFillObj(param).layoutId(getItemLayout(position));
        holder.rxValue.fillView(holder.holderViews.values());
        RxValueList.OnFillItemViewListener afterListener = rxValueList.getBeforeFillView();
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
        return view;
    }

    @Override
    public int getViewTypeCount() {
        return itemLayout.size() < 1 ? 1 : itemLayout.size();
    }

    @Override
    public int getItemViewType(int position) {
        final T param = getDataSource().get(position);
        RxValueList.OnViewTypeListener viewTypeListener = rxValueList.getViewTypeListener();
        if (viewTypeListener != null) return viewTypeListener.viewType(position, param);
        return super.getItemViewType(position);
    }

    private int getItemLayout(int position) {
        int itemLayoutId = -1;
        if (itemLayout == null || itemLayout.isEmpty()) {
            if (defaultItemLayoutId == -1) defaultItemLayoutId = rxValueList.getDefaultListItemId();
            itemLayoutId = defaultItemLayoutId;
        } else {
            int viewType = getItemViewType(position);
            if (itemLayout.containsKey(viewType)) {
                itemLayoutId = itemLayout.get(viewType);
            } else if (itemLayout.containsKey(RxValueList.DEFAULT_ITEM_LAYOUT)){
                itemLayoutId = itemLayout.get(RxValueList.DEFAULT_ITEM_LAYOUT);
            }
        }
        return itemLayoutId;
    }
}
