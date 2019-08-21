package com.lingc.nfloatingtile;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.PowerManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;

import com.lingc.nfloatingtile.widget.FloatingTile;
import com.lingc.nfloatingtile.widget.TileObject;

/**
 * Create by LingC on 2019/8/4 21:46
 */
public class NotificationService extends NotificationListenerService {
    private String content;

    @Override
    public void onNotificationPosted(final StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        if (!sbn.isClearable() || sbn.getPackageName().equals(getPackageName())
                || sbn.getPackageName().equals("android")) {
            return;
        }
        PowerManager powerManager = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        if (!powerManager.isScreenOn()) {
            return;
        }
        cancelAllNotifications();

        Bundle extras = sbn.getNotification().extras;
//        int id = sbn.getId();
        final Bitmap icon = extras.getParcelable(android.app.Notification.EXTRA_LARGE_ICON);
        final String title = extras.getString(android.app.Notification.EXTRA_TITLE);
        content = extras.getString(android.app.Notification.EXTRA_TEXT);

        if (content == null) {
            content = "";
        }
        if (TextUtils.isEmpty(title) && TextUtils.isEmpty(content)) {
            return;
        }
        if (content.contains("下载") || content.contains("%")) {
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                FloatingTile floatingTile = new FloatingTile();
                floatingTile.setContent(icon, title, content, sbn.getPackageName(), sbn.getNotification().contentIntent);
                floatingTile.setLastTile(TileObject.lastFloatingTile);
                floatingTile.showWindow(NotificationService.this);
            }
        }).start();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
    }
}
