package com.timusandrei.smartpot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.timusandrei.smartpot.models.User;

import org.jetbrains.annotations.NotNull;

public class Register extends AppCompatActivity implements View.OnClickListener {

    private TextView firstName, lastName, email, password, confirm;
    private FirebaseAuth mAuth;
    private Button register;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        firstName = (EditText) findViewById(R.id.first_name);
        lastName = (EditText) findViewById(R.id.last_name);
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        confirm = (EditText) findViewById(R.id.confirm_password);


        register = (Button) findViewById(R.id.register_button);
        register.setOnClickListener(this);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.register_button:
                registerUser();
                break;
        }
    }

    private void registerUser() {

        String firstNameText = firstName.getText().toString().trim();
        String lastNameText = lastName.getText().toString().trim();
        String emailText = email.getText().toString().trim();
        String passwordText = password.getText().toString().trim();
        String confirmText = confirm.getText().toString().trim();

        if(emailText.isEmpty()) {
            email.setError("Email is required!");
            email.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
            email.setError("Please enter a valid email!");
            email.requestFocus();
            return;
        }

        if(passwordText.isEmpty()) {
            password.setError("Password is required!");
            password.requestFocus();
            return;
        }

        if(passwordText.length() < 6) {
            password.setError("Min password length should be 6 characters!");
            password.requestFocus();
            return;
        }

        if (!passwordText.equals(confirmText)) {
            confirm.setError("Not the same password!");
            confirm.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            User user = new User(emailText, firstNameText, lastNameText);

                            FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                        Toast.makeText(Register.this, "User has been registered successfully!", Toast.LENGTH_LONG).show();

                                    } else {

                                        Toast.makeText(Register.this, "Failed to register! Try again!", Toast.LENGTH_LONG).show();

                                    }
                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                        } else {

                            Toast.makeText(Register.this, "Failed to register! Try again!", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);

                        }
                    }
                });
    }
}