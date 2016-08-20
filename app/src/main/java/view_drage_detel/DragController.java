package view_drage_detel;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * 拖拽控制器
 * Created by csw on 2016/6/10 0010.
 */
public class DragController {
    //静态键值对避免同个Activity在根视图添加多个拖拽视图，使用弱引用避免activity内存泄露
    private static Map<Activity, WeakReference<DragController>> dragControllerMap = new WeakHashMap<>();
    private static int minOffset;//最小移动长度（设备不同，值可能不同）
    private final FrameLayout decorView;//Window根视图
    private final FrameLayout disPatchView;//新增的事件分发控制视图
    private final ImageView dragView;//拖拽Item视图
    private float preX;//前一次触摸位置X坐标
    private float preY;//前一次触摸位置Y坐标
    private final Rect dragViewRect;//dragView的位置记录
    private final Rect parentRect;
    private final Rect windowRect;
    private DragObject dragObject;

    public static DragController getInstance(Context context) {
        return context == null ? null : getInstance((Activity) context);
    }

    private static DragController getInstance(Activity activity) {
        DragController controller;
        WeakReference<DragController> ref = dragControllerMap.get(activity);
        if (ref != null && ref.get() != null) {//value不为空，软引用中对象不为空
            controller = ref.get();
        } else {
            controller = new DragController(activity);
            dragControllerMap.put(activity, new WeakReference<>(controller));
        }
        return controller;
    }

