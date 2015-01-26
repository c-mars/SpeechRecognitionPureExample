package mars.c.speechrecognitionpureexample;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {
    private String TAG = MainActivity.class.getName();

    private TextView returnedText;
    private ToggleButton toggleButton;
    private ProgressBar progressBar;

    private SpeechRecognizer speechRecognizer;
    private SpeechRecognizer.RecognizerListener recognizerListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        returnedText = (TextView) findViewById(R.id.textView1);
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        toggleButton = (ToggleButton) findViewById(R.id.toggleButton1);

        progressBar.setVisibility(View.INVISIBLE);

        recognizerListener = createRecognizerCallbacks();
        speechRecognizer = new SpeechRecognizer(this, recognizerListener, true);

//        this can be used to list available languages and choose not default language
//        speechRecognizer.chooseFromAvailableLanguages(new DialogLanguageChooser());

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                Log.d(TAG, "toggleButton: "+isChecked);
                if (isChecked) {
                    progressBar.setVisibility(View.VISIBLE);
                    speechRecognizer.startListening();
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                    speechRecognizer.stopListening();
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        speechRecognizer.stopListening();
    }

    private SpeechRecognizer.RecognizerListener createRecognizerCallbacks() {
        return new SpeechRecognizer.RecognizerListener() {
            @Override
            public void onInit() {
                progressBar.setMax(10);
            }

            @Override
            public void onResults(ArrayList<String> results) {
                toggleButton.setChecked(false);

                String text = "";
                Log.d(TAG, "onResults: results="+results.size());
                for (String result : results) {
                    Log.d(TAG, result);
                    text += result + "\n";
                }

                returnedText.setText(text);
            }

            @Override
            public void onResult(String result) {
                String text = returnedText.getText().toString();
                returnedText.setText(text + "\n----\nChosen result: \"" + result + "\"");
            }

            @Override
            public void onRms(float rmsdB) {
                progressBar.setProgress((int)(rmsdB));
            }

            @Override
            public void onError(int error, String errorMessage) {
                returnedText.setText(errorMessage);
                toggleButton.setChecked(false);
            }
        };
    }
}
