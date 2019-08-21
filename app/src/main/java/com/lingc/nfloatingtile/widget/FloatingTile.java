package com.lingc.nfloatingtile.widget;

import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lingc.nfloatingtile.R;
import com.lingc.nfloatingtile.util.PackageUtil;
import com.lingc.nfloatingtile.util.SpUtil;

/**
 * Create by LingC on 2019/8/4 22:11
 */
public class FloatingTile {
    private Bitmap icon;
    private String title;
    private String content;
    private String packagename;
    private PendingIntent pendingIntent;
    private Context context;

    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    public boolean isEditPos;
    private boolean isOpen;
    private FloatingTile lastTile;
    public boolean isRemove;
    private View view;

    public void setLastTile(FloatingTile lastTile) {
        this.lastTile = lastTile;
    }

    public void setContent(Bitmap icon, String title, String content, String packagename, PendingIntent pendingIntent) {
        this.icon = icon;
        this.title = title;
        if (!TextUtils.isEmpty(content)) {
            if (content.length() > 17) {
                content = content.substring(0, 18) + "...";
            }
        }
        this.content = content;
        this.packagename = packagename;
        this.pendingIntent = pendingIntent;
    }

    public void showWindow(Context context) {
        this.context = context;
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        view = View.inflate(context, R.layout.window_lay, null);
        final LinearLayout messageLay = view.findViewById(R.id.window_messgae_lay);
        ImageView imageView = view.findViewById(R.id.window_icon_img);
        final TextView titleText = view.findViewById(R.id.window_title_text);
        final TextView contentText = view.findViewById(R.id.window_content_text);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOpen) {
                    messageLay.setVisibility(View.GONE);
                } else {
                    messageLay.setVisibility(View.VISIBLE);
                    titleText.setText(title);
                    contentText.setText(content);
                }
                int width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                int height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                view.measure(width, height);
                int viewWidth = v.getMeasuredWidth();
                int viewHeight = v.getMeasuredHeight();

                layoutParams.width = viewWidth;
                layoutParams.height = viewHeight;
                windowManager.updateViewLayout(v, layoutParams);
                isOpen = !isOpen;
            }
        });
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                try {
                    pendingIntent.send();
                    removeTile();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        });
        if (isEditPos) {
            view.setOnTouchListener(editPosFloatingOnTouchListener);
        } else {
            view.setOnTouchListener(onTouchListener);
        }
        int width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(width, height);
        int viewWidth = view.getMeasuredWidth();
        int viewHeight = view.getMeasuredHeight();

        String showDirection = SpUtil.getSp(context).getString("tileDirection", "down");
//        if (showDirection.equals("up")) {
//            layoutParams.y = -640;
//        } else {
//            layoutParams.y = 300;
//        }
        if (SpUtil.getSp(context).getInt("x", -1) == -1) {
            layoutParams.x = 1024;
            layoutParams.y = 300;
        } else {
            layoutParams.x = SpUtil.getSp(context).getInt("x", -1);
            layoutParams.y = SpUtil.getSp(context).getInt("y", -1);
        }

        int mostShowNum = Integer.valueOf(SpUtil.getSp(context).getString("tileShowNum", "6"));
        if (TileObject.showTileNum == 0) {
            // 屏幕内没有任何 Tile
            lastTile = null;
        }
        if (lastTile != null) {
            if (TileObject.positionArray.size() != mostShowNum) {
                // 此时固定位置并未分配完毕
                if (showDirection.equals("up")) {
                    layoutParams.y = lastTile.layoutParams.y - viewHeight - 18;
                } else {
                    layoutParams.y = lastTile.layoutParams.y + viewHeight + 18;
                }
            } else {
                // OOM
                TileObject.lastFloatingTile = null;
            }
            if (TileObject.positionArray.get(layoutParams.y)) {
                // 位置冲突
                int y = TileObject.getYInNullTile();
                if (y != -1) {
                    // 如果屏幕内有空闲位置
                    layoutParams.y = y;
                    TileObject.positionArray.put(y, true);
                } else {
                    TileObject.waitingForShowingTileList.add(this);
                    return;
                }
            }
        }
        // Todo: 磁贴越界判断
//        if () {
//            TileObject.waitingForShowingTileList.add(this);
//            return;
//        }
        TileObject.positionArray.put(layoutParams.y, true);
        if (TileObject.showTileNum < mostShowNum){
            TileObject.showTileNum++;
        } else {
            // 非法显示，屏幕内磁贴数量已超过指定数量
            TileObject.waitingForShowingTileList.add(this);
            return;
        }
        layoutParams.width = viewWidth;
        layoutParams.height = viewHeight;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.windowAnimations = android.R.style.Animation_Translucent;

        if (icon == null) {
            imageView.setImageDrawable(PackageUtil.getAppIconFromPackname(context, packagename));
        } else {
            imageView.setImageBitmap(icon);
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    windowManager.addView(view, layoutParams);
                } catch (WindowManager.BadTokenException e) {
                    // 无悬浮窗权限
                    e.printStackTrace();
                }

                if (!isEditPos) {
                    TileObject.showingFloatingTileList.add(FloatingTile.this);
                }
            }
        });
        TileObject.lastFloatingTile = this;
    }


    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        private float x1 = 0;
        private float y1 = 0;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x1 = event.getX();
                    y1 = event.getY();
                    break;
                case MotionEvent.ACTION_UP:
                    Log.d("Touch X", event.getX() + "/" + x1);
                    Log.d("Touch Y", event.getY() + "/" + y1);
                    if (event.getX() - x1 > ViewConfiguration.get(context).getScaledTouchSlop()) {
                        TileObject.clearShowingTile();
                    } else if (event.getY() - y1 > ViewConfiguration.get(context).getScaledTouchSlop()) {
                        removeTile();
                    }
                    break;
            }
            return false;
        }
    };

    /**
     * 设置位置
     */
    private View.OnTouchListener editPosFloatingOnTouchListener = new View.OnTouchListener() {
        private int x;
        private int y;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x = (int) event.getRawX();
                    y = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int nowX = (int) event.getRawX();
                    int nowY = (int) event.getRawY();
                    int movedX = nowX - x;
                    int movedY = nowY - y;
                    x = nowX;
                    y = nowY;
                    layoutParams.x = layoutParams.x + movedX;
                    layoutParams.y = layoutParams.y + movedY;
                    // 更新悬浮窗控件布局
                    windowManager.updateViewLayout(v, layoutParams);
                    break;
                case MotionEvent.ACTION_UP:
                    SpUtil.getSp(context).edit().putInt("x", layoutParams.x).apply();
                    SpUtil.getSp(context).edit().putInt("y", layoutParams.y).apply();
                    removeTile();
                    Toast.makeText(context, "修改成功", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    public void removeTile() {
        // It's time to show some Floating Tile that waiting for showing.
        if (isRemove) {
            return;
        }
        TileObject.showTileNum--;
        isRemove = true;
        TileObject.showingFloatingTileList.remove(this);
        TileObject.positionArray.put(layoutParams.y, false);
        showWaitingTile();
        removeView();
    }

    public void showWaitingTile() {
        if (!TileObject.waitingForShowingTileList.isEmpty()) {
            FloatingTile floatingTile = TileObject.waitingForShowingTileList.get(0);
            floatingTile.setLastTile(lastTile);
            floatingTile.showWindow(context);
            TileObject.waitingForShowingTileList.remove(floatingTile);
        }
    }

    public void removeView() {
        windowManager.removeView(view);
    }

}
