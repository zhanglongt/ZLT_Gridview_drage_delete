package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import yfw.com.zlt_gridview_drage_delete.R;

/**
 * Created by Administrator on 2016/8/20 0020.
 */
public class GridViewaddAdapter extends BaseAdapter {
    private List myList;
    private Context mContext;
    private TextView name_tv;

    public GridViewaddAdapter(List myList, Context mContext) {
        this.myList = myList;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return myList.size();
    }

    @Override
    public Object getItem(int i) {
        return myList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = LayoutInflater.from(mContext).inflate(
                R.layout.item_add, null);
        name_tv = (TextView) view.findViewById(R.id.text);
        name_tv.setText(myList.get(i).toString());
        return view;
    }
}
