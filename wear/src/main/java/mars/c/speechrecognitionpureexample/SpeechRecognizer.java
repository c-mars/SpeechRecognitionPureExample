package mars.c.speechrecognitionpureexample;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

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
    private Locale locale;
    private boolean listening = false;

//    custom
    private RecognizerListener recognizerListener;
    private Logger logger;
    private final int MAX_RESULTS = 3;

    public SpeechRecognizer(Context context, RecognizerListener recognizerListener, boolean useLogger) {
        this.context = context;
        this.recognizerListener = recognizerListener;
        this.logger = new Logger();
        this.logger.setOn(useLogger);

        this.recognitionListener = createRecognitionListener(this.recognizerListener, this.logger);

        this.locale = Locale.getDefault();
        this.recognizerIntent = createIntent(context.getPackageName(), MAX_RESULTS, this.locale.getLanguage());
    }

//    we need to re-create speech recognizer each time (but maybe this bug should be fixed in future)
    public void startListening() {
        speechRecognizer = android.speech.SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizer.setRecognitionListener(recognitionListener);
        speechRecognizer.startListening(this.recognizerIntent);
        listening = true;
    }

    public void stopListening() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
            speechRecognizer.destroy();
            listening = false;
        }
    }

    public void setLocale(Locale locale) {
        logger.log("setLocale: "+locale.getDisplayLanguage());
        this.locale = locale;
        this.recognizerIntent = createIntent(context.getPackageName(), MAX_RESULTS, this.locale.getLanguage());

//        restart listening if needed
        if (listening) {
            logger.log("restarting listening");
            stopListening();
            startListening();
        }
    }



    private static RecognitionListener createRecognitionListener(final RecognizerListener recognizerListener, final Logger logger) {
        return new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                logger.logCall();
                recognizerListener.onInit();
            }

            @Override
            public void onBeginningOfSpeech() {
                logger.logCall();
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                logger.logCall();
                recognizerListener.onRms(rmsdB);
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
                recognizerListener.onError(error, getErrorText(error));
            }

            @Override
            public void onResults(Bundle results) {
                logger.logCall();
                ArrayList<String> resultsStringArrayList = results.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION);
                recognizerListener.onResults(resultsStringArrayList);
                if (resultsStringArrayList.size()>0) {
                    recognizerListener.onResult(resultsStringArrayList.get(0));
                } else {
                    recognizerListener.onResult(null);
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

    private static Intent createIntent(String packageName, int maxResults, String language) {
        Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, language);
//        recognizerIntent.putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", new String[]{language, locale});
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
    public static interface RecognizerListener {
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

        public void log(String message) {
            if(on) {
                Log.d(TAG, message);
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

    public void chooseFromAvailableLanguages(LanguageChooser languageChooser) {
        Intent detailsIntent = new Intent(RecognizerIntent.ACTION_GET_LANGUAGE_DETAILS);
        LanguageChooser.LanguageChoiceListener languageChoiceListener = new LanguageChooser.LanguageChoiceListener() {
            @Override
            public void onLanguageChoice(String language) {
                locale = createLocale(language);
                setLocale(locale);
            }
        };
        context.sendOrderedBroadcast(detailsIntent, null, new LanguageDetailsChecker(languageChooser, languageChoiceListener), null, Activity.RESULT_OK, null, null);
    }

    public interface LanguageChooser {
        public abstract void chooseLanguage(Context context, final ArrayList<String> supportedLanguages, final LanguageChoiceListener languageChoiceListener);

        public interface LanguageChoiceListener {
            public void onLanguageChoice(String language);
        }
    }

    private class LanguageDetailsChecker extends BroadcastReceiver {
        String languagePreference;
        ArrayList<String> languages;
        LanguageChooser languageChooser;
        LanguageChooser.LanguageChoiceListener languageChoiceListener;

        private LanguageDetailsChecker(LanguageChooser languageChooser, LanguageChooser.LanguageChoiceListener languageChoiceListener) {
            this.languageChooser = languageChooser;
            this.languageChoiceListener = languageChoiceListener;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle results = getResultExtras(true);

            if (results.containsKey(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE)) {
                languagePreference = results.getString(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE);
                logger.log("LanguageDetailsChecker.onReceive: languagePreference="+languagePreference);
            }

            if (results.containsKey(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES)) {
                languages = results.getStringArrayList(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES);
                logger.log("LanguageDetailsChecker.onReceive: languages="+languages.toString());
            }

            languageChooser.chooseLanguage(context, languages, languageChoiceListener);
        }
    }

    public static Locale createLocale(String localeString) {
        String[] parts = localeString.split("-");
        Locale locale = (parts.length > 1
                ? new Locale(parts[0], parts[1])
                : new Locale(parts[0]));
        return locale;
    }
}
