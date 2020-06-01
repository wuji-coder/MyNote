# MyNote
根据Google官方的Notepad源码使用Jetpack进行重构改造

**架构简介**

引入Jetpack中的ViewModel架构将用户界面上的数据从Controller（Activity）分离到ViewModel中，并引入LiveData架构对数据进行实时观察。使得Controller层只需关注自身的业务逻辑。让数据的操作与通知。

同时引入Jetpack库中Room持久性库在 SQLite 的基础上提供了一个抽象层，获享更强健的数据库访问机制。并且通过Jetpack库中Navigation控制fragment切换代替Activity间Intent显隐式调用

![img](https://upload-images.jianshu.io/upload_images/1975281-0f927ea02175ea30.jpg)

**架构目录**

<img  src="https://img-blog.csdnimg.cn/2020060115241129.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxNDc0MjIy,size_16,color_FFFFFF,t_70#pic_center" width="50%"   />

## 扩展功能实现

### 1.时间戳展示

**界面展示**

<img src="https://img-blog.csdnimg.cn/20200601152210672.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxNDc0MjIy,size_16,color_FFFFFF,t_70#pic_center" width="50%" alt=""/>

在数据库实体类Note中引入复杂数据类型Date字段。如果你想追求时区的精确控制等，这里推荐你使用`OffsetDateTime`来作为时间的类型

```java
@ColumnInfo(name = "last_update_time")
private Date lastUpdateTime;
```

Room不支持开箱即用，Room 提供了在基元类型和盒装类型之间进行转换的功能，但不允许实体之间进行对象引用。此时需要引入类型转换器。

```java
/**
 * 类型转换类，需要在database中使用@TypeConverters({Converters.class})生效
 * @author 98578
 * @create 2020-05-29 16:49
 */
public class Converters {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
```

接着，将 [`@TypeConverters`](https://developer.android.com/reference/androidx/room/TypeConverters) 注释添加到 `NoteDatabase` 类中，以便 Room 可以使用您为该 `NoteDatabase` 中的每个[实体](https://developer.android.com/training/data-storage/room/defining-data)和 [DAO](https://developer.android.com/training/data-storage/room/accessing-data) 定义的转换器：

```java
/**
 * 数据库管理,采用单例模式
 * @author 98578
 * @create 2020-05-29 11:25
 */
@Database(entities = {Note.class},version = 1,exportSchema = false)
@TypeConverters({Converters.class})
public abstract class NoteDatabase extends RoomDatabase {

    private static NoteDatabase INSTANCE;

    public synchronized static NoteDatabase getINSTANCE(Context context) {
        if (INSTANCE==null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),NoteDatabase.class,"note_datebase")
                    .build();
        }
        return INSTANCE;
    }

    /**
     * 在@Database中 多个entity则写多个Dao
     */
    public abstract NoteDao getNoteDao();
}
```

通过使用这些转换器，您就可以在其他查询中使用自定义类型，就像使用基元类型一样，在数据库Dao中代码段所示：

```java
/**
 * 数据库操作的接口
 * @author 98578
 * @create 2020-05-29 11:25
 */
@Dao
public interface NoteDao {
    @Query("select * from note order by last_update_time desc")
    LiveData<List<Note>> queryAllNotes();

}
```

### 2.搜索

**界面展示**

- 搜索前
<img src="https://img-blog.csdnimg.cn/20200601145506550.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxNDc0MjIy,size_16,color_FFFFFF,t_70#pic_center" alt="image-20200601121654311" width="50%;" />

- 搜索时
<img src="https://img-blog.csdnimg.cn/20200601150137124.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxNDc0MjIy,size_16,color_FFFFFF,t_70#pic_center" width="50%;" />



首先引入menu组件的SearchView，注意你要将组件单独在菜单栏中显示需要为SearchView添加`app:showAsAction="always"`字段，具体引入如下

```xml
<item
        android:id="@+id/app_bar_search"
        android:icon="@drawable/ic_search_black_24dp"
        android:title="Search"
        app:actionViewClass="android.widget.SearchView"
        app:showAsAction="always" />
```

而后需要在fragment中重写`onCreateOptionsMenu`对显式菜单项进行显示与监听，对于SearchView的封装可以进行提交搜索或实时搜索，你可以分别实现监听`SearchView.OnQueryTextListener`监听中的`onQueryTextSubmit`或`onQueryTextChange`。此处实现实时的模糊搜索，代码如下：

```java
/**
     * 初始化菜单栏，并实现显式菜单项功能show
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_menu, menu);

        //搜索
        SearchView searchView = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
        //控制搜索框长度
        int maxWidth = searchView.getMaxWidth();
        searchView.setMaxWidth((int) (0.5 * maxWidth));
        //设置搜索框的实时监听
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //去除多余前后空格
                String pattern = newText.trim();
                noteLive = noteViewModel.queryNotesWithPattern(pattern);
                /*
                  注意：重新赋予LiveData后最好先移除之前的观察。
                  大坑：观察的移除和注入都必须是getViewLifecycleOwner获取的LifecycleOwner。其对应fragment的生命周期
                 */
                noteLive.removeObservers(getViewLifecycleOwner());
                //对LiveData重新进行观察,注意Owner的生命周期，需要注入fragment的owner
                noteLive.observe(getViewLifecycleOwner(), notes -> {
                    //备份列表
                    allNotes = notes;
                    //将观察的数据注入RecycleAdapt中
                    myAdapt.submitList(notes);
                });
                //修改为返回true后事件不会再向下传递，默认false会继续传递
                return true;
            }
        });
    }
```

