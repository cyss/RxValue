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
```

```
