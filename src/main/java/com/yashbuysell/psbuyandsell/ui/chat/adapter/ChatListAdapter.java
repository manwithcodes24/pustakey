package com.yashbuysell.psbuyandsell.ui.chat.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.razorpay.Checkout;
import com.yashbuysell.psbuyandsell.BuyerdetailsForm;
import com.yashbuysell.psbuyandsell.R;
import com.yashbuysell.psbuyandsell.databinding.ItemChatListImageReceiverAdapterBinding;
import com.yashbuysell.psbuyandsell.databinding.ItemChatListImageSenderAdapterBinding;
import com.yashbuysell.psbuyandsell.databinding.ItemChatListMakeOfferReceiverBinding;
import com.yashbuysell.psbuyandsell.databinding.ItemChatListMakeOfferSenderBinding;
import com.yashbuysell.psbuyandsell.databinding.ItemChatListOfferAcceptedReceiverBinding;
import com.yashbuysell.psbuyandsell.databinding.ItemChatListOfferAcceptedSenderBinding;
import com.yashbuysell.psbuyandsell.databinding.ItemChatListOfferRejectedReceiverBinding;
import com.yashbuysell.psbuyandsell.databinding.ItemChatListOfferRejectedSenderBinding;
import com.yashbuysell.psbuyandsell.databinding.ItemChatListReceiverAdapterBinding;
import com.yashbuysell.psbuyandsell.databinding.ItemChatListSenderAdapterBinding;
import com.yashbuysell.psbuyandsell.databinding.ItemChatListTimeBinding;
import com.yashbuysell.psbuyandsell.databinding.ItemItemHasBeenSoldBinding;
import com.yashbuysell.psbuyandsell.ui.common.DataBoundListAdapter;
import com.yashbuysell.psbuyandsell.utils.Constants;
import com.yashbuysell.psbuyandsell.utils.Objects;
import com.yashbuysell.psbuyandsell.viewmodel.item.ItemViewModel;
import com.yashbuysell.psbuyandsell.viewobject.messageHolder.Message;

import org.json.JSONObject;

import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;

