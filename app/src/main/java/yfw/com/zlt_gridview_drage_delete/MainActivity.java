package yfw.com.zlt_gridview_drage_delete;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import adapter.GridViewAdapter;
import view_drage_detel.DraggableGridView;
import view_drage_detel.IDragChangeHandler;
/** GridView拖拽排序，长按删除*/
public class MainActivity extends AppCompatActivity implements AdapterView.OnItemLongClickListener
                                         , DraggableGridView.OnTouchInvalidPositionListener{
    private DraggableGridView gv;
    private GridViewAdapter gridViewAdapter;
    private boolean isShowDelete;//是否显示删除按钮
    private Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn= (Button) findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,Main2Activity.class));
            }
        });
        gv = (DraggableGridView) findViewById(R.id.gv);
        gridViewAdapter=new GridViewAdapter(this,getGrayData());
        gv.setAdapter(gridViewAdapter);
        //此方法必须通过setiDragChangeHandler方法传入拖拽改变处理对象
        gv.setDragOutOfParent(true);
        gv.setDragChangeHandler(new IDragChangeHandler() {
            @Override
            public Object getItemDataByPos(int position) {
                return gridViewAdapter.getItem(position);
            }

            @Override
            public void onDragPosChange(int oldPos, int newPos) {
                Log.i("ii", oldPos + ":" + newPos +  ":::" + dataSourceList1);
                Object item = dataSourceList1.remove(oldPos);
                dataSourceList1.add(newPos, item);
                gridViewAdapter.notifyDataSetChanged();
                Log.i("ii", "::::"+((HashMap<String,String>)dataSourceList1.get(0)).get("text"));
            }
        });
        gv.setOnItemLongClickListener(this);//长按删除按钮显示
        gv.setOnTouchInvalidPositionListener(this);//点击空白，删除按钮消失
    }

    //添加数据
    List dataSourceList1;

    private List<HashMap<String, Object>> getGrayData() {
        dataSourceList1 = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < 10; i++) {
            HashMap<String, Object> itemHashMap = new HashMap<String, Object>();
            itemHashMap.put("text", "text+" + i);
            dataSourceList1.add(itemHashMap);
        }
        return dataSourceList1;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        isShowDelete=true;
        gridViewAdapter.setIsShowDelete(isShowDelete);
        return false;
    }

    @Override
    public boolean onTouchInvalidPosition(int motionEvent) {
        isShowDelete=false;
        gridViewAdapter.setIsShowDelete(isShowDelete);
        return false;
    }
}
