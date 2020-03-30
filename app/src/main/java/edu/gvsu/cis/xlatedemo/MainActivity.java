package edu.gvsu.cis.xlatedemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

public class MainActivity extends AppCompatActivity {

    Button b,d;
    TextView t;
    boolean modelsDownloaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        d = (Button) findViewById(R.id.downloadButton);
        t = (TextView) findViewById(R.id.textToXlate);
        b = (Button) findViewById(R.id.xlateButton);

        // Create an English-Dutch translator:
        FirebaseTranslatorOptions options =
                new FirebaseTranslatorOptions.Builder()
                        .setSourceLanguage(FirebaseTranslateLanguage.EN)
                        .setTargetLanguage(FirebaseTranslateLanguage.NL)
                        .build();
        final FirebaseTranslator englishDutchTranslator =
                FirebaseNaturalLanguage.getInstance().getTranslator(options);

        // This listener will download the translator model
        d.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                        .requireWifi()
                        .build();
                englishDutchTranslator.downloadModelIfNeeded(conditions)
                        .addOnSuccessListener(
                                new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void v) {
                                        // Model downloaded successfully. Okay to start translating.
                                        modelsDownloaded = true;
                                        CharSequence text = "Model is downloaded.  You can translate!";
                                        int duration = Toast.LENGTH_SHORT;

                                        Toast toast = Toast.makeText(getApplicationContext(), text, duration);
                                        toast.show();
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Model couldn’t be downloaded or other internal error.
                                        CharSequence text = "Error downloading model.";
                                        int duration = Toast.LENGTH_SHORT;

                                        Toast toast = Toast.makeText(getApplicationContext(), text, duration);
                                        toast.show();
                                    }
                                });
            }
        });


        // This listener will kick off the actual translation.
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!modelsDownloaded) {
                    CharSequence text = "You must first download the translation model by clicking the button below.";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(getApplicationContext(), text, duration);
                    toast.show();
                    return;
                }

                // if we get here, we are all ready to start the translation!
                String text = t.getText().toString();
                englishDutchTranslator.translate(text)
                        .addOnSuccessListener(
                                new OnSuccessListener<String>() {
                                    @Override
                                    public void onSuccess(@NonNull final String translatedText) {
                                        // Translation successful.
                                        // Callback may not be on main UI thread, so we switch the update back to the UI thread
                                        t.post(new Runnable() {
                                            public void run() {
                                                t.setText(translatedText);
                                            }
                                        });

                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Error.
                                        CharSequence text = "Arg, for some sad reason the translation failed.";
                                        int duration = Toast.LENGTH_SHORT;

                                        Toast toast = Toast.makeText(getApplicationContext(), text, duration);
                                        toast.show();
                                    }
                                });
            }
        });

    }
}
