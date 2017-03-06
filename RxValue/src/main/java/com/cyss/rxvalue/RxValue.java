package com.cyss.rxvalue;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cyss.rxvalue.annotation.DateConfig;
import com.cyss.rxvalue.annotation.IdName;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by chenyang on 2017/2/7.
 */

public class RxValue<T> extends RxValueBuilder<T, RxValue<T>>{

    private static final String LAYOUT_NAME = "layout";
    private static final String ID_NAME = "id";

    //view缓存，避免重复查找
    private Map<View, Set<View>> viewCache = new HashMap<>();

    private RxValue(Context context){this.context = context;}

    public static <T> RxValue<T> create(Context context) {
        RxValue<T> rxValue = new RxValue<>(context);
        Map<Class<? extends View>, CustomFillAction> actions = new HashMap<>();
        actions.putAll(globalCustomFillActionMap);
        for (Map.Entry<Class<? extends View>, CustomFillAction> item : globalCustomFillActionMap.entrySet()) {
            try {
                actions.put(item.getKey(), cloneCustomFillAction(item.getValue()));
            } catch (ClassNotFoundException e) {
            } catch (IOException e) {
            }
        }
        rxValue.registerActions(actions);
        return rxValue;
    }

    public static Boolean init(Context context) {
        String RName = context.getPackageName() + ".R$";
        if (layoutMap == null) {
            layoutMap = new HashMap<>();
            layoutResMap = new HashMap<>();
            injectFields(RName + LAYOUT_NAME, layoutMap, layoutResMap);
        }
        if (idsMap == null) {
            idsMap = new HashMap<>();
            idsResMap = new HashMap<>();
            injectFields(RName + ID_NAME, idsMap, idsResMap);
        }
        return layoutMap != null && idsMap != null;
    }

    public static void registerGlobalAction(Class<? extends View> clazz, CustomFillAction action) {
        globalCustomFillActionMap.put(clazz, action);
    }

    public static void registerGlobalActions(Map<Class<? extends View>, CustomFillAction> actions) {
        globalCustomFillActionMap.putAll(actions);
    }

