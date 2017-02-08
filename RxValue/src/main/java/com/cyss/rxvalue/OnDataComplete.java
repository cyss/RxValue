package com.cyss.rxvalue;

/**
 * Created by chenyang on 2017/2/7.
 */

public interface OnDataComplete<T> {
    public void complete(T data);
}
