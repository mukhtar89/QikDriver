package com.equinox.qikdriver.Enums;

import com.equinox.qikdriver.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mukht on 10/22/2016.
 */

public enum QikList {

    GROCERY(0, "Grocery",  "grocery_or_supermarket", new String[]{"supermarket","grocery"}, R.drawable.ic_local_grocery_store_white_48dp),
    RESTAURANT(0, "Restaurant", "restaurant", new String[]{"supermarket"}, R.drawable.ic_restaurant_white_48dp),
    CLOTHING(0, "Clothing", "clothing_store", new String[]{"supermarket"},  R.drawable.ic_shop_white_48dp),
    LAUNDRY(0, "Laundry", "laundry", new String[]{"supermarket"}, R.drawable.ic_local_laundry_service_white_48dp),
    DOORTODOOR(0, "Door-to-door", "sublocality", new String[]{"supermarket"}, R.drawable.ic_directions_run_white_48dp);

    private int position;
    private String listName;
    private String typeName;
    private String[] keyword;
    private int icon;

    QikList(int position, String listName, String typeName, String[] keyword, int icon) {
        this.listName = listName;
        this.typeName = typeName;
        this.keyword = keyword;
        this.position = position;
        this.icon = icon;
    }

    public int getPosition() {
        return position;
    }
    public String getListName() {
        return listName;
    }
    public String getTypeName() { return typeName; }
    public int getIcon() {
        return icon;
    }
    public String[] getKeyword() {
        return keyword;
    }
    public static List<String> getListTypeNames() {
        List<String> tempList = new ArrayList<>();
        tempList.add("All");
        for (QikList qikList : QikList.values())
            tempList.add(qikList.getListName());
        return tempList;
    }
}
