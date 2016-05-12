package fr.ecole42.fbicher.ft_hangouts.activity;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import fr.ecole42.fbicher.ft_hangouts.R;
import fr.ecole42.fbicher.ft_hangouts.database.ContactsContract;
import fr.ecole42.fbicher.ft_hangouts.util.NotifyingAsyncQueryHandler;
import fr.ecole42.fbicher.ft_hangouts.util.Util;

public class ContactEditActivity extends BaseActivity {

    private static final String TAG = "ContactEditActivity";

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

    // This Activity can be started by more than one action. Each action is represented
    // as a "state" constant
    private static final int ACTIVITY_STATE_EDIT = 0;
    private static final int ACTIVITY_STATE_INSERT = 1;

    // Tokens for AsyncQueryHandler operations
    private static final int QUERY_TOKEN = 1;
    private static final int INSERT_TOKEN = 2;
    private static final int UPDATE_TOKEN = 3;
    private static final int DELETE_TOKEN = 4;

    // Request codes for activities
    private static final int PICK_AVATAR_CODE = 0;

    // Instance states for saving
    private static final String STATE_STATE = "state";
    private static final String STATE_URI = "uri";
    private static final String STATE_AVATAR_URI = "avatar_uri";

    // Global mutable variables
    private int mState;
    private Uri mUri;
    private Cursor mCursor;
    private Uri mAvatarUri;

    private FrameLayout mAvatarHolderFl;
    private ImageView mAvatarIv;
    private EditText mNameEt;
    private EditText mLastNameEt;
    private EditText mPhoneEt;
    private EditText mEmailEt;
    private EditText mAddressEt;

    /**
     * This method is called by Android when the Activity is first started. From the incoming
     * Intent, it determines what kind of editing is desired, and then does it.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_edit);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState != null) {
            Log.d(TAG, "onCreate: restore instance state");
            mState = savedInstanceState.getInt(STATE_STATE);
            String uri = savedInstanceState.getString(STATE_URI);
            if (uri != null) {
                mUri = Uri.parse(uri);
            }
            String avatarUri = savedInstanceState.getString(STATE_AVATAR_URI);
            if (avatarUri != null) {
                mAvatarUri = Uri.parse(avatarUri);
            }
        }
        // Get reference to the avatar view. It is done before defining the action because
        // insert action needs to load default avatar.
        mAvatarIv = (ImageView) findViewById(R.id.iv_avatar);
        Util.loadAvatar(mAvatarUri, mAvatarIv, R.drawable.ic_person_white_24dp, 500,
                ContactEditActivity.this);

        /*
         * Creates an Intent to use when the Activity object's result is sent back to the
         * caller.
         */
        final Intent intent = getIntent();

        /*
         *  Sets up for the edit, based on the action specified for the incoming Intent.
         */

        // Gets the action that triggered the intent filter for this Activity
        final String action = intent.getAction();

        if (Intent.ACTION_EDIT.equals(action)) {
            Log.d(TAG, "onCreate: state edit");
            // For an edit action:
            // Sets the Activity state to EDIT, and gets the URI for the data to be edited.
            mState = ACTIVITY_STATE_EDIT;
            mUri = intent.getData();
        } else if (Intent.ACTION_INSERT.equals(action)) {
            Log.d(TAG, "onCreate: state insert");
            // For an insert action:
            // Sets the Activity state to INSERT
            mState = ACTIVITY_STATE_INSERT;
            // Sets the title to "create" for inserts
            setTitle(getText(R.string.title_create));
        } else {
            // If the action was other than EDIT or INSERT:
            // Logs an error that the action was not understood, finishes the Activity, and
            // returns RESULT_CANCELED to an originating Activity.
            Log.e(TAG, "Unknown action, exiting");
            finish();
            return;
        }

        // Get handles to the widgets in the the layout.
        mAvatarHolderFl = (FrameLayout) findViewById(R.id.fl_avatar_holder);
        mNameEt = (EditText) findViewById(R.id.et_name);
        mLastNameEt = (EditText) findViewById(R.id.et_last_name);
        mPhoneEt = (EditText) findViewById(R.id.et_phone);
        mEmailEt = (EditText) findViewById(R.id.et_email);
        mAddressEt = (EditText) findViewById(R.id.et_address);

