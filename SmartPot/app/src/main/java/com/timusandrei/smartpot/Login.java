package com.timusandrei.smartpot;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import org.jetbrains.annotations.NotNull;

public class Login extends AppCompatActivity implements View.OnClickListener {

    private EditText email, password;
    private Button login, register;
    private TextView forgotPassword;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);

        login = (Button) findViewById(R.id.login_button);
        login.setOnClickListener(this);

        register = (Button) findViewById(R.id.register_button);
        register.setOnClickListener(this);

        forgotPassword = (TextView) findViewById(R.id.forgot_password);
        forgotPassword.setOnClickListener(this);

        progressBar = (ProgressBar) findViewById((R.id.progress_bar));
    }

    @Override
    public void onClick(View view) {

        String emailText = email.getText().toString().trim();
        String passwordText = password.getText().toString().trim();

        switch (view.getId()) {
            case R.id.login_button:
                login(emailText, passwordText);
                break;
            case R.id.register_button:
                startActivity(new Intent(Login.this, Register.class));
                break;
            case R.id.forgot_password:
                resetPassword(emailText);
                break;
        }
    }

    private void resetPassword(String userEmail) {

        if(userEmail.isEmpty()) {
            email.setError("Email is required!");
            email.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
            email.setError("Please enter a valid email!");
            email.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        FirebaseAuth.getInstance().sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if(task.isSuccessful()) {

                    Toast.makeText(Login.this, "You have received an e-mail to reset the password!", Toast.LENGTH_LONG).show();

                } else {

                    Toast.makeText(Login.this, "Something went wrong! Try again!", Toast.LENGTH_LONG).show();

                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void login(String userEmail, String userPassword) {
        progressBar.setVisibility(View.VISIBLE);

        if(userEmail.isEmpty()) {
            email.setError("Email is required!");
            email.requestFocus();
            progressBar.setVisibility(View.GONE);
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
            email.setError("Please enter a valid email!");
            email.requestFocus();
            progressBar.setVisibility(View.GONE);
            return;
        }

        if(userPassword.isEmpty()) {
            password.setError("Enter the password!");
            password.requestFocus();
            progressBar.setVisibility(View.GONE);
            return;
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                if(task.isSuccessful()) {

                    email.setText("");
                    password.setText("");

                    startActivity(new Intent(Login.this, MainMenu.class));

                } else {

                    Toast.makeText(Login.this, "Wrong e-mail or password!", Toast.LENGTH_LONG).show();

                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}
