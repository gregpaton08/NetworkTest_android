package com.example.networktest;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Date;

/**
 * Created by Greg on 3/18/14.
 */
public class MainViewFragment extends Fragment {

        private static final int ERROR_TEMP = -1111;
        private SharedPreferences m_cachedTemp;
        private static final String FAIL = "FAIL";
        private static final String CACHED_TEMP = "CACHED_TEMP";
        private static final String IN_TEMP = "IN_TEMP";
        private static final String OUT_TEMP = "OUT_TEMP";
        private static final String TIME_TEMP = "TIME_TEMP";

        private TextSwitcher m_tsInTemp;
        private TextSwitcher m_tsOutTemp;
        private TextView m_tvTimeTemp;


        private void log(String str) {
            Log.w("NetworkTest", str);
        }

        public MainViewFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            EditText et_ip = null;
            View view = getView();
            if (null != view)
                et_ip = (EditText)getView().findViewById(R.id.et_ipAddress);
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

                et_ip.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((EditText)view).setCursorVisible(true);
                    }
                });
            }

            m_tvTimeTemp = (TextView)getView().findViewById(R.id.tv_timeTemp);
            m_tsInTemp = (TextSwitcher)getView().findViewById(R.id.ts_inTemp);
            m_tsInTemp.setFactory(new ViewSwitcher.ViewFactory() {
                @Override
                public View makeView() {
                    TextView tv = new TextView(getActivity());
                    tv.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
                    tv.setTextSize(50);
                    tv.setTextColor(Color.BLACK);
                    return tv;
                }
            });

            m_tsOutTemp = (TextSwitcher)getView().findViewById(R.id.ts_outTemp);
            m_tsOutTemp.setFactory(new ViewSwitcher.ViewFactory() {
                @Override
                public View makeView() {
                    TextView tv = new TextView(getActivity());
                    tv.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
                    tv.setTextSize(50);
                    tv.setTextColor(Color.BLACK);
                    return tv;
                }
            });

            // Declare the in and out animations and initialize them
            Animation in = AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_in_left);
            Animation out = AnimationUtils.loadAnimation(getActivity(),android.R.anim.slide_out_right);

            // set the animation type of textSwitcher
            m_tsInTemp.setInAnimation(in);
            m_tsInTemp.setOutAnimation(out);
            m_tsOutTemp.setInAnimation(in);
            m_tsOutTemp.setOutAnimation(out);

            m_tsInTemp.setCurrentText("-");
            m_tsOutTemp.setCurrentText("-");
        }

        @Override
        public void onResume() {
            super.onResume();

            m_cachedTemp = getActivity().getSharedPreferences(CACHED_TEMP, 0);
            SharedPreferences.Editor editor = m_cachedTemp.edit();

            // Update temperature if more than 5 minutes have passed
            long time = m_cachedTemp.getLong(TIME_TEMP, 0);
            if (System.currentTimeMillis() - time > 300000 ||
                ((TextView)m_tsInTemp.getCurrentView()).getText().toString().equals("-") ||
                ((TextView)m_tsOutTemp.getCurrentView()).getText().toString().equals("-")) {
                updateWeather();
            }
        }

        private boolean updateWeather() {
            ConnectivityManager connMgr = (ConnectivityManager)
                    getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected() && ConnectivityManager.TYPE_WIFI == networkInfo.getType()) {
                GetWeatherTaskParams gwtp = new GetWeatherTaskParams(getIpAddress(), 12345);
                new GetWeatherTask().execute(gwtp);
            }
            else {
                log("WIFI unavailable");
                createWifiDialog();
                setCachedTemp();
                return false;
            }

            return true;
        }

        private void createWifiDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Please turn on WIFI")
                    .setTitle("WIFI is off")
                    .setCancelable(false)
                    .setPositiveButton("Settings",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent i = new Intent(Settings.ACTION_WIFI_SETTINGS);
                                    startActivity(i);
                                }
                            })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        }

        public String getIpAddress() {
            EditText et_ip = (EditText)getView().findViewById(R.id.et_ipAddress);
            if (null != et_ip)
                return et_ip.getText().toString();

            return "";
        }

        public void setTemp(int inTemp, int outTemp) {
            // Cache the current temperature and time
            SharedPreferences.Editor editor = m_cachedTemp.edit();
            editor.putInt(IN_TEMP, inTemp);
            editor.putInt(OUT_TEMP, outTemp);
            editor.putLong(TIME_TEMP, System.currentTimeMillis());
            editor.commit();

            // Set temperature in view
            setInTemp(inTemp);
            setOutTemp(outTemp);
            m_tvTimeTemp.setText("");
        }

        public void setCachedTemp() {
            int inTemp = m_cachedTemp.getInt(IN_TEMP, ERROR_TEMP);
            int outTemp = m_cachedTemp.getInt(OUT_TEMP, ERROR_TEMP);
            long time = m_cachedTemp.getLong(TIME_TEMP, 0);

            setInTemp(inTemp);
            setOutTemp(outTemp);
            setTimeTemp(time);
        }

        public void setInTemp(int tmp) {
            boolean enable = false;
            String tmpStr = String.valueOf((float)tmp / 10.0);
            if (ERROR_TEMP == tmp) {
                m_tsInTemp.setText("-");
            }
            else {
                m_tsInTemp.setText(tmpStr);
                enable = true;
            }
            m_tsInTemp.setEnabled(enable);
        }

        public void setOutTemp(int tmp) {
            boolean enable = false;
            String tmpStr = String.valueOf((float)tmp / 10.0);
            if (ERROR_TEMP == tmp) {
                m_tsOutTemp.setText("-");
            }
            else {
                m_tsOutTemp.setText(tmpStr);
                enable = true;
            }
            m_tsOutTemp.setEnabled(enable);
        }

        public void setTimeTemp(long time) {
            Date date = new Date(time);
            String timeTempStr = "as of " + date.toString();
            m_tvTimeTemp.setText(timeTempStr);
        }

        int parseForInTemp(String tempStr) {
            int temp = ERROR_TEMP;
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
            int temp = ERROR_TEMP;
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
                    return new String(buf, 0, count, charset);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }

                return FAIL;
            }

            protected void onProgressUpdate(Integer... progress) {

            }

            protected void onPreExecute() {

            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                log(result);
                int inTemp;
                int outTemp;
                if (false == result.equals(FAIL)) {
                    // Parse temperatures from result string
                    inTemp = parseForInTemp(result);
                    outTemp = parseForOutTemp(result);

                    setTemp(inTemp, outTemp);
                }
                else {
                    // If fail to get temp then display most recent values
                    setCachedTemp();
                }
            }
        }
}
