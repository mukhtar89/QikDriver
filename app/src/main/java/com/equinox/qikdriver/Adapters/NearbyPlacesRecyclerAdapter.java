package com.equinox.qikdriver.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.equinox.qikdriver.Models.Constants;
import com.equinox.qikdriver.Models.DataHolder;
import com.equinox.qikdriver.Models.Place;
import com.equinox.qikdriver.R;
import com.equinox.qikdriver.Utils.StringManipulation;
import com.equinox.qikdriver.ViewHolders.NearbyPlacesViewHolder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static com.equinox.qikdriver.Enums.OrderStatus.COMPLETED;
import static com.equinox.qikdriver.Enums.OrderStatus.INCOMING;
import static com.equinox.qikdriver.Enums.OrderStatus.PROCESSING;

/**
 * Created by mukht on 11/14/2016.
 */

public class NearbyPlacesRecyclerAdapter extends RecyclerView.Adapter<NearbyPlacesViewHolder> {

    private List<Place> placeList;

    public NearbyPlacesRecyclerAdapter(List<Place> placeList) {
        this.placeList = placeList;
    }

    @Override
    public NearbyPlacesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View holder = LayoutInflater.from(parent.getContext()).inflate(R.layout.pending_order_shop_list_item, parent, false);
        return new NearbyPlacesViewHolder(holder);
    }

    @Override
    public void onBindViewHolder(final NearbyPlacesViewHolder holder, int position) {
        final Place place = placeList.get(position);
        holder.getPlaceName().setText(StringManipulation.CapsFirst(place.getName()));
        final DatabaseReference businessOrderReference = DataHolder.database.getReference().child(place.getPlaceId()).child(Constants.ORDER);
        businessOrderReference.addValueEventListener(new ValueEventListener() {
            Integer incomingCount=0, processingCount=0, completedCount=0, deadline=Integer.MAX_VALUE;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    Iterator<DataSnapshot> iteratorIncoming = dataSnapshot.getChildren().iterator();
                    HashMap<String, Object> iteratorObject;
                    while (iteratorIncoming.hasNext()) {
                        DataSnapshot orderShot = iteratorIncoming.next();
                        iteratorObject = (HashMap<String, Object>) orderShot.getValue();
                        if (iteratorObject.get("orderStatus").equals(INCOMING.getNodeName())) incomingCount++;
                        if (iteratorObject.get("orderStatus").equals(PROCESSING.getNodeName())) {
                            processingCount++;
                            if (incomingCount > 0) incomingCount--;
                        }
                        if (iteratorObject.get("orderStatus").equals(COMPLETED.getNodeName())) {
                            completedCount++;
                            if (processingCount > 0) processingCount--;
                        }
                        if (iteratorObject.containsKey("deadline"))
                            if ((Long) iteratorObject.get("deadline") < deadline)
                                deadline = (int) (long) iteratorObject.get("deadline");
                    }
                    holder.getStatusIncoming().setText("Incoming: " + incomingCount);
                    holder.getStatusProcessing().setText("Processing: " + processingCount);
                    holder.getStatusCompleted().setText("Completed: " + completedCount);
                    if (deadline != Integer.MAX_VALUE) holder.getOrderStatusDeadline().setText(String.valueOf(deadline));
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }
}
