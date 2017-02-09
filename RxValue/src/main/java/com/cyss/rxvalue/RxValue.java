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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by chenyang on 2017/2/7.
 */

public class RxValue<T> {

    private static final String LAYOUT_NAME = "layout";
    private static final String ID_NAME = "id";

    private static Map<String, Integer> layoutMap;
    private static Map<Integer, String> layoutResMap;
    private static Map<String,Integer> idsMap;
    private static Map<Integer, String> idsResMap;
    private static Map<Class<? extends View>, CustomFillAction> globalCustomFillActionMap = new HashMap<>();

    private Context context;
    //填充的数据
    private T fillObj;
    //layout id前缀
    private String prefix;
    //layout id后缀
    private String suffix;
    //需要填充的set view集合，若为空则默认填充全部支持类型
    private Set<Class<? extends View>> fillViewType = new HashSet<>();
    //java bean 名称转换注解信息集合
    private Map<String, String> objIdNameMap = new HashMap<>();
    //java bean date转换注解信息集合
    private Map<String, String> objDateMap = new HashMap<>();
    private Map<Class<? extends View>, CustomFillAction> customFillActionMap = new HashMap<>();
    private OnDataComplete<T> dataComplete;
    private OnDataError dataError;
    private OnFillComplete fillComplete;
    private OnFillError fillError;
    private int layoutId = 1;
    //view缓存，避免重复查找
    private Map<View, Set<View>> viewCache = new HashMap<>();

    private RxValue(Context context){this.context = context;}

