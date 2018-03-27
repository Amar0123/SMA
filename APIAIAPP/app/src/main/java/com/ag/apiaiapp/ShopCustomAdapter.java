package com.ag.apiaiapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class ShopCustomAdapter extends ArrayAdapter<Data> {

    Context context;
    List<Data> datas;
    LayoutInflater inflater;

    public ShopCustomAdapter(Context context, List<Data> datas) {
        super(context, R.layout.shop_custom_row, datas);
        this.context = context;
        this.datas = datas;
    }

    public static class ViewHolder {
        static TextView shopName;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.shop_custom_row, null);
        }

        final ViewHolder holder = new ViewHolder();
        holder.shopName= (TextView) convertView.findViewById(R.id.shopName);

        final Data data = datas.get(position);

        holder.shopName.setText(data.getShopname());

        return convertView;
    }
}