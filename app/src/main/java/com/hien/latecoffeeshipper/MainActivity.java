package com.hien.latecoffeeshipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hien.latecoffeeshipper.common.Common;
import com.hien.latecoffeeshipper.model.ShipperUserModel;

import java.util.Arrays;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity {
    private static int APP_REQUEST_CODE = 7171;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;
    private AlertDialog dialog;
    private DatabaseReference severRef;
    private List<AuthUI.IdpConfig> providers;

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(listener);
    }

    @Override
    protected void onStop() {
        if (listener != null) {
            firebaseAuth.removeAuthStateListener(listener);
            super.onStop();
        }
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        providers = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build());
        severRef = FirebaseDatabase.getInstance().getReference(Common.SHIPPER_REF);
        firebaseAuth = FirebaseAuth.getInstance();
        dialog = new SpotsDialog.Builder().setCancelable(false).setContext(this).build();
        listener = firebaseAuth -> {
            //Request permission when start app

            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {

                checkUserFromFirebase(user);
            } else {
                //Not login

                phoneLogin();

            }
        };


    }


    //Check user from Firebase
    private void checkUserFromFirebase(FirebaseUser user) {
        dialog.show();
        severRef.child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            ShipperUserModel userModel = dataSnapshot.getValue(ShipperUserModel.class);
                            if (userModel.isActive()) {
                                goToHomeActivity(userModel);
                            } else {
                                dialog.dismiss();
                                Toast.makeText(MainActivity.this, "You must be allowed from admin to login", Toast.LENGTH_SHORT).show();
                            }
                        }else {
                            dialog.dismiss();
                            showRegisterDialog(user);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        dialog.dismiss();
                        Toast.makeText(MainActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //Show Register
    private void showRegisterDialog(FirebaseUser user) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Sign Up");
        builder.setMessage(R.string.msg_fill_info);

        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_register, null);
        EditText edt_name = itemView.findViewById(R.id.edt_name);
        EditText edt_phone = itemView.findViewById(R.id.edt_phone);

        //Set data
        edt_phone.setText(user.getPhoneNumber());
        builder.setView(itemView);
        builder.setNegativeButton(R.string.btn_cancel, (dialogInterface, i) -> dialogInterface.dismiss())
                .setPositiveButton(R.string.btn_register, (dialogInterface, i) -> {
                    if (TextUtils.isEmpty(edt_name.getText().toString())) {
                        Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
                        return;

                    }

                    ShipperUserModel shipperUserModel = new ShipperUserModel();
                    shipperUserModel.setUid(user.getUid());
                    shipperUserModel.setName(edt_name.getText().toString());
                    shipperUserModel.setPhone(edt_phone.getText().toString());
                    shipperUserModel.setActive(false);

                    dialog.show();

                    severRef.child(shipperUserModel.getUid()).setValue(shipperUserModel).
                            addOnFailureListener(e -> {
                                dialog.dismiss();
                                Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            })
                            .addOnCompleteListener(e -> {
                                dialog.dismiss();
                                Toast.makeText(this, "register success", Toast.LENGTH_SHORT).show();
                                goToHomeActivity(shipperUserModel);

                            });
                });

        builder.setView(itemView);

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    //Go HomeActivity
    private void goToHomeActivity(ShipperUserModel severUserModel) {

        dialog.dismiss();
        Common.currentShipperUser = severUserModel;
        startActivity(new Intent(this, HomeActivity.class));
    }

    private void phoneLogin() {

        startActivityForResult(AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers).build(),
                APP_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_REQUEST_CODE) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            } else {
                Toast.makeText(this, R.string.signin_error, Toast.LENGTH_SHORT).show();
            }
        }

    }


}