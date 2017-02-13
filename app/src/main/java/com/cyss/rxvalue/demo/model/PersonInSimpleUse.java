package com.cyss.rxvalue.demo.model;

import com.cyss.rxvalue.annotation.DateConfig;

import java.util.Date;

/**
 * Created by chenyang on 2017/2/8.
 */

public class PersonInSimpleUse {
    private String name;
    @DateConfig("yyyyMMdd")
    private Date birthday;
    private int age;

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
        return getName() + "," + getAge() + "," + getBirthday();
    }
}
