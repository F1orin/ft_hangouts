package fr.ecole42.fbicher.ft_hangouts.util;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Item decoration for creating proper spacing around items in RecyclerView
 * as per Android Design guidelines.
 * <p/>
 * Created by Florin.Bicher on 20.10.2015.
 */
public class OffsetItemDecoration extends RecyclerView.ItemDecoration {

    private int mItemOffset;

    public OffsetItemDecoration(int itemOffset) {
        mItemOffset = itemOffset;
    }

    public OffsetItemDecoration(@NonNull Context context, @DimenRes int itemOffsetId) {
        this(context.getResources().getDimensionPixelSize(itemOffsetId));
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.set(mItemOffset, mItemOffset, mItemOffset, mItemOffset);
    }
}
