package com.lyc.newtestapplication.newtestapplication.LifeBalance.Adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.lyc.newtestapplication.newtestapplication.LifeBalance.Beans.CountDownBean;
import com.lyc.newtestapplication.newtestapplication.LifeBalance.Beans.MyCountdownTimeFormat;
import com.lyc.newtestapplication.newtestapplication.LifeBalance.Interfaces.MyRecycleviewItemClickListener;
import com.lyc.newtestapplication.newtestapplication.LifeBalance.Interfaces.MyRecycleviewItemLongClickListener;
import com.lyc.newtestapplication.newtestapplication.LifeBalance.Utils.MyTimeUtil;
import com.lyc.newtestapplication.newtestapplication.R;

import java.util.ArrayList;
import java.util.List;


//    当然还有一些很实用的API：
//    findFirstVisibleItemPosition() 返回当前第一个可见Item的position
//    findFirstCompletelyVisibleItemPosition() 返回当前第一个完全可见Item的position
//    findLastVisibleItemPosition() 返回当前最后一个可见Item的position
//    findLastCompletelyVisibleItemPosition() 返回当前最后一个完全可见Item的position

public class CountdownListAdapter extends RecyclerView.Adapter {


    private List<CountDownBean> items;

    MyRecycleviewItemClickListener itemClickListener;
    MyRecycleviewItemLongClickListener itemLongClickListener;

    public void setItemClickListener(MyRecycleviewItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setItemLongClickListener(MyRecycleviewItemLongClickListener itemLongClickListener) {
        this.itemLongClickListener = itemLongClickListener;
    }


    public CountdownListAdapter(@NonNull List<CountDownBean> dataItems) {
        this.items = (dataItems != null ? dataItems : new ArrayList<CountDownBean>());
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.countdownitemlayout, parent, false);
        MyViewHolder holder = new MyViewHolder(view, itemClickListener, itemLongClickListener);
        return holder;
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        CountDownBean bean=items.get(position);
        Log.i("temp", " position is " + position);
        ((MyViewHolder) holder).countdownname.setText(bean.getName());
        if (bean.isFinished()){
            ((MyViewHolder) holder).daysLeft.setText("0");
            ((MyViewHolder) holder).hoursLeft.setText("0");
            ((MyViewHolder) holder).minutesLeft.setText("0");
            ((MyViewHolder) holder).secondsLeft.setText("0");
        }else {
            MyCountdownTimeFormat format = MyTimeUtil.convertToTime(bean.getDurition());
            ((MyViewHolder) holder).daysLeft.setText(format.getDays()+"");
            ((MyViewHolder) holder).hoursLeft.setText(format.getHours()+"");
            ((MyViewHolder) holder).minutesLeft.setText(format.getMinutes()+"");
            ((MyViewHolder) holder).secondsLeft.setText(format.getSeconds()+"");
        }

    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

         TextView countdownname;
         TextView hoursLeft;
         TextView daysLeft;
         TextView minutesLeft;
         TextView secondsLeft;

        MyRecycleviewItemClickListener itemClickListener;
        MyRecycleviewItemLongClickListener itemLongClickListener;

        public MyViewHolder(@NonNull View itemView, MyRecycleviewItemClickListener ClickListener, MyRecycleviewItemLongClickListener LongClickListener) {
            super(itemView);
//            ButterKnife.bind(this, itemView);
            countdownname = itemView.findViewById(R.id.countdownname);
            hoursLeft = itemView.findViewById(R.id.hours_left);
            daysLeft = itemView.findViewById(R.id.days_left);
            minutesLeft = itemView.findViewById(R.id.minutes_left);
            secondsLeft = itemView.findViewById(R.id.seconds_left);


            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            itemClickListener = ClickListener;
            itemLongClickListener = LongClickListener;
        }

        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            Log.i("FunctionItemsListAdapter", "onClick");
            if (itemClickListener != null) {
                itemClickListener.onItemClick(v, getAdapterPosition());

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
            if (itemLongClickListener != null) {
                itemLongClickListener.onItemLongclick(v);
                return true;
            }
            return true;
        }
    }


}
