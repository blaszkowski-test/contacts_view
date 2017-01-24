package com.example.piotr.contactsview;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;

import java.util.Calendar;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.support.v4.app.NotificationCompat.*;

public class NotificationHelper
{

    private final int NOTIFICATION_ID = 1;
    private final int PENDING_ACTIVITY = 3;
    private Context mContext;
    private Intent mIntent;
    private NotificationCompat.Builder mNotification;
    private NotificationManagerCompat mNotificationManager;
    private PendingIntent mContentIntent;

    public NotificationHelper(Context context, Intent intent)
    {
        mContext = context;
        mIntent = intent;
    }

    /**
     * Put the notification into the status bar
     */
    public void createNotification()
    {
        mNotificationManager = NotificationManagerCompat.from(mContext);

        Calendar calendar = Calendar.getInstance();

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addParentStack(DetailsActivity.class);
        stackBuilder.addNextIntent(mIntent);

        mContentIntent =
                stackBuilder.getPendingIntent(
                        PENDING_ACTIVITY,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mNotification = new NotificationCompat.Builder(mContext)
                .setContentTitle(mContext.getString(R.string.app_name))
                .setTicker(mContext.getString(R.string.app_name))
                //.setProgress(100, 0, false)
                .setContentText(mIntent.getStringExtra(MainActivity.User_Name))
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setWhen(calendar.getTimeInMillis())
                .setAutoCancel(true)
                .setDefaults(DEFAULT_SOUND | DEFAULT_VIBRATE | DEFAULT_LIGHTS)
                .setContentIntent(mContentIntent);

        //make this notification appear in the 'Ongoing events' section
        //mNotification.flags = Notification.FLAG_ONGOING_EVENT;

        //show the notification
        mNotificationManager.notify(NOTIFICATION_ID, mNotification.build());
    }

    /**
     * Receives progress updates from the background task and updates the status bar notification appropriately
     *
     * @param percentageComplete
     */
    public void progressUpdate(int percentageComplete)
    {
        //build up the new status message
        CharSequence contentText = percentageComplete + "% complete";
        //publish it to the status bar

        mNotification.setContentText(contentText);
        mNotification.setProgress(100, percentageComplete, false);

        mNotificationManager.notify(NOTIFICATION_ID, mNotification.build());
    }

    /**
     * called when the background task is complete, this removes the notification from the status bar.
     * We could also use this to add a new ?task complete? notification
     */
    public void completed()
    {
        //remove the notification from the status bar
        mNotificationManager.cancel(NOTIFICATION_ID);
    }
}
