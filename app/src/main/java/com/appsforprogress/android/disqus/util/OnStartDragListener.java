package com.appsforprogress.android.disqus.util;

import android.support.v7.widget.RecyclerView;

/**
 * Created by oswal on 2/19/2018.
 */

public interface OnStartDragListener
{
    // called when the user begins a 'drag-and-drop' interaction
    // passes events back to our adapter allowing us to attach the touch listener to an item in our ViewHolder
    void onStartDrag(RecyclerView.ViewHolder viewHolder);
}