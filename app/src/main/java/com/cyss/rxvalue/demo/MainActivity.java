package com.cyss.rxvalue.demo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mSimpleUse;
    private Button mLimitViewType;
    private Button mFillViewHolder;
    private Button mConvertKey;
    private Button mMultipleLayout;
    private Button mImageGlide;
    private Button mCustomView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
    }

    private void bindViews() {
        mSimpleUse = (Button) findViewById(R.id.simpleUse);
        mLimitViewType = (Button) findViewById(R.id.limitViewType);
        mFillViewHolder = (Button) findViewById(R.id.fillViewHolder);
        mConvertKey = (Button) findViewById(R.id.convertKey);
        mMultipleLayout = (Button) findViewById(R.id.multipleLayout);
        mImageGlide = (Button) findViewById(R.id.imageGlide);
        mCustomView = (Button) findViewById(R.id.customView);

        mSimpleUse.setOnClickListener(this);
        mLimitViewType.setOnClickListener(this);
        mFillViewHolder.setOnClickListener(this);
        mConvertKey.setOnClickListener(this);
        mMultipleLayout.setOnClickListener(this);
        mImageGlide.setOnClickListener(this);
        mCustomView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.simpleUse:
                intent = new Intent(this, SimpleUseActivity.class);
                break;
            case R.id.limitViewType:
                intent = new Intent(this, LimitViewTypeActivity.class);
                break;
            case R.id.fillViewHolder:
                intent = new Intent(this, RecyclerViewActivity.class);
                break;
            case R.id.convertKey:
                intent = new Intent(this, ListViewActivity.class);
                break;
            case R.id.multipleLayout:
                break;
            case R.id.imageGlide:
                break;
            case R.id.customView:
                break;
        }
        if (intent != null) startActivity(intent);
    }
}
