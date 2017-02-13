package com.cyss.rxvalue.demo;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.cyss.rxvalue.RxValue;
import com.cyss.rxvalue.RxValueList;
import com.cyss.rxvalue.adapter.RVSimpleViewHolder;
import com.cyss.rxvalue.demo.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by chenyang on 2017/2/13.
 */

public class ListViewActivity extends Activity {

    Map<String, Object> classes = new HashMap<>();
    RxValue<Map<String, Object>> rxValue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);

        initData();

        rxValue = RxValue.<Map<String, Object>>create(this)
                .convertKey("teacherCount", "teacherNumber")
                .withFillObj(classes);
        rxValue.fillViewAsync(this);

        RxValueList rxValueList = (RxValueList) rxValue.getFillAction(ListView.class);
        rxValueList.addViewClick(R.id.save, new RxValueList.OnViewClickListener() {
            @Override
            public void click(RecyclerView.ViewHolder viewHolder, View view, Object obj) {
                RVSimpleViewHolder holder = (RVSimpleViewHolder) viewHolder;
                holder.rxValue.withFillObj(obj).getData(viewHolder.itemView);
                Log.d(getClass().getName(), "=-=-=-=>" + obj);
            }
        });
        BaseAdapter adapter = rxValueList.getListViewAdapter();
        adapter.notifyDataSetChanged();
    }

    private void initData() {
        //init data
//        classes.setClassName("Class Two Grade Three for ListView");
//        classes.setTeacherNumber(12);
        classes.put("className", "Class Two Grade Three for ListView");
        classes.put("teacherCount", 2);

        List<RecyclerViewActivity.Student> students = new ArrayList<>();
        classes.put("students", students);
        for (int i = 0; i < 20; i++) {
            RecyclerViewActivity.Student student = new RecyclerViewActivity.Student();
            student.setAge(i);
            student.setStudentName("小明ListView~~" + i);
            Date day = new Date();
            day.setTime(day.getTime() - i * 24 * 3600 * 1000);
            student.setBirthday(day);
            student.setAvatarUrl("http://lorempixel.com/300/200/sports/COM" + i);
            students.add(student);
        }
    }
}
