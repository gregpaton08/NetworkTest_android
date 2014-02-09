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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.view.Window;
import android.content.pm.ActivityInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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

//        EditText et_ip = m_mainFragment.getEditTextIpAddress();
//        if (null != et_ip) {
//            et_ip.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//                @Override
//                public boolean onEditorAction(TextView textView, int keyCode, KeyEvent keyEvent) {
//                    if ((KeyEvent.ACTION_DOWN == keyEvent.getAction()) &&
//                        (KeyEvent.KEYCODE_ENTER == keyCode)) {
//                        InputMethodManager imm;
//                    }
//                    return false;
//                }
//            });
//        }
    }

    private void log(String str) {
        Log.w("NetworkTest", str);
    }

    // Callback for Send button
    public void onClickSend(View view) {
        updateWeather();
    }

    private boolean updateWeather() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            GetWeatherTaskParams gwtp = new GetWeatherTaskParams(m_mainFragment.getIpAddress(), 12345);
            new GetWeatherTask().execute(gwtp);
        } else {
            Log.w("NetworkTest", "Network unavailable");
            return false;
        }

        return true;
    }

    private class GetWeatherTaskParams {
        String m_ipAddress;
        int m_port;

        public GetWeatherTaskParams() {
            m_ipAddress = "";
            m_port = 0;
        }

        public GetWeatherTaskParams(String ipAddress, int port) {
            m_ipAddress = ipAddress;
            m_port = port;
        }
    }

    private class GetWeatherTask extends AsyncTask<GetWeatherTaskParams, Integer, String> {
        protected String doInBackground(GetWeatherTaskParams... gwtps) {
            try {
                // Open socket to server
                Socket client = new Socket(gwtps[0].m_ipAddress, gwtps[0].m_port);
                if (null == client)
                    return "fail";

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


    public class MainFragment extends Fragment {

        public MainFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            if (null == getView())
                log("NULL View");
            log("frag");

            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            EditText et_ip = (EditText)getView().findViewById(R.id.et_ipAddress);
            if (null != et_ip) {
                et_ip.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView textView, int keyCode, KeyEvent keyEvent) {
                        if (EditorInfo.IME_ACTION_DONE == keyCode) {
                            textView.setCursorVisible(false);
                        }
                        return false;
                    }
                });
            }

            et_ip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((EditText)view).setCursorVisible(true);
                }
            });
        }

        public String getIpAddress() {
            EditText et_ip = (EditText)getView().findViewById(R.id.et_ipAddress);
            if (null != et_ip)
                return et_ip.getText().toString();

            return "";
        }

        public void setInTemp(int tmp) {
            TextView inTemp = (TextView)getView().findViewById(R.id.tv_inTemp);
            boolean enable = false;
            String tmpStr = new String().valueOf((float)tmp / 10.0);
            if (null != inTemp) {
                if (errorTemp == tmp) {
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

        public void setOutTemp(int tmp) {
            TextView outTemp = (TextView)getView().findViewById(R.id.tv_outTemp);
            boolean enable = false;
            String tmpStr = new String().valueOf((float)tmp / 10.0);
            if (null != outTemp) {
                if (errorTemp == tmp) {
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
