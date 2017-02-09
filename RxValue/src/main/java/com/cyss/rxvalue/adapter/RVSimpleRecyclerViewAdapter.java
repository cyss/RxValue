package com.cyss.rxvalue.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenyang on 2017/2/8.
 */

public class RVSimpleRecyclerViewAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private List mDataSource = new ArrayList();

    public RVSimpleRecyclerViewAdapter(List dataSource) {
        if (dataSource != null) this.mDataSource = dataSource;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
    }

    @Override
    public int getItemCount() {
        return mDataSource.size();
    }

    public List getDataSource() {
        return mDataSource;
    }

    public void setDataSource(List mDataSource) {
        this.mDataSource = mDataSource;
    }
}
