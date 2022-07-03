package com.example.languagetranslator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Spinner fromSpinner, toSpinner;
    //private TextInputEditText inputText;
    private EditText inputText;
    private ImageView micBtn, sourcespeaker, destspeaker;
    private MaterialButton translateBtn;
    private TextView outputText;
    private TextToSpeech textToSpeech;

    String[] fromLanguage = {"From", "English", "Afrikaans", "Arabic", "Byelorussian", "Bulgarian", "Bengali", "Catalan", "Czech", "Welsh", "Hindi", "Urdu"};
    String[] toLanguage = {"To", "English", "Afrikaans", "Arabic", "Byelorussian", "Bulgarian", "Bengali", "Catalan", "Czech", "Welsh", "Hindi", "Urdu"};

    private static final int REQUEST_PERMISSION_CODE = 1;
    int languagecode, fromlanguagecode, tolanguagecode=0;
    String speechSourceLanguage, speechDestLanguage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fromSpinner = findViewById(R.id.fromSpinner);
        toSpinner  = findViewById(R.id.toSpinner);
        inputText = findViewById(R.id.idEdtSource);
        outputText = findViewById(R.id.translated_text);
        micBtn = findViewById(R.id.mic);
        translateBtn = findViewById(R.id.translate);

        sourcespeaker = findViewById(R.id.sourceSpeaker);
        destspeaker = findViewById(R.id.destSpeaker);

        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                speechSourceLanguage = fromLanguage[i];
                fromlanguagecode = getLanguagecode(fromLanguage[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter fromAdapter = new ArrayAdapter(this, R.layout.spinner_item, fromLanguage);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fromAdapter);

        toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                speechDestLanguage = toLanguage[i];
                tolanguagecode = getLanguagecode(toLanguage[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter toAdapter = new ArrayAdapter(this, R.layout.spinner_item, toLanguage);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toSpinner.setAdapter(toAdapter);


        translateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                outputText.setText("");

                if(inputText.getText().toString().isEmpty())
                {
                    Toast.makeText(MainActivity.this, "Please enter text or press mic to speak", Toast.LENGTH_LONG).show();
                }
                else if(fromlanguagecode == 0)
                {
                    Toast.makeText(MainActivity.this, "Please select any one of the from language", Toast.LENGTH_LONG).show();
                }
                else if(tolanguagecode == 0)
                {
                    Toast.makeText(MainActivity.this, "Please select any one of the to language", Toast.LENGTH_LONG).show();
                }
                else
                {
                    translateText(fromlanguagecode, tolanguagecode, inputText.getText().toString());
                }
            }
        });

        micBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent micIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                micIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                micIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                micIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to convert to text");

                try {
                    startActivityForResult(micIntent, REQUEST_PERMISSION_CODE);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });


        //input speaker
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {

                // if No error is found then only it will run
                if(i!=TextToSpeech.ERROR){
                    // To Choose language of speech
                    //String lan = getLanguageIso(speechSourceLanguage);
                    textToSpeech.setLanguage(Locale.forLanguageTag(getLanguageIso(fromLanguage[i])));
                }
            }
        });

        sourcespeaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textToSpeech.speak(inputText.getText().toString(),TextToSpeech.QUEUE_FLUSH,null);
            }
        });


        //output speaker
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {

                // if No error is found then only it will run
                if(i!=TextToSpeech.ERROR){
                    // To Choose language of speech
                    //String lan = getLanguageIso(speechSourceLanguage);
                    textToSpeech.setLanguage(Locale.forLanguageTag(getLanguageIso(toLanguage[i])));
                }
            }
        });

        destspeaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textToSpeech.speak(outputText.getText().toString(),TextToSpeech.QUEUE_FLUSH,null);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_PERMISSION_CODE)
        {
            if(resultCode == RESULT_OK && data != null)
            {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                inputText.setText(result.get(0));
            }
        }
    }



    public void translateText(int fromLancode, int toLancode, String enteredText)
    {
        outputText.setText("Translating......");

        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(fromLancode)
                .setTargetLanguage(toLancode)
                .build();

        FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);

        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();

        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                outputText.setText("Almost there......");
                translator.translate(enteredText).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        outputText.setText(s);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "failed to translate", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "failed to convert", Toast.LENGTH_LONG).show();
            }
        });



    }




    public int getLanguagecode(String language)
    {
        int languageCode = 0;

        switch(language)
        {
            case "English" :
                languageCode = FirebaseTranslateLanguage.EN;
                break;
            case "Afrikaans" :
                languageCode = FirebaseTranslateLanguage.AF;
                break;
            case "Arabic" :
                languageCode = FirebaseTranslateLanguage.AR;
                break;
            case "Byelorussian" :
                languageCode = FirebaseTranslateLanguage.BE;
                break;
            case "Bulgarian" :
                languageCode = FirebaseTranslateLanguage.BG;
                break;
            case "Bengali" :
                languageCode = FirebaseTranslateLanguage.BN;
                break;
            case "Catalan" :
                languageCode = FirebaseTranslateLanguage.CA;
                break;
            case "Czech" :
                languageCode = FirebaseTranslateLanguage.CS;
                break;
            case "Welsh" :
                languageCode = FirebaseTranslateLanguage.CY;
                break;
            case "Hindi" :
                languageCode = FirebaseTranslateLanguage.HI;
                break;
            case "Urdu" :
                languageCode = FirebaseTranslateLanguage.UR;
                break;
            default : languageCode = 0;

        }


        return languageCode;
    }

    public String getLanguageIso(String language)
    {

        String languageCode;

        switch(language)
        {
            case "English" :
                return "EN";

            case "Afrikaans" :
                return "AF";

            case "Arabic" :
                return "ara";

            case "Belarusians" :
                return "BE";

            case "Bulgarian" :
                return "BG";

            case "Bengali" :
                return "BN";

            case "Catalan" :
                return "CA";

            case "Czech" :
                return "CS";

            case "Welsh" :
                return "wel";

            case "Hindi" :
                return "HI";

            case "Urdu" :
                return "UR";

            default : languageCode = "";

        }


        return languageCode;
    }
}