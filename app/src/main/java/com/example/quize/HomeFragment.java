package com.example.quize;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;


import java.util.List;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class HomeFragment extends Fragment {

    public static final int REQUEST_CODE_QUIZ = 1;
    public static final String EXTRA_CATEGORY_ID = "extraCategoryID";
    public static final String EXTRA_CATEGORY_NAME = "extraCategoryName";
    public static final String EXTRA_DIFFICULTY = "extraDifficulty";

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String KEY_HIGHSCORE = "keyHighscore";

    private TextView textViewHighScore;
    private Button continueButton;
    private Spinner spinnerCategory;

    private int highScore = 0;

    private Dialog dialog;
    RelativeLayout popupLinearView;
    TextView popupCogrtsText;
    TextView popupDesText;
    LinearLayout linearLayout;
    TextView highscores;
    TextView incorrectAnswer;
    TextView correctAnswer;
    TextView totalQestions ;
    Button popupClose;

    Animation fromsmall, fromnothing, togo;

    public HomeFragment()
    {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        dialog = new Dialog(this.getActivity());

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        textViewHighScore = view.findViewById(R.id.text_view_highscore);
        continueButton = view.findViewById(R.id.button_continue_quiz);
        spinnerCategory = view.findViewById(R.id.spinner_Category);

        fromsmall = AnimationUtils.loadAnimation(this.getActivity(), R.anim.fromsmall);
        fromnothing = AnimationUtils.loadAnimation(this.getActivity(), R.anim.fromnothing);
        togo = AnimationUtils.loadAnimation(this.getActivity(), R.anim.togo);

        loadCategories();
        loadHighscore();



        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startQuiz();
            }
        });

        return view;

    }

    private void startQuiz() {
        Category selectedCategory = (Category) spinnerCategory.getSelectedItem();
        int categoryID = selectedCategory.getId();
        String categoryName = selectedCategory.getName();

        Intent intent = new Intent(this.getActivity(), QuizeActivity.class);
        intent.putExtra(EXTRA_CATEGORY_ID, categoryID);
        intent.putExtra(EXTRA_CATEGORY_NAME, categoryName);
        startActivityForResult(intent, REQUEST_CODE_QUIZ);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_QUIZ)
        {
            if (resultCode == RESULT_OK)
            {

                dialog.setContentView(R.layout.popup_activity);

                final RelativeLayout popupLayout = dialog.findViewById(R.id.popup_relative_layout);
                final RelativeLayout popupLayoutContent = dialog.findViewById(R.id.popup_content_layout);
                highscores = dialog.findViewById(R.id.highscore);
                incorrectAnswer = dialog.findViewById(R.id.incorrect_answer);
                correctAnswer = dialog.findViewById(R.id.correct_answer);
                totalQestions = dialog.findViewById(R.id.total_question);
                popupClose = dialog.findViewById(R.id.button_popup_close);

                popupLayout.setAlpha(1);
                popupLayout.startAnimation(fromsmall);
                popupClose.setAnimation(fromnothing);


                int score = data.getIntExtra(QuizeActivity.EXTRA_SCORE, 0);
                if (score > highScore)
                {
                    updateHighScore(score);
                    highscores.setText(String.valueOf(highScore));
                }
                else { highscores.setText(String.valueOf(highScore)); }
                int incorrect = data.getIntExtra(QuizeActivity.TOTAL_INCORRECT, 0);
                int totalQuesiton = data.getIntExtra(QuizeActivity.TOTAL_QUESTIONS, 0);

                incorrectAnswer.setText(String.valueOf(incorrect));
                totalQestions.setText(String.valueOf(totalQuesiton));
                correctAnswer.setText(String.valueOf(score));

                popupClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        popupLayout.setAlpha(0);
                        popupLayoutContent.startAnimation(togo);

                        ViewCompat.animate(popupLayout).setStartDelay(1000).alpha(0).start();
                        ViewCompat.animate(popupLayoutContent).setStartDelay(1000).alpha(0).start();

                        dialog.dismiss();
                    }
                });

                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                dialog.show();
            }
        }
    }


    private void loadCategories() {
        Context context = this.getActivity();
        QuizDbHelper dbHelper = QuizDbHelper.getInstance(context);
        List<Category> categories = dbHelper.getAllCategories();

        ArrayAdapter<Category> adapterCategories = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, categories);
        adapterCategories.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinnerCategory.setAdapter(adapterCategories);
    }

    private void loadHighscore()
    {
        SharedPreferences preferences = this.getActivity().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        highScore = preferences.getInt(KEY_HIGHSCORE, 0);
        textViewHighScore.setText("Highscore: " + highScore);
    }

    private void updateHighScore(int highScoreNew) {
        highScore = highScoreNew;
        textViewHighScore.setText("Highscore: " + highScore);

        SharedPreferences preferences = this.getActivity().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_HIGHSCORE, highScore);
        editor.apply();
    }
}
