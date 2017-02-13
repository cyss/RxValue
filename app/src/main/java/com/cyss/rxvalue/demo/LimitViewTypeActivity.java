package com.cyss.rxvalue.demo;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cyss.rxvalue.CustomFillAction;
import com.cyss.rxvalue.annotation.DateConfig;
import com.cyss.rxvalue.listener.OnDataError;
import com.cyss.rxvalue.listener.OnFillError;
import com.cyss.rxvalue.RxValue;
import com.cyss.rxvalue.RxValueBuilder;

import java.util.Date;

/**
 * Created by chenyang on 2017/2/8.
 */

public class LimitViewTypeActivity extends AppCompatActivity implements View.OnClickListener, OnFillError, OnDataError {

    private Button mShowResult;
    private RxValue<Person> rxValue;
    private Person person = new Person();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_limit_view_type);
        mShowResult = (Button) findViewById(R.id.showResult);
        mShowResult.setOnClickListener(this);
        rxValue = RxValue.<Person>create(this)
                .withFillError(this)
                .withDataError(this);
    }

    @Override
    public void onClick(View view) {
        person = new Person();
        rxValue.withPrefix("")
                .withFillObj(person)
                .clearViewType()
                .getData(this);
        Log.d(this.getClass().getName(), "person:" + person);
        person = new Person();
        rxValue.withPrefix("")
                .registerAction(ImageView.class, new CustomFillAction() {
                    @Override
                    public void action1(Context context, Object view, Object obj, RxValueBuilder builder) {

                    }

                    @Override
                    public Object action2(Context context, Object view, RxValueBuilder builder) {
                        return null;
                    }
                })
                .withFillObj(person)
                .viewType(EditText.class) // only get EditText, testLabel should be null
                .getData(this);
        Log.d(this.getClass().getName(), "person:" + person);
        rxValue.withPrefix("show_")
                .viewType(TextView.class) // only set TextView
                .fillView(this);
    }

    @Override
    public void error(Throwable error) {
        Log.e("", "", error);
    }

    public class Person {
        private String name;
        @DateConfig("yyyyMMdd")
        private Date birthday;
        private int age;
        //no need view
        private String testLabel;

        public String getTestLabel() {
            return testLabel;
        }

        public void setTestLabel(String testLabel) {
            this.testLabel = testLabel;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Date getBirthday() {
            return birthday;
        }

        public void setBirthday(Date birthday) {
            this.birthday = birthday;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        @Override
        public String toString() {
            return getName() + "," + getAge() + "," + getBirthday() + "," + getTestLabel();
        }
    }
}
