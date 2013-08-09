package com.android.providers.downloads.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;

import org.cyanogenmod.support.ui.LiveFolder;

public class LiveFolderConfigActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = new Intent("");
        // Set the receiver responsible for folder updates
        ComponentName receiver = new ComponentName(this, LiveFolderReceiver.class);
        i.putExtra(LiveFolder.Constants.FOLDER_RECEIVER_EXTRA, receiver);
        i.putExtra(LiveFolder.Constants.FOLDER_TITLE_EXTRA, getString(R.string.app_label));
        setResult(RESULT_OK, i);
        finish();
    }

}
