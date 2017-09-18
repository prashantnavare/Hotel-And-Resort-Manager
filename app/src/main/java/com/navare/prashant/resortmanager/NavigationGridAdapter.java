package com.navare.prashant.resortmanager;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by prashant on 9/17/2017.
 */

public class NavigationGridAdapter extends BaseAdapter {
    private Context mContext;
    private final String[] text;
    private final int[] imageid;

    public NavigationGridAdapter(Context c,String[] text,int[] imageid ) {
        mContext = c;
        this.imageid = imageid;
        this.text = text;
    }

    @Override
    public int getCount() {
        return text.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View grid;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {

            grid = new View(mContext);
            grid = inflater.inflate(R.layout.navigation_grid_single, null);
            LinearLayout ll = (LinearLayout) grid.findViewById(R.id.grid_ll);
            TextView textView = (TextView) grid.findViewById(R.id.grid_text);
            ImageView imageView = (ImageView)grid.findViewById(R.id.grid_image);
            ll.setBackgroundColor(ContextCompat.getColor(mContext,R.color.windowBackground));
            textView.setText(text[position]);
            imageView.setImageResource(imageid[position]);
        }
        else {
            grid = (View) convertView;
        }

        return grid;
    }
}
