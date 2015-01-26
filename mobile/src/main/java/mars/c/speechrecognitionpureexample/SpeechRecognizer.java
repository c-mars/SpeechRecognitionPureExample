package mars.c.speechrecognitionpureexample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Constantine Mars on 1/26/15.
 *
 * This wrapper gives us simplified interface to use speech.SpeechRecognizer
 */

public class SpeechRecognizer {
    public static final String TAG = SpeechRecognizer.class.getName();

//    standard
    private Context context;
    private android.speech.SpeechRecognizer speechRecognizer;
    private RecognitionListener recognitionListener;
    private Intent recognizerIntent;

//    custom
    private RecognizerCallbacks recognizerCallbacks;
    private Logger logger;
    private final int MAX_RESULTS = 3;

    public SpeechRecognizer(Context context, RecognizerCallbacks recognizerCallbacks, boolean useLogger) {
        this.context = context;
        this.recognizerCallbacks = recognizerCallbacks;
        this.logger = new Logger();
        this.logger.setOn(useLogger);

        this.recognitionListener = createRecognitionListener(this.recognizerCallbacks, this.logger);
        this.recognizerIntent = createIntent(context.getPackageName(), MAX_RESULTS);
    }

//    we need to re-create speech recognizer each time (but maybe this bug should be fixed in future)
    public void startListening() {
        speechRecognizer = android.speech.SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizer.setRecognitionListener(recognitionListener);
        speechRecognizer.startListening(this.recognizerIntent);
    }

    public void stopListening() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
            speechRecognizer.destroy();
        }
    }

    private static RecognitionListener createRecognitionListener(final RecognizerCallbacks recognizerCallbacks, final Logger logger) {
        return new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                logger.logCall();
                recognizerCallbacks.onInit();
            }

            @Override
            public void onBeginningOfSpeech() {
                logger.logCall();
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                logger.logCall();
                recognizerCallbacks.onRms(rmsdB);
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                logger.logCall();
            }

            @Override
            public void onEndOfSpeech() {
                logger.logCall();
            }

            @Override
            public void onError(int error) {
                logger.logCall();
                recognizerCallbacks.onError(error, getErrorText(error));
            }

            @Override
            public void onResults(Bundle results) {
                logger.logCall();
                ArrayList<String> resultsStringArrayList = results.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION);
                recognizerCallbacks.onResults(resultsStringArrayList);
                if (resultsStringArrayList.size()>0) {
                    recognizerCallbacks.onResult(resultsStringArrayList.get(0));
                } else {
                    recognizerCallbacks.onResult(null);
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                logger.logCall();
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                logger.logCall();
            }
        };
    }

    private static Intent createIntent(String packageName, int maxResults) {
        Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, maxResults);
        return recognizerIntent;
    }

    private static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case android.speech.SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case android.speech.SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case android.speech.SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case android.speech.SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case android.speech.SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case android.speech.SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case android.speech.SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case android.speech.SpeechRecognizer.ERROR_SERVER:
                message = "Error from server";
                break;
            case android.speech.SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

//    only important callbacks - other we leave out of sight
    public static interface RecognizerCallbacks {
        public void onInit();
        public void onResults(ArrayList<String> results);
        public void onResult(String result);
        public void onRms(float rmsDb);
        public void onError(int error, String errorMessage);
    }

//  logCall should display current method name using STACK_DEPTH for this
    public static class Logger {
        public void logCall() {
            if (on) {
                Log.d(TAG, Thread.currentThread().getStackTrace()[STACK_DEPTH].getMethodName());
            }
        }

        public void setOn(boolean on) {
            this.on = on;
        }

        private static final String TAG = SpeechRecognizer.TAG;
        private final int DEFAULT_STACK_DEPTH = 4;
        private final int STACK_DEPTH = DEFAULT_STACK_DEPTH + 2;
        private boolean on = true;
    }
}
