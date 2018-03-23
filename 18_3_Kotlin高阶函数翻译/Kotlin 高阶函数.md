## Standard.kt部分 ##

包括以下函数

- run
- T.run
- with
- apply
- also (1.1)
- let
- takeif (1.1)
- takeUnless (1.1)
- repeat

### run ###

```kotlin
	public inline fun <R> run(block: () -> R): R = block()

```

官方翻译：
Calls the specified function [block] and returns its result.

调用指定的 [block] 方法，然后返回特定的值。

此函数可以可以在内部最终返回任意类型的值，即泛型 R ，注意在闭包代码块中 return 需要加上 @run 注解。

### T.run ###

```kotlin
	public inline fun <T, R> T.run(block: T.() -> R): R = block()

```

官方翻译：
Calls the specified function [block] with this value as its receiver and returns its result.

使用 this 类型的值作为接收者,即 T 类型来调用指定的 [block] 方法，然后返回特定的值。

此函数与 run 方法相似，只是可以在任意对象后执行，即 T ，run 方法内可以不书写被调用者 T 直接执行方法。关于返回的处理和 run 方法相同。

例子：
```kotlin
        mRecyclerView.run {
					layoutManager = GridLayoutManager(mBaseActivity, 2)
					adapter = mAdapter
					return@run "fuck"
        }
```

### with ###

```kotlin
	public inline fun <T, R> with(receiver: T, block: T.() -> R): R = receiver.block()

```
官方翻译：
Calls the specified function [block] with the given [receiver] as its receiver and returns its result.

使用 receiver 类型的值作为接收者,即 T 类型来调用指定的 [block] 方法，然后返回特定的值。

此函数与 T.run 函数相似，只是将 T 作为参数而不是函数调用者，其他与 T.run 相同。

例子：
```kotlin
        with(mRecyclerView) {
					layoutManager = GridLayoutManager(mBaseActivity, 2)
					adapter = mAdapter
					return@run "fuck"
        }
```
### apply ###

```kotlin
	public inline fun <T> T.apply(block: T.() -> Unit): T { block(); return this }
```
官方翻译：
Calls the specified function [block] with this value as its receiver and returns this value.

使用 receiver 类型的值作为接收者,即 T 类型来调用指定的 [block] 方法，然后返回 T 值。

apply 方法可以跟在任何对象之后调用，在闭包中可以不需要声明即可直接调用 T 对象方法，特别适用于初始化对象后的附加操作。

例子：
```kotlin
		var mRecyclerView = RecyclerView(mBaseActivity).apply {
				layoutManager=LinearLayoutManager(mBaseActivity)
				adapter=mAdapter
		}
```

### also ###

```kotlin
	public inline fun <T> T.also(block: (T) -> Unit): T { block(this); return this }

```
官方翻译：
Calls the specified function [block] with this value as its argument and returns this value.

使用 receiver 类型的值作为参数,即 T 类型来参与指定的 [block] 方法调用，然后返回 T 值。

also 与 apply 相似，只是在闭包中需要使用 it 来指代 T 对象。

例子：
```kotlin
	var mRecyclerView = RecyclerView(mBaseActivity).also {
			it.layoutManager=LinearLayoutManager(mBaseActivity)
			it.adapter=mAdapter
	}
```

### let ###

```kotlin
	public inline fun <T, R> T.let(block: (T) -> R): R = block(this)

```
官方翻译：
Calls the specified function [block] with this value as its argument and returns its result.

使用当前对象作为函数参数，即 T 类型并返回结果，类型 R。

例子：
```kotlin
	var let = mRecyclerView.let {
			it.layoutManager = LinearLayoutManager(mBaseActivity)
			it.adapter = mAdapter
			return@let "fuck"
	}
```

let 方法几乎是 also 与 run 方法的结合体，可以使用 it，同时可以指定返回值类型。

### takeIf ###

```kotlin
	public inline fun <T> T.takeIf(predicate: (T) -> Boolean): T? = if (predicate(this)) this else null

```
官方翻译：
Returns this value if it satisfies the given [predicate] or null, if it doesn't.

当满足predicate条件，即返回 this 值（T），否则为空。

例子：
```kotlin
		var takeIf = mRecyclerView.takeIf {
			return@takeIf it.childCount != 0
		}
```

takeif 方法可以根据闭包条件来确定返回值，不符合要求返回空。


### takeUnless ###

```kotlin
	public inline fun <T> T.takeUnless(predicate: (T) -> Boolean): T? = if (!predicate(this)) this else null

```
官方翻译：
Returns this value if it _does not_ satisfy the given [predicate] or null, if it does.

当不满足predicate条件，即返回 this 值（T），否则为空。

例子：
```kotlin
	var takeIf = mRecyclerView.takeUnless {
      return@takeUnless it.childCount == 0
		}
```

效果与 takeIf 相反。

### repeat ###
```kotlin
	public inline fun repeat(times: Int, action: (Int) -> Unit) {
	    for (index in 0..times - 1) {
	        action(index)
	    }
	}
```


例子：
```kotlin
	repeat(mRecyclerView.childCount){
		var view = mRecyclerView.getChildAt(it)
	}

```

这个方法真正做到了随时随地不需要 for 关键字实现 for 循环。
