package com.stressfreeroads.gradproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Signup extends AppCompatActivity {

    private Button btnSignUpNext;
    private EditText signupInputName, signupInputEmail, signupInputMobileno;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        signupInputName = (EditText) findViewById(R.id.signup_input_name);
        signupInputEmail = (EditText) findViewById(R.id.signup_input_email);
        signupInputMobileno = (EditText) findViewById(R.id.signup_input_mobile_no);
        btnSignUpNext = (Button) findViewById(R.id.btn_signup_next);

        btnSignUpNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO:Store or pass data to new intent
                //submitForm();
                Intent i = new Intent(getApplicationContext(),ProfileManager.class);
                startActivity(i);
            }
        });
    }
}
