package jp.trackparty.android.transport_item_list;

import jp.trackparty.android.data.realm.TransportItem;
import jp.trackparty.android.databinding.ListItemTransportItemBinding;
import jp.trackparty.android.etc.BaseStaticListViewHolder;

class TransportItemListViewHolder extends BaseStaticListViewHolder<TransportItem> {
    private final ListItemTransportItemBinding binding;

    public TransportItemListViewHolder(ListItemTransportItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    @Override
    public void bind(TransportItem transportItem) {
        binding.setTransportItem(transportItem);
    }
}