public class ChatListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final androidx.databinding.DataBindingComponent dataBindingComponent;
    private DataBoundListAdapter.DiffUtilDispatchedInterface diffUtilDispatchedInterface;
    private final clickCallBack callback;
    private List<Message> messageList;
    private int dataVersion = 0;
    private String userId;
    private String itemId;
    private String otherUserProfileUrl;
    private ItemViewModel itemViewModel ;
    private DatabaseReference mDatabase , courierInfoDB;

    private String labelUrl ;
    public ChatListAdapter(androidx.databinding.DataBindingComponent dataBindingComponent, DataBoundListAdapter.DiffUtilDispatchedInterface diffUtilDispatchedInterface, String userId,
                           clickCallBack callback, String itemId, String otherUserProfileUrl) {
        this.dataBindingComponent = dataBindingComponent;
        this.diffUtilDispatchedInterface = diffUtilDispatchedInterface;
        this.userId = userId;
        this.callback = callback;
        this.itemId = itemId;
        this.otherUserProfileUrl = otherUserProfileUrl;

    }

    public static interface AdapterCallback {
        void onMethodCallback();
    }

    public class ImageSenderViewHolder extends RecyclerView.ViewHolder {

        private final ItemChatListImageSenderAdapterBinding imageAdapterBinding;

        ImageSenderViewHolder(ItemChatListImageSenderAdapterBinding itemChatListImageAdapterBinding) {
            super(itemChatListImageAdapterBinding.getRoot());
            this.imageAdapterBinding = itemChatListImageAdapterBinding;
        }

        public void bind(Message message) {
            imageAdapterBinding.setMessage(message);
            imageAdapterBinding.executePendingBindings();


            imageAdapterBinding.chatImageView.setOnClickListener(v -> {
                Message chatMessage = imageAdapterBinding.getMessage();

                callback.onImageClicked(chatMessage);
            });
        }
    }

    public class TextSenderViewHolder extends RecyclerView.ViewHolder {

        private final ItemChatListSenderAdapterBinding textAdapterBinding;

        TextSenderViewHolder(ItemChatListSenderAdapterBinding itemChatListAdapterBinding) {
            super(itemChatListAdapterBinding.getRoot());
            this.textAdapterBinding = itemChatListAdapterBinding;
        }

        public void bind(Message message) {
            textAdapterBinding.setMessage(message);
            textAdapterBinding.executePendingBindings();
        }
    }

    public class ImageReceiverViewHolder extends RecyclerView.ViewHolder {

        private final ItemChatListImageReceiverAdapterBinding imageAdapterBinding;

        ImageReceiverViewHolder(ItemChatListImageReceiverAdapterBinding itemChatListImageAdapterBinding) {
            super(itemChatListImageAdapterBinding.getRoot());
            this.imageAdapterBinding = itemChatListImageAdapterBinding;
        }

        public void bind(Message message) {
            imageAdapterBinding.setMessage(message);

            imageAdapterBinding.profileImageView.setOnClickListener(v -> {
                Message chatMessage = imageAdapterBinding.getMessage();
                if (chatMessage != null && callback != null) {
                    callback.onProfileClicked();
                }
            });

            imageAdapterBinding.chatImageView.setOnClickListener(v -> {
                Message chatMessage = imageAdapterBinding.getMessage();
                callback.onImageClicked(chatMessage);
            });

            dataBindingComponent.getFragmentBindingAdapters().bindCircleImage(imageAdapterBinding.profileImageView, otherUserProfileUrl);


            imageAdapterBinding.executePendingBindings();
        }
    }

    public class TextReceiverViewHolder extends RecyclerView.ViewHolder {

        private final ItemChatListReceiverAdapterBinding textAdapterBinding;

        TextReceiverViewHolder(ItemChatListReceiverAdapterBinding itemChatListAdapterBinding) {
            super(itemChatListAdapterBinding.getRoot());
            this.textAdapterBinding = itemChatListAdapterBinding;
        }

        public void bind(Message message) {
            textAdapterBinding.setMessage(message);

            textAdapterBinding.profileImageView.setOnClickListener(v -> {
                Message chatMessage = textAdapterBinding.getMessage();
                if (chatMessage != null && callback != null) {
                    callback.onProfileClicked();
                }
            });

            dataBindingComponent.getFragmentBindingAdapters().bindCircleImage(textAdapterBinding.profileImageView, otherUserProfileUrl);


            textAdapterBinding.executePendingBindings();
        }
    }

    public class TimeViewHolder extends RecyclerView.ViewHolder {

        private final ItemChatListTimeBinding itemChatListTimeBinding;

        TimeViewHolder(ItemChatListTimeBinding itemChatListTimeBinding) {
            super(itemChatListTimeBinding.getRoot());
            this.itemChatListTimeBinding = itemChatListTimeBinding;
        }

        public void bind(Message message) {
            itemChatListTimeBinding.setMessage(message);
            itemChatListTimeBinding.executePendingBindings();
        }
    }

    public class MakeOfferSenderViewHolder extends RecyclerView.ViewHolder {

        private final ItemChatListMakeOfferSenderBinding itemChatListMakeOfferSenderBinding;

        MakeOfferSenderViewHolder(ItemChatListMakeOfferSenderBinding itemChatListMakeOfferSenderBinding) {
            super(itemChatListMakeOfferSenderBinding.getRoot());
            this.itemChatListMakeOfferSenderBinding = itemChatListMakeOfferSenderBinding;
        }

        public void bind(Message message) {
            itemChatListMakeOfferSenderBinding.setMessage(message);
            itemChatListMakeOfferSenderBinding.executePendingBindings();
        }
    }

    public class MakeOfferReceiverViewHolder extends RecyclerView.ViewHolder {

        private final ItemChatListMakeOfferReceiverBinding itemChatListMakeOfferReceiverBinding;

        MakeOfferReceiverViewHolder(ItemChatListMakeOfferReceiverBinding itemChatListMakeOfferReceiverBinding) {
            super(itemChatListMakeOfferReceiverBinding.getRoot());
            this.itemChatListMakeOfferReceiverBinding = itemChatListMakeOfferReceiverBinding;
        }

        @SuppressLint("StringFormatInvalid")
        public void bind(Message message) {
            itemChatListMakeOfferReceiverBinding.setMessage(message);

            if (message.offerStatus == Constants.CHAT_STATUS_ACCEPT
                    || message.offerStatus == Constants.CHAT_STATUS_REJECT) {
                itemChatListMakeOfferReceiverBinding.acceptButton.setVisibility(View.GONE);
                itemChatListMakeOfferReceiverBinding.rejectButton.setVisibility(View.GONE);
                itemChatListMakeOfferReceiverBinding.spaceView.setVisibility(View.GONE);
            } else {
                itemChatListMakeOfferReceiverBinding.acceptButton.setVisibility(View.VISIBLE);
                itemChatListMakeOfferReceiverBinding.rejectButton.setVisibility(View.VISIBLE);
                itemChatListMakeOfferReceiverBinding.spaceView.setVisibility(View.VISIBLE);
            }

            itemChatListMakeOfferReceiverBinding.profileImageView.setOnClickListener(v -> {
                Message chatMessage = itemChatListMakeOfferReceiverBinding.getMessage();
                if (chatMessage != null && callback != null) {
                    callback.onProfileClicked();
                }
            });

            itemChatListMakeOfferReceiverBinding.acceptButton.setOnClickListener(v -> {
                Message chatMessage = itemChatListMakeOfferReceiverBinding.getMessage();

                callback.onAcceptButtonClicked(chatMessage);
            });

            itemChatListMakeOfferReceiverBinding.rejectButton.setOnClickListener(v -> {
                Message chatMessage = itemChatListMakeOfferReceiverBinding.getMessage();

                callback.onRejectButtonClicked(chatMessage);
            });

            dataBindingComponent.getFragmentBindingAdapters().bindCircleImage(itemChatListMakeOfferReceiverBinding.profileImageView, otherUserProfileUrl);

            itemChatListMakeOfferReceiverBinding.executePendingBindings();
        }
    }

    public class OfferSenderAcceptedViewHolder extends RecyclerView.ViewHolder {

        private final ItemChatListOfferAcceptedSenderBinding itemChatListOfferAcceptedSenderBinding;

        OfferSenderAcceptedViewHolder(ItemChatListOfferAcceptedSenderBinding itemChatListOfferAcceptedSenderBinding) {
            super(itemChatListOfferAcceptedSenderBinding.getRoot());
            this.itemChatListOfferAcceptedSenderBinding = itemChatListOfferAcceptedSenderBinding;
        }

        public void bind(Message message) {
            itemChatListOfferAcceptedSenderBinding.setMessage(message);
            itemChatListOfferAcceptedSenderBinding.executePendingBindings();

            if (message.isSold) {
                itemChatListOfferAcceptedSenderBinding.markAsSoldButton.setVisibility(View.GONE);
            } else {
                itemChatListOfferAcceptedSenderBinding.markAsSoldButton.setVisibility(View.VISIBLE);
            }

            itemChatListOfferAcceptedSenderBinding.markAsSoldButton.setOnClickListener(v -> {
                Message chatMessage = itemChatListOfferAcceptedSenderBinding.getMessage();

                callback.onMarkAsSoldButtonClicked(chatMessage);
            });



        }
    }

    public class OfferReceiverAcceptedViewHolder extends RecyclerView.ViewHolder {


        private final ItemChatListOfferAcceptedReceiverBinding itemChatListOfferAcceptedReceiverBinding;

        OfferReceiverAcceptedViewHolder(ItemChatListOfferAcceptedReceiverBinding OfferReceiverAcceptedViewHolder) {
            super(OfferReceiverAcceptedViewHolder.getRoot());
            this.itemChatListOfferAcceptedReceiverBinding = OfferReceiverAcceptedViewHolder;
        }

        public void bind(Message message) {
            itemChatListOfferAcceptedReceiverBinding.setMessage(message);

            itemChatListOfferAcceptedReceiverBinding.profileImageView.setOnClickListener(v -> {
                Message chatMessage = itemChatListOfferAcceptedReceiverBinding.getMessage();
                if (chatMessage != null && callback != null) {
                    callback.onProfileClicked();
                }
            });
//            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMessageReceiver,
//                    new IntentFilter("com.yashbuysell.psbuyandsell_broadcast-Label"));
            mDatabase = FirebaseDatabase.getInstance().getReference(itemId);
            mDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        String paymentStatus =   snapshot.child("payment").getValue().toString() ;


//                            labelUrl =  snapshot.child("labelUrl").getValue().toString() ;
//                            binding.get().labelButton.setVisibility(View.VISIBLE);

                        labelUrl =  snapshot.child("labelUrl").getValue().toString() ;
                        Log.d("orderStatus" , "labelUrl = " + labelUrl);
                        if(labelUrl != null && labelUrl != Constants.EMPTY_STRING && labelUrl != "null"){
                            itemChatListOfferAcceptedReceiverBinding.payButton.setVisibility(View.GONE);

                        }





                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            }) ;


            itemChatListOfferAcceptedReceiverBinding.payButton.setOnClickListener(v -> {
                Log.d("courierOrder", "itemId = " + itemId) ;




                try {
                    mDatabase = FirebaseDatabase.getInstance().getReference(itemId) ;
                    courierInfoDB = mDatabase.child("courierDetails");
                    Log.d("courierOrder", "final Price = " + itemChatListOfferAcceptedReceiverBinding.priceTextView.getText().toString().substring(2)) ;
                    courierInfoDB.child("price").setValue(itemChatListOfferAcceptedReceiverBinding.priceTextView.getText().toString().substring(2));


                }
                catch (Exception e) {
                    e.printStackTrace();
                    Log.d("string" , e.toString()) ;
                }

                Log.d("itemId" , "itemId at ChatList " + itemId);
                Checkout.preload(getApplicationContext());
//                startPayment();
                try {

                    Intent myIntent = new Intent(getApplicationContext(), BuyerdetailsForm.class);
                    myIntent.putExtra("itemId", itemId);
                    myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(myIntent);
                }
                catch (Exception  e) {
                    Log.d("error" , e.toString()) ;
                }


            });

