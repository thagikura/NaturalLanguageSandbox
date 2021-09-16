package com.example.naturallanguagesandbox;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.language.v1beta2.CloudNaturalLanguage;
import com.google.api.services.language.v1beta2.CloudNaturalLanguageRequestInitializer;
import com.google.api.services.language.v1beta2.model.AnnotateTextRequest;
import com.google.api.services.language.v1beta2.model.AnnotateTextResponse;
import com.google.api.services.language.v1beta2.model.Document;
import com.google.api.services.language.v1beta2.model.Features;
import com.google.api.services.language.v1beta2.model.Sentiment;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private CloudNaturalLanguage naturalLanguageService;

    // You should never do this for the production code.
    private static String API_KEY = "your api key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        naturalLanguageService = new CloudNaturalLanguage.Builder(
                new NetHttpTransport(),
                new GsonFactory(),
                null
        ).setCloudNaturalLanguageRequestInitializer(
                new CloudNaturalLanguageRequestInitializer(API_KEY)
        ).build();

        Button button = findViewById(R.id.button_run);
        button.setOnClickListener(view -> {
            EditText editText = findViewById(R.id.edittext);
            if (TextUtils.isEmpty(editText.getText())) {
                return;
            }

            Document document = new Document();
            document.setType("PLAIN_TEXT");
            document.setLanguage("en-US");
            document.setContent(editText.getText().toString());

            Features features = new Features();
            features.setExtractEntities(true);
            features.setExtractSyntax(true);
            features.setExtractDocumentSentiment(true);

            final AnnotateTextRequest request = new AnnotateTextRequest();
            request.setDocument(document);
            request.setFeatures(features);

            // Don't use it for production this is only to try the API
            new AsyncTask<Object, Void, AnnotateTextResponse>() {
                @Override
                protected AnnotateTextResponse doInBackground(Object... params) {
                    AnnotateTextResponse response = null;
                    try {
                        response = naturalLanguageService.documents().annotateText(request).execute();

                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                    }
                    return response;
                }

                @Override
                protected void onPostExecute(AnnotateTextResponse response) {
                    super.onPostExecute(response);
                    if (response != null) {
                        Sentiment sent = response.getDocumentSentiment();
                        Log.i(TAG, "Score : " + sent.getScore() + " Magnitude : " + sent.getMagnitude());

                        TextView sentiment = findViewById(R.id.textview_sentiment);
                        TextView magnitude = findViewById(R.id.textview_magnitude);
                        sentiment.setText(sent.getScore().toString());
                        magnitude.setText(sent.getMagnitude().toString());
                    }
                }
            }.execute();
        });
    }
}