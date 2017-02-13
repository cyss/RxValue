package com.cyss.rxvalue.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.cyss.rxvalue.listener.OnDataError;
import com.cyss.rxvalue.listener.OnFillError;
import com.cyss.rxvalue.RxValue;
import com.cyss.rxvalue.demo.model.PersonInSimpleUse;

/**
 * Created by chenyang on 2017/2/8.
 */

public class SimpleUseActivity extends AppCompatActivity implements View.OnClickListener, OnFillError, OnDataError {

    private Button mShowResult;
    private RxValue<PersonInSimpleUse> rxValue;
    private PersonInSimpleUse person = new PersonInSimpleUse();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_use);
        mShowResult = (Button) findViewById(R.id.showResult);
        mShowResult.setOnClickListener(this);
        rxValue = RxValue.<PersonInSimpleUse>create(this)
                .withFillObj(person)
                .withFillError(this)
                .withDataError(this);
    }

    @Override
    public void onClick(View view) {
        rxValue.withPrefix("").getData(this);
        Log.d(this.getClass().getName(), "" + person);
        Log.d(this.getClass().getName(), "================================");
        rxValue.withPrefix("show_").fillView(this);
    }

    @Override
    public void error(Throwable error) {
        Log.e("", "", error);
    }
}