    public static <T> RxValue<T> create(Context context) {
        RxValue<T> rxValue = new RxValue<>(context);
        Map<Class<? extends View>, CustomFillAction> actions = new HashMap<>();
        actions.putAll(globalCustomFillActionMap);
        return rxValue.registerActions(actions);
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

    public static Integer getLayoutIdByName(String name) {
        return layoutMap.get(name);
    }

    public static String getLayoutNameById(Integer id) {
        return layoutResMap.get(id);
    }

    public static Integer getIdByName(String id) {
        return idsMap.get(id);
    }

    public static String getNameById(Integer id) {
        return idsResMap.get(id);
    }

    /**
     * 设置需要填充或获取的obj参数
     * @param obj
     * @return
     */
    public RxValue<T> withFillObj(T obj) {
        if (obj == null) return this;
        this.fillObj = obj;
        if (!(obj instanceof Map)) {
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
     * 添加view id前缀
     * @param prefix
     * @return
     */
    public RxValue<T> withPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * 添加view id后缀
     * @param suffix
     * @return
     */
    public RxValue<T> withSuffix(String suffix) {
        this.suffix = suffix;
        return this;
    }

    /**
     * 限制填充类型
     * @param clazz 填充类型
     * @return
     */
    public RxValue<T> viewType(Class<? extends View> clazz) {
        fillViewType.add(clazz);
        return this;
    }

    /**
     * 限制填充类型
     * @param viewSets 填充类型集合
     * @return
     */
    public RxValue<T> viewSets(Set<Class<? extends View>> viewSets) {
        fillViewType.addAll(viewSets);
        return this;
    }

    public RxValue<T> clearViewType() {
        fillViewType.clear();
        return this;
    }

    /**
     * 注册自定义填充行为
     * @param clazz   填充view类型
     * @param action  填充行为
     * @return
     */
    public RxValue<T> registerAction(Class<? extends View> clazz, CustomFillAction action) {
        customFillActionMap.put(clazz, action);
        return this;
    }

    /**
     * 注册自定义填充行为
     * @param actions
     * @return
     */
    public RxValue<T> registerActions(Map<Class<? extends View>, CustomFillAction> actions) {
        customFillActionMap.putAll(actions);
        return this;
    }

    /**
     * 填充完成回调
     * @param complete
     * @return
     */
    public RxValue<T> withFillComplete(OnFillComplete complete) {
        this.fillComplete = complete;
        return this;
    }

    /**
     * 获取数据完成回调
     * @param complete
     * @return
     */
    public RxValue<T> withDataComplete(OnDataComplete<T> complete) {
        this.dataComplete = complete;
        return this;
    }

    /**
     * 填充错误回调
     * @param error
     * @return
     */
    public RxValue<T> withFillError(OnFillError error) {
        this.fillError = error;
        return this;
    }

    /**
     * 填充错误回调
     * @param error
     * @return
     */
    public RxValue<T> withDataError(OnDataError error) {
        this.dataError = error;
        return this;
    }

    /**
     * 添加需要转换的参数key，多用于fillObj为Map时。Java Bean可使用@IdName注解
     * @param paramName   参数的名称
     * @param layoutName  xml中id名称
     * @return
     */
    public RxValue<T> convertKey(String paramName, String layoutName) {
        objIdNameMap.put(layoutName, paramName);
        return this;
    }

    /**
     * 设置layout id
     * @param layoutId
     * @return
     */
    public RxValue<T> layoutId(int layoutId) {
        this.layoutId = layoutId;
        if (fillObj != null) withFillObj(fillObj);
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
     * @param view 需要填充的view
     */
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
    private void getData(View view, boolean isAsync) {
        if(!checkInit()) {
            return;
        }
        Map map = null;
        if (fillObj instanceof Map) {
            map = (Map) fillObj;
        }
        final Map finalMap = map;
        findView(view).subscribe(new Action1<View>() {
            @Override
            public void call(View v) {
                Integer id = v.getId();
                String name = RxValue.getNameById(id);
                name = handleLayoutName(name);
                if (name != null) {
                    Object param = getViewData(v);
                    if (finalMap == null) {
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
                            methodName = toSetMethodName(name);
                            try {
                                invokeSetMethod(fillObj, findMethod(methodName, fillObj.getClass().getDeclaredMethods()), param);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        if (objIdNameMap.containsKey(name)) {
                            name = objIdNameMap.get(name);
                        }
                        if (param != null) finalMap.put(name, param);
                    }
                }
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
                    fillObj.getClass().getDeclaredField(name);
                    hasFieldFlag = true;
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
                        action.action1(context, v, obj);
                    } else {
                        ((TextView) v).setText(obj.toString());
                    }
                } else if (v instanceof Button) {
                    CustomFillAction action = getCustomFillAction(Button.class);
                    if (action != null) {
                        action.action1(context, v, obj);
                    } else {
                        ((Button) v).setText(obj.toString());
                    }
                } else if (v instanceof EditText) {
                    CustomFillAction action = getCustomFillAction(EditText.class);
                    if (action != null) {
                        action.action1(context, v, obj);
                    } else {
                        ((EditText) v).setText(obj.toString());
                    }
                } else {
                    CustomFillAction action = getCustomFillAction(v.getClass());
                    if (action != null) {
                        action.action1(context, v, obj);
                    }
                }
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
            obj = action.action2(context, v);
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
                        methodName = toGetMethodName(fName);
                    } else {
                        methodName = toGetMethodName(name);
                    }
                    Method method = fillObj.getClass().getMethod(methodName);
                    obj = method.invoke(fillObj);
                    if (objDateMap.containsKey(name) && obj instanceof Date) {
                        String formatStr = objDateMap.get(name);
                        SimpleDateFormat sdf = new SimpleDateFormat(formatStr);
                        obj = sdf.format(obj);
                    }
                } catch (NoSuchMethodException e){
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
                                return Observable.from(new View[]{}).concatWith(childView);
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

    private String toGetMethodName(String name) {
        return "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    private String toSetMethodName(String name) {
        return "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
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
        if (layout.length == idNameValue.length) {
            for (int i = 0; i < layout.length;i++) {
                if (layout[i] == layoutId) {
                    idName = idNameValue[i];
                    break;
                }
            }
        }
        return idName;
    }
}
