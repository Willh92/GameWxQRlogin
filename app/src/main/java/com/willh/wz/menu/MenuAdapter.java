package com.willh.wz.menu;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.willh.wz.R;
import com.willh.wz.bean.GameInfo;
import com.willh.wz.util.ImageLoaderUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

public class MenuAdapter extends BaseAdapter implements Filterable {

    private final Object mLock = new Object();

    private final Context mContext;
    private final LayoutInflater mInflater;

    private ArrayList<GameInfo> mItems;
    private ArrayList<GameInfo> mOriginalValues;
    private ArrayFilter mFilter;

    private boolean mNotifyOnChange = true;
    private boolean mObjectsFromResources;

    public MenuAdapter(Context content, ArrayList<GameInfo> mItems) {
        this.mContext = content;
        this.mItems = mItems;
        this.mInflater = LayoutInflater.from(content);
    }

    public Context getContext() {
        return mContext;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    public int getOriginalCount() {
        return mOriginalValues == null ? mItems.size() : mOriginalValues.size();
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
            holder.icon = convertView.findViewById(R.id.iv_icon);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        GameInfo menuItem = mItems.get(position);
        holder.title.setText(menuItem.name);
        ImageLoaderUtil.getInstance().loadImage(menuItem.icon, holder.icon, true, "menu");
        return convertView;
    }

    static class ViewHolder {
        TextView title;
        ImageView icon;
    }

    public void add(GameInfo object) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.add(object);
            } else {
                mItems.add(object);
            }
            mObjectsFromResources = false;
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    public void addAll(Collection<? extends GameInfo> collection) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.addAll(collection);
            } else {
                mItems.addAll(collection);
            }
            mObjectsFromResources = false;
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    public void addAll(GameInfo... items) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                Collections.addAll(mOriginalValues, items);
            } else {
                Collections.addAll(mItems, items);
            }
            mObjectsFromResources = false;
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    public void insert(GameInfo object, int index) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.add(index, object);
            } else {
                mItems.add(index, object);
            }
            mObjectsFromResources = false;
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }


    public void remove(GameInfo object) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.remove(object);
            } else {
                mItems.remove(object);
            }
            mObjectsFromResources = false;
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    public void clear() {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.clear();
            } else {
                mItems.clear();
            }
            mObjectsFromResources = false;
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }


    public void sort(Comparator<? super GameInfo> comparator) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                Collections.sort(mOriginalValues, comparator);
            } else {
                Collections.sort(mItems, comparator);
            }
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        mNotifyOnChange = true;
    }

    public void setNotifyOnChange(boolean notifyOnChange) {
        mNotifyOnChange = notifyOnChange;
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ArrayFilter();
        }
        return mFilter;
    }

    private class ArrayFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            final FilterResults results = new FilterResults();

            if (mOriginalValues == null) {
                synchronized (mLock) {
                    mOriginalValues = new ArrayList<>(mItems);
                }
            }

            if (prefix == null || prefix.length() == 0 || prefix.toString().replaceAll("\\s", "").isEmpty()) {
                final ArrayList<GameInfo> list;
                synchronized (mLock) {
                    list = new ArrayList<>(mOriginalValues);
                }
                results.values = list;
                results.count = list.size();
            } else {
                final String prefixString = prefix.toString().replaceAll("\\s", "").toLowerCase();

                final ArrayList<GameInfo> values;
                synchronized (mLock) {
                    values = new ArrayList<>(mOriginalValues);
                }

                final int count = values.size();
                final ArrayList<GameInfo> newValues = new ArrayList<>();

                for (int i = 0; i < count; i++) {
                    final GameInfo value = values.get(i);
                    final String name = value.name.toLowerCase();
                    if (name.contains(prefixString)) {
                        newValues.add(value);
                        continue;
                    }
                    if (!TextUtils.isEmpty(value.py)) {
                        final String py = value.py.toLowerCase();
                        if (py.contains(prefixString)) {
                            newValues.add(value);
                        }
                    }
                }
                results.values = newValues;
                results.count = newValues.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mItems = (ArrayList<GameInfo>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }

}
