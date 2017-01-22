package com.example.piotr.contactsview;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.content.Context.ALARM_SERVICE;


public class ContactsFragment extends ListFragment implements
        AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener,
        LoaderManager.LoaderCallbacks<Cursor>
{

    private static final String LIST_POSITION = "ContactsFragmentSListPosition";
    private static final String LIST_STATE = "ContactsFragmentSListView";
    private static final String[] PROJECTION =
            {
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.LOOKUP_KEY,
                    ContactsContract.Contacts.DISPLAY_NAME
            };
    private final int PENDING_BROADCAST = 2;
    private final int LIST_LOADER = 1;
    private Parcelable stateOfList = null;
    private int positionOfList = -1;
    private OnContactClickListener mCallback;
    private String searchSentence;
    private ContactsCursorAdapter mCursorAdapter;
    private Handler mHandler;
    private ContactObserver contactObserver;
    private LruCache<String, Bitmap> photoCache;
    private Set<String> photoNotAvailable;

    public ContactsFragment()
    {
        // Required empty public constructor
    }

    public static ContactsFragment newInstance()
    {
        return new ContactsFragment();
    }

    public void updateSearchText(String searchText)
    {
        searchSentence = searchText.isEmpty() ? null : searchText;
        getLoaderManager().restartLoader(LIST_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args)
    {
        Uri baseUri;
        if (searchSentence != null)
        {
            baseUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI,
                    Uri.encode(searchSentence));
        }
        else
        {
            baseUri = ContactsContract.Contacts.CONTENT_URI;
        }

        String select = "((" + ContactsContract.Contacts.DISPLAY_NAME + " NOTNULL) AND ("
                + ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1) AND ("
                + ContactsContract.Contacts.DISPLAY_NAME + " != '' ))";

        return new CursorLoader(getActivity(), baseUri,
                PROJECTION, select, null,
                ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        // Delete the reference to the existing Cursor
        mCursorAdapter.swapCursor(null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
    {
        // Put the result Cursor in the adapter for the ListView
        mCursorAdapter.swapCursor(cursor);

        if (stateOfList != null)
        {
            getListView().onRestoreInstanceState(stateOfList);
            getListView().setSelection(positionOfList);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        try
        {
            final Cursor cursor = mCursorAdapter.getCursor();
            cursor.moveToPosition(position);

            mCallback.userSelected(cursor.getString(cursor.getColumnIndexOrThrow("LOOKUP")),
                    cursor.getLong(cursor.getColumnIndexOrThrow("_ID")),
                    cursor.getString(cursor.getColumnIndexOrThrow("DISPLAY_NAME")),
                    view);
        } catch (Exception e)
        {
            Toast.makeText(getActivity(),
                    e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
    {
        try
        {
            final Cursor cursor = mCursorAdapter.getCursor();
            cursor.moveToPosition(position);

            Intent intent = new Intent(getActivity(), NotificationReceiver.class);
            intent.putExtra(MainActivity.User_Look, cursor.getString(cursor.getColumnIndexOrThrow("LOOKUP")));
            intent.putExtra(MainActivity.User_Id, cursor.getLong(cursor.getColumnIndexOrThrow("_ID")));
            intent.putExtra(MainActivity.User_Name, cursor.getString(cursor.getColumnIndexOrThrow("DISPLAY_NAME")));

            PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), PENDING_BROADCAST, intent, FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (10 * 1000), pendingIntent);

            Toast.makeText(getActivity(),
                    "ADDED",
                    Toast.LENGTH_LONG).show();

        } catch (Exception e)
        {
            Toast.makeText(getActivity(),
                    e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }

        return true;
    }

    public void onSaveInstanceState(Bundle outState)
    {
        outState.putInt(LIST_POSITION, getListView().getFirstVisiblePosition());
        outState.putParcelable(LIST_STATE, getListView().onSaveInstanceState());
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_contacts, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        mCursorAdapter = new ContactsCursorAdapter(getActivity(), photoCache, photoNotAvailable);
        setListAdapter(mCursorAdapter);
        getListView().setOnItemClickListener(this);
        getListView().setOnItemLongClickListener(this);

        if (savedInstanceState != null)
        {
            stateOfList = savedInstanceState.getParcelable(LIST_STATE);
            positionOfList = savedInstanceState.getInt(LIST_POSITION);
            getLoaderManager().restartLoader(LIST_LOADER, null, this);
        }
        else
        {
            getLoaderManager().initLoader(LIST_LOADER, null, this);
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mHandler = new Handler(Looper.getMainLooper())
        {
            @Override
            public void handleMessage(Message inputMessage)
            {
                getLoaderManager().restartLoader(LIST_LOADER, null, ContactsFragment.this);
            }
        };

        contactObserver = new ContactObserver(mHandler);

        RetainFragment retainFragment =
                RetainFragment.findOrCreateRetainFragment(getFragmentManager());

        photoCache = retainFragment.mRetainedCache;
        photoNotAvailable = retainFragment.photoNotAvailable;

        if (photoCache == null)
        {
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
            final int cacheSize = maxMemory / 8;
            photoCache = new LruCache<String, Bitmap>(cacheSize)
            {
                @Override
                protected int sizeOf(String key, Bitmap bitmap)
                {
                    // The cache size will be measured in kilobytes rather than
                    // number of items.
                    return bitmap.getByteCount() / 1024;
                }
            };
            retainFragment.mRetainedCache = photoCache;
            photoNotAvailable = new HashSet<>();
        }
    }

    @Override
    public void onResume()
    {
        getActivity().getContentResolver().registerContentObserver(
                ContactsContract.Contacts.CONTENT_URI,
                true,
                contactObserver);

        super.onResume();
    }

    @Override
    public void onPause()
    {
        getActivity().getContentResolver().unregisterContentObserver(contactObserver);

        super.onPause();
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

        try
        {
            mCallback = (OnContactClickListener) context;
        } catch (ClassCastException e)
        {
            throw new ClassCastException(context.toString()
                    + " must implement OnContactClickListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
    }

    private class ContactObserver extends ContentObserver
    {
        Handler handler;

        public ContactObserver(Handler h)
        {
            super(h);
            handler = h;
        }

        @Override
        public boolean deliverSelfNotifications()
        {
            return super.deliverSelfNotifications();
        }

        @Override
        public void onChange(boolean selfChange)
        {
            this.onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri)
        {
            handler.sendEmptyMessage(0);
        }
    }

    public interface OnContactClickListener
    {
        void userSelected(String lookUpKey, long id, String displayName, View view);
    }
}