    private static void injectFields(String clazzName, final Map<String, Integer> map1, final Map<Integer, String> map2) {
        try {
            Class clazz = Class.forName(clazzName);
            Field[] fields = clazz.getDeclaredFields();
            Observable.from(fields)
                    .subscribe(new Action1<Field>() {
                        @Override
                        public void call(Field field) {
                            String name = field.getName();
                            Integer id = null;
                            try {
                                id = field.getInt(null);
                            } catch (IllegalArgumentException e){
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (id != null) {
                                map1.put(name, id);
                                map2.put(id, name);
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getLayoutIdByName(String name) {
        if (layoutMap != null && layoutMap.containsKey(name)) return layoutMap.get(name);
        return -1;
    }

    public static String getLayoutNameById(Integer id) {
        if (layoutResMap != null && layoutResMap.containsKey(id)) return layoutResMap.get(id);
        return null;
    }

    public static int getIdByName(String name) {
        if (idsMap != null && idsMap.containsKey(name)) return idsMap.get(name);
        return -1;
    }

    public static String getNameById(Integer id) {
        if (idsResMap != null && idsResMap.containsKey(id)) return idsResMap.get(id);
        return null;
    }

    /**
     * 设置需要填充或获取的obj参数
     * @param obj
     * @return
     */
    @Override
    public RxValue<T> withFillObj(T obj) {
        if (obj == null) return this;
        this.fillObj = obj;
        if (!(obj instanceof Map)) {
            objIdNameMap.clear();
            Observable.from(obj.getClass().getDeclaredFields())
                    .subscribe(new Action1<Field>() {
                        @Override
                        public void call(Field field) {
                            IdName idName = field.getAnnotation(IdName.class);
                            if (idName != null) {
                                String name = RxValue.getNameById(findIdNameByLayout(idName.value(), idName.layout()));
                                if (name != null) {
                                    objIdNameMap.put(name, field.getName());
                                }
                            }
                            DateConfig dateConfig = field.getAnnotation(DateConfig.class);
                            if (dateConfig != null) {
                                objDateMap.put(field.getName(), dateConfig.value());
                            }
                        }
                    });
        }
        return this;
    }

    /**
     * 该方法layout过于复杂时不建议直接使用。
     * 建议使用fillView(View view)
     * @param activity 需要填充的activity
     */
    public void fillView(Activity activity) {
        fillView(activity.getWindow().getDecorView().findViewById(android.R.id.content));
    }

    /**
     * 填充view数据, 同步操作
     * @param view 需要填充的view
     */
    public void fillView(View view) {
        fillView(view, false);
    }

    /**
     * 填充view数据, 异步操作
     * @param activity 需要填充的activity
     */
    public void fillViewAsync(Activity activity) {
        fillView(activity.getWindow().getDecorView().findViewById(android.R.id.content), true);
    }

    public void fillViewAsync(View view) {
        fillView(view, true);
    }

    private void fillView(View view, boolean isAsync) {
        if(!checkInit()) {
            return;
        }
        findView(view).subscribe(new Action1<View>() {
            @Override
            public void call(View v) {
                Integer id = v.getId();
                String name = RxValue.getNameById(id);
                if (name != null) {
                    name = handleLayoutName(name);
                    fillView(name, v);
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                if (fillError != null) fillError.error(throwable);
            }
        }, new Action0() {
            @Override
            public void call() {
                if (fillComplete != null) fillComplete.complete();
            }
        });
    }

    /**
     * 填充ViewHolder
     * @param viewHolder
     */
    public void fillView(RecyclerView.ViewHolder viewHolder) {
        if(!checkInit()) {
            return;
        }
        final RecyclerView.ViewHolder mViewHolder = viewHolder;
        Field[] fields = viewHolder.getClass().getFields();
        Observable.from(fields)
                .filter(new Func1<Field, Boolean>() {
                    @Override
                    public Boolean call(Field field) {
                        Object viewObj = null;
                        try {
                            viewObj = field.get(mViewHolder);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        return viewObj != null && viewObj instanceof View;
                    }
                })
                .subscribe(new Action1<Field>() {
                    @Override
                    public void call(Field field) {
                        try {
                            Object viewObj = field.get(mViewHolder);
                            String name = field.getName();
                            IdName idName = field.getAnnotation(IdName.class);
                            if (idName != null) {
                                name = RxValue.getNameById(findIdNameByLayout(idName.value(), idName.layout()));
                            }
                            name = handleLayoutName(name);
                            fillView(name, (View) viewObj);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        if (fillError != null) fillError.error(throwable);
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        if (fillComplete != null) fillComplete.complete();
                    }
                });
    }

    /**
     * 填充List中的View, List中不会递归遍历子view
     * @param views
     */
    public void fillView(Iterable<View> views) {
        if(!checkInit()) {
            return;
        }
        Observable.<View>from(views)
                .subscribe(new Subscriber<View>() {
                    @Override
                    public void onCompleted() {
                        if (fillComplete != null) fillComplete.complete();
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (fillError != null) fillError.error(e);
                    }

                    @Override
                    public void onNext(View v) {
                        Integer id = v.getId();
                        String name = RxValue.getNameById(id);
                        if (name != null) {
                            name = handleLayoutName(name);
                            fillView(name, v);
                        }
                    }
                });
    }

    /**
     * 该方法layout过于复杂时不建议直接使用。
     * 建议使用getData(View view)
     * @param activity 需要获取的activity
     */
    public void getData(Activity activity) {
        getData(activity.getWindow().getDecorView().findViewById(android.R.id.content));
    }

    /**
     * 获取数据, 同步操作
     * @param view 需要获取view
     */
    public void getData(View view) {
        getData(view, false);
    }

    /**
     * 获取数据, 异步操作
     * @param view 需要获取view
     */
    public void getDataAsync(View view) {
        getData(view, true);
    }

    /**
     * 根据view集合获取数据
     * @param views
     */
    public void getData(Iterable<View> views) {
        if(!checkInit()) {
            return;
        }
        Observable.from(views).subscribe(new Subscriber<View>() {
            @Override
            public void onCompleted() {
                if (dataComplete != null) dataComplete.complete(fillObj);
            }

            @Override
            public void onError(Throwable e) {
                if (dataError != null) dataError.error(e);
            }

            @Override
            public void onNext(View v) {
                Integer id = v.getId();
                String name = RxValue.getNameById(id);
                getData(name, v);
            }
        });
    }

    /**
     * 获取RecyclerView.ViewHolder中view的数据
     * @param viewHolder
     */
    public void getData(RecyclerView.ViewHolder viewHolder) {
        if (!checkInit()) {
            return;
        }
        final RecyclerView.ViewHolder mViewHolder = viewHolder;
        Field[] fields = viewHolder.getClass().getFields();
        Observable.from(fields)
                .filter(new Func1<Field, Boolean>() {
                    @Override
                    public Boolean call(Field field) {
                        Object viewObj = null;
                        try {
                            viewObj = field.get(mViewHolder);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        return viewObj != null && viewObj instanceof View;
                    }
                })
                .subscribe(new Action1<Field>() {
                    @Override
                    public void call(Field field) {
                        try {
                            Object viewObj = field.get(mViewHolder);
                            String name = field.getName();
                            IdName idName = field.getAnnotation(IdName.class);
                            if (idName != null) {
                                name = RxValue.getNameById(findIdNameByLayout(idName.value(), idName.layout()));
                            }
                            getData(name, (View) viewObj);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        if (fillError != null) fillError.error(throwable);
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        if (fillComplete != null) fillComplete.complete();
                    }
                });
    }

    private void getData(View view, boolean isAsync) {
        if(!checkInit()) {
            return;
        }
        findView(view).subscribe(new Action1<View>() {
            @Override
            public void call(View v) {
                Integer id = v.getId();
                String name = RxValue.getNameById(id);
                getData(name, v);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                if (dataError != null) dataError.error(throwable);
            }
        }, new Action0() {
            @Override
            public void call() {
                if (dataComplete != null) dataComplete.complete(fillObj);
            }
        });
    }



    /**
     * 检验是否填充或获取该view
     * @param v
     * @return
     */
    private Boolean checkIsHandleView(View v) {
        Class clazz = v.getClass();
        boolean limitViewFlag = false;
        boolean hasFieldFlag = false;
        if (fillViewType.isEmpty()) {
            limitViewFlag = true;
        } else {
            for (Class item : fillViewType) {
                if (item.isAssignableFrom(clazz)) {
                    limitViewFlag = true;
                    break;
                }
            }
        }
        String name = handleLayoutName(RxValue.getNameById(v.getId()));
        if (name != null) {
            if (objIdNameMap.containsKey(name)) {
                hasFieldFlag = true;
            } else {
                try {
                    if (fillObj instanceof Map) {
                        Map map = (Map) fillObj;
                        hasFieldFlag = map.containsKey(name);
                    } else {
                        fillObj.getClass().getDeclaredField(name);
                        hasFieldFlag = true;
                    }

                } catch (NoSuchFieldException e) {
                }
            }
        }
        return limitViewFlag && hasFieldFlag;
    }

    /**
     * 填充view
     * @param name layout中id的名称
     * @param v 填充view
     */
    private void fillView(String name, View v) {
        if (fillObj != null && name != null && !"".equals(name)) {
            Object obj = getParamByName(name);
            if (obj != null) {
                if (v instanceof TextView) {
                    CustomFillAction action = getCustomFillAction(TextView.class);
                    if (action != null) {
                        action.action1(context, v, obj, this);
                    } else {
                        ((TextView) v).setText(obj.toString());
                    }
                } else if (v instanceof Button) {
                    CustomFillAction action = getCustomFillAction(Button.class);
                    if (action != null) {
                        action.action1(context, v, obj, this);
                    } else {
                        ((Button) v).setText(obj.toString());
                    }
                } else if (v instanceof EditText) {
                    CustomFillAction action = getCustomFillAction(EditText.class);
                    if (action != null) {
                        action.action1(context, v, obj, this);
                    } else {
                        ((EditText) v).setText(obj.toString());
                    }
                } else {
                    CustomFillAction action = getCustomFillAction(v.getClass());
                    if (action != null) {
                        action.action1(context, v, obj, this);
                    }
                }
            }
        }
    }

    private void getData(String pname, View v) {
        Map map = null;
        if (fillObj instanceof Map) {
            map = (Map) fillObj;
        }
        String name = handleLayoutName(pname);
        if (name != null) {
            Object param = getViewData(v);
            if (map == null) {
                if (param != null) {
                    String methodName = null;
                    if (objIdNameMap.containsKey(name)) {
                        name = objIdNameMap.get(name);
                    }
                    if (objDateMap.containsKey(name)) {
                        String formatStr = objDateMap.get(name);
                        SimpleDateFormat sdf = new SimpleDateFormat(formatStr);
                        try {
                            param = sdf.parse(param.toString());
                        } catch (ParseException e) {
                            Log.w(this.getClass().getName(), "RxValue Warn:" + name + ":" + param + " can't date format use '" + formatStr + "'");
                        }
                    }
                    setParamToObj(name, param);
//                    methodName = toSetMethodName(name);
//                    try {
//                        invokeSetMethod(fillObj, findMethod(methodName, fillObj.getClass().getDeclaredMethods()), param);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
                }
            } else {
                if (objIdNameMap.containsKey(name)) {
                    name = objIdNameMap.get(name);
                }
                if (param != null) map.put(name, param);
            }
        }
    }
    /**
     * 获取view数据
     * @param v
     * @return
     */
    private Object getViewData(View v) {
        Object obj = null;
        CustomFillAction action = getCustomFillAction(v.getClass());
        if (action != null) {
            obj = action.action2(context, v, this);
        }
        if (obj == null) {
            if (v != null) {
                if (v instanceof TextView) {
                    obj = ((TextView) v).getText();
                } else if (v instanceof Button) {
                    obj = ((Button) v).getTag();
                } else if (v instanceof EditText) {
                    obj = ((EditText) v).getText();
                }
            }
        }
        return obj;
    }

    /**
     * 根据名称获取fillObj的值
     * @param name 名称
     * @return
     */
    private Object getParamByName(String name) {
        Object obj = null;
        if (fillObj != null) {
            if (fillObj instanceof Map) {
                Map map = (Map) fillObj;
                String fName = name;
                if (objIdNameMap.containsKey(name)) {
                    fName = objIdNameMap.get(name);
                }
                obj = map.get(fName);
            } else {
                try {
                    String methodName = null;
                    if (objIdNameMap.containsKey(name)) {
                        String fName = objIdNameMap.get(name);
                        methodName = fName;
                    } else {
                        methodName = name;
                    }
//                    Method method = null;
//                    try {
//                        method = fillObj.getClass().getMethod(toGetMethodName(methodName, false));
//                    } catch (NoSuchMethodException e) {
//                        method = fillObj.getClass().getMethod(toGetMethodName(methodName, true));
//                    }
//                    obj = method.invoke(fillObj);
                    obj = getParamFromObj(methodName);
                    if (objDateMap.containsKey(methodName) && obj instanceof Date) {
                        String formatStr = objDateMap.get(methodName);
                        SimpleDateFormat sdf = new SimpleDateFormat(formatStr);
                        obj = sdf.format(obj);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return obj;
    }

    /**
     * 递归查询view
     * @param v
     * @return
     */
    private Observable<View> findView(View v) {
        return findView(v, false);
    }

    private Observable<View> findView(View v, boolean isAsync) {
        Observable<View> observable = findAndCacheView(v)
                .filter(new Func1<View, Boolean>() {
                    @Override
                    public Boolean call(View view) {
                        return checkIsHandleView(view);
                    }
                });
        if (isAsync) {
            observable = observable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }
        return observable;
    }

    private Observable<View> findAndCacheView(View v) {
        if (viewCache.containsKey(v)) {
            return Observable.from(viewCache.get(v));
        }
        final Set<View> cacheViewList = new HashSet<>();
        viewCache.put(v, cacheViewList);
        if (v instanceof ViewGroup) {
            final ViewGroup vp = (ViewGroup) v;
            return Observable.range(0, vp.getChildCount())
                    .filter(new Func1<Integer, Boolean>() {
                        @Override
                        public Boolean call(Integer i) {
                            View view = vp.getChildAt(i);
                            return view instanceof ViewGroup || (view.getId() != -1 && view != null);
                        }
                    })
                    .flatMap(new Func1<Integer, Observable<View>>() {
                        @Override
                        public Observable<View> call(Integer i) {
                            View view = vp.getChildAt(i);
                            if (view instanceof ViewGroup) {
                                Observable<View> childView = findAndCacheView(view);
                                return Observable.just(view).concatWith(childView);
                            } else {
                                return Observable.just(view);
                            }
                        }
                    }).doOnNext(new Action1<View>() {
                        @Override
                        public void call(View view) {
                            cacheViewList.add(view);
                        }
                    });
        } else {
            return Observable.just(v);
        }
    }

    /**
     * 执行相应方法
     * @param obj
     * @param method
     * @param val
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws NumberFormatException
     */
    private void invokeSetMethod(Object obj, Method method, Object val) throws InvocationTargetException, IllegalAccessException, NumberFormatException {
        if (method == null) {
            return;
        }
        Class[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 1) {
            Class c = parameterTypes[0];
            if (c.equals(Integer.class) || "int".equals(c.getName())) {
                method.invoke(obj, Integer.parseInt(val.toString()));
            } else if (c.equals(String.class)) {
                method.invoke(obj, val.toString());
            } else if (c.equals(Boolean.class) || "boolean".equals(c.getName())) {
                method.invoke(obj, Boolean.parseBoolean(val.toString()));
            } else if (c.equals(Long.class) || "long".equals(c.getName())) {
                method.invoke(obj, Long.parseLong(val.toString()));
            } else if (c.equals(Float.class) || "float".equals(c.getName())) {
                method.invoke(obj, Float.parseFloat(val.toString()));
            } else if (c.equals(Double.class) || "double".equals(c.getName())) {
                method.invoke(obj, Double.parseDouble(val.toString()));
            } else if (c.equals(Date.class)) {
                if (val instanceof Date) {
                    method.invoke(obj, (Date) val);
                }
            } else {
                method.invoke(obj, val);
            }
        }
    }

    private String handleLayoutName(String name) {
        if (name == null) return null;
        String fName = name;
        if (prefix != null && !"".equals(prefix.trim())){
            if (fName.startsWith(prefix)) {
                fName = fName.substring(prefix.length());
            } else {
                return null;
            }
        }
        if (suffix != null && !"".equals(suffix.trim())) {
            if (fName.endsWith(suffix)) {
                fName = fName.substring(0, fName.length() - suffix.length());
            } else {
                return null;
            }
        }
        return fName;
    }

//    private String toGetMethodName(String name, boolean isBoolean) {
//        return (isBoolean ? "is" : "get") + name.substring(0, 1).toUpperCase() + name.substring(1);
//    }
//
//    private String toSetMethodName(String name) {
//        return "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
//    }

    private Object getParamFromObj(String name) {
        Object returnObj = null;
        try {
            Field field = fillObj.getClass().getDeclaredField(name);
            field.setAccessible(true);
            returnObj = field.get(fillObj);
        } catch (NoSuchFieldException e) {
//            e.printStackTrace();
        } catch (IllegalAccessException e) {
//            e.printStackTrace();
        }
        return returnObj;
    }

    private void setParamToObj(String name, Object param) {
        try {
            Field field = fillObj.getClass().getDeclaredField(name);
            field.setAccessible(true);
            Class c = field.getType();
            if (c.equals(Integer.class) || "int".equals(c.getName())) {
                field.set(fillObj, Integer.parseInt(param.toString()));
            } else if (c.equals(String.class)) {
                field.set(fillObj, param.toString());
            } else if (c.equals(Boolean.class) || "boolean".equals(c.getName())) {
                field.set(fillObj, Boolean.parseBoolean(param.toString()));
            } else if (c.equals(Long.class) || "long".equals(c.getName())) {
                field.set(fillObj, Long.parseLong(param.toString()));
            } else if (c.equals(Float.class) || "float".equals(c.getName())) {
                field.set(fillObj, Float.parseFloat(param.toString()));
            } else if (c.equals(Double.class) || "double".equals(c.getName())) {
                field.set(fillObj, Double.parseDouble(param.toString()));
            } else if (c.equals(Date.class)) {
                if (param instanceof Date) {
                    field.set(fillObj, (Date) param);
                }
            } else {
                field.set(fillObj, param);
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {}
    }

    private CustomFillAction getCustomFillAction(Class<? extends View> clazz) {
        CustomFillAction action = null;
        for (Map.Entry<Class<? extends View>, CustomFillAction> item : customFillActionMap.entrySet()) {
            Class key = item.getKey();
            if (clazz.equals(key) || key.isAssignableFrom(clazz)) {
                action = item.getValue();
                break;
            }
        }
        return action;
    }

    private Method findMethod(String methodName, Method[] methods) {
        Method method = null;
        for (Method m : methods) {
            if (m.getName().equals(methodName)) {
                method = m;
                break;
            }
        }
        return method;
    }

    private Boolean checkInit() {
        objIdNameMap.putAll(objIdNameTempMap);
        if(!init(context)) {
            Log.w(this.getClass().getName(), "RxValue init fail");
            return false;
        }
        if (fillObj == null) {
            Log.w(this.getClass().getName(), "you need call method withFillObj(obj)");
            return false;
        }
        return true;
    }

    private int findIdNameByLayout(int[] idNameValue, int[] layout) {
        int idName = -1;
        int defaultIdName = -1;
        if (layout.length == idNameValue.length) {
            for (int i = 0; i < layout.length;i++) {
                if (layout[i] == DEFAULT_LAYOUT) defaultIdName = idNameValue[i];
                if (layout[i] == layoutId) {
                    idName = idNameValue[i];
                    break;
                }
            }
        }
        return idName == -1 ? defaultIdName : idName;
    }
}
