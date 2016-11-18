package com.equinox.qikdriver.Adapters;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.equinox.qikdriver.Activities.MainActivity;
import com.equinox.qikdriver.Enums.OrderStatus;
import com.equinox.qikdriver.Enums.Vehicle;
import com.equinox.qikdriver.Models.DataHolder;
import com.equinox.qikdriver.Models.Item;
import com.equinox.qikdriver.Models.Order;
import com.equinox.qikdriver.Models.Place;
import com.equinox.qikdriver.Models.User;
import com.equinox.qikdriver.R;
import com.equinox.qikdriver.Utils.StringManipulation;
import com.equinox.qikdriver.ViewHolders.NearbyPlacesViewHolder;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.equinox.qikdriver.Enums.OrderStatus.CANCELLED;
import static com.equinox.qikdriver.Enums.OrderStatus.COMPLETED;
import static com.equinox.qikdriver.Enums.OrderStatus.DELIVERED;
import static com.equinox.qikdriver.Enums.OrderStatus.ENROUTE;
import static com.equinox.qikdriver.Enums.OrderStatus.INCOMING;
import static com.equinox.qikdriver.Enums.OrderStatus.PICKED_UP;
import static com.equinox.qikdriver.Enums.OrderStatus.PROCESSING;
import static com.equinox.qikdriver.Models.Constants.DEADLINE;
import static com.equinox.qikdriver.Models.Constants.DRIVER;
import static com.equinox.qikdriver.Models.Constants.EXCHANGE_ITEM;
import static com.equinox.qikdriver.Models.Constants.LOCATION_LAT;
import static com.equinox.qikdriver.Models.Constants.LOCATION_LNG;
import static com.equinox.qikdriver.Models.Constants.ORDERS;
import static com.equinox.qikdriver.Models.Constants.ORDER_FROM;
import static com.equinox.qikdriver.Models.Constants.ORDER_ITEMS;
import static com.equinox.qikdriver.Models.Constants.ORDER_PAYLOAD;
import static com.equinox.qikdriver.Models.Constants.ORDER_STATUS;
import static com.equinox.qikdriver.Models.Constants.PLACE_NAME;
import static com.equinox.qikdriver.Models.Constants.TIMESTAMP;

/**
 * Created by mukht on 11/14/2016.
 */

public class NearbyPlacesRecyclerAdapter extends RecyclerView.Adapter<NearbyPlacesViewHolder> {

    private List<Place> placeList;
    private Integer orderCount;
    private TextView orderCountView;
    private Activity activity;

    public NearbyPlacesRecyclerAdapter(List<Place> placeList, TextView orderCountView, Activity activity) {
        this.placeList = placeList;
        this.orderCountView = orderCountView;
        this.activity = activity;
    }

    @Override
    public NearbyPlacesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View holder = LayoutInflater.from(parent.getContext()).inflate(R.layout.pending_order_shop_list_item, parent, false);
        return new NearbyPlacesViewHolder(holder, activity, placeList);
    }

    @Override
    public void onBindViewHolder(final NearbyPlacesViewHolder holder, final int position) {
        final Place place = placeList.get(position);
        orderCount = 0;
        holder.getPlaceName().setText(StringManipulation.CapsFirst(place.getName()));
        final DatabaseReference businessOrderReference = DataHolder.database.getReference().child(place.getPlaceId()).child(ORDERS);
        businessOrderReference.addValueEventListener(new ValueEventListener() {
            Integer incomingCount=0, processingCount=0, completedCount=0, deadline=Integer.MAX_VALUE;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.hasChildren()) {
                        Iterator<DataSnapshot> iteratorIncoming = dataSnapshot.getChildren().iterator();
                        HashMap<String, Object> iteratorOrderObj;
                        while (iteratorIncoming.hasNext()) {
                            DataSnapshot orderShot = iteratorIncoming.next();
                            iteratorOrderObj = (HashMap<String, Object>) orderShot.getValue();
                            if (iteratorOrderObj.get(ORDER_STATUS).equals(INCOMING.getNodeName())) incomingCount++;
                            if (iteratorOrderObj.get(ORDER_STATUS).equals(PROCESSING.getNodeName())) {
                                processingCount++;
                                if (incomingCount > 0) incomingCount--;
                            }
                            if (iteratorOrderObj.get(ORDER_STATUS).equals(COMPLETED.getNodeName())) {
                                completedCount++;
                                if (processingCount > 0) processingCount--;
                            }
                            if (iteratorOrderObj.containsKey(DEADLINE))
                                if ((Long) iteratorOrderObj.get(DEADLINE) < deadline)
                                    deadline = (int) (long) iteratorOrderObj.get(DEADLINE);

                            Order tempOrder = new Order();
                            tempOrder.setId(orderShot.getKey());
                            tempOrder.setExchange((Boolean) iteratorOrderObj.get(EXCHANGE_ITEM));
                            if (iteratorOrderObj.get(ORDER_PAYLOAD) instanceof Long)
                                tempOrder.setWeight((float) (long) iteratorOrderObj.get(ORDER_PAYLOAD));
                            else
                                tempOrder.setWeight((float) (double) iteratorOrderObj.get(ORDER_PAYLOAD));
                            tempOrder.setStatus((String) iteratorOrderObj.get(ORDER_STATUS));
                            tempOrder.setFrom(new User().fromMap((HashMap<String, Object>)iteratorOrderObj.get(ORDER_FROM)));
                            tempOrder.setTimestamp((Long) iteratorOrderObj.get(TIMESTAMP));
                            List<Item> tempItemList = new ArrayList<>();
                            HashMap<String, Object> iteratorItemObject = (HashMap<String, Object>) iteratorOrderObj.get(ORDER_ITEMS);
                            for (Map.Entry<String, Object> itemObject : iteratorItemObject.entrySet()) {
                                Item tempItem = new Item();
                                tempItemList.add(tempItem.fromMap((HashMap<String, Object>) itemObject.getValue()));
                            }
                            tempOrder.setItems(tempItemList);
                            tempOrder.setShop(place);
                            updateOrderCount();
                            if (DataHolder.orderList.containsKey(place.getPlaceId()))
                                DataHolder.orderList.get(place.getPlaceId()).add(tempOrder);
                            else {
                                List<Order> tempOrders = new ArrayList<>();
                                tempOrders.add(tempOrder);
                                DataHolder.orderList.put(place.getPlaceId(), tempOrders);
                            }
                        }
                        holder.getStatusIncoming().setText("Incoming: " + incomingCount);
                        holder.getStatusProcessing().setText("Processing: " + processingCount);
                        holder.getStatusCompleted().setText("Completed: " + completedCount);
                        if (deadline != Integer.MAX_VALUE) holder.getOrderStatusDeadline().setText(String.valueOf(deadline));
                    }
                } catch (Exception e) {
                    e.getMessage();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private synchronized void updateOrderCount() {
        orderCount++;
        orderCountView.setText("Order Counts : " + orderCount);
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }
}
