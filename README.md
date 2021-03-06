# RxValue
It's the easiest way to set value to view with [RxJava](https://github.com/ReactiveX/RxJava)

~~TextView tv1 = findViewById(...)~~  
~~TextView tv2 = findViewById(...)~~  
...  
~~EditText et1 = findViewById(...)~~  
~~EditText et2 = findViewById(...)~~  
...  
~~tv1.setText(...)~~  
~~tv2.setText(...)~~  
...  
~~et1.setText(...)~~  
~~et2.setText(...)~~  
...  
just use
```java
RxValue.<Person>create(context).withFillObj(person).fillView(view);
//if view holder
RxValue.<Person>create(context).withFillObj(person).fillView(viewHolder);
//if you want to pass parameters to person just
RxValue.<Person>create(context).withFillObj(person).getData(view);
```
## Install
Add it in your root build.gradle at the end of repositories:
```
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
 Add the dependency
```
//depend on rxjava
compile 'io.reactivex:rxandroid:1.2.+'
compile 'io.reactivex:rxjava:1.2.+'
//rxvalue
compile 'com.github.cyss:RxValue:1.0.7'
```
## Use RxValue  
XML Layout  activiy_simple.xml
```xml
<EditText
	android:id="@+id/name"
  android:layout_width="match_parent"
  android:layout_height="wrap_content"
  android:text="my name"/>
<EditText
  android:id="@+id/age"
  android:layout_width="match_parent"
  android:layout_height="wrap_content"
  android:inputType="number"
  android:text="30" />
```
Person.java
```java
public class Person {
  private String name;
  private int age;
  //get and set methods
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public int getAge() { return age; }
  public void setAge(int age) { this.age = age; }
  @Override
  public String toString() {
      return getName() + "," + getAge();
  }
}
```
Use RxValue In Activity
```java
Person person = new Person();
RxValue<Person> rxJava = RxValue.<Person>create(context).withFillObj(person);
rxJava.getData(this);
Log.d("", "" + person); //my name,30

person.setName("cyss");
person.setAge(20);
rxJava.fillView(this);//It's a surprise
```
Note: In default mode xml view's id must same as class field name.
## Convert Field Key  
if we have a xml layout named activiy_convert.xml like this.
```xml
<EditText
	android:id="@+id/p_name_s"
  android:layout_width="match_parent"
  android:layout_height="wrap_content"
  android:text="my name"/>
<EditText
  android:id="@+id/p_age_s"
  android:layout_width="match_parent"
  android:layout_height="wrap_content"
  android:inputType="number"
  android:text="30" />
```
we need set convert config  
the first way  
```java
RxValue<Person> rxJava = RxValue.<Person>create(context)
  .withPrefix("p_")
  .withSuffix("_s")
  .withFillObj(person);
```
or you can do this  
Person.java
```java
@IdName(value = {R.id.name, R.id.p_name_s}, layout = {R.layout.activity_simple, R.layout.activity_convert})
private String name;
@IdName(value = {R.id.age, R.id.p_age_s}, layout = {R.layout.activity_simple, R.layout.activity_convert})
private int age;
```
In Activity
```java
RxValue<Person> rxJava = RxValue.<Person>create(context).withFillObj(person);
//fill in activiy_simple.xml
rxjava.layoutId(R.layout.activity_simple).fillView(this);
//fill in activiy_convert.xml
rxjava.layoutId(R.layout.activiy_convert).fillView(this);
```
## Limit View Type
Sometimes we just need get/set EditText value
```java
rxjava
  .viewType(EditText.class)
//.viewType(TextView.class) limit view
  .fillView(this);
```
## About Date
just use annotation @DateConfig
```java
@DateConfig("yyyyMMdd")
private Date birthday;
//get and set method
```
## Load Image
How to fill a url to ImageView? If you use Glide to load image.
```java
RxValue.create(context).
  ...
  .registerAction(ImageView.class, new CustomFillAction<ImageView>() {
    @Override
    public void action1(Context context, ImageView view, Object obj) {
        Glide.with(this).load(obj).centerCrop().crossFade().into(view);
    }

    @Override
    public Object action2(Context context, ImageView view) {
      return null;
    }
  })
  ...
  .fillView(..)
```
or you can config in Application onCreate(Bundle ..)
```java
RxValue.registerGlobalAction(ImageView.class, new CustomFillAction<ImageView>() {
  @Override
  public void action1(Context context, ImageView view, Object obj) {
      Glide.with(this).load(obj).centerCrop().crossFade().into(view);
  }

  @Override
  public Object action2(Context context, ImageView view) {
    return null;
  }
});
```
Note: If you config global action with ImageView, and also config when fillView/getData. the later will work.

## RecyclerView or ListView
You can use RxValueList.  
### Simple Use
1. First you can register global action in Application onCreate()
~~~java
RxValue.registerGlobalAction(RecyclerView.class, RxValueList.create()
      .withMode(RxValueList.MODE_SIMPLE));
RxValue.registerGlobalAction(ListView.class, RxValueList.create()
      .withMode(RxValueList.MODE_SIMPLE));
~~~  
2. Fill data in Activity
~~~java
rxValue = RxValue.<Classes>create(RecyclerViewActivity.this)
        .withFillObj(classes);
rxValueList = (RxValueList) rxValue
        .getFillAction(RecyclerView.class)
        // .itemLayout(R.layout.list_item_students); //setting layout item
        // .addViewClick()      //add click listener with view
        // .itemClick()         //when click item
        //if you set your custom adapter, addViewClick(),itemClick(),itemLayout() will not use.
        //and you must call withMode(RxValueList.CUSTOM))
        // .withAdapter(...)    
rxValue.fillViewAsync(RecyclerViewActivity.this);
~~~

Note: if you don't set itemLayout(...), RxValueList will use `list_item_<RecyclerView/ListView id>` for item's layout.

## Custom View
If you know load Image in ImageView well, I think you will know how to do with your custom view.  
```java
@Override
public Object action2(Context context, ImageView view) {
  return null;
}
```
this is use with getData(view).

## About View holder
just use fillView(viewHolder), don't use fillView(viewHolder.itemView).

## Handle Error or Complete
If you are using a async method like fillViewAsync/getDateAsync, you need OnFillComplete/OnDataComplete.
```java
//handle error
rxJava...
  .withDataError(...) //Impl the Interface
  .withFillError(...)
  .fillView..
//handle when complete
rxJava...
  .withDataComplete(...) //Impl the Interface
  .withFillComplete(...)
  .fillView..
```

## Now Support View Type
1. EditText
2. TextView
3. Button
4. ListView
5. RecyclerView
6. coming soon...

## Next Move
1. Data Validate when getData
2. Data Binding?
3. fix bug.

## Finally
Just have fun :)

## License
[http://www.apache.org/licenses/LICENSE-2.0.txt](http://www.apache.org/licenses/LICENSE-2.0.txt)
