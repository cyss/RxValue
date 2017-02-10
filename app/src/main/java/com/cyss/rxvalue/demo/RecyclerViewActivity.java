package com.cyss.rxvalue.demo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cyss.rxvalue.CustomFillAction;
import com.cyss.rxvalue.DateConfig;
import com.cyss.rxvalue.IdName;
import com.cyss.rxvalue.RxValue;
import com.cyss.rxvalue.RxValueBuilder;
import com.cyss.rxvalue.RxValueList;
import com.cyss.rxvalue.adapter.RVSimpleRecyclerViewAdapter;
import com.cyss.rxvalue.demo.model.PersonInSimpleUse;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by chenyang on 2017/2/9.
 */

public class RecyclerViewActivity extends Activity {

    Classes classes = new Classes();

    private RecyclerView recyclerView;
    private RxValueList rxValueList;
    private RVSimpleRecyclerViewAdapter<Student> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_view);

        recyclerView = (RecyclerView) findViewById(R.id.students);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(linearLayoutManager);

        //init data
        classes.setClassName("Class Two Grade Three");
        classes.setTeacherNumber(2);

        List<Student> students = new ArrayList<>();
        classes.setStudents(students);
        for (int i = 0; i < 20; i++) {
            Student student = new Student();
            student.setAge(i);
            student.setStudentName("小明~~" + i);
            Date day = new Date();
            day.setTime(day.getTime() - i * 24 * 3600 * 1000);
            student.setBirthday(day);
            student.setAvatarUrl("http://lorempixel.com/300/200/sports/COM" + i);
            students.add(student);
        }

        //Fill this activity
        rxValueList = RxValueList.create()
                .withMode(RxValueList.MODE_SIMPLE)
                .registerAction(TextView.class, new CustomFillAction<TextView>() {
                    @Override
                    public void action1(Context context, TextView view, Object obj, RxValueBuilder builder) {
                        if (view.getId() == R.id.name) {
                            view.setText(obj.toString() + ",改变一下");
                        } else {
                            view.setText(obj.toString());
                        }
                    }

                    @Override
                    public Object action2(Context context, TextView view, RxValueBuilder builder) {
                        return view.getText();
                    }
                })
                .itemClick(new RxValueList.OnItemClickListener<Student>() {
                    @Override
                    public void click(RecyclerView.Adapter adapter, RecyclerView.ViewHolder viewHolder, int position, Student obj) {
                        Toast.makeText(RecyclerViewActivity.this, obj.getStudentName(), Toast.LENGTH_LONG).show();
                    }
                })
                .addViewClick(R.id.avatarUrl, new RxValueList.OnViewClickListener<Student>() {
                    @Override
                    public void click(RecyclerView.Adapter adapter, RecyclerView.ViewHolder viewHolder, View view, Student obj) {
                        Toast.makeText(RecyclerViewActivity.this, "点击了图片:" + obj.getStudentName(), Toast.LENGTH_LONG).show();
                    }
                })
                .itemLayout(R.layout.list_item_student);
        RxValue.<Classes>create(RecyclerViewActivity.this)
                .withFillObj(classes)
                .registerAction(RecyclerView.class, rxValueList)
                .fillViewAsync(RecyclerViewActivity.this);
        adapter = (RVSimpleRecyclerViewAdapter<Student>) rxValueList.getAdapter();
        //delete first one
        students.remove(0);
        adapter.notifyDataSetChanged();
    }

    public class Classes {
        private String className;
        private int teacherNumber;
        private List<Student> students;

        public List<Student> getStudents() {
            return students;
        }

        public void setStudents(List<Student> students) {
            this.students = students;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public int getTeacherNumber() {
            return teacherNumber;
        }

        public void setTeacherNumber(int teacherNumber) {
            this.teacherNumber = teacherNumber;
        }
    }

    public class Student {
        private String avatarUrl;
        @DateConfig
        private Date birthday;
        @IdName(R.id.name)
        private String studentName;
        private int age;

        public String getStudentName() {
            return studentName;
        }

        public void setStudentName(String studentName) {
            this.studentName = studentName;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
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
    }
}
