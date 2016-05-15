package burningham18.adcatch;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import burningham18.adcatch.Shell;


public class MainKhampf extends Activity {
    public Button start, stop, about, exit;
    final String original = "/etc/hosts";
    final String backup = "/etc/hosts.AdCatch";
    final String temp = Environment.getExternalStorageDirectory().getPath()+"/hosts.tmp";
    public InputStream input = null;
    public InputStream host2 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        start=(Button) findViewById(R.id.start);
        stop=(Button) findViewById(R.id.stop);
        about=(Button) findViewById(R.id.about);
        exit=(Button) findViewById(R.id.exit);



        (new StartUp()).execute("update");



        start.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                (new StartUp()).setContext(v.getContext()).execute("start");
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                (new StartUp()).setContext(v.getContext()).execute("stop");

            }
        });

        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                (new StartUp()).setContext(v.getContext()).execute("about");


            }
        });

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                (new StartUp()).setContext(v.getContext()).execute("exit");
            }
        });
    }



    private class StartUp extends AsyncTask<String,Void,Void> {


        private Context context = null;
        boolean suAvailable = false;

         public StartUp setContext(Context context) {
            this.context = context;
            return this;
        }

        @Override
        protected Void doInBackground(String... params) {
            suAvailable = Shell.SU.available();
            if (suAvailable) {

                switch (params[0]){
                    case "start"  : start();break;
                    case "stop"   : stop();break;
                    case "about": about();break;
                    case "exit"   : exit();break;
                    case "update"  : tryUpdate();break;
                }
            }
            else{
                switch (params[0]) {
                    case "about": about();break;
                    case "exit":exit();break;
                    default:{runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(),"Phone not Rooted",Toast.LENGTH_SHORT).show();
                        }
                    });
                    }break;
                }

            }

            return null;
        }


        public InputStream tryUpdate()  {
            URL url = null;
            HttpURLConnection conn = null;
            try {
                url = new URL("http://someonewhocares.org/hosts/hosts");
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();
                host2 = conn.getInputStream();

            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getApplicationContext(), "Host file successfully updated!", Toast.LENGTH_SHORT).show();
                }
            });
            return host2;
            }
            catch (Exception e) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(),"Host file cannot be updated! \nAdCatch will use a stored one ;)",Toast.LENGTH_SHORT).show();

                    }
                });
            }

            return null;
        }

        protected void start(){
            boolean working=false;

            if(suAvailable){

                File f = new File(backup);
                if(!f.exists() && !f.isDirectory()) {



                    try {
                        //host to temp
                        input = getApplicationContext().getResources().openRawResource(R.raw.host);
                        input.close();
                        if(input==null) throw new IOException();
                        if(host2!=null) input=host2;
                        FileOutputStream outputStream =
                                new FileOutputStream(new File(temp));

                        int read = 0;
                        byte[] bytes = new byte[1024];
                        while ((read = input.read(bytes)) != -1) {
                            outputStream.write(bytes, 0, read);
                        }
                        //
                        //creo backup
                        Shell.SU.run("mv \"/etc/hosts\" \"/etc/hosts.AdCatch\"");
                        //sposto il tmp nella cartella di sistema
                        Shell.SU.run("mv \""+temp+"\" \"/etc/hosts\"");

                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "DONE!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (IOException e) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Can't open host file", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                }
                else{
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Service already running!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            else{
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(),"Phone not Rooted",Toast.LENGTH_SHORT).show();
                    }
                });


            }


        }

        protected void stop(){

            if(suAvailable){

                File f = new File(backup);
                if(f.exists() && !f.isDirectory()){
                    //ho il root, il backup esiste
                    //sostituisco l' host modificato con l' originale
                    Shell.SU.run("mv \"/etc/hosts.AdCatch\" \"/etc/hosts\"");
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), "DONE!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(),"Service already stopped!",Toast.LENGTH_SHORT).show();
                        }
                    });
                     }

            }
            else{

                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(),"Phone not Rooted",Toast.LENGTH_SHORT).show();
                    }
                });
            }

        }

        protected void about(){


            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getApplicationContext(),"Copyright 2016\nBurningHAM18\nGPL License",Toast.LENGTH_SHORT).show();
                }
            });

        }

        public void exit(){
            if(suAvailable){
                File f = new File(backup);
                if(f.exists() && !f.isDirectory()){
                    //ho il root, il backup esiste
                    //sostituisco l' host modificato con l' originale
                    Shell.SU.run("mv \"/etc/hosts.AdCatch\" \"/etc/hosts\"");
                }
                about();
                finish();
                System.exit(0);
            }
            else{about();
            finish();
            System.exit(0);}
        }

    }
}
