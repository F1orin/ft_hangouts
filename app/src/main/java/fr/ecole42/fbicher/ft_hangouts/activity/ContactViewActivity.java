package fr.ecole42.fbicher.ft_hangouts.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import fr.ecole42.fbicher.ft_hangouts.R;
import fr.ecole42.fbicher.ft_hangouts.database.ContactsContract;
import fr.ecole42.fbicher.ft_hangouts.util.NotifyingAsyncQueryHandler;
import fr.ecole42.fbicher.ft_hangouts.util.Util;

public class ContactViewActivity extends BaseActivity {

    private static final String TAG = "ContactViewActivity";

    /**
     * Projection that returns the entry ID and the last name.
     */
    private static final String[] PROJECTION =
            new String[]{
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.COLUMN_NAME_AVATAR,
                    ContactsContract.Contacts.COLUMN_NAME_NAME,
                    ContactsContract.Contacts.COLUMN_NAME_LAST_NAME,
                    ContactsContract.Contacts.COLUMN_NAME_PHONE,
                    ContactsContract.Contacts.COLUMN_NAME_EMAIL,
                    ContactsContract.Contacts.COLUMN_NAME_ADDRESS
            };

    // Tokens for AsyncQueryHandler operations
    private static final int QUERY_TOKEN = 1;
    private static final int DELETE_TOKEN = 2;

    // Instance states for saving
    private static final String STATE_URI = "uri";

    // Global mutable variables
    private Uri mUri;
    private Cursor mCursor;

    private ImageView mAvatarIv;
    private TextView mNameTv;
    private TextView mLastNameTv;
    private RelativeLayout mPhoneHolderRl;
    private TextView mPhoneTv;
    private ImageView mMessageIv;
    private LinearLayout mEmailHolderLl;
    private TextView mEmailTv;
    private TextView mAddressTv;

