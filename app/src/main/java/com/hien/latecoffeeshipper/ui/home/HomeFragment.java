package com.hien.latecoffeeshipper.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hien.latecoffeeshipper.R;
import com.hien.latecoffeeshipper.addapter.MyShippingOderAdapter;
import com.hien.latecoffeeshipper.common.Common;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    @BindView(R.id.recycler_order)
    RecyclerView recycler_order;
    Unbinder unbinder;
    LayoutAnimationController layoutAnimationController;
    MyShippingOderAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        initView(root);
        homeViewModel.getMessageError().observe(getViewLifecycleOwner(),s ->{
            Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
        } );
homeViewModel.getShippingOrderMutableData(Common.currentShipperUser.getPhone()).observe(getViewLifecycleOwner(), shippingOderModels -> {
    adapter = new MyShippingOderAdapter(getContext(),shippingOderModels);
    recycler_order.setAdapter(adapter);
    recycler_order.setLayoutAnimation(layoutAnimationController);
});
        return root;
    }

    private void initView(View root) {
        unbinder = ButterKnife.bind(this,root);
        recycler_order.setHasFixedSize(true);
        LinearLayoutManager layoutManager= new LinearLayoutManager(getContext());
        recycler_order.setLayoutManager(layoutManager);
        recycler_order.addItemDecoration(new DividerItemDecoration(getContext(),layoutManager.getOrientation()));

        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_item_from_left);
    }
}