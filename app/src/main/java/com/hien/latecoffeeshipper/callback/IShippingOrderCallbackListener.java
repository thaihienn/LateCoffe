package com.hien.latecoffeeshipper.callback;

import com.hien.latecoffeeshipper.model.ShippingOderModel;

import java.util.List;

public interface IShippingOrderCallbackListener {
    void onShippingOrderLoadSuccess(List<ShippingOderModel>shippingOderModelList);
    void onShippingOderLoadFail(String message);
}
