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
import android.widget.EditText;
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

    MainFragment m_mainFragment;
    private static final int errorTemp = -1111;
    private static final String errorTempString = new String().valueOf(errorTemp);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Remove title banner
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Lock screen in portrait mode
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_mainFragment = new MainFragment();
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, m_mainFragment)
                    .commit();
        }
    }

    public void log(String str) {
        Log.w("NetworkTest", str);
    }

    // Callback for Send button
    public void onClickSend(View view) {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            BackgroundTaskParams btp = new BackgroundTaskParams(m_mainFragment.getIpAddress(), 12345);
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
                return weather;
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            return "fail";
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPreExecute() {

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            log(result);
            m_mainFragment.setInTemp(parseForInTemp(result));
            m_mainFragment.setOutTemp(parseForOutTemp(result));
        }
    }

    int parseForInTemp(String tempStr) {
        int temp = errorTemp;
        try {
            if (false == tempStr.equals("fail")) {
                // TODO: Remove junk from end of string and remove '- 1' from subString()
                temp = (int) (Float.parseFloat(tempStr.substring(tempStr.indexOf("IT=") + 3,
                        tempStr.length() - 1)) * 10.0);
            }
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
        }
        return temp;
    }

    int parseForOutTemp(String tempStr) {
        int temp = errorTemp;
        try {
            if (false == tempStr.equals("fail")) {
                temp = (int)(Float.parseFloat(tempStr.substring(tempStr.indexOf("OT=") + 3,
                        tempStr.indexOf("IT="))) * 10);
            }
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
        }
        return temp;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class MainFragment extends Fragment {

        public MainFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            return rootView;
        }

        public String getIpAddress() {
            EditText et_ip = (EditText)getView().findViewById(R.id.et_ipAddress);
            if (null != et_ip)
                return et_ip.getText().toString();

            return "";
        }

        public void setInTemp(float tmp) {
            TextView inTemp = (TextView)getView().findViewById(R.id.tv_inTemp);
            boolean enable = false;
            String tmpStr = new String().valueOf((float)tmp / 10.0);
            if (null != inTemp) {
                if (tmpStr.equals("fail")) {
                    inTemp.setText("0");
                }
                else {
                    inTemp.setText(tmpStr);
                    enable = true;
                }
            }
            else {
                inTemp.setText("N/A");
            }
            inTemp.setEnabled(enable);
        }

        public void setOutTemp(float tmp) {
            TextView outTemp = (TextView)getView().findViewById(R.id.tv_outTemp);
            boolean enable = false;
            String tmpStr = new String().valueOf((float)tmp / 10.0);
            if (null != outTemp) {
                if (tmpStr.equals("fail")) {
                    outTemp.setText("0");
                }
                else {
                    outTemp.setText(tmpStr);
                    enable = true;
                }
            }
            else {
                outTemp.setText("N/A");
            }
            outTemp.setEnabled(enable);
        }
    }

}
