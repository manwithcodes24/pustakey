package com.pustakey.pustakey.ui.item.itempricetype;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;

import com.pustakey.pustakey.R;
import com.pustakey.pustakey.databinding.ItemItemPriceTypeBinding;
import com.pustakey.pustakey.ui.common.DataBoundListAdapter;
import com.pustakey.pustakey.ui.common.DataBoundViewHolder;
import com.pustakey.pustakey.utils.Objects;
import com.pustakey.pustakey.viewobject.ItemPriceType;

public class ItemPriceTypeAdapter extends DataBoundListAdapter<ItemPriceType, ItemItemPriceTypeBinding> {

    private final androidx.databinding.DataBindingComponent dataBindingComponent;
    private final ItemPriceTypeAdapter.NewsClickCallback callback;
    private DataBoundListAdapter.DiffUtilDispatchedInterface diffUtilDispatchedInterface = null;
    public String itemPriceTypeId = "";

    public ItemPriceTypeAdapter(androidx.databinding.DataBindingComponent dataBindingComponent,
                           ItemPriceTypeAdapter.NewsClickCallback callback,
                           DiffUtilDispatchedInterface diffUtilDispatchedInterface) {
        this.dataBindingComponent = dataBindingComponent;
        this.callback = callback;
        this.diffUtilDispatchedInterface = diffUtilDispatchedInterface;
    }

    public ItemPriceTypeAdapter(androidx.databinding.DataBindingComponent dataBindingComponent,
                           ItemPriceTypeAdapter.NewsClickCallback callback, String itemPriceTypeId) {
        this.dataBindingComponent = dataBindingComponent;
        this.callback = callback;
        this.itemPriceTypeId = itemPriceTypeId;
    }

    @Override
    protected ItemItemPriceTypeBinding createBinding(ViewGroup parent) {
        ItemItemPriceTypeBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()),
                        R.layout.item_item_price_type, parent, false,
                        dataBindingComponent);

        binding.getRoot().setOnClickListener(v -> {

            ItemPriceType itemType = binding.getPriceType();

            if (itemType != null && callback != null) {

                binding.groupview.setBackgroundColor(parent.getResources().getColor(R.color.md_green_50));

                callback.onClick(itemType, itemType.id);
            }
        });
        return binding;
    }

    @Override
    public void bindView(DataBoundViewHolder<ItemItemPriceTypeBinding> holder, int position) {
        super.bindView(holder, position);

    }

    @Override
    protected void dispatched() {
        if (diffUtilDispatchedInterface != null) {
            diffUtilDispatchedInterface.onDispatched();
        }
    }

    @Override
    protected void bind(ItemItemPriceTypeBinding binding, ItemPriceType item) {
        binding.setPriceType(item);

        if (itemPriceTypeId != null) {
            if (item.id.equals(itemPriceTypeId)) {
                binding.groupview.setBackgroundColor(binding.groupview.getResources().getColor((R.color.md_green_50)));
            }else{
                binding.groupview.setBackgroundColor(binding.groupview.getResources().getColor(R.color.md_white_1000));
            }
        }

    }

    @Override
    protected boolean areItemsTheSame(ItemPriceType oldItem, ItemPriceType newItem) {
        return Objects.equals(oldItem.id, newItem.id);
    }

    @Override
    protected boolean areContentsTheSame(ItemPriceType oldItem, ItemPriceType newItem) {
        return Objects.equals(oldItem.id, newItem.id);
    }

    public interface NewsClickCallback {
        void onClick(ItemPriceType itemType, String id);
    }

}
