package com.example.piotr.contactsview;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver
{
    public NotificationReceiver()
    {
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent detailsIntent = new Intent(context, DetailsActivity.class);
        detailsIntent.putExtras(intent.getExtras());
        detailsIntent.putExtra(DetailsActivity.FROM_BROADCAST, true);
        NotificationHelper notificationHelp = new NotificationHelper(context, detailsIntent);
        notificationHelp.createNotification();
    }
}
