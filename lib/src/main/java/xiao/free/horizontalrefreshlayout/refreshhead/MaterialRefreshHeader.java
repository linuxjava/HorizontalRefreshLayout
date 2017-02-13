package xiao.free.horizontalrefreshlayout.refreshhead;

import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import xiao.free.horizontalrefreshlayout.HorizontalRefreshLayout;
import xiao.free.horizontalrefreshlayout.R;
import xiao.free.horizontalrefreshlayout.RefreshHeader;
import xiao.free.horizontalrefreshlayout.widget.CircleImageView;
import xiao.free.horizontalrefreshlayout.widget.MaterialProgressDrawable;


/**
 * Created by wangqi on 2016/7/21.
 */
public class MaterialRefreshHeader implements RefreshHeader {

    private final int startOrEnd;
    private CircleImageView mCircleView;
    private MaterialProgressDrawable mProgress;
    private ViewGroup parent;

    public MaterialRefreshHeader(int startOrEnd) {
        this.startOrEnd = startOrEnd;
    }

    @Override
    public void onStart(int dragPosition, View refreshHead) {
        mProgress.stop();
        mProgress.showArrow(false);
        mProgress.setAlpha(0);
        mProgress.setStartEndTrim(0f, 0f);
    }

    @Override
    public void onDragging(float distance, float percent, View refreshHead) {
        mProgress.showArrow(true);
        mProgress.setAlpha((int) (percent * 255));
        mProgress.setProgressRotation(percent);
        mProgress.setStartEndTrim(0f, Math.min(.8f, percent));
    }

    @Override
    public void onReadyToRelease(View refreshHead) {
    }

    @NonNull
    @Override
    public View getView(ViewGroup container) {
        this.parent = container;
        ViewGroup view = (ViewGroup) LayoutInflater.from(container.getContext()).inflate(R.layout.material_refresh_header, container, false);
        mCircleView = new CircleImageView(container.getContext(), 0xFFFAFAFA, 40 / 2);
        mProgress = new MaterialProgressDrawable(container.getContext(), container);
        mProgress.setBackgroundColor(0xFFFAFAFA);
        mCircleView.setImageDrawable(mProgress);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (startOrEnd == HorizontalRefreshLayout.LEFT) {
            layoutParams.gravity = Gravity.CENTER;
        } else {
            layoutParams.gravity = Gravity.CENTER;
        }
        mCircleView.setLayoutParams(layoutParams);
        view.addView(mCircleView);
        return view;
    }

    @Override
    public void onRefreshing(View refreshHead) {
        mProgress.showArrow(true);
        mProgress.setAlpha(255);
        mProgress.setProgressRotation(1);
        mProgress.setStartEndTrim(0f, Math.min(.8f, 1));
        mProgress.start();
    }
}
