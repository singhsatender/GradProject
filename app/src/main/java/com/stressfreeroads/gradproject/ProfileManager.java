package com.stressfreeroads.gradproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Manage User Profile questionnaire.
 */
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

    private int quesCount1 = 1;
    DBManager dbManager;

    String questions[] = {"Highways",
            "Exploring unfamiliar roads",
            "Taking longer routes but with less traffic",
            "Having flexible times"};

    ArrayList<String> questionArray = new ArrayList<>(Arrays.asList(questions));

    public static String[] getAnswers() {
        return answers;
    }

    public void setAnswers(String[] answers) {
        this.answers = answers;
    }

    public static String[] answers = new String[7];

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

        dbManager = new DBManager(getApplicationContext());
        dbManager.open();

        //Intent intent = getIntent();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            answers[0] = (String) extras.get("name").toString();
            answers[1] = (String) extras.get("email").toString();
            answers[2] = (String) extras.get("mobile_num").toString();
        }

        showNextQuestion(next);
    }

    /**
     * Save data in db and displays next question if required
     *
     * @param view
     */
    public void showNextQuestion(View view) {

        if ((quesCount1 != 1) && radioGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(getApplicationContext(), "Please select an option", Toast.LENGTH_SHORT).show();
        } else {
            if (quesCount1 == (questions.length + 1)) {
                saveAnswers();
//            dbManager.insert_in_userProfile(answers[0],answers[1],answers[2],answers[3],answers[4],
//                    answers[5],answers[6],answers[7], answers[8], answers[9], answers[10]);

                //Redirect to Maps Hompeage
                Intent i = new Intent(getApplicationContext(), MapHomePage.class);
                startActivity(i);
            } else {
                if (quesCount1 != 1) {
                    saveAnswers();
                }

                questionNum.setText("Q" + quesCount1);

                if (quesCount1 == questions.length) {
                    next.setText("Submit Answers");
                }

                //Pick one question
                String question = questionArray.get(quesCount1 - 1);

                //Set question
                questionText.setText(question);

                //reset radio buttons
                radioGroup.clearCheck();

                quesCount1++;

            }
        }
    }

    /**
     * Saves answers in to an Array.
     */
    public void saveAnswers() {
        //Get selected answer
        int selectedId = radioGroup.getCheckedRadioButtonId();

        // find the radio button by returned id
        radioButton = (RadioButton) findViewById(selectedId);

        String answer = (String) radioButton.getText();

        //Save the values in array
        answers[quesCount1 + 1] = answer;
    }

    @Override
    protected void onStop() {
        super.onStop();
        dbManager.close();
    }


}
