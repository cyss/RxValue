package com.cyss.rxvalue;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.cyss.rxvalue.adapter.RVSimpleListViewAdapter;
import com.cyss.rxvalue.adapter.RVSimpleRecyclerViewAdapter;

import java.io.Serializable;
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

public class RxValueList extends RxValueBuilder<List, RxValueList> implements CustomFillAction<ViewGroup> {

    public static final int MODE_SIMPLE = 100;
    public static final int MODE_MULTIPLE = 101;
    public static final int MODE_CUSTOM = 102;
    public static final int DEFAULT_ITEM_LAYOUT = -1991;

    private Map<Integer, Integer> itemLayouts = new HashMap<>();
    private int mode = MODE_CUSTOM;
    private OnItemClickListener itemClick;
    private Map<Integer, OnViewClickListener> viewClickMap = new HashMap<>();
    private RecyclerView.Adapter adapter;
    private BaseAdapter listViewAdapter;
    private OnFillItemViewListener beforeFillView;
    private OnFillItemViewListener afterFillView;
    private int listId = -1;
    private OnViewTypeListener viewTypeListener;
    private Map<Integer, Integer> appendCountMap = new HashMap<>();

    private RxValueList() {}

    public static RxValueList create() {
        RxValueList rxValueList = new RxValueList();
        return rxValueList;
    }

    public RxValueList itemLayout(int layoutId) {
        this.itemLayouts.put(DEFAULT_ITEM_LAYOUT, layoutId);
        return this;
    }

    public RxValueList itemLayout(int viewType, int layoutId) {
        this.itemLayouts.put(viewType, layoutId);
        return this;
    }

    public RxValueList itemClick(OnItemClickListener clickListener) {
        this.itemClick = clickListener;
        return this;
    }

    public RxValueList addAppendCount(int startIndex, int count) {
        appendCountMap.put(startIndex, count);
        return this;
    }

    public RxValueList removeAppendCount(int startIndex) {
        appendCountMap.remove(startIndex);
        return this;
    }

    public RxValueList clearAppendCount() {
        appendCountMap.clear();
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

    public RxValueList withAdapter(BaseAdapter adapter) {
        this.listViewAdapter = adapter;
        return this;
    }

    public RxValueList viewTypeSetting(OnViewTypeListener viewTypeListener) {
        this.viewTypeListener = viewTypeListener;
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
    public Map<Integer, Integer> getItemLayouts() {
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

    public RecyclerView.Adapter getRecyclerViewAdapter() {
        return adapter;
    }

    public BaseAdapter getListViewAdapter() {
        return listViewAdapter;
    }

    public OnFillItemViewListener getBeforeFillView() {
        return beforeFillView;
    }

    public OnFillItemViewListener getAfterFillView() {
        return afterFillView;
    }

    public int getListId() {
        return listId;
    }

    public OnViewTypeListener getViewTypeListener() {
        return viewTypeListener;
    }

    public Map<Integer, Integer> getAppendCountMap() {
        return appendCountMap;
    }

    public int getDefaultListItemId() {
        String name = RxValue.getNameById(this.listId);
        if (name == null) return -1;
        String layoutName = "list_item_" + name;
        Log.i(getClass().getName(), "====>RxValue info:default item layout '" + layoutName + ".xml'" );
        return RxValue.getLayoutIdByName(layoutName);
    }

    @Override
    public void action1(Context context, ViewGroup view, Object obj, RxValueBuilder builder) {
        this.listId = view.getId();
        if (view instanceof RecyclerView) {
            RecyclerView recyclerView = (RecyclerView) view;
            if (mode == MODE_SIMPLE || mode == MODE_MULTIPLE) {
                if (obj instanceof List) {
                    if (!itemLayouts.isEmpty() && !itemLayouts.containsKey(DEFAULT_ITEM_LAYOUT) && viewTypeListener == null) {
                        throw new RuntimeException("You need call viewTypeSetting() to set item's type.");
                    }
                    adapter = new RVSimpleRecyclerViewAdapter(context, RxValueList.this, (List)obj);
                    recyclerView.setAdapter(adapter);
                }
            } else if (mode == MODE_CUSTOM) {
                if (adapter != null) {
                    recyclerView.setAdapter(adapter);
                }
            }
        } else if (view instanceof ListView) {
            ListView listView = (ListView) view;
            if (mode == MODE_SIMPLE || mode == MODE_MULTIPLE) {
                if (obj instanceof List) {
                    if (!itemLayouts.isEmpty() && !itemLayouts.containsKey(DEFAULT_ITEM_LAYOUT) && viewTypeListener == null) {
                        throw new RuntimeException("You need call viewTypeSetting() to setting.");
                    }
                    listViewAdapter = new RVSimpleListViewAdapter(context, RxValueList.this, (List)obj);
                    listView.setAdapter(listViewAdapter);
                }
            } else if (mode == MODE_CUSTOM) {
                if (adapter != null) {
                    listView.setAdapter(listViewAdapter);
                }
            }
        }
    }

    @Override
    public Object action2(Context context, ViewGroup view, RxValueBuilder builder) {
        if (view instanceof RecyclerView) {
            RecyclerView recyclerView = (RecyclerView) view;
            RecyclerView.Adapter adapter = recyclerView.getAdapter();
            if (adapter instanceof RVSimpleRecyclerViewAdapter) {
                return ((RVSimpleRecyclerViewAdapter) adapter).getDataSource();
            }
        } else if (view instanceof ListView) {
            ListView listView = (ListView) view;
            ListAdapter adapter = listView.getAdapter();
            if (adapter instanceof RVSimpleListViewAdapter) {
                return ((RVSimpleListViewAdapter) adapter).getDataSource();
            }
        }
        return null;
    }

    public interface OnFillItemViewListener {
        void action(RecyclerView.ViewHolder viewHolder, int position, Object obj);
    }

    public interface OnItemClickListener<T> {
        void click(RecyclerView.ViewHolder viewHolder, int position, T obj);
    }

    public interface OnViewClickListener<T> {
        void click(RecyclerView.ViewHolder viewHolder, View view, T obj);
    }

    public interface OnViewTypeListener<T> {
        int viewType(int position, T obj);
    }
}
