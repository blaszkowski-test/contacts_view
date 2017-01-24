package com.example.piotr.contactsview;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class DetailsActivity extends AppCompatActivity implements PermissionHelper.PermissionHelperCallback
{
    public static final String FROM_BROADCAST = "LoadedFromBroadcast";
    private PermissionHelper permissionHelper;
    private boolean permissionGranted = false;
    private String lookUpKey = "";
    private String userName = "";
    private long userId = -1;
    private boolean LoadedFromBroadcast = false;

    @Override
    protected void onNewIntent(Intent intent)
    {
        lookUpKey = intent.getStringExtra(MainActivity.User_Look);
        userId = intent.getLongExtra(MainActivity.User_Id, -1);
        userName = intent.getStringExtra(MainActivity.User_Name);
        LoadedFromBroadcast = intent.hasExtra(FROM_BROADCAST);
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();

        /* TaskStackBuilder works
        if (LoadedFromBroadcast)
        {
            Intent intent = new Intent(DetailsActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        else
        {
            super.onBackPressed();
        }
        */
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.details_pager);

        if (getIntent() != null)
        {
            lookUpKey = getIntent().getStringExtra(MainActivity.User_Look);
            userId = getIntent().getLongExtra(MainActivity.User_Id, -1);
            userName = getIntent().getStringExtra(MainActivity.User_Name);
            LoadedFromBroadcast = getIntent().hasExtra(FROM_BROADCAST);
        }
        else if (savedInstanceState != null)
        {
            lookUpKey = savedInstanceState.getString(MainActivity.User_Look);
            userId = savedInstanceState.getLong(MainActivity.User_Id);
            userName = savedInstanceState.getString(MainActivity.User_Name);
            LoadedFromBroadcast = savedInstanceState.getBoolean(FROM_BROADCAST);
        }

        permissionHelper = new PermissionHelper(this);

        permissionGranted = permissionHelper.getPermissions(
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.INTERNET
        );

        if (permissionGranted)
        {
            Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(myToolbar);
            myToolbar.setTitle(userName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
            tabLayout.addTab(tabLayout.newTab().setText("Photo"));
            tabLayout.addTab(tabLayout.newTab().setText("Tab"));
            tabLayout.addTab(tabLayout.newTab().setText("Camera"));
            tabLayout.addTab(tabLayout.newTab().setText("Map"));
            tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

            final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
            final DetailsPager adapter = new DetailsPager
                    (getSupportFragmentManager(),
                            tabLayout.getTabCount(),
                            lookUpKey,
                            userId,
                            userName);

            viewPager.setAdapter(adapter);

            viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
            {
                @Override
                public void onTabSelected(TabLayout.Tab tab)
                {
                    viewPager.setCurrentItem(tab.getPosition());
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab)
                {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab)
                {

                }
            });
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        permissionHelper.helperPermissionsResult(requestCode, permissions, grantResults);
    }

    protected void onSaveInstanceState(Bundle outState)
    {
        outState.putString(MainActivity.User_Look, lookUpKey);
        outState.getLong(MainActivity.User_Id, userId);
        outState.putString(MainActivity.User_Name, userName);
        outState.putBoolean(FROM_BROADCAST, LoadedFromBroadcast);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_details, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return false;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void permissionSuccess()
    {
        permissionGranted = true;
        finish();
        startActivity(getIntent());
    }

    @Override
    public void permissionFailure()
    {
        Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
    }
}
