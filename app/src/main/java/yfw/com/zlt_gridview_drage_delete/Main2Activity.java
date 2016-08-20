package yfw.com.zlt_gridview_drage_delete;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import adapter.GridViewAddDelAdapter;
import adapter.GridViewaddAdapter;
import view_myGridview.MyGridview;

public class Main2Activity extends AppCompatActivity implements AdapterView.OnItemClickListener{
    private MyGridview gvz,gvl;
    private GridViewAddDelAdapter gridViewAdapter;
    private GridViewaddAdapter addAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        addData=new ArrayList();
        gvz= (MyGridview) findViewById(R.id.gvz);
        gvl= (MyGridview) findViewById(R.id.gvl);
        gridViewAdapter=new GridViewAddDelAdapter(getGrayData(),this);
        gvz.setAdapter(gridViewAdapter);
        gvz.setOnItemClickListener(this);


    }

    //添加数据
    List dataSourceList1;
    List addData;
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
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {


        if(view.findViewById(R.id.text1).getVisibility()==View.GONE){
            view.findViewById(R.id.text1).setVisibility(View.VISIBLE);
            view.findViewById(R.id.text).setVisibility(View.GONE);
            addData.add(((HashMap<String , String>)adapterView.getAdapter().getItem(i)).get("text"));
        }else {
            view.findViewById(R.id.text).setVisibility(View.VISIBLE);
            view.findViewById(R.id.text1).setVisibility(View.GONE);
            addData.remove(((HashMap<String , String>)adapterView.getAdapter().getItem(i)).get("text"));
        }
        addAdapter=new GridViewaddAdapter(addData,this);
        gvl.setAdapter(addAdapter);
        Log.i("ii",adapterView.getAdapter().getItem(i).toString()+"::"+dataSourceList1+":"+addData);
    }
}
