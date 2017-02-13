package com.cyss.rxvalue.demo;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.cyss.rxvalue.RxValue;
import com.cyss.rxvalue.RxValueList;
import com.cyss.rxvalue.adapter.RVSimpleViewHolder;
import com.cyss.rxvalue.demo.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by chenyang on 2017/2/13.
 */

public class ListViewActivity extends Activity {

    RecyclerViewActivity.Classes classes = new RecyclerViewActivity.Classes();
    RxValue<RecyclerViewActivity.Classes> rxValue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);

        initData();

        rxValue = RxValue.<RecyclerViewActivity.Classes>create(this)
                .withFillObj(classes);

        classes.getStudents().remove(0);
        classes.getStudents().remove(0);
        classes.getStudents().remove(0);
        classes.getStudents().remove(0);

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
        rxValueList.getListViewAdapter().notifyDataSetChanged();
    }

    private void initData() {
        //init data
        classes.setClassName("Class Two Grade Three for ListView");
        classes.setTeacherNumber(12);

        List<RecyclerViewActivity.Student> students = new ArrayList<>();
        classes.setStudents(students);
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
