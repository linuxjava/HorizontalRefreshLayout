package xiao.free.horizontalrefreshlayout;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by wangqi on 2015/12/24.
 */
public interface RefreshHeader {

    /**
     * @param dragPosition  HorizontalRefreshLayout.START or HorizontalRefreshLayout.END
     */
    void onStart(int dragPosition, View refreshHead);

    /**
     * @param distance
     */
    void onDragging(float distance, float percent, View refreshHead);

    void onReadyToRelease(View refreshHead);

    @NonNull View getView(ViewGroup container);

    void onRefreshing(View refreshHead);
}
