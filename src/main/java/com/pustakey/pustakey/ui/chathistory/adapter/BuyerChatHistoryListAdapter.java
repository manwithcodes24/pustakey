package com.pustakey.pustakey.ui.chathistory.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;

import com.pustakey.pustakey.Config;
import com.pustakey.pustakey.R;
import com.pustakey.pustakey.databinding.ItemBuyerChatHistoryListAdapterBinding;
import com.pustakey.pustakey.ui.common.DataBoundListAdapter;
import com.pustakey.pustakey.ui.common.DataBoundViewHolder;
import com.pustakey.pustakey.utils.Constants;
import com.pustakey.pustakey.utils.Objects;
import com.pustakey.pustakey.utils.Utils;
import com.pustakey.pustakey.viewobject.ChatHistory;

public class BuyerChatHistoryListAdapter extends DataBoundListAdapter<ChatHistory, ItemBuyerChatHistoryListAdapterBinding> {

    private final androidx.databinding.DataBindingComponent dataBindingComponent;
    private final ChatHistoryClickCallback callback;
    private DiffUtilDispatchedInterface diffUtilDispatchedInterface;

    public BuyerChatHistoryListAdapter(androidx.databinding.DataBindingComponent dataBindingComponent,
                                       ChatHistoryClickCallback callback,
                                       DiffUtilDispatchedInterface diffUtilDispatchedInterface) {
        this.dataBindingComponent = dataBindingComponent;
        this.callback = callback;
        this.diffUtilDispatchedInterface = diffUtilDispatchedInterface;
    }

    @Override
    protected ItemBuyerChatHistoryListAdapterBinding createBinding(ViewGroup parent) {
        ItemBuyerChatHistoryListAdapterBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()),
                        R.layout.item_buyer_chat_history_list_adapter, parent, false,
                        dataBindingComponent);

        binding.getRoot().setOnClickListener(v -> {

            ChatHistory chatHistory = binding.getChatHistory();

            if (chatHistory != null && callback != null) {

                callback.onClick(chatHistory, chatHistory.id);
            }
        });
        return binding;
    }

    @Override
    public void bindView(DataBoundViewHolder<ItemBuyerChatHistoryListAdapterBinding> holder, int position) {
        super.bindView(holder, position);
    }

    @Override
    protected void dispatched() {
        if (diffUtilDispatchedInterface != null) {
            diffUtilDispatchedInterface.onDispatched();
        }
    }

    @Override
    protected void bind(ItemBuyerChatHistoryListAdapterBinding binding, ChatHistory chatHistory) {
        binding.setChatHistory(chatHistory);

        binding.itemConditionTextView.setText(binding.getRoot().getResources().getString(R.string.item_condition__type, chatHistory.item.itemCondition.name));


        if (!chatHistory.item.itemCurrency.currencySymbol.equals("") && !chatHistory.item.price.equals("")) {

            String currencySymbol = chatHistory.item.itemCurrency.currencySymbol;
            String price;
            try {
                price = Utils.format(Double.parseDouble(chatHistory.item.price));
            }catch (Exception e){
                price = chatHistory.item.price;
            }

            Utils.psLog(price + "******");


            String currencyPrice;
            if (Config.SYMBOL_SHOW_FRONT) {
                currencyPrice = currencySymbol + " " + price;
            } else {
                currencyPrice = price + " " + currencySymbol;
            }

            binding.priceTextView.setText(currencyPrice);
        }
        if (chatHistory.sellerUnreadCount.equals(Constants.ZERO)) {
            binding.countTextView.setVisibility(View.GONE);
        } else {
            binding.countTextView.setVisibility(View.VISIBLE);
        }

        if (chatHistory.item.isSoldOut.equals(Constants.ONE)) {
            binding.soldTextView.setVisibility(View.VISIBLE);
        } else {
            binding.soldTextView.setVisibility(View.GONE);
        }

    }

    @Override
    protected boolean areItemsTheSame(ChatHistory oldItem, ChatHistory newItem) {
        return Objects.equals(oldItem.id, newItem.id) &&
                oldItem.addedDate.equals(newItem.addedDate) &&
                oldItem.sellerUnreadCount.equals(newItem.sellerUnreadCount);
    }

    @Override
    protected boolean areContentsTheSame(ChatHistory oldItem, ChatHistory newItem) {
        return Objects.equals(oldItem.id, newItem.id) &&
                oldItem.addedDate.equals(newItem.addedDate) &&
                oldItem.sellerUnreadCount.equals(newItem.sellerUnreadCount);
    }

    public interface ChatHistoryClickCallback {
        void onClick(ChatHistory chatHistory, String id);
    }


}
