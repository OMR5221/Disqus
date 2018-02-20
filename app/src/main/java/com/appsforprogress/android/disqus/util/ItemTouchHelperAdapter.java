package com.appsforprogress.android.disqus.util;

/**
 * Created by oswal on 2/19/2018.
 */

public interface ItemTouchHelperAdapter
{
    // Allows user to drag item:
    boolean onItemMove(int fromPosition, int toPosition);

    // Allows user to swipe away:
    void onItemDismiss(int position);
}