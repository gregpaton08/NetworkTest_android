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
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

public class MainActivity extends Activity {

    TextView tv;

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

        tv = new TextView(getApplicationContext());
        tv.setText("Hello");
        ((FrameLayout) findViewById(R.id.container)).addView(tv);
    }

    // Callback for Send button
    public void onClickSend(View view) {
        Log.w("NetworkText", "Send");
        if (null != tv) {
            tv.setText("Send");
        }
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            BackgroundTaskParams btp = new BackgroundTaskParams("192.168.0.31", 12345);
            new BackgroundTask().execute(btp);
        } else {
            Log.w("NetworkTest", "Network unavailable");
        }
    }

    private  class BackgroundTaskParams {
        String m_ipAddress;
        int m_port;

        public BackgroundTaskParams() {
            m_ipAddress = "";
            m_port = 0;
        }

        public BackgroundTaskParams(String ipAddress, int port) {
            m_ipAddress = ipAddress;
            m_port = port;
        }
    }

    private class BackgroundTask extends AsyncTask<BackgroundTaskParams, Integer, String> {
        protected String doInBackground(BackgroundTaskParams... btps) {
            try {
                // Open socket to server
                Socket client = new Socket(btps[0].m_ipAddress, btps[0].m_port);

                // Get input and output streams
                InputStream in = client.getInputStream();
                OutputStream out = client.getOutputStream();

                byte[] sendStr = new byte[2];
                sendStr[0] = '4';
                sendStr[1] = 0;
                out.write(sendStr);
                byte[] buf = new byte[64];
                buf[0] = 0;
                int count = in.read(buf);
                buf[count] = '\0';
                Charset charset = Charset.forName("UTF-8");
                String weather = new String(buf, 0, count, charset);
                Log.w("NetworkTest", weather);
                return weather;
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

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (null != tv) {
                tv.setText(result);
            }
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
