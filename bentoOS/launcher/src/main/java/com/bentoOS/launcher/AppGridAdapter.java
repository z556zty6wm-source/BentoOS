package com.bentoOS.launcher;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

public class AppGridAdapter extends BaseAdapter {

    private final Context context;
    private final List<ResolveInfo> apps;

    public AppGridAdapter(Context context, List<ResolveInfo> apps) {
        this.context = context;
        this.apps = apps;
    }

    @Override
    public int getCount() { return apps.size(); }

    @Override
    public Object getItem(int position) { return apps.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_app, parent, false);
            holder = new ViewHolder();
            holder.icon = convertView.findViewById(R.id.app_icon);
            holder.label = convertView.findViewById(R.id.app_label);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ResolveInfo info = apps.get(position);
        holder.label.setText(info.loadLabel(context.getPackageManager()));
        holder.icon.setImageDrawable(info.loadIcon(context.getPackageManager()));

        convertView.setOnFocusChangeListener((v, hasFocus) -> {
            v.animate()
                .scaleX(hasFocus ? 1.1f : 1.0f)
                .scaleY(hasFocus ? 1.1f : 1.0f)
                .translationZ(hasFocus ? 12f : 0f)
                .setDuration(150)
                .start();
        });

        return convertView;
    }

    static class ViewHolder {
        ImageView icon;
        TextView label;
    }
}