//            itemChatListOfferAcceptedReceiverBinding.labelButton.setOnClickListener(view -> {
//                Uri uri = Uri.parse(labelUrl); // missing 'http://' will cause crashed
//                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//                getApplicationContext().startActivity(intent);
//            });




            dataBindingComponent.getFragmentBindingAdapters().bindCircleImage(itemChatListOfferAcceptedReceiverBinding.profileImageView, otherUserProfileUrl);

            itemChatListOfferAcceptedReceiverBinding.executePendingBindings();
        }
        public void startPayment() {

            /**
             * Instantiate Checkout
             */
            Checkout checkout = new Checkout();
            checkout.setKeyID(getApplicationContext().getString(R.string.razor_key));
            /**
             * Set your logo here
             */
            checkout.setImage(R.drawable.app_icon);

            /**
             * Reference to current activity
             */

            /**
             * Pass your payment options to the Razorpay Checkout as a JSONObject
             */
            try {
                JSONObject options = new JSONObject();

                options.put("name", "Merchant Name");
                options.put("description", "Reference No. #123456");
                options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png");
                options.put("order_id", itemId);//from response of step 3.
                options.put("theme.color", "#3399cc");
                options.put("currency", "INR");
                options.put("amount", "50000");//pass amount in currency subunits
                options.put("prefill.email", "gaurav.kumar@example.com");
                options.put("prefill.contact","9988776655");
                checkout.open((Activity) getApplicationContext(), options);
            } catch(Exception e) {
                Log.e("rzError", "Error in starting Razorpay Checkout", e);
            }
        }
