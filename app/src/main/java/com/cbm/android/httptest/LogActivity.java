package com.cbm.android.httptest;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Looper;
import android.support.v4.widget.DrawerLayout;
import android.text.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.view.ContextMenu;
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
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Handler;
import java.util.regex.Pattern;

public class LogActivity extends Activity {

    private String slog = "";
    private LinearLayout ll;
    private ScrollView svLog;
    private List<String> lstLog=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        ll = findViewById(R.id.llLoggings);
        svLog = findViewById(R.id.svLog);
        ll.setSaveEnabled(true);
        //getLogFile();

        String[] permissions = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"
        };
        requestPermissions(permissions,200);
        refresh();
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
        } else if(item.getItemId()==R.id.mRefresh) {
            refresh();
        } else if(item.getItemId()==R.id.mLastLog) {
            View v = ll.getChildAt(ll.getChildCount()-1);
            unselectAll();
            v.setSelected(true);
            scrollToSelected(v);
        } else if(item.getItemId()==R.id.mClearSelection) {
            unselectAll();
        } else if(item.getItemId()==R.id.mClearLog) {
            new AlertDialog.Builder(this)
                    .setMessage("Delete all in the Log?")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                FileWriter fw = new FileWriter(new File(MainActivity.getLog()));
                                fw.write("");
                                fw.close();
                            } catch(Exception ex) {
                                ex.printStackTrace();
                            }
                            Toast.makeText(getApplicationContext(), "Log is now empty.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .create().show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        //if(v.getTag().equals("logEntry"))new MenuInflater(this).inflate(R.menu.menu_log_item, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getTag().toString().startsWith("logEntry")) {
            menu.add(11, R.id.mliShowFullLog, 1, "Show Full Log").setActionView(v);
            menu.add(11, R.id.mliCopyLog, 2, "Copy Log").setActionView(v);
        }
        //if(lstLog!=null&&lstLog.size()>0) {
        //}
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getGroupId()==11) {
            if(item.getItemId()==R.id.mliShowFullLog) {
                try {
                    new LogFileAsyncTask("FullLog").execute("log", item.getActionView().getTag().toString().replace("logEntry", ""));
                    /*new AlertDialog.Builder(this)
                            .setView(createFullView(getLogFile().get(Integer.parseInt(((TextView) item.getActionView()).getTag().toString().replace("logEntry", "")))))
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setCancelable(false)
                            .create()
                            .show();*/
                } catch(Exception ex) {
                    Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
                }
                Toast.makeText(this, "Show Full Log", Toast.LENGTH_SHORT).show();
                return true;
            } else if(item.getItemId()==R.id.mliCopyLog) {
                try {
                    //if(lstLog!=null&&lstLog.size()>0) {
                    new LogFileAsyncTask("CopyLog").execute("log", item.getActionView().getTag().toString().replace("logEntry", ""));
                    /*String txt = getLogFile().get(Integer.parseInt(((TextView) item.getActionView()).getTag().toString().replace("logEntry", "")));
                        ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        cm.setText(txt);
                        Toast.makeText(this, "Copy Log:\n" + txt, Toast.LENGTH_SHORT).show();*/
                    //}
                } catch(Exception ex) {
                    ex.printStackTrace();
                    Toast.makeText(this, "Error:\n"+ex.getMessage(), Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        }
        return super.onContextItemSelected(item);
    }

    private List<String> getLogFile() {
        String loca = MainActivity.getLog();
        List<String> lst = new ArrayList<>();

        File file = new File(loca);
        try {
            FileReader fr = new FileReader(file);
            int c = 0;
            while((c = fr.read())!=-1) {
                slog+=(char)c;
            }

            return Arrays.asList(slog.replace("[@!","").split("@!]"));
        } catch(Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }

        return new ArrayList<>();
    }

    private TextView createTV(String s) {
        final TextView tv = new TextView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(16,16,16,16);
        tv.setLayoutParams(lp);
        tv.setPadding(16,16,16,16);
        tv.setTag("logEntry");
        tv.setText(s);
        tv.setTextColor(Color.BLACK);
        tv.setBackgroundResource(R.drawable.bgr_white);

        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!v.isSelected())v.showContextMenu();
                unselectAll();
                v.setSelected(true);
                scrollToSelected();
                v.requestFocus();
            }
        });

        tv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                tv.showContextMenu();
                return true;
            }
        });

        registerForContextMenu(tv);

        return tv;
    }

    private View createFullView(Context c, List<String> ltext) {
        ScrollView sv= new ScrollView(c);
        sv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        LinearLayout llFV = new LinearLayout(c);
        llFV.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        llFV.setOrientation(LinearLayout.VERTICAL);
        sv.addView(llFV);
        String innerText = "";

        for(int x=0; x<ltext.size(); x++) {
            innerText+=ltext.get(x);
            View v = createTV(innerText);
            v.setOnClickListener(null);
            v.setOnLongClickListener(null);
            v.setClickable(false);

            unregisterForContextMenu(v);
            llFV.addView(v);
        }

        return sv;
    }

    private void fillLog(List<String> list) {
        ll.removeAllViews();
        List<String> l = list;
        if(list.size()<1)l=getLogFile();
        for(int x=0; x<l.size(); x++) {
            View v = createTV(l.get(x).substring(0, l.get(x).length()>249?249:l.get(x).length()));
            v.setTag(v.getTag().toString()+x);
            if(!l.get(x).isEmpty()&&l.get(x).indexOf(":")>-1)ll.addView(v);
        }
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

    private void refresh() {
        ll.removeAllViews();
        ll.addView(createTV("Loading list of logs..."));
        new LogFileAsyncTask(ll).execute();
        //fillLog(new ArrayList<String>());
    }

    private class LogFileAsyncTask extends AsyncTask<String, String, List<String>> {
        ProgressDialog pd;
        View v;
        String type;
        AlertDialog.Builder ad;

        public LogFileAsyncTask() {}

        public LogFileAsyncTask(String type) {
            this.type=type;

        }

        public LogFileAsyncTask(View v, String type) {this.v=v; this.type=type;}
        public LogFileAsyncTask(View v) {
            this.v =v;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //pd = new ProgressDialog(getApplicationContext());
            //pd.setIndeterminate(true);
            //pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            //pd.
            //pd.show();

            ad = new AlertDialog.Builder(LogActivity.this);

        }

        @Override
        protected List<String> doInBackground(String... params) {
            List<String> lst = new ArrayList<>();
            //String loca = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
            //    if (!loca.endsWith("/")) loca+="/";
            //        loca += ".httptest_log.txt";
            String loca = MainActivity.getLog();

                String result = "";
                File file = new File(loca);
                try {
                    FileReader fr = new FileReader(file);
                    int ch = 0;

                    String log = "";
                    int logIndx=0;
                    int endLog=0;
                    boolean checkKey = false;
                    String chkdKey="";
                    while ((ch = fr.read()) != -1) {
                        //result += (char) c;
                        String c = (char) ch + "";
                        if (endLog==0) {
                            log = "";
                            if((c.equals("["))) {
                                checkKey = true;
                                chkdKey=c;
                            } else if(checkKey) {
                                if(!chkdKey.isEmpty()) {
                                    if(chkdKey.length()==1&&chkdKey.equals("[")) {

                                    } else if(chkdKey.length()==2&&chkdKey.equals("[@")) {
                                        chkdKey+=c;
                                        if(chkdKey.length()==3&&chkdKey.equals("[@!")) {
                                            chkdKey="";
                                            checkKey = false;
                                            if(type!=null&&!type.equals("LogList")) {
                                                if(logIndx==Integer.parseInt(params[1])) endLog=1;
                                                else logIndx+=1;
                                            } else {
                                                endLog=1;
                                            }
                                            continue;
                                        }
                                    } else {
                                        chkdKey="";
                                        checkKey = false;
                                        continue;
                                    }
                                    chkdKey+=c;
                                }
                            }
                            //continue;
                        } /*else if (endLog==2||(c.equals("@") && (result.charAt(x + 1) + "").equals("!") && (result.charAt(x + 2) + "").equals("]"))) {
                            lst.add(log);
                            //  publishProgress(new String[]{log.substring(0,log.length()>=250?250:log.length())});
                            log = "";
                            logIndx=0;
                            endLog=0;
                            x += 2;
                        }*/ else if(endLog==1) {
                            if(log.isEmpty()&&!Pattern.matches("[0-9]",c))continue;
                            else if(c.equals("@")) {checkKey=true;chkdKey=c;continue;}

                            if(checkKey) {
                                if(chkdKey.length()==1&&chkdKey.equals("@")) {
                                    chkdKey+=c;
                                } else if(chkdKey.length()==2&&chkdKey.equals("@!")) {
                                    chkdKey+=c;
                                } else if(chkdKey.length()==3&&chkdKey.equals("@!]")) {
                                    chkdKey="";
                                    checkKey = false;
                                    lst.add(log);

                                    if(type==null||type.equals("LogList"))endLog=0;
                                    else {endLog=4;break;}
                                } else {
                                    log+=chkdKey+c;
                                    chkdKey="";
                                    checkKey = false;
                                }

                            } else {
                                log += c;
                            }

                            if(log.length()==250) {
                                lst.add(log);
                                log="";
                                chkdKey="";
                                checkKey=false;
                                if(type==null||type.equals("LogList"))endLog=0;

                            }
                        } else if(endLog==4) {
                            break;
                        }
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    lst.add(ex.getMessage());
                }

                /*try {
                    String log = "";
                    int logIndx=0;
                    int endLog=1;

                    for (int x = 0; x < result.toCharArray().length; x++) {
                        String c = result.charAt(x) + "";
                        if (endLog==0||(c.equals("[") && (result.charAt(x + 1) + "").equals("@") && (result.charAt(x + 2) + "").equals("!"))) {
                            x += 2;
                            endLog=1;
                            //continue;
                        } else if (endLog==2||(c.equals("@") && (result.charAt(x + 1) + "").equals("!") && (result.charAt(x + 2) + "").equals("]"))) {
                            lst.add(log);
                            //  publishProgress(new String[]{log.substring(0,log.length()>=250?250:log.length())});
                            log = "";
                            logIndx=0;
                            endLog=0;
                            x += 2;
                        } else {
                            if(log.isEmpty()&&!Pattern.matches("[0-9]",c))continue;
                            else if(logIndx==250){endLog=2;x=result.indexOf("@!]",x);continue;}
                            log += c;
                            logIndx+=1;
                        }

                    }
                } catch(Exception ex) {
                    ex.printStackTrace();
                    lst.add(ex.getMessage());
                    return lst;
                }*/

                return lst;
        }

        @Override
        protected void onPostExecute(final List<String> s) {
            //super.onPostExecute(s);
            try {
                if (type == null || type.equals("LogList")) {
                    fillLog(s);
                    Toast.makeText(getBaseContext(), "Operation done.", Toast.LENGTH_SHORT).show();
                } else {
                    //Handler h = new Handler(Looper.getMainLooper());
                    new FullLogAT(type).execute(s.toArray(new String[]{}));


                }
                //pd.dismiss();
            } catch(Exception ex) {
                Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class FullLogAT extends AsyncTask<String, String, String> {
        View v;
        AlertDialog.Builder ad;
        ProgressDialog pd;
        String type;

        public FullLogAT() {
        }

        public FullLogAT(String type) {
            this.type = type;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //pd = new ProgressDialog(getApplicationContext());
            //pd.setIndeterminate(true);
            //pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            //pd.
            //pd.show();

            ad = new AlertDialog.Builder(LogActivity.this);
        }

        @Override
        protected String doInBackground(final String... ps) {
            try {
                runOnUiThread(new Runnable() {
                    public void run() {
                        ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        if (type.equals("CopyLog")) {
                            String txt = "";
                            for (int x = 0; x < ps.length; x++) {
                                txt += ps[x];
                            }
                            cm.setText(txt);
                        } else {
                            v = createFullView(LogActivity.this, Arrays.asList(ps));
                        }
                    }
                });

                //return (v != null)||cm.hasText() ? "Good" : "Bad";
                return "Good";
            } catch(Exception ex) {
                ex.printStackTrace();
                return ex.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if(result.equals("Good")) {
                if(type.equals("CopyLog")) {
                    Toast.makeText(getBaseContext(), "Copied Log", Toast.LENGTH_SHORT).show();
                } else {
                    ad.setView(v)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setCancelable(false)
                        .create()
                        .show();
                }
            } else {
                Toast.makeText(LogActivity.this, "Error occured.\n"+result, Toast.LENGTH_SHORT).show();
            }
        }
    }

}
