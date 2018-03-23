对于 Android 上的软件架构，开发者不断地在自己造轮子，对于 MVC 、MVP、MVVC 有着各种各样的理解， 网上相关的文章也是大相径庭，所以我们的真主 Google 教父，在 GitHub 有一个开源项目 [android-architecture](https://github.com/googlesamples/android-architecture)，里面对 Android 的软件架构做出了一些官方解释，并在不同的分支上分别做了很多不同的微小贡献。

Google 的这个开源项目 [android-architecture](https://github.com/googlesamples/android-architecture) 有着如下分支：
- todo-mvp
- todo-mvp-clean
- todo-mvp-contentproviders
- todo-mvp-dagger
- todo-mvp-loadders
- todo-mvp-rxjava
- todo-mvvm-databinding

所以从分支名字就可以看出来，这个 Demo 应用主要是实现了一个todo（待办事项）应用的功能，内容包括添加事件，激活事件，完成事件，分类别查看事件列表等内容。

这篇文章，我们就来看一看其中一个分支 todo-mvp 在 MVP 架构下是如何实现功能的。

我们选择了这个包来分析：com\example\android\architecture\blueprints\todoapp\tasks </br>(此应用依据功能来分包，所以每一个功能点都有一个独立的包)

在这个包里，主要的功能就是查看已存在的事件，并可以根据是否激活，是否完成来筛选展示事件。

这个包里有如下类：
- ScrollChildSwipeRefreshLayout.java (下拉刷新 View 类)
- TasksActivity.java （内容上层视图）
- TasksContract.java （ P 和 V 的接口定义类）
- TasksFilterType.java （描述任务筛选条件，包括所有、已激活、已完成）
- TasksFragment.java （内容视图，也是 View 的实现类 ）
- TasksPresenter.java （ Presenter 的实现类）

同时在 com\example\android\architecture\blueprints\todoapp\data 这个包中，存在M的一些内容，也是有必要提到的：
- Task.java (待办事项的实体类，描述了其数据结构)
- TasksDataSource.java （ M 的接口定义类）
- TasksRepository.java （ Model 的一个实现类）

#### MVP的接口定义 ####

我们首先来看 Presenter 和 View 的接口定义类，TasksContract.java。
```java
public interface TasksContract {

    interface View extends BaseView<Presenter> {
        void setLoadingIndicator(boolean active);
        void showTasks(List<Task> tasks);
        void showAddTask();
        void showTaskDetailsUi(String taskId);
        void showTaskMarkedComplete();
        void showTaskMarkedActive();
        void showCompletedTasksCleared();
        void showLoadingTasksError();
        void showNoTasks();
        void showActiveFilterLabel();
        void showCompletedFilterLabel();
        void showAllFilterLabel();
        void showNoActiveTasks();
        void showNoCompletedTasks();
        void showSuccessfullySavedMessage();
        boolean isActive();
        void showFilteringPopUpMenu();
    }

    interface Presenter extends BasePresenter {
        void result(int requestCode, int resultCode);
        void loadTasks(boolean forceUpdate);
        void addNewTask();
        void openTaskDetails(@NonNull Task requestedTask);
        void completeTask(@NonNull Task completedTask);
        void activateTask(@NonNull Task activeTask);
        void clearCompletedTasks();
        void setFiltering(TasksFilterType requestType);
        TasksFilterType getFiltering();
    }
}

public interface BaseView<T> {
    void setPresenter(T presenter);
}

public interface BasePresenter {
    void start();
}
```


对于 View 这个接口，实现了 View 的所有 UI 操作的定义，定义的所有方法都是 show 开头，表示了这是一个 UI 操作，同时 View 的接口 BaseView ，则只是定义了一个 setPresenter 方法，用来在 View 层级去操作 Presenter 来执行逻辑。

然后对于接口 Presenter 则是实现了逻辑操作的定义，这些方法执行一般都由一些 UI 动作去引发，比如点击事件，下拉刷新等。同时，基接口 BasePresenter 还定义了一个 start 方法，一般用来执行一些初始化时的逻辑操作（比如说加载首页数据）。

在 MVP 架构中，Model 和 View 是不会有任何的直接联系的，一起都交由 Presenter 来连结。所以对于 Presenter 来说，是能唯一操作 View 层的，
所以这个类的名字也取得很有含义[ Contract ]，这个单词有合同的意思，可做动词和名词。

看完了 View 和 Presenter 的接口定义，我们接下来来看看 Model 层级的接口定义，TasksDataSource.java。
```java
public interface TasksDataSource {

    interface LoadTasksCallback {
        void onTasksLoaded(List<Task> tasks);
        void onDataNotAvailable();
    }

    interface GetTaskCallback {
        void onTaskLoaded(Task task);
        void onDataNotAvailable();
    }

    void getTasks(@NonNull LoadTasksCallback callback);
    void getTask(@NonNull String taskId, @NonNull GetTaskCallback callback);
    void saveTask(@NonNull Task task);
    void completeTask(@NonNull Task task);
    void completeTask(@NonNull String taskId);
    void activateTask(@NonNull Task task);
    void activateTask(@NonNull String taskId);
    void clearCompletedTasks();
    void refreshTasks();
    void deleteAllTasks();
    void deleteTask(@NonNull String taskId);
}
```
Model 层级的实现则是完全对数据进行的一些操作，比如说修改数据的属性啊，取出数据等，当然，如果取数据时异步的，需要添加一个接口来进行回调。

Model 层和 View 层完全分离之后，View 层级在展示数据的时候并不去关心也是不知道数据是怎么来的，到底是从内存，磁盘还是网络，这一点使得逻辑更加分离，耦合度更加低，Model 层逻辑内部自己实现，由Presenter根据业务需求选择合适的方法。

#### Presenter 和 View 的具体实现 ####


以下文件位于 com\example\android\architecture\blueprints\todoapp\tasks\TasksPresenter.java

```java
public class TasksPresenter implements TasksContract.Presenter {

    private final TasksRepository mTasksRepository;
    private final TasksContract.View mTasksView;

    ···
    public TasksPresenter(@NonNull TasksRepository tasksRepository, @NonNull TasksContract.View tasksView) {
        mTasksRepository = checkNotNull(tasksRepository, "tasksRepository cannot be null");
        mTasksView = checkNotNull(tasksView, "tasksView cannot be null!");
        mTasksView.setPresenter(this);
    }

    @Override
    public void start() {
        loadTasks(false);
    }

    @Override
    public void result(int requestCode, int resultCode) {
        if (AddEditTaskActivity.REQUEST_ADD_TASK == requestCode && Activity.RESULT_OK == resultCode) {
            mTasksView.showSuccessfullySavedMessage();
        }
    }

    @Override
    public void loadTasks(boolean forceUpdate) {
        loadTasks(forceUpdate || mFirstLoad, true);
        mFirstLoad = false;
    }

    private void loadTasks(boolean forceUpdate, final boolean showLoadingUI) {

        ···

        mTasksRepository.getTasks(new TasksDataSource.LoadTasksCallback() {
            @Override
            public void onTasksLoaded(List<Task> tasks) {
                List<Task> tasksToShow = new ArrayList<Task>();

                ···

                processTasks(tasksToShow);
            }

            @Override
            public void onDataNotAvailable() {
                mTasksView.showLoadingTasksError();
            }
        });
    }

    private void processTasks(List<Task> tasks) {

            ···
            mTasksView.showTasks(tasks);

            ···
    }

    ···
}
```

以下文件位于 com\example\android\architecture\blueprints\todoapp\tasks\TasksFragment.java

```java
public class TasksFragment extends Fragment implements TasksContract.View {

    private TasksContract.Presenter mPresenter;

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Override
    public void setPresenter(@NonNull TasksContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ...
        final ScrollChildSwipeRefreshLayout swipeRefreshLayout =
                (ScrollChildSwipeRefreshLayout) root.findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPresenter.loadTasks(false);
            }
        });
        ...
        return root;
    }

    @Override
    public void showTasks(List<Task> tasks) {
        mListAdapter.replaceData(tasks);
        ···
    }
    ...
}
```

以下文件位于 com\example\android\architecture\blueprints\todoapp\tasks\TasksActivity.java
```java
public class TasksActivity extends AppCompatActivity {

    private TasksPresenter mTasksPresenter;

    ···

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ···

        TasksFragment tasksFragment =
                (TasksFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);

        ···

        mTasksPresenter = new TasksPresenter(
                Injection.provideTasksRepository(getApplicationContext()), tasksFragment);

       ···
    }
}
```
上面的这些代码，主要提供了这样一些功能。
在程序启动的时候从数据源那里加载待办事项，同时提供了一个下拉刷新的组件，可供重新加载数据。
我们如果不用MVP架构的话，通常是在 Activity 或者 Fragment 的生命周期方法，比如 onCreateView，onResume 等方法中去获取数据源数据，然后直接加载到View上。</br>
但是如果上面这个过程用MVP做的话，可能有些不一样。


先来看一下上面代码的一些结构。</br>


Presenter 实现类 = TasksPresenter.java</br>
View 实现类 = TasksFragment.java</br>
Model 实现类 = TasksRepository.java</br>
同时程序的入口为 TasksActivity.java 在其 onCreate 方法中调用了 先后实例化了 View 层的 TasksFragment 和 Presenter 层 TasksPresenter 的构造方法，接下来就来一点一点看。


1. 首先在程序的入口 TasksActivity 的 onCreate 方法中，首先构造了View 层的对象 TasksFragment ，然后调用了 Presenter 的构造方法 ，并将 View（TaskFragment）和 Model（TasksRepository）的具体实现当作参数传入，这样 Presenter 就持有了 Model 和 View 的引用。
同时，在 Presenter的构造方法中，也将自身传入了 View 的 setPresenter 方法，这样，Presenter 和 View 层就相互持有了引用。

2. 接下来，作为 View 层的实现类，TasksFragment 在生命周期方法 onResume 中，调用了 Presenter 的 start 方法。
这个就是在界面初始化完毕， View 层需要去处理业务逻辑（请求数据）， 通知 Presenter，让其调用相应的方法来响应。
在 Presenter 的 start 方法中，调用了 Presenter 的 loadTasks方法。

3. 同时，在 View 层中的 onCreateView 方法中，添加了下拉刷新的监听器，在监听器的回调中，也执行了 Presenter 的 loadTasks方法。
所以在上面两种情况下，都会调用 Presenter 的 loadTasks方法。

4. 在 Presenter 的 loadTasks 方法中，因为 Presenter 持有 Model 层实现类 TasksRepository 的引用，调用了 TasksRepository 的 getTasks 方法来异步获取数据，
在成功获取数据之后，调用了 processTasks 方法，在这个方法里面又去调用了 View 层的 showTasks方法，将 Model 层的数据，经过 Presenter 层，加载到了 View 层上。</br>
如果获取数据失败，则是调用了 View 层的 showLoadingTasksError 方法，去更新 UI 至数据失败的情况，这个也是一个 Presenter 层操作 View 层的例子。

5.我们这里没有提到关于 Model 层，也就是 TasksRepository 的实现，因为这个并不重要，只要你将整个获取数据的过程封装到 Model 内部，如果是异步的话有回调接口，同步的话直接返回，交至 Presenter 层，然后在 Presenter 层去操作 View 层。
