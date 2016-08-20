package view_drage_detel;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * 封装好的GridView Item拖拽控件
 * Created by csw on 2016/6/11 0011.
 */
public class DraggableGridView extends GridView {
    private DragController controller;//拖拽控制器
    private MyOnItemLongClickListener listenerDecorator;//长按监听
    private DragController.CallBack callback;//拖拽回调
    private DragStatusType dragStatusType;//拖拽状态
    private int currDragItemPosition;//当前拖拽Item的position，不处于拖拽状态 = -1
    private OnItemDragListener onItemDragListener;//拖拽监听
    private boolean dragOutOfParent;//是否可以拖出父控件范围（default : true）
    private MyIDragChangeHandler dragChangeHandlerDecorator;//拖拽状态变化处理接口

    private OnTouchInvalidPositionListener mTouchInvalidPosListener;

    public DraggableGridView(Context context) {
        super(context);
        init();
    }

    public DraggableGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DraggableGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public interface OnTouchInvalidPositionListener {//GridView点击空白地方事件扩展
        /**
         * motionEvent 可使用 MotionEvent.ACTION_DOWN 或者 MotionEvent.ACTION_UP等来按需要进行判断
         * @return 是否要终止事件的路由
         */
        boolean onTouchInvalidPosition(int motionEvent);
    }

    /**
     * 点击空白区域时的响应和处理接口
     */
    public void setOnTouchInvalidPositionListener(OnTouchInvalidPositionListener listener) {
        mTouchInvalidPosListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {//空白

        if(mTouchInvalidPosListener == null) {
            return super.onTouchEvent(event);
        }

        if (!isEnabled()) {
            // A disabled view that is clickable still consumes the touch
            // events, it just doesn't respond to them.
            return isClickable() || isLongClickable();
        }

        final int motionPosition = pointToPosition((int)event.getX(), (int)event.getY());


        if( motionPosition == INVALID_POSITION ) {
            super.onTouchEvent(event);
            return mTouchInvalidPosListener.onTouchInvalidPosition(event.getActionMasked());
        }

        return super.onTouchEvent(event);
    }

    /**
     * 初始化操作
     */
    private void init() {
        dragOutOfParent = true;
        currDragItemPosition = -1;
        dragStatusType = DragStatusType.NONE;
        //拖拽控制
        controller = DragController.getInstance(getContext());
        //长按监听
        listenerDecorator = new MyOnItemLongClickListener();
        super.setOnItemLongClickListener(listenerDecorator);
        //拖拽状态处理
        dragChangeHandlerDecorator = new MyIDragChangeHandler();

        callback = new DragController.CallBack() {
            @Override
            public void prepareDrag() {
                dragStatusType = DragStatusType.START;
                if (dragChangeHandlerDecorator.hasHandler()) {
                    //刷新界面
                    dragChangeHandlerDecorator.updateStatues(dragStatusType, 0, 0);
                }
                if (onItemDragListener != null) {
                    onItemDragListener.onDragStart(currDragItemPosition);
                }
            }

            @Override
            public void inAnimation(int centerX, int centerY) {
                int pos = pointToPosition(centerX, centerY);
                //如果拖拽控件移动到别的控件上方
                if (pos >= 0 && currDragItemPosition != pos) {
                    dragStatusType = DragStatusType.UPDATE;
                    int temp = currDragItemPosition;
                    currDragItemPosition = pos;
                    //交换数据位置
                    if (dragChangeHandlerDecorator.hasHandler()) {
                        dragChangeHandlerDecorator.updateStatues(dragStatusType, temp, pos);
                    }
                    if (onItemDragListener != null) {
                        onItemDragListener.onDragging(temp, pos);
                    }
                }
            }

            @Override
            public void completeAnimation(int centerX, int centerY) {
                //结束拖动状态,清除坐标信息
                dragStatusType = DragStatusType.END;
                //清除坐标数据
                if (dragChangeHandlerDecorator.hasHandler()) {
                    dragChangeHandlerDecorator.updateStatues(dragStatusType, 0, 0);
                }

                dragStatusType = DragStatusType.NONE;
                //清除坐标数据
                if (dragChangeHandlerDecorator.hasHandler()) {
                    dragChangeHandlerDecorator.updateStatues(dragStatusType, 0, 0);
                }
                if (onItemDragListener != null) {
                    onItemDragListener.onDragEnd(currDragItemPosition);
                }
            }
        };
    }

    @Override
    public void requestLayout() {
        //调用适配器方法更新UI，最终会走这里更新界面
        if (dragStatusType == DragStatusType.UPDATE && dragChangeHandlerDecorator.handler != null) {
            View view;
            for (int i = 0; i < getChildCount(); i++) {
                view = getChildAt(i);
                //设置当前选中位置的Item不可见
                view.setVisibility(currDragItemPosition == getFirstVisiblePosition() + i ? View.INVISIBLE : View.VISIBLE);
                dragChangeHandlerDecorator.updateToNewPos(view, getFirstVisiblePosition() + i, currDragItemPosition != (getFirstVisiblePosition() + i));
            }
        }
        super.requestLayout();
    }

    /**
     * 自定义GridView内部实现item拖拽动画
     * 1）获取或者初始化拖拽控制器
     * <p/>
     * 对外提供接口，提供拖拽状态，和被拖拽的Item pos(用于adapter设置被拖拽Item的效果)
     */
    @Override
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        listenerDecorator.listener = listener;
    }

