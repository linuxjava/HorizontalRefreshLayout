:running:HorizontalRefreshLayout-Android:running:
============

开发者使用 HorizontalRefreshLayout-Android 可以对RecycView、Listview、ScrollView等控件实现左右刷新

## Gradle配置
compile 'xiao.free.horizontalrefreshlayout:lib:v0.1.2'

## XML配置
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
