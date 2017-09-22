package jp.trackparty.android.transport_item_list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jp.trackparty.android.data.realm.TransportItem;
import jp.trackparty.android.databinding.ListItemTransportItemBinding;
import jp.trackparty.android.etc.BaseStaticListAdapter;
import jp.trackparty.android.etc.BaseStaticListViewHolder;

class TransportItemListAdapter extends BaseStaticListAdapter<TransportItem> {
    @Override
    public BaseStaticListViewHolder<TransportItem> onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ListItemTransportItemBinding binding = ListItemTransportItemBinding.inflate(inflater, parent, false);
        return new TransportItemListViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(BaseStaticListViewHolder<TransportItem> holder, int position) {
        super.onBindViewHolder(holder, position);

        TransportItem item = getItem(position);
        holder.itemView.setTag(item);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onTransportItemClickListener != null && view.getTag() instanceof TransportItem) {
                    onTransportItemClickListener.onTransportItemClicked((TransportItem) view.getTag());
                }
            }
        });
    }

    public interface OnTransportItemClickListener {
        void onTransportItemClicked(TransportItem item);
    }
    private OnTransportItemClickListener onTransportItemClickListener;

    public void setOnTransportItemClickListener(OnTransportItemClickListener onTransportItemClickListener) {
        this.onTransportItemClickListener = onTransportItemClickListener;
    }
}
