package com.lingc.nfloatingtile.util;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.lingc.nfloatingtile.widget.TileObject;

/**
 * Create by LingC on 2019/8/6 22:01
 */
public class DialogUtil {

    public interface onClickListener {
        void onClick();
    }

    public static void showDialog(Context context, final onClickListener onClickListener) {

        new AlertDialog.Builder(context)
                .setTitle("在此之前")
                .setMessage("修改应用配置前，我需要清除您的所有悬浮磁贴（包括显示中和待显示的磁贴）" +
                        "\n点击“确定“后我们可以随时开始")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TileObject.clearAllTile();
                        onClickListener.onClick();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }


}
