package view_drage_detel;

/**
 * 拖拽改变处理接口
 * Created by csw on 2016/6/11 0011.
 */
public interface IDragChangeHandler {

    /**
     * 获取Item对应数据对象
     *
     * @param position Item位置
     * @return 数据对象
     */
    Object getItemDataByPos(int position);

    /**
     * 拖拽位置改变
     * 实现者在此对数据位置进行交换，并调用适配器方法更新数据
     *
     * @param oldPos 旧Position
     * @param newPos 新Position
     */
    void onDragPosChange(int oldPos, int newPos);
}
