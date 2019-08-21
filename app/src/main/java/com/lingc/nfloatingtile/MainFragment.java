package com.lingc.nfloatingtile;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.text.TextUtils;
import android.widget.Toast;

import com.lingc.nfloatingtile.util.DialogUtil;
import com.lingc.nfloatingtile.widget.FloatingTile;
import com.lingc.nfloatingtile.widget.TileObject;

import java.util.Set;

/**
 * Create by LingC on 2019/8/4 21:54
 */
public class MainFragment extends PreferenceFragmentCompat {
    private boolean isCanDrawWindow;

    @Override
    public void onResume() {
        super.onResume();
        boolean isNotificatListenerEnable = true;
        isCanDrawWindow = true;
        if (!isNotificationListenerEnable(getContext())) {
            isNotificatListenerEnable = false;
            findPreference("notificatListen").setIcon(R.drawable.ic_warning_black_24dp);
        } else {
            findPreference("notificatListen").setIcon(null);
        }
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(getContext())) {
                isCanDrawWindow = false;
                findPreference("flotatingWindow").setIcon(R.drawable.ic_warning_black_24dp);
            } else {
                findPreference("flotatingWindow").setIcon(null);
            }
        }
        if (!(isNotificatListenerEnable && isCanDrawWindow)) {
            Toast.makeText(getContext(), "权限不足，将无法正常使用应用", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        getPreferenceManager().setSharedPreferencesName("appSettings");
        addPreferencesFromResource(R.xml.pref_lay);

        findPreference("notificatListen").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                gotoNotificationAccessSetting();
                return false;
            }
        });
        findPreference("flotatingWindow").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getContext().getPackageName())), 1);
                return false;
            }
        });
        findPreference("sendNotification").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                onResume();
                if (!isCanDrawWindow) {
                    Toast.makeText(preference.getContext(), "无悬浮窗权限", Toast.LENGTH_SHORT).show();
                    return false;
                }
                FloatingTile floatingTile = new FloatingTile();
                floatingTile.setContent(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher),
                        "Title", "Message", getContext().getPackageName(), null);
                floatingTile.setLastTile(TileObject.lastFloatingTile);
                floatingTile.showWindow(getContext());
                return false;
            }
        });
        findPreference("tileShowNum").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                TileObject.clearAllTile();
                return false;
            }
        });
        findPreference("tileDirection").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                TileObject.clearAllTile();
                return false;
            }
        });
        findPreference("tilePosition").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DialogUtil.showDialog(preference.getContext(), new DialogUtil.onClickListener() {
                    @Override
                    public void onClick() {
                        FloatingTile floatingTile = new FloatingTile();
                        floatingTile.setContent(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher),
                                "Title", "Messgae", getContext().getPackageName(), null);
                        floatingTile.setLastTile(TileObject.lastFloatingTile);
                        floatingTile.isEditPos = true;
                        floatingTile.showWindow(getContext());
                    }
                });
                return false;
            }
        });
        findPreference("about").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(preference.getContext())
                        .setTitle("关于 - " + getString(R.string.app_name))
                        .setMessage("版本：" + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")" +
                                "\n开发者：LingC" +
                                "\n编译日期：" + BuildConfig.releaseTime)
                        .setPositiveButton("关闭", null)
                        .show();
                return false;
            }
        });
    }

    private boolean isNotificationListenerEnable(Context context) {
        if (TextUtils.isEmpty(context.getPackageName())) {
            return false;
        }
        Set<String> packagenameSet = NotificationManagerCompat.getEnabledListenerPackages(context);
        return packagenameSet.contains(context.getPackageName());
    }

    private boolean gotoNotificationAccessSetting() {
        try {
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            try {
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.Settings$NotificationAccessSettingsActivity");
                intent.setComponent(cn);
                intent.putExtra(":settings:show_fragment", "NotificationAccessSettings");
                startActivity(intent);
                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return false;
        }
    }

}
