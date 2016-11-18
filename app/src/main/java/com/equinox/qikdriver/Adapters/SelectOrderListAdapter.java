package com.equinox.qikdriver.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.equinox.qikdriver.Models.Order;
import com.equinox.qikdriver.Models.DataHolder;
import com.equinox.qikdriver.R;

import java.util.List;

/**
 * Created by mukht on 11/9/2016.
 */

public class SelectOrderListAdapter extends BaseAdapter {

    private List<Order> orderSelectionList;
    private Context context;

    public SelectOrderListAdapter(List<Order> orderSelectionList, Context context) {
        this.orderSelectionList = orderSelectionList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return orderSelectionList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Order order = orderSelectionList.get(position);
        CheckoutViewHolder holder;
        if (convertView == null) {
            holder = new CheckoutViewHolder();
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.order_select_list_item, parent, false);
            holder.userName = (TextView) convertView.findViewById(R.id.user_name);
            holder.orderStatus = (TextView) convertView.findViewById(R.id.order_status);
            holder.deadlineMessage = (TextView) convertView.findViewById(R.id.deadline_message);
            holder.userImage = (NetworkImageView) convertView.findViewById(R.id.user_image);
            convertView.setTag(holder);
        } else holder = (CheckoutViewHolder) convertView.getTag();

        holder.userName.setText(order.getFrom().getName());
        holder.orderStatus.setText(order.getStatus());
        holder.deadlineMessage.setText(order.getTimestamp() < System.currentTimeMillis() ? "Go Now!" : "");
        holder.userImage.setImageUrl(order.getFrom().getPhotoURL(), DataHolder.getInstance().getImageLoader());
        return convertView;
    }

    public static class CheckoutViewHolder {
        public TextView userName, orderStatus, deadlineMessage;
        public NetworkImageView userImage;
    }
}
