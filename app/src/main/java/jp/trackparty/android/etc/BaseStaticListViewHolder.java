package jp.trackparty.android.etc;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 */
public abstract class BaseStaticListViewHolder<T> extends RecyclerView.ViewHolder {
    public BaseStaticListViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void bind(T model);
}
