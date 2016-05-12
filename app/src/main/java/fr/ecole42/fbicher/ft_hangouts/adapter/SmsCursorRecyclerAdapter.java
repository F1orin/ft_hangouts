package fr.ecole42.fbicher.ft_hangouts.adapter;

import android.content.Context;
import android.database.Cursor;
import android.provider.Telephony;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Date;

import fr.ecole42.fbicher.ft_hangouts.R;
import fr.ecole42.fbicher.ft_hangouts.util.Util;

/**
 * Implementation of {@link CursorRecyclerAdapter} for the list of sms.
 * <p/>
 * Created by fbicher on 1/27/16.
 */
public class SmsCursorRecyclerAdapter extends CursorRecyclerAdapter {

    /**
     * Context for this adapter.
     */
    private Context mContext;

    public SmsCursorRecyclerAdapter(Context context, Cursor cursor) {
        super(cursor);
        mContext = context;
    }

    @Override
    public void onBindViewHolderCursor(RecyclerView.ViewHolder holder, Cursor cursor) {
        SmsViewHolder vh = (SmsViewHolder) holder;

        // adjust alignment: received on the left, sent on the right
        int type = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.TYPE));
        switch (type) {
            case Telephony.Sms.MESSAGE_TYPE_INBOX:
                // align body
                RelativeLayout.LayoutParams pBodyIn = (RelativeLayout.LayoutParams) vh.tvBody.getLayoutParams();
                pBodyIn.addRule(RelativeLayout.ALIGN_PARENT_END, 0);
                pBodyIn.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
                vh.tvBody.setLayoutParams(pBodyIn);
                // align time
                RelativeLayout.LayoutParams pTimeIn = (RelativeLayout.LayoutParams) vh.tvTime.getLayoutParams();
                pTimeIn.addRule(RelativeLayout.ALIGN_PARENT_END, 0);
                pTimeIn.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
                vh.tvTime.setLayoutParams(pTimeIn);
                break;
            case Telephony.Sms.MESSAGE_TYPE_SENT:
                // align body
                RelativeLayout.LayoutParams pBodyOut = (RelativeLayout.LayoutParams) vh.tvBody.getLayoutParams();
                pBodyOut.addRule(RelativeLayout.ALIGN_PARENT_START, 0);
                pBodyOut.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
                vh.tvBody.setLayoutParams(pBodyOut);
                // align time
                RelativeLayout.LayoutParams pTimeOut = (RelativeLayout.LayoutParams) vh.tvTime.getLayoutParams();
                pTimeOut.addRule(RelativeLayout.ALIGN_PARENT_START, 0);
                pTimeOut.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
                vh.tvTime.setLayoutParams(pTimeOut);
                break;
        }

        // load body
        String body = cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY));
        vh.tvBody.setText(body);

        // load time
        long time = cursor.getLong(cursor.getColumnIndex(Telephony.Sms.DATE));
        CharSequence formatted = DateFormat.format(Util.SMS_DATE_FORMAT, new Date(time));
        vh.tvTime.setText(formatted);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sms, parent, false);
        return new SmsViewHolder(v);
    }

    public static class SmsViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout root;
        TextView tvBody;
        TextView tvTime;

        public SmsViewHolder(View v) {
            super(v);
            root = (RelativeLayout) v.findViewById(R.id.root);
            tvBody = (TextView) v.findViewById(R.id.tv_body);
            tvTime = (TextView) v.findViewById(R.id.tv_time);
        }
    }
}
