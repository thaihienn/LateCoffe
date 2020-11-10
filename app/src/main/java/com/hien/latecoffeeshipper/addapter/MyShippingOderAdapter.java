package com.hien.latecoffeeshipper.addapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.hien.latecoffeeshipper.R;
import com.hien.latecoffeeshipper.ShippingActivity;
import com.hien.latecoffeeshipper.common.Common;
import com.hien.latecoffeeshipper.model.ShippingOderModel;

import java.text.SimpleDateFormat;
import java.util.List;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.paperdb.Paper;

public class MyShippingOderAdapter extends RecyclerView.Adapter<MyShippingOderAdapter.MyViewHolder> {

    Context context;
    List<ShippingOderModel> shippingOderModelList;
    SimpleDateFormat simpleDateFormat;

    public MyShippingOderAdapter(Context context, List<ShippingOderModel> shippingOderModelList) {
        this.context = context;
        this.shippingOderModelList = shippingOderModelList;
        simpleDateFormat =new SimpleDateFormat("dd-mm-yyyy HH:mm:ss");
        Paper.init(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View itemView= LayoutInflater.from(context)
           .inflate(R.layout.layout_oder_shipper,parent,false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context)
                .load(shippingOderModelList.get(position).getOrderModel().getCartItemList()
                .get(0).getFoodImage())
                .into(holder.img_food);
        holder.txt_date.setText(new StringBuilder(
               simpleDateFormat.format(shippingOderModelList.get(position).getOrderModel().getCreateDate())
        ));
        Common.setSpanStringColor("No.: ",
                shippingOderModelList.get(position).getOrderModel().getKey(),
                holder.txt_oder_number, Color.parseColor("#BA454A"));
        Common.setSpanStringColor("Address: ",
                shippingOderModelList.get(position).getOrderModel().getShippingAddress(),
                holder.txt_address, Color.parseColor("#BA454A"));
        Common.setSpanStringColor("Payment: ",
                shippingOderModelList.get(position).getOrderModel().getTransactionId(),
                holder.txt_payment, Color.parseColor("#BA454A"));
        //disable button when already in a trip
        if (shippingOderModelList.get(position).isStartTrip()){
            holder.btn_ship.setEnabled(false);
        }

        //onclick
        holder.btn_ship.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Paper.book().write(Common.SHIPPING_ORDER_DATA,new Gson().toJson(shippingOderModelList.get(position)));
                context.startActivity(new Intent(context, ShippingActivity.class));
            }
        });



    }

    @Override
    public int getItemCount() {
        return shippingOderModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
         private Unbinder unbinder;
//ánh xạ
         @BindView(R.id.txt_date)
         TextView txt_date;
         @BindView(R.id.txt_oder_number)
         TextView txt_oder_number;
         @BindView(R.id.txt_address)
         TextView txt_address;
         @BindView(R.id.txt_payment)
         TextView txt_payment;
         @BindView(R.id.img_food)
         ImageView img_food;
         @BindView(R.id.btn_shipp)
         MaterialButton btn_ship;
         public MyViewHolder(@NonNull View itemView) {
             super(itemView);
             unbinder = ButterKnife.bind(this,itemView);

         }



    }

}