    private String mName;
    private String mLastName;
    private String mPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState != null) {
            Log.d(TAG, "onCreate: restore instance state");
            String uri = savedInstanceState.getString(STATE_URI);
            if (uri != null) {
                mUri = Uri.parse(uri);
            }
        }

        // Get handles to the widgets in the the layout.
        mAvatarIv = (ImageView) findViewById(R.id.iv_avatar);
        mNameTv = (TextView) findViewById(R.id.tv_name);
        mLastNameTv = (TextView) findViewById(R.id.tv_last_name);
        mPhoneHolderRl = (RelativeLayout) findViewById(R.id.rl_phone_holder);
        mPhoneTv = (TextView) findViewById(R.id.tv_phone);
        mMessageIv = (ImageView) findViewById(R.id.iv_message);
        mEmailHolderLl = (LinearLayout) findViewById(R.id.ll_email_holder);
        mEmailTv = (TextView) findViewById(R.id.tv_email);
        mAddressTv = (TextView) findViewById(R.id.tv_address);

        /*
         * Creates an Intent to use when the Activity object's result is sent back to the
         * caller.
         */
        final Intent intent = getIntent();

        // Gets the action that triggered the intent filter for this Activity
        final String action = intent.getAction();

        // For a VIEW action:
        if (Intent.ACTION_VIEW.equals(action)) {
            mUri = intent.getData();
        } else {
            // Logs an error that the action was not understood, finishes the Activity, and
            // returns RESULT_CANCELED to an originating Activity.
            Log.e(TAG, "Unknown action, exiting");
            finish();
        }

        // set up call action
        mPhoneHolderRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_DIAL);
                i.setData(Uri.parse("tel:" + mPhoneTv.getText()));
                startActivity(i);
            }
        });

        // set up message action
        mMessageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setAction(Util.ACTION_VIEW_SMS);
                i.setData(mUri);
                // pass phone
                i.putExtra(Util.EXTRA_PHONE, mPhone);
                // pass name
                int nameLen = mName.length();
                int lastNameLen = mLastName.length();
                String name;
                if (nameLen == 0 && lastNameLen == 0) {
                    name = mPhone;
                } else if (nameLen > 0 && lastNameLen == 0) {
                    name = mName;
                } else if (nameLen == 0 && lastNameLen > 0) {
                    name = mLastName;
                } else {
                    name = mName + " " + mLastName;
                }
                i.putExtra(Util.EXTRA_NAME, name);
                startActivity(i);
            }
        });

        // set up email action
        mEmailHolderLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SENDTO);
                i.setData(Uri.parse("mailto:" + mEmailTv.getText()));
                startActivity(i);
            }
        });
    }

    /**
     * This method is called when the Activity is about to come to the foreground. This happens
     * when the Activity comes to the top of the task stack, OR when it is first starting.
     * <p/>
     * Moves to the first entry in the list, sets an appropriate title for the action chosen by
     * the user, puts the entry contents into the TextView, and saves the original text as a
     * backup.
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, R.string.txt_storage_permission_refused, Toast.LENGTH_LONG).show();
            finish();
        }

        NotifyingAsyncQueryHandler queryHandler = new NotifyingAsyncQueryHandler(this,
                new NotifyingAsyncQueryHandler.AsyncQueryListener() {
                    @Override
                    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
                        mCursor = cursor;
                        if (mCursor != null) {
                            // Moves to the first record. Always call moveToFirst() before accessing
                            // data in a Cursor for the first time. The semantics of using a Cursor
                            // are that when it is created, its internal index is pointing to a
                            // "place" immediately before the first record.
                            mCursor.moveToFirst();

                            // Modifies the window title for the Activity according to the current
                            // Activity state.
                            setTitle(getText(R.string.title_view));

                            // Gets the entry text from the Cursor and puts it in the TextView,
                            // but doesn't change the text cursor's position.

                            // Avatar
                            int avatarIndex = mCursor.getColumnIndex(
                                    ContactsContract.Contacts.COLUMN_NAME_AVATAR);
                            Util.loadAvatar(Uri.parse(mCursor.getString(avatarIndex)),
                                    mAvatarIv, R.drawable.ic_person_white_24dp,
                                    500, ContactViewActivity.this);

                            // Name
                            int nameIndex = mCursor.getColumnIndex(
                                    ContactsContract.Contacts.COLUMN_NAME_NAME);
                            String name = mCursor.getString(nameIndex);
                            mName = name;
                            if (name.length() == 0) {
                                name = getString(R.string.txt_no_name);
                            }
                            mNameTv.setTextKeepState(name);

                            // Last name
                            int lastNameIndex = mCursor.getColumnIndex(
                                    ContactsContract.Contacts.COLUMN_NAME_LAST_NAME);
                            String lastName = mCursor.getString(lastNameIndex);
                            mLastName = lastName;
                            if (lastName.length() == 0) {
                                lastName = getString(R.string.txt_no_lastname);
                            }
                            mLastNameTv.setTextKeepState(lastName);

                            // Phone
                            int phoneIndex = mCursor.getColumnIndex(
                                    ContactsContract.Contacts.COLUMN_NAME_PHONE);
                            String phone = mCursor.getString(phoneIndex);
                            mPhone = phone;
                            if (phone.length() == 0) {
                                phone = getString(R.string.txt_no_phone);
                            }
                            mPhoneTv.setTextKeepState(phone);

                            // Email
                            int emailIndex = mCursor.getColumnIndex(
                                    ContactsContract.Contacts.COLUMN_NAME_EMAIL);
                            String email = mCursor.getString(emailIndex);
                            if (email.length() == 0) {
                                email = getString(R.string.txt_no_email);
                            }
                            mEmailTv.setTextKeepState(email);

                            // Address
                            int addressIndex = mCursor.getColumnIndex(
                                    ContactsContract.Contacts.COLUMN_NAME_ADDRESS);
                            String address = mCursor.getString(addressIndex);
                            if (address.length() == 0) {
                                address = getString(R.string.txt_no_address);
                            }
                            mAddressTv.setTextKeepState(address);
                        } else {
                            // Something is wrong. The Cursor should always contain data.
                            // Report an error in the entry.
                            setTitle(getText(R.string.title_error));
                        }
                    }

                    @Override
                    public void onInsertComplete(int token, Object cookie, Uri uri) {
                        // unused
                    }

                    @Override
                    public void onUpdateComplete(int token, Object cookie, int result) {
                        // unused
                    }

                    @Override
                    public void onDeleteComplete(int token, Object cookie, int result) {
                        // unused
                    }
                });
        // Using the URI passed in with the triggering Intent, gets the entry or entries in
        // the provider.
        queryHandler.startQuery(QUERY_TOKEN, null, mUri, PROJECTION, null, null, null);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "onSaveInstanceState: called");
        if (mUri != null) {
            savedInstanceState.putString(STATE_URI, mUri.toString());
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_contact_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_edit) {
            // open contact edit activity
            startActivity(new Intent(Intent.ACTION_EDIT, mUri));
            finish();
            return true;
        } else if (id == R.id.action_delete) {
            // delete the contact
            deleteEntry();
            finish();
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, ContactsListActivity.class));
        finish();
        super.onBackPressed();
    }

    /**
     * Take care of deleting an entry.
     */
    private void deleteEntry() {
        // TODO: 2/5/16 may be moved to Util
        Log.d(TAG, "deleteEntry: called");
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
            NotifyingAsyncQueryHandler deleteHandler = new NotifyingAsyncQueryHandler(this,
                    new NotifyingAsyncQueryHandler.AsyncQueryListener() {
                        @Override
                        public void onQueryComplete(int token, Object cookie, Cursor cursor) {
                            // unused
                        }

                        @Override
                        public void onInsertComplete(int token, Object cookie, Uri uri) {
                            // unused
                        }

                        @Override
                        public void onUpdateComplete(int token, Object cookie, int result) {
                            // unused
                        }

                        @Override
                        public void onDeleteComplete(int token, Object cookie, int result) {
                            Toast.makeText(ContactViewActivity.this, R.string.txt_contact_deleted,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
            deleteHandler.startDelete(DELETE_TOKEN, null, mUri, null, null);
        } else {
            Log.d(TAG, "deleteEntry: cursor is null");
        }
    }
}
