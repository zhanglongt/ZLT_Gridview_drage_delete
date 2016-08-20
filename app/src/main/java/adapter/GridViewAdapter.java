package adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import yfw.com.zlt_gridview_drage_delete.R;


public class GridViewAdapter extends BaseAdapter {
    private List<HashMap<String, Object>> myList;//List<HashMap<String, Object>> list
    private Context mContext;
    private TextView name_tv;
    private ImageView img;
    private View deleteView;
    private boolean isShowDelete;//根据这个变量来判断是否显示删除图标，true是显示，false是不显示

    public void setMyList(List<HashMap<String, Object>> myList) {
        this.myList = myList;
    }

    public List<HashMap<String, Object>> getMyList() {
        return myList;
    }

    public GridViewAdapter(Context mContext, List<HashMap<String, Object>> myList) {
        this.mContext = mContext;
        this.myList = myList;
    }

    public void setIsShowDelete(boolean isShowDelete) {
        this.isShowDelete = isShowDelete;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {

        return myList.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return myList.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(mContext).inflate(
                R.layout.item_drag_delete, null);

        name_tv = (TextView) convertView.findViewById(R.id.text);
        deleteView = convertView.findViewById(R.id.iv_delete);
        deleteView.setVisibility(isShowDelete ? View.VISIBLE : View.INVISIBLE);//设置删除按钮是否显示
        name_tv.setText(myList.get(position).get("text").toString());
        deleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myList.remove(position);
                notifyDataSetChanged();
                Log.i("ii", "da:" + myList);
            }
        });
        return convertView;
    }


}
