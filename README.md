# RxValue
It's the easiest way to set value to view with RxJava  

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
# Install
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
compile 'com.github.cyss:RxValue:1.0.1'
```
# Use RxValue  
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
# Convert Field Key  
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
1. the first way  
```java
RxValue<Person> rxJava = RxValue.<Person>create(context)
  .withPrefix("p_")
  .withSuffix("_s")
  .withFillObj(person);
```
2. or you can do this  
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
# About Date
just use annotation @DateConfig
```java
@DateConfig("yyyyMMdd")
private Date birthday;
//get and set method
```
# Load Image
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
Note: If you config global action with ImageView well, and also config when fillView/getData. the later will work.

# Custom View
If you know load Image in ImageView, I think you will know how to do with your custom view.  
```java
@Override
public Object action2(Context context, ImageView view) {
  return null;
}
```
this is use with getData(view).

# About View holder
just use fillView(viewHolder), don't use fillView(viewHolder.itemView).

# Handle Error or Complete
For now fillView and getData is sync.
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
# Next Move
1. Suport ListView or RecyclerView
2. Data Validate when getData
3. Data Binding?
4. fix bug.
5.
# Finally
Just have fun :)

# License
[http://www.apache.org/licenses/LICENSE-2.0.txt](http://www.apache.org/licenses/LICENSE-2.0.txt)
