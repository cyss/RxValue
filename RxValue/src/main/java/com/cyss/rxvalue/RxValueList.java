package com.cyss.rxvalue;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.cyss.rxvalue.adapter.RVSimpleRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by chenyang on 2017/2/8.
 */

public class RxValueList extends RxValueBuilder<List, RxValueList> implements CustomFillAction<RecyclerView> {

    public static final int MODE_SIMPLE = 100;
    public static final int MODE_MULTIPLE = 101;
    public static final int MODE_CUSTOM = 102;

    private List<Integer> itemLayouts = new ArrayList<>();
    private int mode = MODE_CUSTOM;
    private OnItemClickListener itemClick;
    private Map<Integer, OnViewClickListener> viewClickMap = new HashMap<>();
    private RecyclerView.Adapter adapter;
    private OnFillItemViewListener beforeFillView;
    private OnFillItemViewListener afterFillView;

    private RxValueList() {}

    public static RxValueList create() {
        RxValueList rxValueList = new RxValueList();
        return rxValueList;
    }

    public RxValueList itemLayout(int layoutId) {
        this.itemLayouts.add(layoutId);
        return this;
    }

    public RxValueList itemClick(OnItemClickListener clickListener) {
        this.itemClick = clickListener;
        return this;
    }

    public RxValueList addViewClick(int viewId, OnViewClickListener clickListener) {
        viewClickMap.put(viewId, clickListener);
        return this;
    }

    public RxValueList withMode(int mode) {
        this.mode = mode;
        return this;
    }

    public RxValueList withAdapter(RecyclerView.Adapter adapter) {
        this.adapter = adapter;
        return this;
    }

    public RxValueList withBeforeFillItemView(OnFillItemViewListener fillItemViewListener) {
        this.beforeFillView = fillItemViewListener;
        return this;
    }

    public RxValueList withAfterFillItemView(OnFillItemViewListener fillItemViewListener) {
        this.afterFillView = fillItemViewListener;
        return this;
    }

    /**
     * 获取设置每一项xml layout的id
     * @return
     */
    public List<Integer> getItemLayouts() {
        return itemLayouts;
    }

    public int getMode() {
        return mode;
    }

    public OnItemClickListener getItemClick() {
        return itemClick;
    }

    public Map<Integer, OnViewClickListener> getViewClickMap() {
        return viewClickMap;
    }

    public RecyclerView.Adapter getAdapter() {
        return adapter;
    }

    public OnFillItemViewListener getBeforeFillView() {
        return beforeFillView;
    }

    public OnFillItemViewListener getAfterFillView() {
        return afterFillView;
    }

    @Override
    public void action1(Context context, RecyclerView view, Object obj, RxValueBuilder builder) {
        if (mode == MODE_SIMPLE) {
            if (obj instanceof List) {
                if (adapter == null) {
                    adapter = new RVSimpleRecyclerViewAdapter(context, itemLayouts.get(itemLayouts.size() - 1), (List)obj);
                }
                RVSimpleRecyclerViewAdapter rxAdapter = (RVSimpleRecyclerViewAdapter) adapter;
                rxAdapter.setRxValueList(this);
                view.setAdapter(rxAdapter);
            }
        } if (adapter != null) {
            view.setAdapter(adapter);
        }
    }

    @Override
    public Object action2(Context context, RecyclerView view, RxValueBuilder builder) {
        RecyclerView.Adapter adapter = view.getAdapter();
        if (adapter instanceof RVSimpleRecyclerViewAdapter) {
            return ((RVSimpleRecyclerViewAdapter) adapter).getDataSource();
        } else {

        }
        return null;
    }

    public interface OnFillItemViewListener {
        public void action(RecyclerView.ViewHolder viewHolder, int position, Object obj);
    }

    public interface OnItemClickListener<T> {
        public void click(RecyclerView.Adapter adapter, RecyclerView.ViewHolder viewHolder, int position, T obj);
    }

    public interface OnViewClickListener<T> {
        public void click(RecyclerView.Adapter adapter, RecyclerView.ViewHolder viewHolder, View view, T obj);
    }
}
