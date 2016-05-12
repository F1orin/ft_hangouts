package fr.ecole42.fbicher.ft_hangouts.database;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines a contract between the Contacts content provider and its clients. A contract defines the
 * information that a client needs to access the provider as one or more data tables. A contract
 * is a public, non-extendable (final) class that contains constants defining column names and
 * URIs. A well-written client depends only on the constants in the contract.
 * <p>
 * Created by fbicher on 1/22/16.
 */
public final class ContactsContract {

    public static final String AUTHORITY = "fr.ecole42.fbicher.provider.ft_hangouts";

    // This class cannot be instantiated
    private ContactsContract() {
    }

    /**
     * Entries table contract
     */
    public static final class Contacts implements BaseColumns {

        // This class cannot be instantiated
        private Contacts() {
        }

        /**
         * The table name offered by this provider
         */
        public static final String TABLE_NAME = "contacts";

        /*
         * URI definitions
         */

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        /**
         * Path parts for the URIs
         */

        /**
         * Path part for the Entries URI
         */
        private static final String PATH_ENTRIES = "/contacts";

        /**
         * Path part for the Entry ID URI
         */
        private static final String PATH_ENTRY_ID = "/contacts/";

        /**
         * 0-relative position of an entry ID segment in the path part of a entry ID URI
         */
        public static final int ENTRY_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_ENTRIES);

        /**
         * The content URI base for a single entry. Callers must
         * append a numeric entry id to this Uri to retrieve an entry
         */
        public static final Uri CONTENT_ID_URI_BASE
                = Uri.parse(SCHEME + AUTHORITY + PATH_ENTRY_ID);

        /**
         * The content URI match pattern for a single entry, specified by its ID. Use this to match
         * incoming URIs or to construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN
                = Uri.parse(SCHEME + AUTHORITY + PATH_ENTRY_ID + "/#");

        /*
         * MIME type definitions
         */

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of entries.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.florin.contacts";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single entry.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.florin.contacts";

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "name ASC";

        /*
         * Column definitions
         */

        /**
         * Column name for the avatar URI
         * <P>Type: TEXT</P>
         */
        public static final String COLUMN_NAME_AVATAR = "avatar";

        /**
         * Column name for the name
         * <P>Type: TEXT</P>
         */
        public static final String COLUMN_NAME_NAME = "name";

        /**
         * Column name for the last name
         * <P>Type: TEXT</P>
         */
        public static final String COLUMN_NAME_LAST_NAME = "last_name";

        /**
         * Column name for the phone number
         * <P>Type: TEXT</P>
         */
        public static final String COLUMN_NAME_PHONE = "phone";

        /**
         * Column name for the email
         * <P>Type: TEXT</P>
         */
        public static final String COLUMN_NAME_EMAIL = "email";

        /**
         * Column name for the address
         * <P>Type: TEXT</P>
         */
        public static final String COLUMN_NAME_ADDRESS = "address";

        /**
         * Column name for the creation timestamp
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String COLUMN_NAME_CREATE_DATE = "created";

        /**
         * Column name for the modification timestamp
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String COLUMN_NAME_MODIFICATION_DATE = "modified";
    }
}
