package jp.trackparty.android.etc;

import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseStaticListAdapter<T> extends RecyclerView.Adapter<BaseStaticListViewHolder<T>> {
    private final ArrayList<T> data = new ArrayList<>();

    @Override
    public void onBindViewHolder(BaseStaticListViewHolder<T> holder, int position) {
        holder.bind(getItem(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    protected T getItem(int position) {
        return data.get(position);
    }

    public void updateData(List<T> newData) {
        data.clear();
        data.addAll(newData);
        notifyDataSetChanged();
    }
}