//        private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
//
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                Log.d("receiver" , "Got label url") ;
//                labelUrl = intent.getStringExtra("label");
//                itemChatListOfferAcceptedReceiverBinding.payButton.setVisibility(View.GONE);
//                if(labelUrl != Constants.EMPTY_STRING) {
//                    itemChatListOfferAcceptedReceiverBinding.labelButton.setVisibility(View.VISIBLE);
//
//                }
//
//
//            }
//        };
    }

    public class OfferSenderRejectedViewHolder extends RecyclerView.ViewHolder {

        private final ItemChatListOfferRejectedSenderBinding itemChatListOfferRejectedSenderBinding;

        OfferSenderRejectedViewHolder(ItemChatListOfferRejectedSenderBinding itemChatListOfferRejectedSenderBinding) {
            super(itemChatListOfferRejectedSenderBinding.getRoot());
            this.itemChatListOfferRejectedSenderBinding = itemChatListOfferRejectedSenderBinding;
        }

        public void bind(Message message) {
            itemChatListOfferRejectedSenderBinding.setMessage(message);
            itemChatListOfferRejectedSenderBinding.executePendingBindings();
        }
    }

    public class OfferReceiverRejectedViewHolder extends RecyclerView.ViewHolder {

        private final ItemChatListOfferRejectedReceiverBinding itemChatListOfferRejectedReceiverBinding;

        OfferReceiverRejectedViewHolder(ItemChatListOfferRejectedReceiverBinding OfferReceiverAcceptedViewHolder) {
            super(OfferReceiverAcceptedViewHolder.getRoot());
            this.itemChatListOfferRejectedReceiverBinding = OfferReceiverAcceptedViewHolder;
        }

        public void bind(Message message) {
            itemChatListOfferRejectedReceiverBinding.setMessage(message);
            itemChatListOfferRejectedReceiverBinding.profileImageView.setOnClickListener(v -> {
                Message chatMessage = itemChatListOfferRejectedReceiverBinding.getMessage();
                if (chatMessage != null && callback != null) {
                    callback.onProfileClicked();
                }
            });

            dataBindingComponent.getFragmentBindingAdapters().bindCircleImage(itemChatListOfferRejectedReceiverBinding.profileImageView, otherUserProfileUrl);

            itemChatListOfferRejectedReceiverBinding.executePendingBindings();
        }
    }

    public class ItemHasBeenSoldViewHolder extends RecyclerView.ViewHolder {

        private final ItemItemHasBeenSoldBinding itemItemHasBeenSoldBinding;

        ItemHasBeenSoldViewHolder(ItemItemHasBeenSoldBinding itemItemHasBeenSoldBinding) {
            super(itemItemHasBeenSoldBinding.getRoot());
            this.itemItemHasBeenSoldBinding = itemItemHasBeenSoldBinding;
        }

        public void bind(Message message) {
            itemItemHasBeenSoldBinding.setMessage(message);
            itemItemHasBeenSoldBinding.executePendingBindings();

            itemItemHasBeenSoldBinding.giveReviewButton.setOnClickListener(v -> callback.onGiveReviewClicked());
        }
    }

    protected void dispatched() {
        if (diffUtilDispatchedInterface != null) {
            diffUtilDispatchedInterface.onDispatched();
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == Constants.CHAT_DATE_UI) {

            ItemChatListTimeBinding itemChatListTimeBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_chat_list_time, parent, false, dataBindingComponent);

            return new TimeViewHolder(itemChatListTimeBinding);

        } else if (viewType == Constants.CHAT_SENDER_UI) {

            ItemChatListSenderAdapterBinding textSenderBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_chat_list_sender_adapter, parent, false, dataBindingComponent);

            return new TextSenderViewHolder(textSenderBinding);

        } else if (viewType == Constants.CHAT_SENDER_IMAGE_UI) {

            ItemChatListImageSenderAdapterBinding imageSenderBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_chat_list_image_sender_adapter, parent, false, dataBindingComponent);

            return new ImageSenderViewHolder(imageSenderBinding);

        } else if (viewType == Constants.CHAT_RECEIVER_UI) {

            ItemChatListReceiverAdapterBinding textReceiverBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_chat_list_receiver_adapter, parent, false, dataBindingComponent);

            return new TextReceiverViewHolder(textReceiverBinding);

        } else if (viewType == Constants.CHAT_RECEIVER_IMAGE_UI) {

            ItemChatListImageReceiverAdapterBinding imageReceiverBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_chat_list_image_receiver_adapter, parent, false, dataBindingComponent);

            return new ImageReceiverViewHolder(imageReceiverBinding);

        } else if (viewType == Constants.CHAT_SENDER_OFFER_UI) {

            ItemChatListMakeOfferSenderBinding makeOfferSenderBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_chat_list_make_offer_sender, parent, false, dataBindingComponent);

            return new MakeOfferSenderViewHolder(makeOfferSenderBinding);

        } else if (viewType == Constants.CHAT_RECEIVER_OFFER_UI) {

            ItemChatListMakeOfferReceiverBinding makeOfferReceiverBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_chat_list_make_offer_receiver, parent, false, dataBindingComponent);

            return new MakeOfferReceiverViewHolder(makeOfferReceiverBinding);

        } else if (viewType == Constants.CHAT_SENDER_OFFER_ACCEPT_UI) {

            ItemChatListOfferAcceptedSenderBinding itemChatListOfferAcceptedSenderBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_chat_list_offer_accepted_sender, parent, false, dataBindingComponent);

            return new OfferSenderAcceptedViewHolder(itemChatListOfferAcceptedSenderBinding);

        } else if (viewType == Constants.CHAT_RECEIVER_OFFER_ACCEPT_UI) {

            ItemChatListOfferAcceptedReceiverBinding itemChatListOfferAcceptedReceiverBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_chat_list_offer_accepted_receiver, parent, false, dataBindingComponent);

            return new OfferReceiverAcceptedViewHolder(itemChatListOfferAcceptedReceiverBinding);

        } else if (viewType == Constants.CHAT_SENDER_OFFER_REJECT_UI) {

            ItemChatListOfferRejectedSenderBinding itemChatListOfferRejectedSenderBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_chat_list_offer_rejected_sender, parent, false, dataBindingComponent);

            return new OfferSenderRejectedViewHolder(itemChatListOfferRejectedSenderBinding);

        } else if (viewType == Constants.CHAT_RECEIVER_OFFER_REJECT_UI) {

            ItemChatListOfferRejectedReceiverBinding itemChatListOfferRejectedReceiverBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_chat_list_offer_rejected_receiver, parent, false, dataBindingComponent);

            return new OfferReceiverRejectedViewHolder(itemChatListOfferRejectedReceiverBinding);

        } else if (viewType == Constants.CHAT_ITEM_HAS_BEEN_SOLD) {

            ItemItemHasBeenSoldBinding itemItemHasBeenSoldBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_item_has_been_sold, parent, false, dataBindingComponent);

            return new ItemHasBeenSoldViewHolder(itemItemHasBeenSoldBinding);

        } else {

            ItemChatListTimeBinding itemChatListTimeBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_chat_list_time, parent, false, dataBindingComponent);

            return new TimeViewHolder(itemChatListTimeBinding);

        }
    }

    @Override
    public int getItemViewType(int position) {

        if (messageList != null && messageList.size() > 0) {

            if (position != messageList.size()) {

                if (messageList.get(position).type == Constants.CHAT_TYPE_DATE) {

                    return Constants.CHAT_DATE_UI;

                } else {
                    if (messageList.get(position).sendByUserId.equals(userId)) { //sender

                        if (messageList.get(position).type == Constants.CHAT_TYPE_TEXT) {

                            if (messageList.get(position).offerStatus == Constants.CHAT_STATUS_OFFER) {
                                return Constants.CHAT_SENDER_OFFER_UI;
                            } else if (messageList.get(position).offerStatus == Constants.CHAT_STATUS_ACCEPT) {
                                return Constants.CHAT_SENDER_OFFER_ACCEPT_UI;
                            } else if (messageList.get(position).offerStatus == Constants.CHAT_STATUS_REJECT) {
                                return Constants.CHAT_SENDER_OFFER_REJECT_UI;
                            } else {
                                return Constants.CHAT_SENDER_UI;
                            }

                        } else if (messageList.get(position).type == Constants.CHAT_TYPE_OFFER) {

                            return Constants.CHAT_SENDER_OFFER_UI;

                        } else if (messageList.get(position).type == Constants.CHAT_TYPE_SOLD) {

                            return Constants.CHAT_ITEM_HAS_BEEN_SOLD;

                        } else {

                            return Constants.CHAT_SENDER_IMAGE_UI;

                        }
                    } else { //receiver
                        if (messageList.get(position).type == Constants.CHAT_TYPE_TEXT) {

                            if (messageList.get(position).offerStatus == Constants.CHAT_STATUS_OFFER) {
                                return Constants.CHAT_RECEIVER_OFFER_UI;
                            } else if (messageList.get(position).offerStatus == Constants.CHAT_STATUS_ACCEPT) {
                                return Constants.CHAT_RECEIVER_OFFER_ACCEPT_UI;
                            } else if (messageList.get(position).offerStatus == Constants.CHAT_STATUS_REJECT) {
                                return Constants.CHAT_RECEIVER_OFFER_REJECT_UI;
                            } else {
                                return Constants.CHAT_RECEIVER_UI;
                            }

                        } else if (messageList.get(position).type == Constants.CHAT_TYPE_OFFER) {
                            return Constants.CHAT_RECEIVER_OFFER_UI;
                        } else if (messageList.get(position).type == Constants.CHAT_TYPE_SOLD) {
                            return Constants.CHAT_ITEM_HAS_BEEN_SOLD;
                        } else {
                            return Constants.CHAT_RECEIVER_IMAGE_UI;
                        }
                    }
                }
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (messageList.get(position).itemId.equals(itemId)) {
            if (holder instanceof TextSenderViewHolder) {

                ((TextSenderViewHolder) holder).bind(messageList.get(position));

            } else if (holder instanceof ImageSenderViewHolder) {

                ((ImageSenderViewHolder) holder).bind(messageList.get(position));

            } else if (holder instanceof TextReceiverViewHolder) {

                ((TextReceiverViewHolder) holder).bind(messageList.get(position));

            } else if (holder instanceof ImageReceiverViewHolder) {

                ((ImageReceiverViewHolder) holder).bind(messageList.get(position));

            } else if (holder instanceof TimeViewHolder) {

                ((TimeViewHolder) holder).bind(messageList.get(position));

            } else if (holder instanceof MakeOfferSenderViewHolder) {

                ((MakeOfferSenderViewHolder) holder).bind(messageList.get(position));

            } else if (holder instanceof MakeOfferReceiverViewHolder) {

                ((MakeOfferReceiverViewHolder) holder).bind(messageList.get(position));

            } else if (holder instanceof OfferSenderAcceptedViewHolder) {

                ((OfferSenderAcceptedViewHolder) holder).bind(messageList.get(position));

            } else if (holder instanceof OfferReceiverAcceptedViewHolder) {

                ((OfferReceiverAcceptedViewHolder) holder).bind(messageList.get(position));

            } else if (holder instanceof OfferSenderRejectedViewHolder) {

                ((OfferSenderRejectedViewHolder) holder).bind(messageList.get(position));

            } else if (holder instanceof OfferReceiverRejectedViewHolder) {

                ((OfferReceiverRejectedViewHolder) holder).bind(messageList.get(position));

            } else if (holder instanceof ItemHasBeenSoldViewHolder) {

                ((ItemHasBeenSoldViewHolder) holder).bind(messageList.get(position));

            }

        }
    }

    public interface clickCallBack {
        void onImageClicked(Message message);

        void onAcceptButtonClicked(Message message);

        void onRejectButtonClicked(Message message);

        void onMarkAsSoldButtonClicked(Message message);

        void onProfileClicked();

        void onGiveReviewClicked();
    }

    @Override
    public int getItemCount() {

        if (messageList != null && messageList.size() > 0) {
            return messageList.size();
        } else {
            return 0;
        }
    }

    @SuppressLint("StaticFieldLeak")
    @MainThread
    public void replaceMessageList(List<Message> update) {
        dataVersion++;
        if (messageList == null) {
            if (update == null) {
                return;
            }
            messageList = update;
            notifyDataSetChanged();
        } else if (update == null) {
            int oldSize = messageList.size();
            messageList = null;
            notifyItemRangeRemoved(0, oldSize);

        } else {
            final int startVersion = dataVersion;
            final List<Message> oldItems = messageList;
            new AsyncTask<Void, Void, DiffUtil.DiffResult>() {
                @Override
                protected DiffUtil.DiffResult doInBackground(Void... voids) {
                    return DiffUtil.calculateDiff(new DiffUtil.Callback() {
                        @Override
                        public int getOldListSize() {
                            return oldItems.size();
                        }

                        @Override
                        public int getNewListSize() {
                            return update.size();
                        }

                        @Override
                        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                            Message oldItem = oldItems.get(oldItemPosition);
                            Message newItem = update.get(newItemPosition);

                            return Objects.equals(oldItem.addedDate, newItem.addedDate)
                                    && oldItem.message.equals(newItem.message);
                        }

                        @Override
                        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                            Message oldItem = oldItems.get(oldItemPosition);
                            Message newItem = update.get(newItemPosition);

                            return Objects.equals(oldItem.addedDate, newItem.addedDate)
                                    && oldItem.message.equals(newItem.message);
                        }
                    });
                }

                @Override
                protected void onPostExecute(DiffUtil.DiffResult diffResult) {
                    if (startVersion != dataVersion) {
                        // ignore update
                        return;
                    }

                    diffResult.dispatchUpdatesTo(ChatListAdapter.this);
                    dispatched();

                    messageList = update;
                    notifyDataSetChanged();
                    diffResult.dispatchUpdatesTo(ChatListAdapter.this);
                }
            }.execute();
        }
    }



}





