package com.example.piotr.contactsview;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements
        ContactsFragment.OnContactClickListener, PermissionHelper.PermissionHelperCallback
{
    public static final String User_Look = "UserLookUpId";
    public static final String User_Id = "UserId";
    public static final String User_Name = "UserName";
    public static final String ARG_PARAM1 = "ContactsFragmentSearchText";
    private PermissionHelper permissionHelper;
    private boolean permissionGranted = false;
    private SearchView searchView;
    private ContactsFragment contactsFragment;
    private MenuItem searchItem;
    private String searchSentence;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null)
        {
            searchSentence = savedInstanceState.getString(ARG_PARAM1);
        }

        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        permissionHelper = new PermissionHelper(this);

        permissionGranted = permissionHelper.getPermissions(Manifest.permission.READ_CONTACTS);

        if (permissionGranted)
        {
            loadListFragment();
        }
    }

    private void loadListFragment()
    {
        contactsFragment = ContactsFragment.newInstance();
        Helper.LoadFragment(getSupportFragmentManager(), R.id.contact_body, contactsFragment, false);
    }

    protected void onSaveInstanceState(Bundle outState)
    {
        outState.putString(ARG_PARAM1, searchView.getQuery().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String query)
            {
                contactsFragment.updateSearchText(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText)
            {
                contactsFragment.updateSearchText(newText);
                return false;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener()
        {
            @Override
            public boolean onClose()
            {
                return true;
            }
        });

        if (searchSentence != null && !searchSentence.isEmpty())
        {
            searchItem.expandActionView();
            searchView.setQuery(searchSentence, true);
            searchView.clearFocus();
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        permissionHelper.helperPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void userSelected(String lookUpKey, long id, String displayName)
    {
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra(User_Look, lookUpKey);
        intent.putExtra(User_Id, id);
        intent.putExtra(User_Name, displayName);
        startActivity(intent);
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
