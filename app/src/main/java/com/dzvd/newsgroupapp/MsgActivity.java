package com.dzvd.newsgroupapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.R)
public class MsgActivity extends AppCompatActivity {
    static private List forbiddenWords = List.of("Newsgroup", "Xref", "Followup", "Reply", "User-agent", "Keywords", "Summary", "References", "Distribution", "Organization");
    private String url;
    TextView txtViewTitle, txtViewDetails, txtViewMsg;
    String title, details, msg;
    String previous, next = "";
    Button prevButton, nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msg);
        //appointing views and buttons to the ones created in xml
        txtViewTitle = findViewById(R.id.txtViewTitle);
        txtViewDetails = findViewById(R.id.txtViewDetails);
        txtViewMsg = findViewById(R.id.txtViewMsg);
        txtViewMsg.setMovementMethod(ScrollingMovementMethod.getInstance());
        url = getIntent().getStringExtra("url");
        prevButton = findViewById(R.id.buttonPrev);
        nextButton = findViewById(R.id.buttonNext);

        //setting onclick event listeners for buttons, to go to prev and next message
        prevButton.setOnClickListener(view -> {
                    if (previous != null) {
                        Intent intent = new Intent(view.getContext(), MsgActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                        intent.putExtra("url", url + previous);
                        startActivity(intent);
                        finish();
                    } else warn();
                }
        );
        nextButton.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View view) {
                                              if (next != null) {
                                                  Intent intent = new Intent(view.getContext(), MsgActivity.class);
                                                  intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                                                  intent.putExtra("url", url + next);
                                                  startActivity(intent);
                                                  finish();
                                              } else warn();
                                          }
                                      }
        );
        Content content = new Content();
        content.execute();
    }

    //warn if there is no next or previous message
    public void warn() {
        AlertDialog alertDialog = new AlertDialog.Builder(MsgActivity.this).create();
        alertDialog.setTitle("Warning");
        alertDialog.setMessage("Trying to retrieve something that doesn't exist");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }

    private class Content extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //setting text
            txtViewTitle.setText(title);
            txtViewDetails.setText(details);
            txtViewMsg.setText(msg);

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        public String formatDetails(String details) {
            details = details.replace("Date", "Date:");
            details = details.replace("From", "From:");
            details = details.replace("Subject", "Subject:");
            return details;
        }

        public String formatUrl(String url) {
            url = url.replace("msg", "");
            if (url.contains("0"))
                url = url.substring(0, url.indexOf("0"));
            return url;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Document document;
                //parsing url for use on further messages
                if (!url.contains(".html")) {
                    document = Jsoup.connect(url + "msg00000.html").get();
                } else {
                    document = Jsoup.connect(url).get();
                }
                url = formatUrl(url);
                //setting title of message
                title = document.select("h1").text().replace("Mail Index", "");
                details = "";
                //filling up the details, skipping not so important information, because its irrelevant + takes time to parse and format into design
                Elements tablerows = document.select("tr");
                for (int i = 0; i < tablerows.size(); i++) {
                    int finalI = i;
                    if (forbiddenWords.stream().anyMatch(word -> tablerows.select("tr").eq(finalI).select("td").text().contains((String) word)))
                        continue;
                    details += tablerows.select("tr").eq(i).select("td").text();
                    details += "\n";
                }
                //formatting for nicer look
                details = formatDetails(details);
                //selecting msg consisting of original text and quotes
                msg = document.select("pre,blockquote").text();
                //preparing next and previous message links to go to
                Elements links = document.select("a");
                for (int i = 0; i < links.size(); i++) {
                    if (links.select("a").eq(i).text().contains("Date Prev"))
                        previous = links.select("a").eq(i).attr("href");
                    if (links.select("a").eq(i).text().contains("Date Next"))
                        next = links.select("a").eq(i).attr("href");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}