package mars.c.speechrecognitionpureexample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Constantine Mars on 1/26/15.
 */
public class DialogLanguageChooser implements SpeechRecognizer.LanguageChooser {

    @Override
    public void chooseLanguage(Context context, final ArrayList<String> supportedLanguages, final LanguageChoiceListener languageChoiceListener) {
        String[] languages = new String[supportedLanguages.size()];

//        transform locale descriptions to languages
        for(int i=0; i<supportedLanguages.size(); i++) {
            Locale locale = SpeechRecognizer.createLocale(supportedLanguages.get(i));
            languages[i] = locale.getDisplayLanguage() + " (" + locale.getDisplayCountry() + ")";
        }

        AlertDialog.Builder b = new AlertDialog.Builder(context);
            b.setTitle("Choose your language");
            b.setItems(languages, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    String selected = supportedLanguages.get(which);
                    languageChoiceListener.onLanguageChoice(selected);
                }

            });
            b.show();
    }
}
