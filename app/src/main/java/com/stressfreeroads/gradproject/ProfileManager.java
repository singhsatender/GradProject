package com.stressfreeroads.gradproject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class ProfileManager extends AppCompatActivity {

    private TextView questionNum;
    private TextView questionText;
    private RadioButton ansBtn1;
    private RadioButton ansBtn2;
    private RadioButton ansBtn3;
    private RadioButton ansBtn4;
    private RadioButton ansBtn5;
    private Button next;
    private RadioGroup radioGroup;
    private RadioButton radioButton;

    private int quesCount1=1;

    String questions[] = {"Highways",
                          "Exploring unfamiliar roads",
                          "Stuck behind slow drivers",
                          "Having flexible times",
                          "Driving in rainy weather",
                          "Driving in snowy weather",
                          "Driving at night",
                          "Taking longer routes but with less traffic"};

    ArrayList<String> questionArray = new ArrayList<>(Arrays.asList(questions));


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_manager);

        questionNum = (TextView) findViewById(R.id.question_num);
        questionText = (TextView) findViewById(R.id.question_text);
        ansBtn1 = (RadioButton) findViewById(R.id.ansBtn1);
        ansBtn2 = (RadioButton) findViewById(R.id.ansBtn2);
        ansBtn3 = (RadioButton) findViewById(R.id.ansBtn3);
        ansBtn4 = (RadioButton) findViewById(R.id.ansBtn4);
        ansBtn5 = (RadioButton) findViewById(R.id.ansBtn5);
        next = (Button) findViewById(R.id.next_btn);
        radioGroup = (RadioGroup) findViewById(R.id.radio);

        showNextQuestion(next);


    }

    public void showNextQuestion(View view) {

        if (quesCount1 == (questions.length + 1)) {

            //TODO: redirect to Maps Hompeage

        } else {


            if (quesCount1 != 1) {

                //Get selected answer
                int selectedId = radioGroup.getCheckedRadioButtonId();

                // find the radio button by returned id
                radioButton = (RadioButton) findViewById(selectedId);

                String answer = (String) radioButton.getText();

                //TODO:Save the value to the DB
                System.out.println(answer
                );
            }


            questionNum.setText("Q" + quesCount1);

            if (quesCount1 == questions.length) {
                next.setText("Submit Answers");
            }

            Random random = new Random();
            int randomNum = random.nextInt(questionArray.size());

            //Pick one question
            String question = questionArray.get(randomNum);

            //Set question
            questionText.setText(question);


            // remove and shuffle
            questionArray.remove(randomNum);
            Collections.shuffle(questionArray);

            //reset radio buttons
            radioGroup.clearCheck();

            quesCount1++;

        }
    }
}
