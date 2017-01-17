package com.example.piotr.contactsview;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by piotr on 2017-01-12.
 */

public class PermissionHelper
{
    private static final int PERMISSIONS_REQUEST = 100;
    private Activity activityContext;
    private PermissionHelperCallback callback;
    private List<String> toMakeList;

    public PermissionHelper(Activity context)
    {
        toMakeList = new ArrayList<String>();
        activityContext = context;

        try
        {
            callback = (PermissionHelperCallback) context;
        } catch (ClassCastException e)
        {
            throw new ClassCastException(context.toString()
                    + " must implement PermissionHelperCallback");
        }
    }

    public boolean getPermissions(String... permissionNames)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
        {
            return true;
        }

        boolean requestWasCancelled = false;

        for (String permission : permissionNames)
        {
            if (ContextCompat.checkSelfPermission(activityContext,
                    permission) != PackageManager.PERMISSION_GRANTED)
            {
                if (!requestWasCancelled &&
                        ActivityCompat.shouldShowRequestPermissionRationale(activityContext,
                                permission))
                {
                    requestWasCancelled = true;
                }

                toMakeList.add(permission);
            }
        }

        if (!toMakeList.isEmpty())
        {
            checkPermission(toMakeList.toArray(new String[toMakeList.size()]), requestWasCancelled);
            return false;
        }

        return true;
    }

    public void helperPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        if (PERMISSIONS_REQUEST == requestCode)
        {
            if (permissions.length == grantResults.length)
            {
                boolean allGranted = true;
                for (int grant : grantResults)
                {
                    if (grant != PackageManager.PERMISSION_GRANTED)
                    {
                        allGranted = false;
                        break;
                    }
                }
                if (allGranted)
                {
                    callback.permissionSuccess();
                }
                else
                {
                    callback.permissionFailure();
                }
            }
            else
            {
                callback.permissionFailure();
            }
        }
    }

    private void checkPermission(String[] permissionsArray, boolean requestWasCancelled)
    {
        if (requestWasCancelled)
        {
            final String[] finalPermissionNames = permissionsArray;
            showMessageOKCancel("You need to allow access, because app will not work",
                    new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            makePermissionRequest(finalPermissionNames);
                        }
                    });

        }
        else
        {
            makePermissionRequest(permissionsArray);
        }
    }

    private void makePermissionRequest(String[] permissionNames)
    {
        ActivityCompat.requestPermissions(activityContext,
                permissionNames,
                PERMISSIONS_REQUEST);
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener)
    {
        new AlertDialog.Builder(activityContext)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    public interface PermissionHelperCallback
    {
        public void permissionSuccess();

        public void permissionFailure();
    }
}
