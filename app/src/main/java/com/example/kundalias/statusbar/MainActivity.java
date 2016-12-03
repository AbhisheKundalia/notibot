package com.example.kundalias.statusbar;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    /**
     * URL for fetching JSON from API.AI
     */
    private static final String API_REQUEST_URL =
            "https://api.api.ai/v1/query";

    //Member variables for the UI XML ids
    private TableLayout tab;
    //BroadcastReciever to perform action when the broadcasted intent from NotificationService is Recieved
    private BroadcastReceiver onNotice = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            //Extract data from the broadcasted intent
            String pack = intent.getStringExtra("package");
            String title = intent.getStringExtra("title");
            String text = intent.getStringExtra("text");
            //String bigtext = intent.getStringExtra("bigtext");

            //Create URL with appending query values
            Uri baseUri = Uri.parse(API_REQUEST_URL);
            Uri.Builder uriBuilder = baseUri.buildUpon();

            uriBuilder.appendQueryParameter("v", "20150910");
            uriBuilder.appendQueryParameter("query", text);
            uriBuilder.appendQueryParameter("lang", "en");
            uriBuilder.appendQueryParameter("sessionId", "1234567990");


            // Create an {@link AsyncTask} to perform the HTTP request to the given URL
            // on a background thread. When the result is received on the main UI thread,
            // then update the UI.
            NotificationAsyncTask task = new NotificationAsyncTask();
            task.execute(uriBuilder.toString());

            //Display the extracted field values on UI using Table layout defined in XML
            TableRow tr = new TableRow(getApplicationContext());
            tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            TextView textview = new TextView(getApplicationContext());
            textview.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
            textview.setTextSize(20);
            textview.setTextColor(Color.parseColor("#0B0719"));
            textview.setText(pack + "->" + title + " :->" + text);
            tr.addView(textview);
            tab.addView(tr);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // To map xml nodes in Java
        tab = (TableLayout) findViewById(R.id.tab);

        //Checks if Notification listener service access is granted
        startNLService();

        //Register a Broadcast reciever to perform "onNotice" when recieves intent "msg"
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, new IntentFilter("msg"));


    }


    // This method will Check for the Notification listener service access else asks user to grant permission
    private void startNLService() {
        //Check if application has access to Notification listener service
        if (isNLServiceRunning()) {
            Toast.makeText(getApplicationContext(), "Notification listener Service access is granted", Toast.LENGTH_SHORT).show();
            //btn.setVisibility(View.GONE);
        } else {
            Toast.makeText(getApplicationContext(), "Please grant access to Application to access notification", Toast.LENGTH_SHORT).show();
            //On coming back from the intent will call OnActivityResult method
            startActivityForResult(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"), 2);
        }
    }

    // Will now popup user with dialog to quit app or grant NLS access
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2) {
            if (resultCode != Activity.RESULT_OK) {
                if (!isNLServiceRunning()) {
                    new AlertDialog.Builder(this)
                            .setMessage("You have not granted required access to application! So app will quit now")
                            .setCancelable(false)
                            .setPositiveButton("Quit", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    MainActivity.this.finish();
                                }
                            })
                            .setNegativeButton("Give Permission", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    startNLService();
                                }
                            })
                            .show();
                }
            }

        }
    }

    //Checks if the NLService is running for the app
    private boolean isNLServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (NotificationService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Update the UI with the given Response information.
     */
    private void updateUi(String response) {
        TableRow tr = new TableRow(getApplicationContext());
        tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        TextView textview = new TextView(getApplicationContext());
        textview.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
        textview.setTextSize(20);
        textview.setTextColor(Color.parseColor("#0B0719"));
        textview.setText(getString(R.string.autoresponse) + "->" + response);
        tr.addView(textview);
        tab.addView(tr);
    }

    private class NotificationAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            // Don't perform the request if there are no URLs, or the first URL is null.
            if (params.length < 1 || params[0] == null) {
                return null;
            }
            return Utils.fetchReplyData(params[0]);
        }

        @Override
        protected void onPostExecute(String response) {
            if (response == null) {
                return;
            }
            updateUi(response);
        }
    }

}