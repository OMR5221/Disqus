package com.appsforprogress.android.disqus.helpers;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.appsforprogress.android.disqus.ExploreFragment;
import com.appsforprogress.android.disqus.R;
import com.appsforprogress.android.disqus.objects.FBLike;
import com.appsforprogress.android.disqus.util.ItemTouchHelperAdapter;
import com.appsforprogress.android.disqus.util.OnStartDragListener;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by oswal on 3/11/2018.
 */

// Create adapter to handle FBLike refreshing
public class FireBaseFBLikeAdapter extends FirebaseRecyclerAdapter<FBLike, FireBaseFBLikeViewHolder> implements ItemTouchHelperAdapter
{
    private ChildEventListener mChildEventListener;
    private ArrayList<FBLike> mFBLikes = new ArrayList<>();
    private DatabaseReference mRef;
    private Context mContext;
    // Processes user touch:
    private OnStartDragListener mOnStartDragListener;


    public FireBaseFBLikeAdapter(Class<FBLike> modelClass, int modelLayout,
                                 Class<FireBaseFBLikeViewHolder> viewHolderClass,
                                 Query ref, OnStartDragListener onStartDragListener, Context context)
    {
        super(modelClass, modelLayout, viewHolderClass, ref);
        mRef = ref.getRef();
        mOnStartDragListener = onStartDragListener;
        mContext = context;
        // mFBLikes = mFBLikeItems;

        mChildEventListener = mRef.addChildEventListener(new ChildEventListener()
        {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                // Updates listing of likes
                // mFBLikes.clear();
                mFBLikes.add(dataSnapshot.getValue(FBLike.class));
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s)
            {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot)
            {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName)
            {
                // fetchData(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    @Override
    protected void populateViewHolder(final FireBaseFBLikeViewHolder fbLikeHolder, FBLike fbLike, int position)
    {
        fbLikeHolder.bindFBLike(fbLike);

        // Set Touch Listener on ImageView to allow for sorting:
        fbLikeHolder.mLikePic.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN)
                {
                    mOnStartDragListener.onStartDrag(fbLikeHolder);
                }

                return false;
            }
        });



        /*
        fbLikeHolder.itemView.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                int itemPosition = fbLikeHolder.getAdapterPosition();

                if (mOrientation == Configuration.ORIENTATION_LANDSCAPE)
                {
                    createDetailFragment(itemPosition);
                } else {
                    Intent intent = new Intent(mContext, RestaurantDetailActivity.class);
                    intent.putExtra(DBNodeConstants.EXTRA_KEY_POSITION, itemPosition);
                    intent.putExtra(DBNodeConstants.EXTRA_KEY_RESTAURANTS, Parcels.wrap(mRestaurants));
                    intent.putExtra(DBNodeConstants.KEY_SOURCE, Constants.SOURCE_SAVED);
                    mContext.startActivity(intent);
                }

            }
        });
        */
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition)
    {
        Collections.swap(mFBLikes, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        setIndexListener();
        return false;
    }

    @Override
    // Allow user to remove item from their listing
    // Any way to batch unlike to FB?
    public void onItemDismiss(int index)
    {
        mFBLikes.remove(index);
        getRef(index).removeValue();
    }

    // Will re-assign the index in our ArrayList:
    private void setIndexListener()
    {
        for (final FBLike fbLike : mFBLikes)
        {
            final int index = mFBLikes.indexOf(fbLike);

            Query query = mRef.orderByChild("fbid").equalTo(fbLike.getFBID());

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    DataSnapshot nodeShot = dataSnapshot.getChildren().iterator().next();
                    String key = nodeShot.getKey();
                    HashMap<String, Object> update = new HashMap<>();
                    update.put("index", index);
                    mRef.child(key).updateChildren(update);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            /*
            int index = mFBLikes.indexOf(fbLike);
            DatabaseReference ref = getRef(fbLike);
            ref.child("index").setValue(index);

            */
            /*
            fbLike.setIndex(index);
            ref.setValue(fbLike);
            */
        }
    }

    @Override
    // We only officially update the index of FBLikes in the DB upon the closing of the app:
    public void cleanup()
    {
        super.cleanup();
        mRef.removeEventListener(mChildEventListener);
    }
}