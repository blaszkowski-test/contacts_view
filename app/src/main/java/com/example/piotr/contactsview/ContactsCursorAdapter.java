package com.example.piotr.contactsview;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * Created by piotr on 2016-12-31.
 */

public class ContactsCursorAdapter extends CursorAdapter
{
    private LayoutInflater mInflater;
    private Context mContext;

    public ContactsCursorAdapter(Context context)
    {
        super(context, null, 0);
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        final View itemLayout = mInflater.inflate(R.layout.contact_adapter, parent, false);

        //final ViewHolder holder = new ViewHolder();

        return itemLayout;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor)
    {
        TextView text = (TextView) view.findViewById(R.id.textView1);
        final ImageView image = (ImageView) view.findViewById(R.id.imageView1);

        final long id = cursor.getLong(cursor.getColumnIndexOrThrow("_ID"));
        final String name = cursor.getString(cursor.getColumnIndexOrThrow("DISPLAY_NAME"));

        text.setText(name);
        openPhoto(image, id);

    }

    public void openPhoto(ImageView iv, long contactId)
    {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        new LoadThumbnail(iv).execute(photoUri);
    }

    private class LoadThumbnail extends AsyncTask<Uri, Void, Bitmap>
    {
        private ImageView imageView;

        public LoadThumbnail(ImageView iv)
        {
            imageView = iv;
        }

        @Override
        protected Bitmap doInBackground(Uri... params)
        {
            Cursor cursor = mContext.getContentResolver().query(params[0],
                    new String[]{ContactsContract.Contacts.Photo.PHOTO}, null, null, null);

            if (cursor == null)
            {
                return null;
            }
            try
            {
                if (cursor.moveToFirst())
                {
                    byte[] data = cursor.getBlob(0);
                    if (data != null)
                    {
                        Bitmap b = BitmapFactory.decodeByteArray(data, 0, data.length);
                        return b;
                    }
                }
            } finally
            {
                cursor.close();
            }
            return null;
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
