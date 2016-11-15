package com.equinox.qikdriver.ViewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.equinox.qikdriver.R;

/**
 * Created by mukht on 11/15/2016.
 */
public class NearbyPlacesViewHolder extends RecyclerView.ViewHolder {

    private ImageView placeTypeIcon;
    private TextView placeName, statusIncoming, statusProcessing, statusCompleted, orderStatusDeadline;


    public NearbyPlacesViewHolder(View itemView) {
        super(itemView);
        placeTypeIcon = (ImageView) itemView.findViewById(R.id.place_type_icon);
        placeName = (TextView) itemView.findViewById(R.id.order_shop_name);
        statusIncoming = (TextView) itemView.findViewById(R.id.order_status_incoming);
        statusProcessing = (TextView) itemView.findViewById(R.id.order_status_processing);
        statusCompleted = (TextView) itemView.findViewById(R.id.order_status_completed);
        orderStatusDeadline = (TextView) itemView.findViewById(R.id.order_status_deadline);
    }

    public ImageView getPlaceTypeIcon() {
        return placeTypeIcon;
    }
    public TextView getPlaceName() {
        return placeName;
    }
    public TextView getStatusIncoming() {
        return statusIncoming;
    }
    public TextView getOrderStatusDeadline() {
        return orderStatusDeadline;
    }
    public TextView getStatusProcessing() {
        return statusProcessing;
    }
    public TextView getStatusCompleted() {
        return statusCompleted;
    }
}
