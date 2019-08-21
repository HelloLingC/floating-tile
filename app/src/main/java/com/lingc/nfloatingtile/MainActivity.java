package com.lingc.nfloatingtile;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.lingc.nfloatingtile.util.SpUtil;

/**
 * 1.0.190807
 * Hello World
 *
 * 1.1.190808
 * 悬浮磁贴图标增加圆角
 * 优化删除全部磁贴功能
 * 解决显示空白磁贴的问题
 * 优化其他内容
 *
 * 1.2.190813
 * 加入了 输入法防挡 功能
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= 23) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#e5e5e5"));
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }


        boolean isFirstBoot = SpUtil.getSp(this).getBoolean("isFirstBoot", true);
        if (isFirstBoot) {
            new AlertDialog.Builder(this)
                    .setTitle("欢迎使用 " + getString(R.string.app_name))
                    .setMessage("在使用之前，您需要了解一些内容：" +
                            "\n\n点击悬浮磁贴可展开或缩放" +
                            "\n长按悬浮磁贴可响应事件，跳转应用" +
                            "\n向下滑动磁贴可移除此悬浮磁贴" +
                            "\n而向右滑动磁贴即可移除屏幕中所有悬浮磁贴（不包括待显示列表的磁贴）" +
                            "\n如果您不手动移除悬浮磁贴它将会一直驻留在屏幕中，您可以修改屏幕内最多磁贴显示数量，默认为6个" +
                            "" +
                            "\n\n应用启动需要通知使用权和悬浮窗权限，带有感叹号的是未赋予的权限，您必须赋予后才能正常使用" +
                            "" +
                            "\n\n在修改应用配置时（如磁贴位置，磁贴方向）应用会清除所有悬浮磁贴（包括显示中和待显示）")
                    .setCancelable(false)
                    .setPositiveButton("了解", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SpUtil.getSp(MainActivity.this).edit().putBoolean("isFirstBoot", false).apply();
                        }
                    })
                    .show();
        }
    }
}
