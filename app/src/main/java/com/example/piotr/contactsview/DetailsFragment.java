package com.example.piotr.contactsview;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class DetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{
    private static final String ARG_PARAM1 = "UserLookUpId";
    private static final String ARG_PARAM2 = "UserId";
    private static final String ARG_PARAM3 = "UserName";
    private static final String SELECTION = ContactsContract.Data.LOOKUP_KEY + " = ?";
    private static final String SORT_ORDER = ContactsContract.Data.MIMETYPE;
    private static final String[] PROJECTION =
            {
                    ContactsContract.CommonDataKinds.Phone._ID,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.TYPE,
                    ContactsContract.CommonDataKinds.Phone.LABEL
            };
    private final int DETAILS_LOADER = 1;
    private TextView text;
    private ImageView image;
    private String lookUpKey = "";
    private String userName = "";
    private long userId = -1;
    private String[] mSelectionArgs = {""};

    public DetailsFragment()
    {
        // Required empty public constructor
    }

    public static DetailsFragment newInstance(String uLookUpKey, long id, String displayName)
    {
        DetailsFragment fragment = new DetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, uLookUpKey);
        args.putLong(ARG_PARAM2, id);
        args.putString(ARG_PARAM3, displayName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            lookUpKey = getArguments().getString(ARG_PARAM1);
            userId = getArguments().getLong(ARG_PARAM2);
            userName = getArguments().getString(ARG_PARAM3);
        }
    }

    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_details, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        text = (TextView) getActivity().findViewById(R.id.userName);
        image = (ImageView) getActivity().findViewById(R.id.userImage);

        getLoaderManager().initLoader(DETAILS_LOADER, null, this);
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
    }

    public void openDisplayPhoto(ImageView iv)
    {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, userId);
        Uri displayPhotoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.DISPLAY_PHOTO);
        new LoadPhoto(iv).execute(displayPhotoUri);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args)
    {
        mSelectionArgs[0] = lookUpKey;
        CursorLoader mLoader =
                new CursorLoader(
                        getActivity(),
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        PROJECTION,
                        SELECTION,
                        mSelectionArgs,
                        SORT_ORDER
                );
        return mLoader;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
    {
        try
        {
            if (cursor.moveToFirst())
            {
                text.setText(cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                text.setOnClickListener(new View.OnClickListener()
                {

                    @Override
                    public void onClick(View v)
                    {

                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:" + text.getText()));
                        startActivity(intent);

                    }
                });

                openDisplayPhoto(image);

            }
        } catch (Exception e)
        {
            Toast.makeText(getActivity(),
                    e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private class LoadPhoto extends AsyncTask<Uri, Void, Bitmap>
    {
        private ImageView imageView;

        public LoadPhoto(ImageView iv)
        {
            imageView = iv;
        }

        @Override
        protected Bitmap doInBackground(Uri... params)
        {
            try
            {
                AssetFileDescriptor fd =
                        getActivity().getContentResolver().openAssetFileDescriptor(params[0], "r");

                Bitmap b = BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor());
                return b;
            } catch (Exception e)
            {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result)
        {
            if (result != null)
            {
                imageView.setImageBitmap(result);
            }
            else
            {
                imageView.setImageResource(R.drawable.ic_profile);
            }
        }
    }
}