        mAvatarHolderFl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChooseAvatarDialog();
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
        Log.d(TAG, "onResume: called");
        super.onResume();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, R.string.txt_storage_permission_refused,
                    Toast.LENGTH_LONG).show();
            finish();
        }

        if (mState == ACTIVITY_STATE_EDIT) {
            Log.d(TAG, "onResume: state edit, query");
            // Using the URI passed in with the triggering Intent, gets the entry or entries in
            // the provider.
            NotifyingAsyncQueryHandler queryHandler = new NotifyingAsyncQueryHandler(this,
                    new NotifyingAsyncQueryHandler.AsyncQueryListener() {
                        @Override
                        public void onQueryComplete(int token, Object cookie, Cursor cursor) {
                            Log.d(TAG, "onQueryComplete: called");
                            mCursor = cursor;

                            if (mCursor != null) {
                                // Moves to the first record. Always call moveToFirst() before accessing
                                // data in a Cursor for the first time. The semantics of using a Cursor
                                // are that when it is created, its internal index is pointing to a
                                // "place" immediately before the first record.
                                mCursor.moveToFirst();

                                // Sets the title to "edit" for edits
                                setTitle(getText(R.string.title_edit));

                                // Gets the entry text from the Cursor and puts it in the TextView,
                                // but doesn't change the text cursor's position.

                                // Avatar
                                if (mAvatarUri == null) {
                                    int avatarIndex = mCursor.getColumnIndex(
                                            ContactsContract.Contacts.COLUMN_NAME_AVATAR);
                                    mAvatarUri = Uri.parse(mCursor.getString(avatarIndex));
                                    Util.loadAvatar(mAvatarUri, mAvatarIv,
                                            R.drawable.ic_person_white_24dp,
                                            500, ContactEditActivity.this);
                                }

                                // Name
                                int nameIndex = mCursor.getColumnIndex(
                                        ContactsContract.Contacts.COLUMN_NAME_NAME);
                                String name = mCursor.getString(nameIndex);
                                mNameEt.setTextKeepState(name);

                                // Last name
                                int lastNameIndex = mCursor.getColumnIndex(
                                        ContactsContract.Contacts.COLUMN_NAME_LAST_NAME);
                                String lastName = mCursor.getString(lastNameIndex);
                                mLastNameEt.setTextKeepState(lastName);

                                // Phone
                                int phoneIndex = mCursor.getColumnIndex(
                                        ContactsContract.Contacts.COLUMN_NAME_PHONE);
                                String phone = mCursor.getString(phoneIndex);
                                mPhoneEt.setTextKeepState(phone);

                                // Email
                                int emailIndex = mCursor.getColumnIndex(
                                        ContactsContract.Contacts.COLUMN_NAME_EMAIL);
                                String email = mCursor.getString(emailIndex);
                                mEmailEt.setTextKeepState(email);

                                // Address
                                int addressIndex = mCursor.getColumnIndex(
                                        ContactsContract.Contacts.COLUMN_NAME_ADDRESS);
                                String address = mCursor.getString(addressIndex);
                                mAddressEt.setTextKeepState(address);
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
            queryHandler.startQuery(QUERY_TOKEN, null, mUri, PROJECTION, null, null, null);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "onSaveInstanceState: called");
        savedInstanceState.putInt(STATE_STATE, mState);
        if (mAvatarUri != null) {
            savedInstanceState.putString(STATE_AVATAR_URI, mAvatarUri.toString());
        }
        if (mUri != null) {
            savedInstanceState.putString(STATE_URI, mUri.toString());
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu: called");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_contact_edit, menu);
        // Hide the delete action for the insert state
        if (mState == ACTIVITY_STATE_INSERT) {
            menu.findItem(R.id.action_delete).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_save) {
            saveEntry();
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
        if (mState == ACTIVITY_STATE_INSERT) {
            startActivity(new Intent(this, ContactsListActivity.class));
            finish();
        } else if (mState == ACTIVITY_STATE_EDIT) {
            startActivity(new Intent(Intent.ACTION_VIEW, mUri));
            finish();
        }
        super.onBackPressed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: called");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_AVATAR_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Log.d(TAG, "onActivityResult: uri=" + data.getData());
                mAvatarUri = data.getData();
                Util.loadAvatar(mAvatarUri, mAvatarIv, R.drawable.ic_person_white_24dp,
                        500, ContactEditActivity.this);
            } else {
                Toast.makeText(this, R.string.txt_avatar_load_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Replaces the current entry contents with the text from text fields and avatar URI.
     */
    private void saveEntry() {
        Log.d(TAG, "saveEntry: called");

        ContentValues values = new ContentValues();
        if (mState == ACTIVITY_STATE_EDIT) {
            // Sets up a map to contain values to be updated in the provider.
            values.put(ContactsContract.Contacts.COLUMN_NAME_MODIFICATION_DATE,
                    System.currentTimeMillis());
        }

        // Retrieve data for saving
        String avatarUri = "";
        if (mAvatarUri != null) {
            avatarUri = mAvatarUri.toString();
        }
        String name = mNameEt.getText().toString();
        String lastName = mLastNameEt.getText().toString();
        String phone = mPhoneEt.getText().toString();
        String email = mEmailEt.getText().toString();
        String address = mAddressEt.getText().toString();

        // This puts the desired entry text into the map.
        values.put(ContactsContract.Contacts.COLUMN_NAME_AVATAR, avatarUri);
        values.put(ContactsContract.Contacts.COLUMN_NAME_NAME, name);
        values.put(ContactsContract.Contacts.COLUMN_NAME_LAST_NAME, lastName);
        values.put(ContactsContract.Contacts.COLUMN_NAME_PHONE, phone);
        values.put(ContactsContract.Contacts.COLUMN_NAME_EMAIL, email);
        values.put(ContactsContract.Contacts.COLUMN_NAME_ADDRESS, address);

        /*
         * Updates the provider with the new values in the map. The RecyclerView is updated
         * automatically. The provider sets this up by setting the notification URI for
         * query Cursor objects to the incoming URI. The content resolver is thus
         * automatically notified when the Cursor for the URI changes, and the UI is
         * updated.
         */
        NotifyingAsyncQueryHandler saveHandler = new NotifyingAsyncQueryHandler(this,
                new NotifyingAsyncQueryHandler.AsyncQueryListener() {
                    @Override
                    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
                        // unused
                    }

                    @Override
                    public void onInsertComplete(int token, Object cookie, Uri uri) {
                        Toast.makeText(ContactEditActivity.this, R.string.txt_contact_created,
                                Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Intent.ACTION_VIEW, uri));
                        finish();
                    }

                    @Override
                    public void onUpdateComplete(int token, Object cookie, int result) {
                        Toast.makeText(ContactEditActivity.this, R.string.txt_contact_updated,
                                Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Intent.ACTION_VIEW, mUri));
                        finish();
                    }

                    @Override
                    public void onDeleteComplete(int token, Object cookie, int result) {
                        // unused
                    }
                });
        if (mState == ACTIVITY_STATE_INSERT) {
            saveHandler.startInsert(INSERT_TOKEN, null, ContactsContract.Contacts.CONTENT_URI, values);
        } else if (mState == ACTIVITY_STATE_EDIT) {
            saveHandler.startUpdate(UPDATE_TOKEN, null, mUri, values, null, null);
        }

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
                            Toast.makeText(ContactEditActivity.this, R.string.txt_contact_deleted,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
            deleteHandler.startDelete(DELETE_TOKEN, null, mUri, null, null);
        } else {
            Log.d(TAG, "deleteEntry: cursor is null");
        }
    }

    private void showChooseAvatarDialog() {
        CharSequence[] items = {
                getString(R.string.txt_choose_avatar),
                getString(R.string.txt_remove_avatar)
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.txt_change_avatar);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        // pick avatar from gallery
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType("image/*");
                        startActivityForResult(intent, PICK_AVATAR_CODE);
                        break;
                    case 1:
                        // set avatar to default
                        mAvatarUri = Uri.parse("");
                        Util.loadAvatar(null, mAvatarIv, R.drawable.ic_person_white_24dp, 500,
                                ContactEditActivity.this);
                        break;
                }
            }
        });
        builder.setNegativeButton(R.string.txt_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
