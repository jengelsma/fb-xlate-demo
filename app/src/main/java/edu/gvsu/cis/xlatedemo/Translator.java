package edu.gvsu.cis.xlatedemo;

import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import org.w3c.dom.Text;

// abstract interface for defining how to handle completed translation.
interface TranslationCompletedListener {
    void onSuccess(String translatedText);
    void onFailure();
}

// Translator singleton class.  Provides a high-level class interface to Firebase Translation
public class Translator {

    private static Translator singleton = null;
    private static boolean modelsLoaded = false;
    private FirebaseTranslator fbTranslator = null;

    public static Translator getInstance() {
        if(singleton == null) {
            singleton = new Translator();
        }
        return singleton;
    }

    Translator() {
        modelsLoaded = false;
    }

    public void setLanguages(int srcLanguage, int targetLanguage) {
        // Load the language models based on the parameters passed.
        FirebaseTranslatorOptions options =
                new FirebaseTranslatorOptions.Builder()
                        .setSourceLanguage(srcLanguage)
                        .setTargetLanguage(targetLanguage)
                        .build();
        fbTranslator = FirebaseNaturalLanguage.getInstance().getTranslator(options);


        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .requireWifi()
                .build();
        fbTranslator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void v) {
                                // Model downloaded successfully. Okay to start translating.
                                modelsLoaded = true;
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Model couldn’t be downloaded or other internal error.
                                // TODO: probably should do something more realistic here.
                                System.out.println("ERROR LOADING MODEL");
                                modelsLoaded = false;

                            }
                        });
    }

    /**
     * Translate the given text and use the listener to handle the results when they eventually come in.
     * @param text text to be translated
     * @param listener the code we will call on success or failure.
     * @return returns false if models are not loaded, true otherwise.
     */
    public boolean translate(String text, final TranslationCompletedListener listener) {
        if(modelsLoaded) {
            fbTranslator.translate(text)
                    .addOnSuccessListener(
                            new OnSuccessListener<String>() {
                                @Override
                                public void onSuccess(@NonNull final String translatedText) {
                                    // Translation successful.  Call the listener's success method.
                                    listener.onSuccess(translatedText);

                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Translation was not successful.  Call the listener's failure method.
                                    listener.onFailure();
                                }
                            });
            return true;  // translation successfully submitted, results will follow on above listener.
        } else {
            return false; // oops, languages haven't downloaded yet.
        }
    }

}

