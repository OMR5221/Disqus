package com.appsforprogress.android.disqus.helpers;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.appsforprogress.android.disqus.R;
import com.appsforprogress.android.disqus.objects.FBLike;
import com.appsforprogress.android.disqus.util.ItemTouchHelperViewHolder;
import com.squareup.picasso.Picasso;

/**
 * Created by oswal on 3/11/2018.
 */

public class FireBaseFBLikeViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder
{
    private static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;

    public TextView mCategoryTextView;
    public TextView mLikeName;
    public ImageView mLikePic;
    Context mContext;
    View mView;


    public FireBaseFBLikeViewHolder(View itemView)
    {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
    }


    public void bindFBLike(FBLike fbLikeItem)
    {
        mCategoryTextView = (TextView) itemView.findViewById(R.id.user_fb_category_name);
        mLikeName = (TextView) itemView.findViewById(R.id.user_fb_like_name);
        mLikePic = (ImageView) itemView.findViewById(R.id.user_fb_like_image);

        mLikeName.setText(fbLikeItem.getName().toString());
        //new DownloadImage(mLikePic).execute(fbLikeItem.getPicURL().toString());
        mCategoryTextView.setText(fbLikeItem.getCategory().toString());

        Picasso.with(mContext)
                .load(fbLikeItem.getPicURL())
                .resize(MAX_WIDTH, MAX_HEIGHT)
                .centerCrop()
                .into(mLikePic);

    }




    @Override
    public void onItemSelected() {
        AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(mContext,
                R.animator.drag_scale_on);
        set.setTarget(itemView);
        set.start();
    }

    @Override
    public void onItemClear() {
        AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(mContext,
                R.animator.drag_scale_off);
        set.setTarget(itemView);
        set.start();
    }
}
