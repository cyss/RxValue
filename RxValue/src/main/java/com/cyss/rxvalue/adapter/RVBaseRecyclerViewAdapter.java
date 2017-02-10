package com.cyss.rxvalue.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;

import com.cyss.rxvalue.RxValueBuilder;
import com.cyss.rxvalue.RxValueList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenyang on 2017/2/8.
 */

public abstract class RVBaseRecyclerViewAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    protected List<T> mDataSource = new ArrayList();
    protected Context mContext;
    protected RxValueList rxValueList;

    public RVBaseRecyclerViewAdapter(Context context) {
        this.mContext = context;
    }

    public RVBaseRecyclerViewAdapter(Context context, List<T> dataSource) {
        this.mContext = context;
        if (dataSource != null) this.mDataSource = dataSource;
    }

    @Override
    public int getItemCount() {
        return mDataSource.size();
    }

    public List<T> getDataSource() {
        return mDataSource;
    }

    public void setDataSource(List<T> mDataSource) {
        this.mDataSource = mDataSource;
    }

    public RxValueList getRxValueList() {
        return rxValueList;
    }

    public void setRxValueList(RxValueList rxValueList) {
        this.rxValueList = rxValueList;
    }
}