    public void setDragChangeHandler(IDragChangeHandler iDragChangeHandler) {
        this.dragChangeHandlerDecorator.handler = iDragChangeHandler;
    }

    public void setOnItemDragListener(OnItemDragListener onItemDragListener) {
        this.onItemDragListener = onItemDragListener;
    }

    public void setDragOutOfParent(boolean dragOutOfParent) {
        this.dragOutOfParent = dragOutOfParent;
    }

    /**
     * 长按监听代理类
     */
    private class MyOnItemLongClickListener implements OnItemLongClickListener {
        private OnItemLongClickListener listener = null;

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if (id >= 0) {
                currDragItemPosition = (int) id;
                controller.startDragAnimation(parent, view, dragOutOfParent, callback);
            }
            if (listener != null) {
                listener.onItemLongClick(parent, view, position, id);
            }
            return true;
        }
    }

    /**
     * 拖拽改变处理类
     */
    private class MyIDragChangeHandler implements IDragChangeHandler {
        private IDragChangeHandler handler = null;
        private int[] currPos;//没什么卵用就是用于获取视图位置
        private Map<Object, int[]> posRecord;//数据对象位置记录

        public MyIDragChangeHandler() {
            currPos = new int[2];
            //数据对象位置记录
            posRecord = new WeakHashMap<>();
        }

        public void updateStatues(DragStatusType type, int oldPos, int newPos) {
            View view;
            switch (type) {
                case START:
                    //即将开始拖拽
                    //1)记录当前位置
                    for (int i = 0; i < getChildCount(); i++) {
                        view = getChildAt(i);
                        initPos(view, getItemDataByPos(getFirstVisiblePosition() + i));
                    }
                    //更新显示状态（选中的Item不可见）
                    getChildAt(currDragItemPosition - getFirstVisiblePosition()).setVisibility(INVISIBLE);
                    break;
                case UPDATE:
                    //外部对数据位置进行交换，外部交换完成后需要自己更新界面
                    onDragPosChange(oldPos, newPos);
                    break;
                case END:
                    //坐标记录清除
                    int[] ints;
                    Set<Map.Entry<Object, int[]>> set = posRecord.entrySet();
                    Iterator<Map.Entry<Object, int[]>> iterator = set.iterator();
                    for (; iterator.hasNext(); ) {
                        ints = iterator.next().getValue();
                        ints[0] = 0;
                        ints[1] = 0;
                    }
                    break;
                case NONE:
                    //选中的Item可见
                    getChildAt(currDragItemPosition - getFirstVisiblePosition()).setVisibility(VISIBLE);
                    currDragItemPosition = -1;
                default:
                    break;
            }
        }

        @Override
        public Object getItemDataByPos(int position) {
            return handler.getItemDataByPos(position);
        }

        @Override
        public void onDragPosChange(int oldPos, int newPos) {
            handler.onDragPosChange(oldPos, newPos);
        }

        public boolean hasHandler() {
            if (handler == null) {
                throw new RuntimeException("必须通过setiDragChangeHandler方法传入拖拽改变处理对象");
            }
            return true;
        }

        /**
         * 记录初始位置
         *
         * @param view 视图
         */
        public void initPos(View view, Object o) {
            int[] newPos = getPosArrByKey(o);
            view.getLocationOnScreen(newPos);
        }

        /**
         * 更新视图位置记录,启动位置变化动画
         *
         * @param view 视图
         * @param ani  是否启动动画
         */
        public void updateToNewPos(View view, int pos, boolean ani) {
            //获取视图坐标
            int[] prePos = getPosArrByKey(getItemDataByPos(pos));
            view.getLocationOnScreen(currPos);
            int offsetX = prePos[0] - currPos[0];
            int offsetY = prePos[1] - currPos[1];
            if (offsetX != 0 || offsetY != 0) {//对比之前的位置，如果发生位移
                //更新旧坐标，执行位移动画
                prePos[0] = currPos[0];
                prePos[1] = currPos[1];
                if (ani) {//空对象不需要执行动画
                    TranslateAnimation animation = new TranslateAnimation(offsetX, 0, offsetY, 0);
                    animation.setDuration(300);
                    animation.setInterpolator(new AccelerateDecelerateInterpolator());
                    view.startAnimation(animation);
                }
            }
        }

        /**
         * 获取item数据对象对应的坐标记录
         *
         * @param o item数据对象
         * @return 坐标
         */
        private int[] getPosArrByKey(Object o) {
            int[] ints = posRecord.get(o);
            if (ints == null) {
                ints = new int[2];
                posRecord.put(o, ints);
            }
            return ints;
        }
    }
    /**
     * 设置不滚动
     */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }
}
