package com.stressfreeroads.gradproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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


                if( TextUtils.isEmpty(signupInputName.getText())|| TextUtils.isEmpty(signupInputMobileno.getText()) || TextUtils.isEmpty(signupInputEmail.getText())){
                    Toast.makeText(getApplicationContext(), "One or more fields are empty.", Toast.LENGTH_SHORT).show();
                } else {
                    //Pass data to new intent
                    Intent i = new Intent(getApplicationContext(), ProfileManager.class);
                    i.putExtra("name", signupInputName.getText());
                    i.putExtra("email", signupInputEmail.getText());
                    i.putExtra("mobile_num", signupInputMobileno.getText());
                    startActivity(i);
                }
            }
        });
    }
}
