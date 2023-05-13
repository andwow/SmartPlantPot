package com.timusandrei.smartpot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.timusandrei.smartpot.models.SmartPot;

import org.jetbrains.annotations.NotNull;

public class AddDevice extends AppCompatActivity {

    private EditText deviceName;
    private EditText deviceDescription;
    private EditText productId;

    private Button addButton;

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);

        deviceName = findViewById(R.id.device_name);
        deviceDescription = findViewById(R.id.device_description);
        productId = findViewById(R.id.product_id);

        progressBar = findViewById(R.id.progress_bar);

        addButton = findViewById(R.id.add_button);

        addButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String deviceNameText = deviceName.getText().toString().trim();
                String deviceDescriptionText = deviceDescription.getText().toString().trim();
                String productIdText = productId.getText().toString().trim();

                if (deviceNameText.isEmpty()) {
                    deviceName.setError("The name is empty!");
                    deviceName.requestFocus();
                    return;
                }

                if (productIdText.isEmpty()) {
                    deviceName.setError("The Product ID is empty!");
                    deviceName.requestFocus();
                    return;
                }

                FirebaseDatabase.getInstance().getReference(productIdText).addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                        if (!snapshot.exists()) {
                            productId.setError("This device doesn\'t exist!");
                            productId.requestFocus();
                            return;
                        }

                        if (snapshot.child("used").exists()) {
                            productId.setError("This Product ID is already used!");
                            productId.requestFocus();
                            return;
                        }

                        progressBar.setVisibility(View.VISIBLE);

                        FirebaseDatabase.getInstance().getReference(productIdText+"/used").setValue(true);

                        SmartPot smartPot = new SmartPot(deviceNameText, deviceDescriptionText, productIdText);

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        String userId = user.getUid();

                        FirebaseFirestore.getInstance().collection("Users").document(userId).collection("SmartPots").add(smartPot).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {

                            @Override
                            public void onComplete(@NonNull @NotNull Task<DocumentReference> task) {
                                if (task.isSuccessful()) {

                                    Toast.makeText(AddDevice.this, "The device has been added successfully!", Toast.LENGTH_LONG).show();

                                } else {

                                    Toast.makeText(AddDevice.this, "Failed to add! Try again!", Toast.LENGTH_LONG).show();

                                }
                                progressBar.setVisibility(View.GONE);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
                        Toast.makeText(AddDevice.this, error.getMessage() , Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}