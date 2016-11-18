package com.equinox.qikdriver.ViewHolders;

import android.app.Activity;
import android.app.Dialog;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.equinox.qikdriver.Adapters.SelectOrderListAdapter;
import com.equinox.qikdriver.Models.DataHolder;
import com.equinox.qikdriver.Models.Order;
import com.equinox.qikdriver.Models.Place;
import com.equinox.qikdriver.R;

import java.util.List;

/**
 * Created by mukht on 11/15/2016.
 */
public class NearbyPlacesViewHolder extends RecyclerView.ViewHolder {

    private ImageView placeTypeIcon;
    private TextView placeName, statusIncoming, statusProcessing, statusCompleted, orderStatusDeadline;

    public NearbyPlacesViewHolder(View itemView, final Activity activity, final List<Place> placeList) {
        super(itemView);
        placeTypeIcon = (ImageView) itemView.findViewById(R.id.place_type_icon);
        placeName = (TextView) itemView.findViewById(R.id.order_shop_name);
        statusIncoming = (TextView) itemView.findViewById(R.id.order_status_incoming);
        statusProcessing = (TextView) itemView.findViewById(R.id.order_status_processing);
        statusCompleted = (TextView) itemView.findViewById(R.id.order_status_completed);
        orderStatusDeadline = (TextView) itemView.findViewById(R.id.order_status_deadline);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog selectorDialog = new Dialog(activity);
                selectorDialog.setContentView(R.layout.dialog_select_order);
                selectorDialog.setTitle("Select an Order");
                final ListView selectorList = (ListView) selectorDialog.findViewById(R.id.order_select_list);
                final SelectOrderListAdapter selectOrderListAdapter =
                        new SelectOrderListAdapter(DataHolder.orderList.get(placeList.get(getLayoutPosition()).getPlaceId()),activity);
                selectorList.setAdapter(selectOrderListAdapter);
                final Button okayButton = (Button) selectorDialog.findViewById(R.id.dialog_yes_button);
                final Button cancelButton = (Button) selectorDialog.findViewById(R.id.dialog_no_button);
                selectorList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                        okayButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Order tempOrder = DataHolder.orderList.get(placeList.get(getLayoutPosition()).getPlaceId())
                                        .get(position);
                                Toast.makeText(activity, "You have selected " + tempOrder.getFrom().getName(), Toast.LENGTH_LONG).show();
                                selectorDialog.dismiss();
                            }
                        });
                        cancelButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                selectorDialog.dismiss();
                            }
                        });
                    }
                });
                selectorDialog.show();
            }
        });
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
