package com.example.networktest;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.view.Window;
import android.content.pm.ActivityInfo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Remove title banner
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Lock screen in portrait mode
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    private class BackgroundTask extends AsyncTask<URL, Integer, String> {
        protected String doInBackground(URL... urls) {
            ConnectivityManager connMgr = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                try {
                    HttpURLConnection urlConnection = (HttpURLConnection) urls[0].openConnection();

                    urlConnection.setDoOutput(true);
                    urlConnection.setChunkedStreamingMode(0);

                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());

                    byte[] sendStr = new byte[2];
                    sendStr[0] = '4';
                    sendStr[1] = 0;
                    out.write(sendStr);
                    byte[] buf = new byte[64];
                    in.read(buf);
                    Log.w("NetworkTest", String.format("%s", buf));
                    return String.format("%s", buf);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Log.w("NetworkTest", "Network unavailable");
            }

            return "Fail";
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected  void onPreExecute() {

        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.w("NetworkTest", String.format("%s", result));
        }
    }

    // Callback for Send button
    public void onClickSend(View view) {
        Log.w("NetworkText", "Send");

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            try {
                URL url = new URL("http://192.168.0.31:12345");

                new BackgroundTask().execute(url);
//                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
//
//                urlConnection.setDoOutput(true);
//                urlConnection.setChunkedStreamingMode(0);

//                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
//                OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
//
//                byte[] sendStr = new byte[2];
//                sendStr[0] = '4';
//                sendStr[1] = 0;
//                //out.write(sendStr);
//                byte[] buf = new byte[64];
//                //in.read(buf);
//                //Log.w("NetworkTest", String.format("%s", buf));
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.w("NetworkTest", "Network unavailable");
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

}
