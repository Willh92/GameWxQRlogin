package com.willh.wz.menu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.willh.wz.R;
import com.willh.wz.bean.GameInfo;

import java.util.ArrayList;

public class MenuAdapter extends BaseAdapter {

    private ArrayList<GameInfo> mItems;
    private Context mContext;
    private LayoutInflater mInflater;

    public MenuAdapter(Context content, ArrayList<GameInfo> mItems) {
        this.mContext = content;
        this.mItems = mItems;
        this.mInflater = LayoutInflater.from(content);
    }

    public ArrayList<GameInfo> getItems() {
        return mItems;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public GameInfo getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.menu_item, parent, false);
            holder = new ViewHolder();
            convertView.setTag(holder);
            holder.title = (TextView) convertView.findViewById(R.id.tv_title);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        GameInfo menuItem = mItems.get(position);
        holder.title.setText(menuItem.name);
        return convertView;
    }

    static class ViewHolder {
        TextView title;
    }

}
