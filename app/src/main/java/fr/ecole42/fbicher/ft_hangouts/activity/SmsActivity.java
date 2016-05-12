package fr.ecole42.fbicher.ft_hangouts.activity;

import android.Manifest;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import fr.ecole42.fbicher.ft_hangouts.R;
import fr.ecole42.fbicher.ft_hangouts.adapter.SmsCursorRecyclerAdapter;
import fr.ecole42.fbicher.ft_hangouts.util.Util;

public class SmsActivity extends BaseActivity
        implements LoaderManager.LoaderCallbacks<Cursor>,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = "SmsActivity";

    private static final String[] SMS_PROJECTION = new String[]{
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.TYPE,
            Telephony.Sms.DATE
    };
    private static final String SMS_SORT_ORDER = Telephony.Sms.DATE + " ASC";

    /**
     * Id to identify a SMS permission request.
     */
    private static final int REQUEST_READ_SMS = 1;

    /**
     * Id to identify a contacts loader.
     */
    private static final int SMS_LOADER_ID = 2;

    private Uri mUri;
    private String mPhone;
    private String mName;
    private SmsCursorRecyclerAdapter mAdapter;

    private CoordinatorLayout mCoordinatorLayout;
    private TextView mNoMessagesTv;
    private RecyclerView mRecyclerView;
    private EditText mSendMessageEt;
    private ImageButton mSendMessageIb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        /*
         * Creates an Intent to use when the Activity object's result is sent back to the
         * caller.
         */
        final Intent intent = getIntent();

        // Gets the action that triggered the intent filter for this Activity
        final String action = intent.getAction();

        // For a VIEW_SMS action:
        if (Util.ACTION_VIEW_SMS.equals(action)) {
            mUri = intent.getData();
            Bundle extras = intent.getExtras();
            if (extras != null) {
                mName = extras.getString(Util.EXTRA_NAME);
                mPhone = extras.getString(Util.EXTRA_PHONE);
            }
        } else {
            // Logs an error that the action was not understood, finishes the Activity, and
            // returns RESULT_CANCELED to an originating Activity.
            Log.e(TAG, "Unknown action, exiting");
            finish();
        }

        setTitle(mName);

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        mNoMessagesTv = (TextView) findViewById(R.id.tv_no_messages);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mSendMessageEt = (EditText) findViewById(R.id.et_send_message);
        mSendMessageIb = (ImageButton) findViewById(R.id.ib_send_message);

        // set up the layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        // set up the adapter
        mAdapter = new SmsCursorRecyclerAdapter(this, null);
        mRecyclerView.setAdapter(mAdapter);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            // READ_SMS permission is available.
            // Prepare the loader. Either re-connect with an existing one, or start a new one.
            getLoaderManager().initLoader(SMS_LOADER_ID, null, this);
        } else {
            // READ_SMS permission is not available.
            mNoMessagesTv.setText(R.string.txt_no_sms_permission);
            requestReadSmsPermission();
        }

        mSendMessageIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(SmsActivity.this,
                        Manifest.permission.SEND_SMS)
                        == PackageManager.PERMISSION_GRANTED) {
                    // READ_SMS permission is available.
                    String message = mSendMessageEt.getText().toString();
                    if (message.length() > 0) {
                        SmsManager.getDefault().sendTextMessage(mPhone, null, message, null, null);
                        mSendMessageEt.setText(null);
                    } else {
                        Toast.makeText(SmsActivity.this, R.string.txt_empty_message,
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // READ_SMS permission is not available.
                    requestReadSmsPermission();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: called");
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: called");
        super.onBackPressed();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader: called");
        String selection = Telephony.Sms.ADDRESS + "=?";
        String[] selectionArgs = {mPhone};
        return new CursorLoader(
                this,
                Telephony.Sms.CONTENT_URI,
                SMS_PROJECTION,
                selection,
                selectionArgs,
                SMS_SORT_ORDER
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in. (The framework will take care of closing the
        // old cursor once we return.)
        Log.d(TAG, "onLoadFinished: cursor size=" + data.getCount());
        if (data.getCount() > 0) {
            mNoMessagesTv.setVisibility(View.GONE);
            mAdapter.swapCursor(data);
            mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount() - 1);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        Log.d(TAG, "onLoaderReset: called");
        mAdapter.swapCursor(null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called");
        if (requestCode == REQUEST_READ_SMS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission granted
                recreate();
            } else {
                // permission refused
                Toast.makeText(this, R.string.txt_sms_permission_refused, Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    /**
     * Requests the read sms permission.
     * If the permission has been denied previously, a SnackBar will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    private void requestReadSmsPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_SMS)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Snackbar.make(mCoordinatorLayout, R.string.txt_read_sms_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.txt_ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityCompat.requestPermissions(SmsActivity.this,
                                    new String[]{Manifest.permission.READ_SMS},
                                    REQUEST_READ_SMS);
                        }
                    })
                    .show();
        } else {
            // Location permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_SMS},
                    REQUEST_READ_SMS);
        }
    }
}
