package com.example.piotr.contactsview;

import android.animation.ObjectAnimator;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v4.util.LruCache;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.Set;


/**
 * Created by piotr on 2016-12-31.
 */

public class ContactsCursorAdapter extends CursorAdapter
{
    private LayoutInflater mInflater;
    private Context mContext;
    private LruCache<String, Bitmap> photoCache;
    private Set<String> photoNotAvailable;

    public ContactsCursorAdapter(Context context)
    {
        super(context, null, 0);
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    public ContactsCursorAdapter(Context context, LruCache<String, Bitmap> cache, Set<String> photoNot)
    {
        super(context, null, 0);
        mContext = context;
        mInflater = LayoutInflater.from(context);
        photoCache = cache;
        photoNotAvailable = photoNot;
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap)
    {
        if (getBitmapFromMemCache(key) == null)
        {
            photoCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key)
    {
        return photoCache.get(key);
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

    public void openPhoto(ImageView image, long contactId)
    {
        image.setTag(contactId);
        image.setImageResource(R.drawable.ic_profile);

        if(photoNotAvailable.contains(String.valueOf(contactId)))
        {
            return;
        }

        final Bitmap bitmap = getBitmapFromMemCache(String.valueOf(contactId));
        if (bitmap != null)
        {
            fadeInDisplay(image,bitmap);
            //iv.setImageBitmap(bitmap);
        }
        else
        {
            Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
            Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);

            new LoadThumbnail(new WeakReference<ImageView>(image), contactId).execute(photoUri);
        }
    }


    public void fadeInDisplay(ImageView imageView, Bitmap bitmap) {
        final TransitionDrawable td =
                new TransitionDrawable(new Drawable[]{
                        new ColorDrawable(0),
                        new BitmapDrawable(imageView.getResources(), bitmap)
                });
        imageView.setImageDrawable(td);
        td.startTransition(1000);
    }

    private class LoadThumbnail extends AsyncTask<Uri, Void, Bitmap>
    {
        private WeakReference<ImageView> weakImageView;
        private long contactId;

        public LoadThumbnail(WeakReference<ImageView> image, long contactId)
        {
            weakImageView = image;
            this.contactId = contactId;
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
                addBitmapToMemoryCache(String.valueOf(contactId), result);

                ImageView currentImage = weakImageView.get();
                if(currentImage != null)
                {
                    long tagData = (long) currentImage.getTag();
                    if (tagData == contactId)
                    {
                        fadeInDisplay(currentImage, result);
                    }
                }
            }
            else
            {
                photoNotAvailable.add(String.valueOf(contactId));
            }
        }
    }
}
