:running:HorizontalRefreshLayout-Android:running:
============

开发者使用 HorizontalRefreshLayout-Android 可以对RecycView、Listview、ScrollView等控件实现左右刷新

##  [APK下载](https://github.com/bingoogolapple/BGARefreshLayoutDemo)

## Gradle配置
compile 'xiao.free.horizontalrefreshlayout:lib:v0.1.2'
## XML配置
```xml
<xiao.free.horizontalrefreshlayout.HorizontalRefreshLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/refresh"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/blue">

    <com.lsjwzh.widget.recyclerviewpager.RecyclerViewPager
        android:id="@+id/viewpager"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:clipToPadding="false"
        app:rvp_flingFactor="0.15"
        app:rvp_singlePageFling="false"
        app:rvp_triggerOffset="0.5" />

</xiao.free.horizontalrefreshlayout.HorizontalRefreshLayout>
```
## Java代码
```java
refreshLayout = (HorizontalRefreshLayout) findViewById(R.id.refresh);
refreshLayout.setRefreshCallback(this);
refreshLayout.setRefreshHeader(new LoadingRefreshHeader(this), HorizontalRefreshLayout.LEFT);
refreshLayout.setRefreshHeader(new LoadingRefreshHeader(this), HorizontalRefreshLayout.RIGHT);
```
通过setRefreshHeader方法可以设置左右刷新头部，库中已支持三种刷新效果，如下图所示：

![image](https://github.com/linuxjava/HorizontalRefreshLayout/raw/master/gif/1.gif) 
![image](https://github.com/linuxjava/HorizontalRefreshLayout/raw/master/gif/2.gif)
![image](https://github.com/linuxjava/HorizontalRefreshLayout/raw/master/gif/3.gif)
![image](https://github.com/linuxjava/HorizontalRefreshLayout/raw/master/gif/4.gif)

## 自定义Header
