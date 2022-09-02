package com.cbm.android.httptest;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class LogActivity extends Activity {

    private String slog = "";
    private LinearLayout ll;
    private ScrollView svLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        ll = findViewById(R.id.llLoggings);
        svLog = findViewById(R.id.svLog);
        //getLogFile();

        ll.addView(createTV("Loading list of logs..."));
        new LogFileAsyncTask(ll).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.log_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.mSearch) {
            Toast.makeText(this, "Search for text", Toast.LENGTH_SHORT).show();
        } else if(item.getItemId()==R.id.mLastLog) {
            View v = ll.getChildAt(ll.getChildCount()-1);
            unselectAll();
            v.setSelected(true);
            scrollToSelected(v);
        } else if(item.getItemId()==R.id.mClearSelection) {
            unselectAll();
        }
        return super.onOptionsItemSelected(item);
    }

    private File getLogFile() {
        String loca = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
        if(!loca.endsWith("/"))loca+="/.httptest_log.txt";

        File file = new File(loca);
        try {
            FileReader fr = new FileReader(file);
            int c = 0;
            while((c = fr.read())!=-1) {
                slog+=(char)c;
            }

        } catch(Exception ex) {
            ex.printStackTrace();
        }

        return file;
    }

    private TextView createTV(String s) {
        TextView tv = new TextView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(16,16,16,16);
        tv.setLayoutParams(lp);
        tv.setPadding(16,16,16,16);
        tv.setText(s);
        tv.setTextColor(Color.BLACK);
        tv.setBackgroundResource(R.drawable.bgr_white);

        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unselectAll();
                v.setSelected(true);
                scrollToSelected();
                v.requestFocus();
            }
        });

        return tv;
    }

    private void unselectAll() {
        for(int a=0; a<ll.getChildCount(); a++) {
            ll.getChildAt(a).setSelected(false);
        }
    }

    private void scrollToSelected(View v) {
        if(v!=null) svLog.smoothScrollTo(0, Math.round(v.getY()));
        else {
            for(int a=0; a<ll.getChildCount(); a++) {
                if(ll.getChildAt(a).isSelected()) {
                    svLog.smoothScrollTo(0, Math.round(ll.getChildAt(a).getY()));
                    break;
                }
            }
        }
    }

    private void scrollToSelected() {
        scrollToSelected(null);
    }

    private class LogFileAsyncTask extends AsyncTask<String, String, List<String>> {
        ProgressBar pb;
        View v;

        public LogFileAsyncTask(View v) {
            this.v =v;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //pb = new ProgressBar(getBaseContext());
            //pb.setIndeterminate(true);

        }

        @Override
        protected List<String> doInBackground(String... params) {
            List<String> lst = new ArrayList<>();
            String loca = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
                if (!loca.endsWith("/")) loca += "/.httptest_log.txt";

                String result = "";
                File file = new File(loca);
                try {
                    FileReader fr = new FileReader(file);
                    int c = 0;
                    while ((c = fr.read()) != -1) {
                        result += (char) c;
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                try {
                    String log = "";

                    for (int x = 0; x < result.toCharArray().length; x++) {
                        String c = result.charAt(x) + "";
                        if (c.equals("[") && (result.charAt(x + 1) + "").equals("@") && (result.charAt(x + 2) + "").equals("!")) {
                            x += 2;
                            continue;
                        } else if (c.equals("@") && (result.charAt(x + 1) + "").equals("!") && (result.charAt(x + 2) + "").equals("]")) {
                            lst.add(log);
                            log = "";
                            x += 2;
                        } else {
                            if(log.isEmpty()&&!Pattern.matches("[0-9]",c))continue;
                            log += c;
                        }

                    }
                } catch(Exception ex) {
                    ex.printStackTrace();
                    lst.add(ex.getMessage());
                    return lst;
                }

                return lst;
        }

        @Override
        protected void onPostExecute(List<String> s) {
            //super.onPostExecute(s);
            ((LinearLayout)v).removeAllViews();
            for(int b=0; b<s.size(); b++) {
                ((LinearLayout)v).addView(createTV(s.get(b)));
            }
        }
    }

}
