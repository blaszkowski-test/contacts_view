package com.example.piotr.contactsview;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

/**
 * Created by piotr on 2017-01-04.
 */

public class Helper
{
    public static void LoadFragment(FragmentManager fragmentManager, int id, Fragment fragment, boolean addToStack)
    {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(id, fragment);
        if (addToStack)
        {
            fragmentTransaction.addToBackStack(null);
        }
        fragmentTransaction.commit();
    }

    public static void DisplayIntent(Context context, Intent intent)
    {
        Bundle b = intent.getExtras();
        StringBuilder sb = new StringBuilder();
        for (String key : b.keySet())
        {
            Object value = b.get(key);
            sb.append(String.format("%s %s (%s)", key,
                    value.toString(), value.getClass().getName()));
        }

        Toast.makeText(context,
                sb.toString(),
                Toast.LENGTH_LONG).show();
    }
}
