package com.minimattapps.siripi;



import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioSource;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import android.speech.tts.TextToSpeech;
public class MainActivity extends Activity implements
TextToSpeech.OnInitListener {
    boolean recording = false;
    int barprogress = 0;
    MediaRecorder recorder;
    String serverip;
    public static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
    // TTS object
    public TextToSpeech myTTS;
    // status check code
    public int MY_DATA_CHECK_CODE = 0;
   
 // setup TTS
    public void onInit(int initStatus) {

        // check for successful instantiation
        if (initStatus == TextToSpeech.SUCCESS) {
            if (myTTS.isLanguageAvailable(Locale.US) == TextToSpeech.LANG_AVAILABLE)
                myTTS.setLanguage(Locale.US);
        } else if (initStatus == TextToSpeech.ERROR) {
            Toast.makeText(this, "Sorry! Text To Speech failed...",
                    Toast.LENGTH_LONG).show();
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
   
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_main);
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
        SharedPreferences settings = this.getPreferences(Activity.MODE_PRIVATE);
        MainActivity.this.serverip = settings.getString("serverip", MainActivity.this.serverip);
        final Button button = (Button) findViewById(R.id.button1);
        final ProgressBar bar = (ProgressBar) findViewById(R.id.progressBar1);
       bar.setVisibility(View.INVISIBLE);
   
 
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	
           if (recording == false){
        	   button.setText("Listening");
        	   recording = true;
        	   bar.setVisibility(View.VISIBLE);
        	   
        	   recorder = new MediaRecorder();

        	   recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        	   recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        	   recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        	   recorder.setAudioEncodingBitRate(44000);
        	   recorder.setOutputFile("/sdcard/voice.3gp");


        	   try {
        	   	recorder.prepare();
        	   	recorder.start();
        	   } catch (IllegalStateException e) {
        	   	e.printStackTrace();
        	   } catch (IOException e) {
        	   	e.printStackTrace();
        	   }
        	  

        	   
        	
           } else {
        	   button.setText("Listen");
        	   recording = false;
        	   bar.setVisibility(View.INVISIBLE);
        	  
        	        Thread thread = new Thread(){
        	        public void run(){                              
        	                if (recorder != null)                                   
        	                    recorder.stop();
        	                MediaPlayer p = new MediaPlayer();
        	                try {
								p.setDataSource("/sdcard/voice.3gp");
					
							} catch (IllegalArgumentException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (SecurityException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (IllegalStateException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
        	                try {
								p.prepare();
							} catch (IllegalStateException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
        	                //p.start();
        	                    String results = UploadFile("/sdcard/voice.3gp", "http://" + MainActivity.this.serverip + "/SiriPi/detect.php");
        	                    speakWords(results);
        	        }
        	    };
        	    thread.start();                             
        	  
        	    }
           }
            
        });
    }

   
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.menu_settings:
        	AlertDialog.Builder alert = new AlertDialog.Builder(this);

        	alert.setTitle("Settings");
        	alert.setMessage("Enter Server IP");

        	// Set an EditText view to get user input 
        	final EditText input = new EditText(this);
        	alert.setView(input);
            input.setText(MainActivity.this.serverip);
        	alert.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int whichButton) {
        	  Editable value = input.getText();
        	  String output = value.toString();
        	  SharedPreferences settings = MainActivity.this.getPreferences(Activity.MODE_PRIVATE);
        	  SharedPreferences.Editor editor = settings.edit();
        	  editor.putString("serverip", MainActivity.this.serverip);
        	  editor.commit();
        	  MainActivity.this.serverip = output;
        	  }
        	});

        	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        	  public void onClick(DialogInterface dialog, int whichButton) {
        	    // Canceled.
        	  }
        	});

        	alert.show();
            return true;
        
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    
   public String UploadFile(String file, String server) {
	       
           HttpURLConnection conn = null;
           DataOutputStream dos = null;
           DataInputStream inStream = null;
           String existingFileName = file;
          
           String lineEnd = "\r\n";
           String twoHyphens = "--";
           String boundary =  "*****";
           int bytesRead, bytesAvailable, bufferSize;
           byte[] buffer;
           int maxBufferSize = 1*1024*1024;
           String responseFromServer = "";
           String urlString = server;
           try
           {
            //------------------ CLIENT REQUEST
           FileInputStream fileInputStream = new FileInputStream(new File(existingFileName) );
            // open a URL connection to the Servlet
            URL url = new URL(urlString);
            // Open a HTTP connection to the URL
            conn = (HttpURLConnection) url.openConnection();
            // Allow Inputs
            conn.setDoInput(true);
            // Allow Outputs
            conn.setDoOutput(true);
            // Don't use a cached copy.
            conn.setUseCaches(false);
            // Use a post method.
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
            dos = new DataOutputStream( conn.getOutputStream() );
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + existingFileName + "\"" + lineEnd);
            dos.writeBytes(lineEnd);
            // create a buffer of maximum size
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];
            // read file and write it into form...
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            while (bytesRead > 0)
            {
             dos.write(buffer, 0, bufferSize);
             bytesAvailable = fileInputStream.available();
             bufferSize = Math.min(bytesAvailable, maxBufferSize);
             bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }
            // send multipart form data necesssary after file data...
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            // close streams
            Log.e("Debug","File is written");
            fileInputStream.close();
            dos.flush();
            dos.close();
           }
           catch (MalformedURLException ex)
           {
                Log.e("Debug", "error: " + ex.getMessage(), ex);
           }
           catch (IOException ioe)
           {
                Log.e("Debug", "error: " + ioe.getMessage(), ioe);
           }
           //------------------ read the SERVER RESPONSE
           try {
                 inStream = new DataInputStream ( conn.getInputStream() );
                 String str;
                String serverresponse = "";
                Looper.prepare();
                 while (( str = inStream.readLine()) != null)
                 {
                      serverresponse = serverresponse + str;
                 }
                 inStream.close();
                 
                 Log.e("Debug", serverresponse);
                 return serverresponse;
           }
           catch (IOException ioex){
                Log.e("Debug", "error: " + ioex.getMessage(), ioex);
           }
         return "";
	   }
   
   @Override
  
   protected void onStop(){
  
   super.onStop();
   
  
   }
   
   /**
    * Handle the results from the recognition activity.
    */
   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {

       if (requestCode == MY_DATA_CHECK_CODE) {
           if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
               // the user has the necessary data - create the TTS
               myTTS = new TextToSpeech(this, this);
           } else {
               // no data - install it now
               Intent installTTSIntent = new Intent();
               installTTSIntent
                       .setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
               startActivity(installTTSIntent);
           }
       }

       super.onActivityResult(requestCode, resultCode, data);

   }

   @Override
   protected void onDestroy() {
       super.onDestroy();
       myTTS.shutdown();
   }




   // speak the user text
   public void speakWords(String speech) {

       // speak straight away
       myTTS.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
   }
}
    

