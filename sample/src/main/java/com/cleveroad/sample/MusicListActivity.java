package com.cleveroad.sample;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.Collection;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MusicListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Collection<MusicItem>>,
        SearchView.OnQueryTextListener {

    private static final int MUSIC_LOADER_ID = 1;
    private static final int OVERLAY_PERMISSION_REQ_CODE = 1;
    private static final int EXT_STORAGE_PERMISSION_REQ_CODE = 2;

    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;

    @Bind(R.id.empty_view)
    View emptyView;

    private MusicAdapter adapter;
    private EmptyViewObserver emptyViewObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);
        ButterKnife.bind(this);
        adapter = new MusicAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);
        emptyViewObserver = new EmptyViewObserver(emptyView);
        emptyViewObserver.bind(recyclerView);
        MusicFilter filter = new MusicFilter(ContextCompat.getColor(this, R.color.colorAccent));
        adapter.withFilter(filter);
        ItemClickSupport.addTo(recyclerView)
                .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClick(RecyclerView parent, View view, int position, long id) {
                        MusicItem item = adapter.getItem(position);
                        Intent intent = new Intent(MusicListActivity.this, MainActivity.class);
                        intent.putExtra(MainActivity.EXTRA_FILE_URIS, adapter.getSnapshot().toArray(new MusicItem[adapter.getNonFilteredCount()]));
                        intent.putExtra(MainActivity.EXTRA_SELECT_TRACK, item);
                        startActivity(intent);
                    }
                });
        checkReadStoragePermission();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(MusicListActivity.this)) {
                onPermissionsNotGranted();
            } else {
                checkReadStoragePermission();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == EXT_STORAGE_PERMISSION_REQ_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (Manifest.permission.READ_EXTERNAL_STORAGE.equals(permissions[i]) &&
                        grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    loadMusic();
                    return;
                }
            }
            onPermissionsNotGranted();
        }
    }

    /**
     * Check if we have necessary permissions.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void checkReadStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)  != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            ActivityCompat.requestPermissions(MusicListActivity.this, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, EXT_STORAGE_PERMISSION_REQ_CODE);
                        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                            onPermissionsNotGranted();
                        }
                        dialog.dismiss();
                    }
                };
                new AlertDialog.Builder(this)
                        .setTitle(R.string.permissions_title)
                        .setMessage(R.string.read_ext_permissions_message)
                        .setPositiveButton(R.string.btn_continue, onClickListener)
                        .setNegativeButton(R.string.btn_cancel, onClickListener)
                        .setCancelable(false)
                        .show();
                return;
            }
            ActivityCompat.requestPermissions(MusicListActivity.this, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, EXT_STORAGE_PERMISSION_REQ_CODE);
            return;
        }
        loadMusic();
    }

    /**
     * Load music.
     */
    private void loadMusic() {
        getSupportLoaderManager().initLoader(MUSIC_LOADER_ID, null, this);
    }

    /**
     * Permissions not granted. Quit.
     */
    private void onPermissionsNotGranted() {
        Toast.makeText(this, R.string.toast_permissions_not_granted, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem searchItem = menu.findItem(R.id.item_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public Loader<Collection<MusicItem>> onCreateLoader(int id, Bundle args) {
        if (id == MUSIC_LOADER_ID)
            return new MusicLoader(this);
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Collection<MusicItem>> loader, Collection<MusicItem> data) {
        adapter.addAll(data);
        adapter.notifyItemRangeInserted(0, data.size());
    }

    @Override
    public void onLoaderReset(Loader<Collection<MusicItem>> loader) {
        int size = adapter.getItemCount();
        adapter.clear();
        adapter.notifyItemRangeRemoved(0, size);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.getFilter().filter(newText);
        return true;
    }

    @Override
    protected void onDestroy() {
        emptyViewObserver.unbind();
        super.onDestroy();
    }

}
