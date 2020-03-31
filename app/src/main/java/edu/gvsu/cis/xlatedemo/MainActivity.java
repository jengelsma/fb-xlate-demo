package edu.gvsu.cis.xlatedemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;

public class MainActivity extends AppCompatActivity {

    Button b;
    TextView t1, t2;
    Translator translator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        t1 = (TextView) findViewById(R.id.textToXlate);
        t2 = (TextView) findViewById(R.id.textToXlate2);
        b = (Button) findViewById(R.id.xlateButton);

        // Grab ourselves a ref to the translator singleton object and setup its source/target languages.
        translator = Translator.getInstance();
        translator.setLanguages(FirebaseTranslateLanguage.EN, FirebaseTranslateLanguage.NL);


        // This listener will kick off the actual translation when the button is pressed. .
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // if we get here, we are all ready to start the translation!
                String text1 = t1.getText().toString();
                String text2 = t2.getText().toString();
                // let's concatenate them together so we can translate both in a single call!  Here we
                // don't expect a @ in the text so we use it as a delimiter.
                final String stringToBeTranslated = text1 + "@" + text2;

                // NOTE: the listener anonymous inner class is what we want to happen later once the
                // translation actually completed.  e.g. in this case bind the translated text to our
                // TextViews t1, t2, currently on the screen. 
                boolean retval = translator.translate(stringToBeTranslated, new TranslationCompletedListener() {
                    @Override
                    public void onSuccess(final String translatedText) {
                        // Note: This code will be called later by the translator, once the text is translated.

                        // Break the translated string into our sub-fields using the @ as delimiter.
                        final String[] tokens = translatedText.split("@");
                        if(tokens.length == 2) {

                            // Callback may not be on main UI thread, so we switch the update back to the UI thread
                            // in order to bind the translated value back to the view.
                            t1.post(new Runnable() {
                                public void run() {
                                    t1.setText(tokens[0]);
                                    t2.setText(tokens[1]);
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure() {
                        CharSequence msg = "Models downloaded, but the actual translation failed.";
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(getApplicationContext(), msg, duration);
                        toast.show();
                        return;
                    }
                });

                if(!retval) {
                    CharSequence msg = "Your translation models aren't downloaded yet.  Try again later!";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(getApplicationContext(), msg, duration);
                    toast.show();
                    return;
                }

            }
        });

    }
}
