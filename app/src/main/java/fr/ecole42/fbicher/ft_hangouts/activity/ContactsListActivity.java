package fr.ecole42.fbicher.ft_hangouts.activity;

import android.Manifest;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import fr.ecole42.fbicher.ft_hangouts.R;
import fr.ecole42.fbicher.ft_hangouts.adapter.ContactsCursorRecyclerAdapter;
import fr.ecole42.fbicher.ft_hangouts.database.ContactsContract;

public class ContactsListActivity extends BaseActivity
        implements LoaderManager.LoaderCallbacks<Cursor>,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = "ContactsListActivity";

    /**
     * The columns needed by the cursor adapter
     */
    private static final String[] PROJECTION = new String[]{
            ContactsContract.Contacts._ID,                      // 0
            ContactsContract.Contacts.COLUMN_NAME_AVATAR,       // 1
            ContactsContract.Contacts.COLUMN_NAME_NAME,         // 2
            ContactsContract.Contacts.COLUMN_NAME_LAST_NAME,    // 3
            ContactsContract.Contacts.COLUMN_NAME_PHONE,        // 4
    };

    /**
     * Id to identify a location permission request.
     */
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 0;

    /**
     * Id to identify a contacts loader.
     */
    private static final int CONTACTS_LOADER_ID = 1;

    private CoordinatorLayout mCoordinatorLayout;
    private TextView mContactsNotAvailableTv;
    private RecyclerView mRecyclerView;
    private ContactsCursorRecyclerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Launches a new Activity using an Intent. The intent filter for the Activity
                // has to have action ACTION_INSERT. No category is set, so DEFAULT is assumed.
                // In effect, this starts the ContactEditActivity.
                startActivity(new Intent(Intent.ACTION_INSERT, getIntent().getData()));
            }
        });

        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);

        /* If no data is given in the Intent that started this Activity, then this Activity
         * was started when the intent filter matched a MAIN action. We should use the default
         * provider URI.
         */
        // Gets the intent that started this Activity.
        Intent intent = getIntent();

        // If there is no data associated with the Intent, sets the data to the default URI, which
        // accesses a list of tasks.
        if (intent.getData() == null) {
            intent.setData(ContactsContract.Contacts.CONTENT_URI);
        }

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        mContactsNotAvailableTv = (TextView) findViewById(R.id.tv_contacts_not_available);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            // READ_EXTERNAL_STORAGE permission is available.
            initRecyclerView();
        } else {
            // READ_EXTERNAL_STORAGE permission is not available.
            mContactsNotAvailableTv.setVisibility(View.VISIBLE);
            requestReadStoragePermission();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Create a new CursorLoader with the following query parameters.
        return new CursorLoader(
                this,
                getIntent().getData(),        // Use the default content URI for the provider.
                PROJECTION,                   // Return the fields from the projection.
                null,                         // No where clause, return all records.
                null,                         // No where clause, therefore no where column values.
                ContactsContract.Contacts.DEFAULT_SORT_ORDER  // Use the default sort order.
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in. (The framework will take care of closing the
        // old cursor once we return.)
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called");
        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission granted
                initRecyclerView();
            } else {
                // permission refused
                Toast.makeText(this, R.string.txt_storage_permission_refused, Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    /**
     * Requests the storage permission.
     * If the permission has been denied previously, a SnackBar will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    private void requestReadStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Snackbar.make(mCoordinatorLayout, R.string.txt_read_storage_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.txt_ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityCompat.requestPermissions(ContactsListActivity.this,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    REQUEST_READ_EXTERNAL_STORAGE);
                        }
                    })
                    .show();
        } else {
            // Location permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_READ_EXTERNAL_STORAGE);
        }
    }

    private void initRecyclerView() {
        // set up recycler view
        mContactsNotAvailableTv.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        mRecyclerView.setHasFixedSize(true);
        // set up the layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        // set up the adapter
        mAdapter = new ContactsCursorRecyclerAdapter(this, null);
        mRecyclerView.setAdapter(mAdapter);

        // Prepare the loader. Either re-connect with an existing one, or start a new one.
        getLoaderManager().initLoader(CONTACTS_LOADER_ID, null, this);
    }
}
