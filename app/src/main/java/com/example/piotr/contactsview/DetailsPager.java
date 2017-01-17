package com.example.piotr.contactsview;

/**
 * Created by piotr on 2017-01-04.
 */

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class DetailsPager extends FragmentStatePagerAdapter
{
    private int mNumOfTabs;
    private String lookUpKey;
    private long userId;
    private String userName;

    public DetailsPager(FragmentManager fm, int NumOfTabs, String _lookUpKey,
                        long _userId,
                        String _userName)
    {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
        lookUpKey = _lookUpKey;
        userId = _userId;
        userName = _userName;
    }

    @Override
    public Fragment getItem(int position)
    {
        Fragment fragment = null;
        switch (position)
        {
            case 0:
                return DetailsFragment.newInstance(lookUpKey, userId, userName);
            case 1:
                return new TabFragment();
            case 2:
                return CameraFragment.newInstance();
            default:
                return null;
        }
    }

    @Override
    public int getCount()
    {
        return mNumOfTabs;
    }
}