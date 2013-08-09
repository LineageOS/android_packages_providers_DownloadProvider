package com.android.providers.downloads.ui;

import java.util.ArrayList;
import java.util.List;

import org.cyanogenmod.support.ui.LiveFolder;

import com.android.providers.downloads.OpenHelper;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

public class LiveFolderReceiver extends BroadcastReceiver {

    private static boolean sHasLiveFolders = false;

    private static Bitmap retrieveAndSetIcon(Context ctx, String mediaType) {
        if (mediaType == null) {
            return null;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromParts("file", "", null), mediaType);
        PackageManager pm = ctx.getPackageManager();
        List<ResolveInfo> list = pm.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        if (list.size() == 0) {
            return BitmapFactory.decodeResource(ctx.getResources(),
                    R.drawable.ic_download_misc_file_type);
        } else {
            return ((BitmapDrawable)list.get(0).activityInfo.loadIcon(pm))
                    .getBitmap();
        }
    }

    public static void updateFolders(Context ctx, long folderId) {
        if (!sHasLiveFolders) {
            return;
        }

        DownloadManager mDownloadManager = (DownloadManager) ctx.
                getSystemService(Context.DOWNLOAD_SERVICE);
        mDownloadManager.setAccessAllDownloads(true);
        DownloadManager.Query baseQuery = new DownloadManager.Query().
                setOnlyIncludeVisibleInDownloadsUi(true);
        Cursor cursor = mDownloadManager.query(baseQuery);

        ArrayList<LiveFolder.Item> folderItems = new ArrayList<LiveFolder.Item>();

        while(cursor.moveToNext() && folderItems.size() < LiveFolder.Constants.MAX_ITEMS) {
            LiveFolder.Item fItem = new LiveFolder.Item();
            fItem.setLabel(cursor.getString(cursor.getColumnIndex(
                    DownloadManager.COLUMN_TITLE)));
            String mediaType = cursor.getString(cursor.getColumnIndex(
                    DownloadManager.COLUMN_MEDIA_TYPE));
            fItem.setIcon(retrieveAndSetIcon(ctx, mediaType));
            fItem.setId((int) cursor.getLong(cursor
                    .getColumnIndexOrThrow(BaseColumns._ID)));
            folderItems.add(fItem);
        }

        if (folderId == 0) {
            LiveFolder.updateAllFolders(ctx, folderItems);
        } else {
            LiveFolder.updateSingleFolder(ctx, folderId, folderItems);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String type = intent.getStringExtra(LiveFolder.Constants.FOLDER_UPDATE_TYPE_EXTRA);

        if (!TextUtils.isEmpty(type)) {

            long folderId = intent.getLongExtra(LiveFolder.Constants.FOLDER_ID_EXTRA, 0);

            if (folderId > 0 || type.equals(LiveFolder.Constants.EXISTING_FOLDERS_CREATED)) {
                if (type.equals(LiveFolder.Constants.NEW_FOLDER_CREATED)) {

                    sHasLiveFolders = true;
                    updateFolders(context, folderId);

                } else if (type.equals(LiveFolder.Constants.EXISTING_FOLDERS_CREATED)) {

                    sHasLiveFolders = true;
                    updateFolders(context, 0);

                } else if (type.equals(LiveFolder.Constants.FOLDER_ITEM_SELECTED)) {

                    int itemId = intent.getIntExtra(LiveFolder.Constants.FOLDER_ITEM_ID_EXTRA, 0);
                    final Intent intenta = OpenHelper.buildViewIntent(context, itemId);
                    intenta.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intenta);

                } else if (type.equals(LiveFolder.Constants.FOLDER_ITEM_REMOVED)) {

                    // Get selected item id
                    int itemId = intent.getIntExtra(LiveFolder.Constants.FOLDER_ITEM_ID_EXTRA, 0);
                    DownloadManager mDownloadManager = (DownloadManager) context.
                            getSystemService(Context.DOWNLOAD_SERVICE);
                    mDownloadManager.setAccessAllDownloads(true);
                    mDownloadManager.markRowDeleted(itemId);

                }
            }
        }
    }

}
