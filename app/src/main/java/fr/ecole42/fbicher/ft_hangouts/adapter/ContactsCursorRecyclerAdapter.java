package fr.ecole42.fbicher.ft_hangouts.adapter;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import fr.ecole42.fbicher.ft_hangouts.R;
import fr.ecole42.fbicher.ft_hangouts.database.ContactsContract;
import fr.ecole42.fbicher.ft_hangouts.util.Util;

/**
 * Implementation of {@link CursorRecyclerAdapter} for the list of contacts.
 * <p/>
 * Created by fbicher on 1/27/16.
 */
public class ContactsCursorRecyclerAdapter extends CursorRecyclerAdapter {

    /**
     * Context for this adapter.
     */
    private Context mContext;

    public ContactsCursorRecyclerAdapter(Context context, Cursor cursor) {
        super(cursor);
        mContext = context;
    }

    @Override
    public void onBindViewHolderCursor(RecyclerView.ViewHolder holder, Cursor cursor) {
        ContactViewHolder vh = (ContactViewHolder) holder;

        final String avatarUri = cursor.getString(cursor.getColumnIndex(
                ContactsContract.Contacts.COLUMN_NAME_AVATAR));
        Util.loadAvatar(Uri.parse(avatarUri), vh.ivAvatar, R.drawable.ic_person_black_24dp, 200, mContext);

        String name = cursor.getString(cursor.getColumnIndex(
                ContactsContract.Contacts.COLUMN_NAME_NAME));
        String lastName = cursor.getString(cursor.getColumnIndex(
                ContactsContract.Contacts.COLUMN_NAME_LAST_NAME));
        int nameLen = name.length();
        int lastNameLen = lastName.length();
        if (nameLen == 0 && lastNameLen == 0) {
            vh.tvName.setText(R.string.txt_no_name);
        } else if (nameLen > 0 && lastNameLen == 0) {
            vh.tvName.setText(name);
        } else if (nameLen == 0 && lastNameLen > 0) {
            vh.tvName.setText(lastName);
        } else {
            String displayName = name + " " + lastName;
            vh.tvName.setText(displayName);
        }

        final String phone = cursor.getString(cursor.getColumnIndex(
                ContactsContract.Contacts.COLUMN_NAME_PHONE));
        if (phone.length() == 0) {
            vh.tvPhone.setText(R.string.txt_no_phone);
        } else {
            vh.tvPhone.setText(phone);
        }

        final long id = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts._ID));
        vh.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Constructs a new URI from the incoming URI and the row ID
                Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);

                // Sends out an Intent to start an Activity that can handle ACTION_EDIT. The
                // Intent's data is the entry ID URI. The effect is to call ContactEditActivity.
                mContext.startActivity(new Intent(Intent.ACTION_VIEW, uri));
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(v);
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout root;
        ImageView ivAvatar;
        TextView tvName;
        TextView tvPhone;

        public ContactViewHolder(View v) {
            super(v);
            root = (RelativeLayout) v.findViewById(R.id.root);
            ivAvatar = (ImageView) v.findViewById(R.id.iv_avatar);
            tvName = (TextView) v.findViewById(R.id.tv_name);
            tvPhone = (TextView) v.findViewById(R.id.tv_phone);
        }
    }
}
