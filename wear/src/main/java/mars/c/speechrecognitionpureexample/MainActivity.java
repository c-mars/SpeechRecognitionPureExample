package mars.c.speechrecognitionpureexample;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends Activity{

    private static final String TAG = MainActivity.class.getName();
    private TextView resultTextView;
    private Button startBtn;
    SpeechRecognizer speechRecognizer;
    SpeechRecognizer.RecognizerListener recognizerListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        FIXME: this code doesn't work on wearables

        recognizerListener = createListener();
        speechRecognizer = new SpeechRecognizer(this, recognizerListener, true);

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                resultTextView = (TextView) stub.findViewById(R.id.result);
                startBtn = (Button) stub.findViewById(R.id.start);
                startBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "startBtn.onClick");
                        speechRecognizer.startListening();
                    }
                });
            }
        });
    }

    private SpeechRecognizer.RecognizerListener createListener() {
        return new SpeechRecognizer.RecognizerListener() {
            @Override
            public void onInit() {
                Log.d(TAG, "onInit");
                resultTextView.setText("init");
            }

            @Override
            public void onResults(ArrayList<String> results) {
                Log.d(TAG, "onResults: " + results);
            }

            @Override
            public void onResult(String result) {
                Log.d(TAG, "onResult: " + result);
                resultTextView.setText(result);
            }

            @Override
            public void onRms(float rmsDb) {

            }

            @Override
            public void onError(int error, String errorMessage) {
                Log.d(TAG, "onError: " + errorMessage);
                resultTextView.setText("error: "+errorMessage);
            }
        };
    }
}
