package com.cyss.rxvalue.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.cyss.rxvalue.RxValueList;

import java.util.List;

/**
 * Created by chenyang on 2017/2/13.
 */

public abstract class RVBaseListViewAdapter<T> extends BaseAdapter {

    protected List<T> mDataSource;
    protected Context mContext;
    protected RxValueList rxValueList;

    public RVBaseListViewAdapter(Context context) {
        this.mContext = context;
    }

    public RVBaseListViewAdapter(Context context, List<T> dataSource) {
        this(context);
        this.mDataSource = dataSource;
    }

    @Override
    public int getCount() {
        return mDataSource.size();
    }

    @Override
    public T getItem(int i) {
        return mDataSource.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public List<T> getDataSource() {
        return mDataSource;
    }

    public void setDataSource(List<T> mDataSource) {
        this.mDataSource = mDataSource;
    }

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context mContext) {
        this.mContext = mContext;
    }

    public RxValueList getRxValueList() {
        return rxValueList;
    }

    public void setRxValueList(RxValueList rxValueList) {
        this.rxValueList = rxValueList;
    }

    public class ViewHolder {
        public View itemView;
        public ViewHolder(View itemView) {
            this.itemView = itemView;
        }
    }
}
