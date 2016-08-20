package view_drage_detel;

/**
 * 滑动监听
 * Created by csw on 2016/6/10 0010.
 */
public interface OnItemDragListener {

    /**
     * 开始拖拽
     * @param pos   拖拽的Item位置
     */
    void onDragStart(int pos);

    /**
     * 拖拽过程中Item位置监听
     * @param oldPos    原先的Item位置
     * @param newPos    最新拖拽的Item位置
     */
    void onDragging(int oldPos, int newPos);

    /**
     * 拖拽结束
     * @param pos   拖拽的Item位置
     */
    void onDragEnd(int pos);
}
