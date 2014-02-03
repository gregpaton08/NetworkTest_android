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
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;

public class MainActivity extends Activity {

    PlaceholderFragment frag;
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Remove title banner
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Lock screen in portrait mode
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        frag = new PlaceholderFragment();
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, frag)
                    .commit();
        }

        TextView tv = new TextView(getApplicationContext());
        tv.setText("Hello");
        tv.setId(274);
        ((FrameLayout) findViewById(R.id.container)).addView(tv);
    }

    private class BackgroundTask extends AsyncTask<URL, Integer, String> {
        protected String doInBackground(URL... urls) {
            Log.w("NetworkTest", "Thread called");
            //return "Hi";
            try {
                Log.w("NetworkTest", "Try");
//                HttpURLConnection urlConnection = (HttpURLConnection) urls[0].openConnection();
//
//
//                Log.w("NetworkTest", "URL Conn");
//                urlConnection.setDoOutput(true);
//                urlConnection.setChunkedStreamingMode(0);

                Socket client = new Socket("192.168.0.31", 12345);
                PrintWriter printwriter = new PrintWriter(client.getOutputStream(), true);

                Log.w("NetworkTest", "Input Stream");
                InputStream in = client.getInputStream();
                //InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                Log.w("NetworkTest", "Output Stream");
                OutputStream out = client.getOutputStream();
                //OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                Log.w("NetworkTest", "PostStreams");

                byte[] sendStr = new byte[2];
                sendStr[0] = '4';
                sendStr[1] = 0;
                Log.w("NetworkTest", "PreWrite");
                out.write(sendStr);
                Log.w("NetworkTest", "PostWrite");
                byte[] buf = new byte[64];
                buf[0] = 0;
                Log.w("NetworkTest", "PreRead");
                in.read(buf);
                Log.w("NetworkTest", new String(buf));
                return new String(buf);
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            return "Fail";
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPreExecute() {

        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.w("NetworkTest", String.format("%s", result));
            //tv.setText(result);
            if (null == tv) {
                tv = (TextView)findViewById(274);
                if (null != tv) {
                    tv.setText("Thread");
                    ;;tv.setText(result);
                }
            }
        }
    }

    // Callback for Send button
    public void onClickSend(View view) {
        Log.w("NetworkText", "Send");
        if (null == tv) {
            tv = (TextView)findViewById(274);
            if (null != tv)
                tv.setText("Send");
        }
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

        public void changeText(String str) {
            TextView mTextView;
            //mTextView = (TextView)getView().findViewById(R.id.tv_temp);
            //mTextView.setText(str);
        }
    }

}
