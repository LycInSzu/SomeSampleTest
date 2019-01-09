package com.lyc.newtestapplication.newtestapplication.LifeBalance.Adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lyc.newtestapplication.newtestapplication.LifeBalance.Interfaces.MyRecycleviewItemClickListener;
import com.lyc.newtestapplication.newtestapplication.LifeBalance.Interfaces.MyRecycleviewItemLongClickListener;
import com.lyc.newtestapplication.newtestapplication.R;

import java.util.ArrayList;
import java.util.List;


//    当然还有一些很实用的API：
//    findFirstVisibleItemPosition() 返回当前第一个可见Item的position
//    findFirstCompletelyVisibleItemPosition() 返回当前第一个完全可见Item的position
//    findLastVisibleItemPosition() 返回当前最后一个可见Item的position
//    findLastCompletelyVisibleItemPosition() 返回当前最后一个完全可见Item的position

public class FunctionItemsListAdapter extends RecyclerView.Adapter {

    private List<String> items;


    MyRecycleviewItemClickListener itemClickListener;
    MyRecycleviewItemLongClickListener itemLongClickListener;

    public void setItemClickListener(MyRecycleviewItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setItemLongClickListener(MyRecycleviewItemLongClickListener itemLongClickListener) {
        this.itemLongClickListener = itemLongClickListener;
    }


    public FunctionItemsListAdapter(@NonNull List<String> dataItems) {
        this.items = (dataItems != null ? dataItems : new ArrayList<String>());
    }




    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.functionitemlayout, parent, false);
        MyViewHolder holder=new MyViewHolder(view,itemClickListener,itemLongClickListener);
        return holder;
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//        Log.i("temp"," position is "+ position);
        ((MyViewHolder)holder).getTextView().setText(items.get(position));

    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return items != null ? items.size() :0;
    }



    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener {

        MyRecycleviewItemClickListener itemClickListener;
        MyRecycleviewItemLongClickListener itemLongClickListener;

        public TextView getTextView() {
            return textView;
        }

        private TextView textView;
        public MyViewHolder(@NonNull View itemView, MyRecycleviewItemClickListener ClickListener, MyRecycleviewItemLongClickListener LongClickListener) {
            super(itemView);
            textView=itemView.findViewById(R.id.functin_name);



            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            itemClickListener=ClickListener;
            itemLongClickListener=LongClickListener;




        }

        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            Log.i("FunctionItemsListAdapter","onClick");
            if (itemClickListener!=null){
                itemClickListener.onItemClick(v,getAdapterPosition());

            }
        }

        /**
         * Called when a view has been clicked and held.
         *
         * @param v The view that was clicked and held.
         * @return true if the callback consumed the long click, false otherwise.
         */
        @Override
        public boolean onLongClick(View v) {
            if (itemLongClickListener!=null){
                itemLongClickListener.onItemLongclick(v);
                return true;
            }
            return true;
        }
    }


}
