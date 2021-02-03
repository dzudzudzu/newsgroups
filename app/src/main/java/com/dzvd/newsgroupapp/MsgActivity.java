package com.dzvd.newsgroupapp;

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

public class MsgActivity extends AppCompatActivity {

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
        txtViewTitle = (TextView) findViewById(R.id.txtViewTitle);
        txtViewDetails = (TextView) findViewById(R.id.txtViewDetails);
        txtViewMsg = (TextView) findViewById(R.id.txtViewMsg);
        txtViewMsg.setMovementMethod(ScrollingMovementMethod.getInstance());
        url = getIntent().getStringExtra("url");
        prevButton = findViewById(R.id.buttonPrev);
        nextButton = findViewById(R.id.buttonNext);

        //setting onclick event listeners for buttons, to go to prev and next message
        prevButton.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View view) {
                                              if (previous != null) {
                                                  Intent intent = new Intent(view.getContext(), MsgActivity.class);
                                                  intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                                                  intent.putExtra("url", url + previous);
                                                  startActivity(intent);
                                                  finish();
                                              } else warn();
                                          }
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
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
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
                url = url.replace("msg", "");
                if (url.contains("0"))
                    url = url.substring(0, url.indexOf("0"));
                //setting title of message
                title = document.select("h1").text().replace("Mail Index", "");
                details = "";
                //filling up the details, skipping not so important information, because its irrelevant + takes time to parse and format into design
                Elements tablerows = document.select("tr");
                for (int i = 0; i < tablerows.size(); i++) {
                    if (tablerows.select("tr").eq(i).select("td").text().contains("Newsgroup"))
                        continue;
                    if (tablerows.select("tr").eq(i).select("td").text().contains("Xref")) continue;
                    if (tablerows.select("tr").eq(i).select("td").text().contains("Followup"))
                        continue;
                    if (tablerows.select("tr").eq(i).select("td").text().contains("Reply"))
                        continue;
                    if (tablerows.select("tr").eq(i).select("td").text().contains("User-agent"))
                        continue;
                    if (tablerows.select("tr").eq(i).select("td").text().contains("Keywords"))
                        continue;
                    if (tablerows.select("tr").eq(i).select("td").text().contains("Summary"))
                        continue;
                    if (tablerows.select("tr").eq(i).select("td").text().contains("References"))
                        continue;
                    if (tablerows.select("tr").eq(i).select("td").text().contains("Distribution"))
                        continue;
                    details += tablerows.select("tr").eq(i).select("td").text();
                    details += "\n";
                }
                //formatting for nicer look
                details = details.replace("Date", "Date:");
                details = details.replace("From", "From:");
                details = details.replace("Subject", "Subject:");
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