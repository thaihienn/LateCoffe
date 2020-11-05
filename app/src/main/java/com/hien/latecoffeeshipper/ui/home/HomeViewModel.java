package com.hien.latecoffeeshipper.ui.home;


import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hien.latecoffeeshipper.callback.IShippingOrderCallbackListener;
import com.hien.latecoffeeshipper.common.Common;
import com.hien.latecoffeeshipper.model.ShippingOderModel;
import java.util.ArrayList;
import java.util.List;


public class HomeViewModel extends ViewModel implements IShippingOrderCallbackListener {

    private MutableLiveData<List<ShippingOderModel>> shippingOrderMutableData;
    private MutableLiveData<String> messageError;
    private IShippingOrderCallbackListener listener;

    public HomeViewModel() {
        shippingOrderMutableData = new MutableLiveData<>();
        messageError = new MutableLiveData<>();
        listener = this;
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    public MutableLiveData<List<ShippingOderModel>> getShippingOrderMutableData(String shipperPhone) {
        loadOrderByShipper(shipperPhone);
        return shippingOrderMutableData;

    }

    private void loadOrderByShipper(String shipperPhone) {
        List<ShippingOderModel> tempList = new ArrayList<>();
        Query orderRef = FirebaseDatabase.getInstance()
                .getReference(Common.SHIPPING_ORDER_REF)
                .orderByChild("shipperPhone")
                .equalTo(Common.currentShipperUser.getPhone());
        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot orderSnapshot:dataSnapshot.getChildren()) {
                        ShippingOderModel shippingOderModel =orderSnapshot.getValue(ShippingOderModel.class);
                        tempList.add(shippingOderModel);
                }
                listener.onShippingOrderLoadSuccess(tempList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onShippingOderLoadFail(databaseError.getMessage());
            }
        });
    }

    @Override
    public void onShippingOrderLoadSuccess(List<ShippingOderModel> shippingOderModelList) {
shippingOrderMutableData.setValue(shippingOderModelList);
    }

    @Override
    public void onShippingOderLoadFail(String message) {
        messageError.setValue(message);
    }
}