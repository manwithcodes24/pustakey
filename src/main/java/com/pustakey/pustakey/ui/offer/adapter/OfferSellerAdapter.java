package com.pustakey.pustakey.ui.offer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;

import com.pustakey.pustakey.Config;
import com.pustakey.pustakey.R;
import com.pustakey.pustakey.databinding.ItemOfferSellerListAdapterBinding;
import com.pustakey.pustakey.ui.common.DataBoundListAdapter;
import com.pustakey.pustakey.ui.common.DataBoundViewHolder;
import com.pustakey.pustakey.utils.Constants;
import com.pustakey.pustakey.utils.Objects;
import com.pustakey.pustakey.utils.Utils;
import com.pustakey.pustakey.viewobject.Offer;

public class OfferSellerAdapter extends DataBoundListAdapter<Offer, ItemOfferSellerListAdapterBinding> {

    private final androidx.databinding.DataBindingComponent dataBindingComponent;
    private final OfferSellerAdapter.OfferListClickCallback callback;
    private DataBoundListAdapter.DiffUtilDispatchedInterface diffUtilDispatchedInterface;

    public OfferSellerAdapter(androidx.databinding.DataBindingComponent dataBindingComponent,
                              OfferSellerAdapter.OfferListClickCallback callback,
                              DiffUtilDispatchedInterface diffUtilDispatchedInterface) {
        this.dataBindingComponent = dataBindingComponent;
        this.callback = callback;
        this.diffUtilDispatchedInterface = diffUtilDispatchedInterface;

    }

    @Override
    protected ItemOfferSellerListAdapterBinding createBinding(ViewGroup parent) {
        ItemOfferSellerListAdapterBinding binding = (ItemOfferSellerListAdapterBinding) DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()),
                        R.layout.item_offer_seller_list_adapter, parent, false,
                        dataBindingComponent);

        binding.getRoot().setOnClickListener(v -> {

            Offer offer = binding.getOfferList();

            if (offer != null && callback != null) {
                callback.OnClick(offer, offer.id);
            }
        });

        return binding;
    }

    @Override
    public void bindView(DataBoundViewHolder<ItemOfferSellerListAdapterBinding> holder, int position) {
        super.bindView(holder, position);

    }

    @Override
    protected void dispatched() {
        if (diffUtilDispatchedInterface != null) {
            diffUtilDispatchedInterface.onDispatched();
        }
    }

    @Override
    protected void bind(ItemOfferSellerListAdapterBinding binding, Offer offer) {
        binding.setOfferList(offer);

        if (offer.isAccept.equals("1")) {
            binding.acceptTextView.setText(R.string.offer__accept_text);
        } else {
            binding.acceptTextView.setText(R.string.offer__offer_text);
        }

        if (!offer.item.itemCurrency.currencySymbol.equals("") && !offer.item.price.equals("")) {
            String currencySymbol = offer.item.itemCurrency.currencySymbol;
            String price;
            try {
                price = Utils.format(Double.parseDouble(offer.item.price));
            } catch (Exception e) {
                price = offer.item.price;
            }
            String currencyPrice;
            if (Config.SYMBOL_SHOW_FRONT) {
                currencyPrice = currencySymbol + " " + price;
            } else {
                currencyPrice = price + " " + currencySymbol;
            }
            binding.priceTextView.setText(currencyPrice);
        }
        binding.itemConditionTextView.setText(binding.getRoot().getResources().getString(R.string.item_condition__type, offer.item.itemCondition.name));

        if (offer.buyerUnreadCount.equals(Constants.ZERO)) {
            binding.countTextView.setVisibility(View.GONE);
        } else {
            binding.countTextView.setVisibility(View.VISIBLE);
        }

        if (offer.item.isSoldOut.equals(Constants.ONE)) {
            binding.soldTextView.setVisibility(View.VISIBLE);
        } else {
            binding.soldTextView.setVisibility(View.GONE);

        }

    }

    @Override
    protected boolean areItemsTheSame(Offer oldItem, Offer newItem) {
        return Objects.equals(oldItem.id, newItem.id) &&
                oldItem.addedDate.equals(newItem.addedDate) &&
                oldItem.buyerUnreadCount.equals(newItem.buyerUnreadCount) &&
                oldItem.isAccept.equals(newItem.isAccept);
    }

    @Override
    protected boolean areContentsTheSame(Offer oldItem, Offer newItem) {
        return Objects.equals(oldItem.id, newItem.id) &&
                oldItem.addedDate.equals(newItem.addedDate) &&
                oldItem.buyerUnreadCount.equals(newItem.buyerUnreadCount) &&
                oldItem.isAccept.equals(newItem.isAccept);
    }

    public interface OfferListClickCallback {
        void OnClick(Offer offer, String id);
    }

}
