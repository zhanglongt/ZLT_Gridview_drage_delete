# ZLT_Gridview_drage_delete
拖拽，删除，添加
拖拽删除在mainactivity里，需用到view_drage_detel包里的自定义gridview，特别注意：
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
具体代码中有写到；

添加变色，再点击还原：最主要用到FrameLayout布局，点击隐藏，点击显示，此方法比较古板，但效果还可以；
