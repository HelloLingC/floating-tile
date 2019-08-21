package com.lingc.nfloatingtile.widget;

import android.util.Log;
import android.util.SparseBooleanArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Create by LingC on 2019/8/6 16:06
 */
public class TileObject {

    public static int showTileNum = 0;

    public static FloatingTile lastFloatingTile;

    public static List<FloatingTile> showingFloatingTileList = new ArrayList<>();

    // key: Tile的y, value: Tile是否在显示
    // 数量一般为指定数量
    public static SparseBooleanArray positionArray = new SparseBooleanArray();

    public static List<FloatingTile> waitingForShowingTileList = new ArrayList<>();

    public static int getYInNullTile() {
        for (int i = 0; i < positionArray.size(); i++) {
            boolean isExiat = positionArray.valueAt(i);
            if (!isExiat) {
                return positionArray.keyAt(i);
            }
        }
        return -1;
    }

    public static void clearShowingTile() {
        lastFloatingTile = null;
        showTileNum = 0;
        positionArray.clear();
        for (FloatingTile floatingTile : showingFloatingTileList) {
            floatingTile.removeView();
            floatingTile.showWaitingTile();
        }
        showingFloatingTileList.clear();
//        for (int i = 0; i < positionArray.size(); i++) {
//            int y = positionArray.keyAt(i);
//            positionArray.put(y, false);
//        }
    }

    public static void clearAllTile() {
        waitingForShowingTileList.clear();
        // 待显示列表必须在这之前清除，否则会触发显示机会事件
        clearShowingTile();
    }

    /**
     * 获取已被移除的 Tile
     * @return
     */
//    public static FloatingTile getNullOneInList() {
//        for (FloatingTile floatingTile : floatingTileList) {
//            if (floatingTile.isRemove) {
//                return floatingTile;
//            }
//        }
//        return null;
//    }

}
