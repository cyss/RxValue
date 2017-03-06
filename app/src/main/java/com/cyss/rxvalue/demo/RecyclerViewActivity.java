package com.cyss.rxvalue.demo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.cyss.rxvalue.CustomFillAction;
import com.cyss.rxvalue.RxValueBuilder;
import com.cyss.rxvalue.adapter.RVSimpleViewHolder;
import com.cyss.rxvalue.annotation.DateConfig;
import com.cyss.rxvalue.annotation.IdName;
import com.cyss.rxvalue.RxValue;
import com.cyss.rxvalue.RxValueList;
import com.cyss.rxvalue.adapter.RVSimpleRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by chenyang on 2017/2/9.
 */

public class RecyclerViewActivity extends Activity implements View.OnClickListener {

    Classes classes = new Classes();

    private RecyclerView recyclerView;
    private RxValueList rxValueList;
    private RVSimpleRecyclerViewAdapter<Student> adapter;
    private Button reFill;
    RxValue rxValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_view);
        reFill = (Button) findViewById(R.id.reFill);
        recyclerView = (RecyclerView) findViewById(R.id.students);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(linearLayoutManager);
        reFill.setOnClickListener(this);

        initData();
        //Fill this activity
//        rxValueList = RxValueList.create()
//                .withMode(RxValueList.MODE_SIMPLE)
//                .registerAction(TextView.class, new CustomFillAction<TextView>() {
//                    @Override
//                    public void action1(Context context, TextView view, Object obj, RxValueBuilder builder) {
//                        if (view.getId() == R.id.name) {
//                            view.setText(obj.toString() + ",改变一下");
//                        } else {
//                            view.setText(obj.toString());
//                        }
//                    }
//
//                    @Override
//                    public Object action2(Context context, TextView view, RxValueBuilder builder) {
//                        return view.getText();
//                    }
//                })
//                .itemClick(new RxValueList.OnItemClickListener<Student>() {
//                    @Override
//                    public void click(RecyclerView.Adapter adapter, RecyclerView.ViewHolder viewHolder, int position, Student obj) {
//                        Toast.makeText(RecyclerViewActivity.this, obj.getStudentName(), Toast.LENGTH_LONG).show();
//                    }
//                })
//                .addViewClick(R.id.avatarUrl, new RxValueList.OnViewClickListener<Student>() {
//                    @Override
//                    public void click(RecyclerView.Adapter adapter, RecyclerView.ViewHolder viewHolder, View view, Student obj) {
//                        Toast.makeText(RecyclerViewActivity.this, "点击了图片:" + obj.getStudentName(), Toast.LENGTH_LONG).show();
//                    }
//                })
//                .itemLayout(R.layout.list_item_students);
        rxValue = RxValue.<Classes>create(RecyclerViewActivity.this)
                .withFillObj(classes);
//                .registerAction(RecyclerView.class, rxValueList)
        rxValue.fillViewAsync(RecyclerViewActivity.this);
        rxValueList = (RxValueList) rxValue.getFillAction(RecyclerView.class);
        rxValueList.registerAction(ImageView.class, new CustomFillAction<ImageView>(){

            @Override
            public void action1(Context context, ImageView view, Object obj, RxValueBuilder builder) {
                Glide.with(context).load(obj).into(view);
            }

            @Override
            public Object action2(Context context, ImageView view, RxValueBuilder builder) {
                return null;
            }
        });
//        rxValueList.itemLayout(R.layout.activity_simple_use);   //try this
        rxValueList.addViewClick(R.id.save, new RxValueList.OnViewClickListener<Student>() {

            @Override
            public void click(RecyclerView.ViewHolder viewHolder, View view, Student obj) {
                RVSimpleViewHolder holder = (RVSimpleViewHolder) viewHolder;
                RxValue rxValue = RxValue.create(RecyclerViewActivity.this)
                        .withFillObj(obj);
                holder.rxValue.getDataAsync(viewHolder.itemView);
                Log.d(getClass().getName(), "====>" + classes);
            }
        });
        adapter = (RVSimpleRecyclerViewAdapter<Student>) rxValueList.getRecyclerViewAdapter();
        //delete first one
        classes.getStudents().remove(0);
        classes.getStudents().remove(0);
        classes.getStudents().remove(0);
        classes.getStudents().remove(0);
        classes.getStudents().remove(0);
        adapter.notifyDataSetChanged();
    }

    private void initData() {
        //init data
        classes.setClassName("Class Two Grade Three");
        classes.setTeacherNumber(2);

        List<Student> students = new ArrayList<>();
        classes.setStudents(students);
        for (int i = 0; i < 20; i++) {
            Student student = new Student();
            student.setAge(i);
            student.setStudentName("小明1~~" + i);
            Date day = new Date();
            day.setTime(day.getTime() - i * 24 * 3600 * 1000);
            student.setBirthday(day);
            student.setAvatarUrl("http://lorempixel.com/300/200/sports/COM" + i);
            students.add(student);
        }
    }

    @Override
    public void onClick(View view) {
        initData();
        rxValue.fillViewAsync(RecyclerViewActivity.this);
    }

    public static class Classes {
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

        @Override
        public String toString() {
            return className + "," + teacherNumber + "," + students;
        }
    }

    public static class Student {
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

        @Override
        public String toString() {
            return studentName + "," + age;
        }
    }
}