    private DragController(Activity activity) {
        this.dragViewRect = new Rect();
        this.parentRect = new Rect();
        this.windowRect = new Rect();
        //设备的触摸状态最小移动距离;
        if (minOffset == 0) {
            minOffset = ViewConfiguration.get(activity).getScaledTouchSlop();
        }
        //向activity根布局添加一个View用于显示拖拽中的视图
        decorView = (FrameLayout) activity.getWindow().getDecorView();
        //根布局添加一个FrameLayout用于拦截所有事件再分发
        disPatchView = new FrameLayout(activity);
        decorView.addView(disPatchView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        //使用一个ImageView设置截图的方式来显示拖拽中的Item
        dragView = new ImageView(activity);
        dragView.setVisibility(View.INVISIBLE);
        disPatchView.addView(dragView, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        //拦截事件并分发
        interceptTouchEvent();
    }

    /**
     * 拦截Activity分发下来的事件，做处理并自己再分发(因为这层View要监听Move,而且是Item被长按后理解接收move事件，所以只能先拦截，再自行分发出去)
     */
    private void interceptTouchEvent() {
        disPatchView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        preX = event.getX();
                        preY = event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (dragObject != null) {//处于滑动状态
                            if ((Math.abs(preX - event.getX()) + Math.abs(preY - event.getY())) > minOffset) {//手指移动距离达到最小滑动
                                //更新拖拽视图位置
                                dragObject.updateDragViewPos((int) (event.getX()), (int) (event.getY()));
                                //准备拖拽
                                dragObject.prepareDrag();
                                //拖拽动画进行中，回调当前进度
                                dragObject.inAnimation(dragObject.getDragViewRect().centerX(), dragObject.getDragViewRect().centerY());
                                preX = event.getX();
                                preY = event.getY();
                            }
                            return true;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        //回调动画完成
                        if (dragObject != null) {
                            dragObject.setDragViewVisibility(false);
                            dragObject.completeAnimation(dragViewRect.centerX(), dragViewRect.centerY());
                            dragObject = null;
                        }
                        break;
                }
                //分发事件
                decorView.getChildAt(0).dispatchTouchEvent(event);
                return true;
            }
        });
    }

    /**
     * 开启拖动动画
     *
     * @param view     执行拖动动画的视图
     * @param callBack 动画执行回调
     */
    public void startDragAnimation(@NonNull View parent, @NonNull View view, boolean outOfParent, @NonNull CallBack callBack) {
        //获取view的当前显示图片设置为拖动视图的背景
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap content = Bitmap.createBitmap(view.getDrawingCache());
        dragView.setImageBitmap(content);
        view.destroyDrawingCache();//销毁view中的缓存图
        //获取拖拽视图区域
        dragViewRect.set(0, 0, content.getWidth(), content.getHeight());
        //获取父控件在屏幕中的可见区域
        parent.getGlobalVisibleRect(parentRect);
        decorView.getGlobalVisibleRect(windowRect);
        dragObject = new DragObject(dragView, dragViewRect, parentRect, windowRect, callBack, outOfParent);
    }

    /**
     * 拖拽包装类
     */
    private static class DragObject implements CallBack {
        private final View dragView;//拖拽视图
        private final Rect dragViewRect;//拖拽视图矩形
        private final Rect parentViewRect;//item 父控件矩形
        private final Rect windowRect;//窗口矩形
        private final CallBack callBack;//拖拽回调
        private boolean outOfParent;//是否可以超出父控件范围

        public DragObject(View dragView, Rect dragViewRect, Rect parentViewRect, Rect windowRect, CallBack callBack, boolean outOfParent) {
            this.dragView = dragView;
            this.dragViewRect = dragViewRect;
            this.parentViewRect = parentViewRect;
            this.windowRect = windowRect;
            this.callBack = callBack;
            this.outOfParent = outOfParent;
        }

        @Override
        public void prepareDrag() {
            if (dragView.getVisibility() == View.INVISIBLE) {//回调拖拽动画开启，开启拖拽动画
                callBack.prepareDrag();
                dragView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void inAnimation(int centerX, int centerY) {
            //传进来的坐标是相对于屏幕的，要转成相对于Parent
            callBack.inAnimation(centerX - parentViewRect.left, centerY - parentViewRect.top);
        }

        @Override
        public void completeAnimation(int centerX, int centerY) {
            //传进来的坐标是相对于屏幕的，要转成相对于Parent
            callBack.completeAnimation(centerX - parentViewRect.left, centerY - parentViewRect.top);
        }

        public void setDragViewVisibility(boolean visible) {
            this.dragView.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }

        /**
         * 更新拖拽视图到(x,y)位置
         *
         * @param x x
         * @param y y
         */
        public void updateDragViewPos(int x, int y) {
            tryMoveCenterTo(x, y);
            updateDragViewPos();
        }

        /**
         * 移动dragViewRect中心位置到坐标点
         *
         * @param x x
         * @param y y
         */
        private void tryMoveCenterTo(int x, int y) {
            //移动矩形中心到点上
            this.dragViewRect.offsetTo(x - this.dragViewRect.width() / 2, y - this.dragViewRect.height() / 2);
            if (!outOfParent) {
                //限制不要超出父控件范围
                limitInRect(this.dragViewRect, this.parentViewRect);
            } else {
                //限制不要超出屏幕范围
                limitInRect(this.dragViewRect, this.windowRect);
            }
        }

        /**
         * 限制内部矩形不要超出外部矩形范围，对超出部分进行修正，（不修改矩形大小，优先保证左上不超出）
         *
         * @param innerRect 内部矩形
         * @param wrapRect  外部矩形
         */
        private void limitInRect(Rect innerRect, Rect wrapRect) {
            //调整矩形区域,右边与下边不越界
            innerRect.offset(Math.min(0, wrapRect.right - innerRect.right), Math.min(0, wrapRect.bottom - innerRect.bottom));
            //调整矩形区域，（优先保证）左边与上边不越界
            innerRect.offset(Math.max(0, wrapRect.left - innerRect.left), Math.max(0, wrapRect.top - innerRect.top));
        }

        /**
         * 更新拖拽视图位置
         */
        private void updateDragViewPos() {
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) dragView.getLayoutParams();
            lp.setMargins(dragViewRect.left, dragViewRect.top, 0, 0);
            dragView.requestLayout();
        }

        public Rect getDragViewRect() {
            return dragViewRect;
        }
    }

    public interface CallBack {

        /**
         * 准备开始拖动前回调
         */
        void prepareDrag();

        /**
         * 处于拖动动画中
         *
         * @param centerX 拖动控件中心点X
         * @param centerY 拖动控件中心点Y
         */
        void inAnimation(int centerX, int centerY);

        /**
         * 动画完成
         *
         * @param centerX 拖动控件中心点X
         * @param centerY 拖动控件中心点Y
         */
        void completeAnimation(int centerX, int centerY);
    }

}
