package xiao.free.horizontalrefreshlayout.refreshhead;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import xiao.free.horizontalrefreshlayout.R;
import xiao.free.horizontalrefreshlayout.RefreshHeader;

/**
 * Created by xiaoguochang on 2015/12/24.
 */
public class NiceRefreshHeader implements RefreshHeader {
    private final Context context;
    private ProgressBar progressBar;
    private ImageView staticLoading;

    public NiceRefreshHeader(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(ViewGroup container) {
        View view = LayoutInflater.from(context).inflate(R.layout.nice_refresh_header, container, false);
        progressBar = (ProgressBar) view.findViewById(R.id.progressbar);
        staticLoading = (ImageView) view.findViewById(R.id.static_loading);
        progressBar.setVisibility(View.INVISIBLE);

        return view;
    }

    @Override
    public void onStart(int dragPosition, View refreshHead) {
        staticLoading.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDragging(float distance, float percent, View refreshHead) {
        int num = (int) (percent * 10);

        switch (num){
            case 1:
            case 4:
            case 7:
                staticLoading.setBackgroundResource(R.drawable.ic_loading_1);
                break;
            case 2:
            case 5:
            case 8:
                staticLoading.setBackgroundResource(R.drawable.ic_loading_2);
                break;
            case 3:
            case 6:
            case 9:
                staticLoading.setBackgroundResource(R.drawable.ic_loading_3);
                break;
        }
    }

    @Override
    public void onReadyToRelease(View refreshHead) {

    }

    @Override
    public void onRefreshing(View refreshHead) {
        staticLoading.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }
}
