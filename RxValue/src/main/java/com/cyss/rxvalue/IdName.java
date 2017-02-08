package com.cyss.rxvalue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by chenyang on 2017/2/7.
 */

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IdName {
    /**
     * xml中定义view的id
     * @return
     */
    int[] value() default -1;

    /**
     * view的id所在的layout id
     * @return
     */
    int[] layout() default 1;
}
