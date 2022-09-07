package com.cbm.android.httptest;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity {

    private EditText etHost, etPort, etSite;
    private TextView tvResult;
    private Button btnSubmit, btnClear;
    private boolean found=false;
    private CheckBox chkDomain;
    private MyAsyncTask myAsyncTask;

    private String dateFormat = "dd/MM/yyyy hh:mm:ss:sss";
    private SimpleDateFormat sdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_main);
            etHost = findViewById(R.id.etHost);
            etPort=findViewById(R.id.etPort);
            etSite=findViewById(R.id.etSite);
            tvResult=findViewById(R.id.tvResult);
            btnSubmit=findViewById(R.id.btnSubmit);
            btnClear=findViewById(R.id.btnClear);
            chkDomain=findViewById(R.id.chkDomain);

            sdf = new SimpleDateFormat(dateFormat);

            tvResult.setFreezesText(true);

            btnSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Toast.makeText(MainActivity.this, "The HttpTest App", Toast.LENGTH_SHORT).show();
                        //StringBuilder sb = new StringBuilder();
                        //sb.append(etHost.getText().toString());
                        //Matcher m = Pattern.compile("http(s?)://[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+").matcher(etHost.getText());
                        //if(m.find()) {
                        //    if(!sb.toString().endsWith(":"))
                        //}
                        //sb.append(new Matcher().usePattern(Pattern.compile("http{s?}://[0-9]")).matches());
                        String host=etHost.getText().toString(), port=etPort.getText().toString(), site=etSite.getText().toString();
                        String url = host+":"+
                                port+"/"+
                                site;

                        if(chkDomain.isChecked()){
                            host="";port="";
                            boolean isHttpThere=false;
                            if(site.startsWith("http:"))isHttpThere=true;
                            if(site.startsWith("https:"))isHttpThere=true;
                            if(!isHttpThere){site="http://"+site;}
                        }

                        tvResult.setText("The URL is: \n"+(chkDomain.isChecked()?site:url)+"\n\n");

                        myAsyncTask = new MyAsyncTask(host,
                                port,
                                site,tvResult);
                        myAsyncTask.execute();
                    } catch(Exception ex) {
                        Toast.makeText(MainActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                        ex.printStackTrace();
                    }

                }
            });

            btnClear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    etHost.setText("");
                    etPort.setText("");
                    etSite.setText("");
                    chkDomain.setChecked(false);
                    tvResult.setText("Result");
                    setFound(false);
                    if(myAsyncTask!=null&&myAsyncTask.getStatus()== AsyncTask.Status.RUNNING) {
                        myAsyncTask.cancel(true);
                    }
                }
            });

            chkDomain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    etHost.setEnabled(!isChecked);
                    etPort.setEnabled(!isChecked);
                }
            });
        } catch(Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.mmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if(item.getItemId()==R.id.mLogFile) {
            startActivity(new Intent(this, LogActivity.class));
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    private void saveToLog(String s) {
        String loca = getLog();

        try {
            File file = new File(loca);
            /*FileReader fr = new FileReader(file);
            String ftxt = "";
            int c = 0;
            while((c=fr.read())!=-1) {
                ftxt+=(char)c;

            }*/
            if(!file.exists())file.createNewFile();

            FileWriter fw = new FileWriter(file, true);
            fw.write("[@!"+s+"@!]\n");
            fw.close();
        } catch(Exception ex) {
            ex.printStackTrace();
        }

    }

    public static String getLog() {
        String loca = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (!loca.endsWith("/")) loca+="/";
        loca+=".httptest_log/";
        if(!new File(loca).exists())new File(loca).mkdir();
        loca += "httptest_log.txt";

        return loca;
    }

    public class MyAsyncTask extends AsyncTask<String, String, String> {
        String host="", port="", site="", result="", startTime="";
        View v=null;
        URL url;
        HttpURLConnection huc;

        public MyAsyncTask() {}

        public MyAsyncTask(String host, String port, String site, View v) {
            this.host=host;
            this.port=port;
            this.site=site;
            this.v=v;
            this.result = ((TextView)v).getText().toString();
            startTime=sdf.format(new Date());
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            String toLog = sdf.format(new Date().getTime())+":\n"+result+"Trying to connect for the next 30 seconds(30000ms)...";
            ((TextView)v).setText(toLog);
            saveToLog(toLog);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            String toLog = sdf.format(new Date())+":\n"+result+"was cancelled.\nStarted at "+startTime;
            ((TextView)v).setText(toLog);
            saveToLog(toLog);
            if(huc!=null)huc.disconnect();
        }

        @Override
        protected String doInBackground(String... ss) {
            try {
                if(!host.isEmpty()&&!port.isEmpty()) {
                    port = ":"+port+"/";
                }
                url = new URL(host+port+site);
                huc = (HttpURLConnection)url.openConnection();
                huc.setRequestProperty("Accept", "application/json");
                huc.setConnectTimeout(30000);

                try {
                    StringBuilder sb = new StringBuilder();
                    String line = "";
                    BufferedReader br = new BufferedReader(new InputStreamReader(huc.getInputStream()));
                    while((line=br.readLine())!=null) {
                        sb.append(line).append('\n');
                    }
                    int rcode=huc.getResponseCode();
                    if(rcode==200) {
                        return "Success.\n"+sb.toString();
                        //setFound(true);
                    } else {
                        return "No connection made with web content.";
                    }
                } catch(Exception ex) {
                    //Toast.makeText(MainActivity.this,ex.getMessage(),Toast.LENGTH_SHORT).show();

                    return sdf.format(new Date().getTime())+"\nFailed\\Error.\n"+ex.getMessage()+"\nStarted at "+startTime;
                } finally {
                    huc.disconnect();
                }
            } catch(Exception ex) {
                //Toast.makeText(MainActivity.this,ex.getMessage(),Toast.LENGTH_SHORT).show();

                return sdf.format(new Date().getTime())+"\nFailed\\Error.\n"+ex.getMessage()+"\nStarted at "+startTime;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                String toLog = sdf.format(new Date().getTime())+":\n"+result+s+"\nStarted at "+startTime;
                ((TextView)v).setText(result + s);
                saveToLog(toLog);
            } catch(Exception ex) {
                Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                ex.printStackTrace();
            }
        }

        public AsyncTask setResultView(View v) {
            this.v = v;
            return this;
        }
    }

    private void setFound(boolean active) {
        found = active;
    }
}
