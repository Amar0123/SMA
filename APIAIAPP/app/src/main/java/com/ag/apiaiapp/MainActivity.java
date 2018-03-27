package com.ag.apiaiapp;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    ImageButton sendBtn;
    EditText messageET;
    ListView messagesContainer;
    ChatAdapter adapter;
    ArrayList<ChatMessage> chatHistory;

    ImageButton sp;
    List<String> list,jokes;
    ArrayAdapter<String> dataAdapter;

    ChatMessage latestChatMsg = null;
    private static final int MY_DATA_CHECK_CODE = 1234;
    private static final int USERSPEAKCODE = 5678;
    TextToSpeech mTts;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    Boolean voiceSetting;
    String userLastMsg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initControls();

        FirebaseApp.initializeApp(this);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if(currentUser == null){
            sendtoStart();//direct user to starting page
        }else{
            Toast.makeText(this, "Welcome Shopper!", Toast.LENGTH_SHORT).show();
        }

        //get voice setting
        prefs = getSharedPreferences("status", MODE_PRIVATE);
        editor = prefs.edit();
        voiceSetting = prefs.getBoolean("status",false);
        
        jokes = new ArrayList<String>();
        jokes.add("What happens to a frog's car when it breaks down?\n" +
                "It gets toad away.");
        jokes.add("My friend thinks he is smart. He told me an onion is the only food that makes you cry, so I threw a coconut at his face.");
        jokes.add("Why was six scared of seven? \n" +
                "Because seven \"ate\" nine.");
        jokes.add("Did you hear about the kidnapping at school? \n" +
                "It's okay. He woke up.");
        jokes.add("Why couldn't the leopard play hide and seek? \n" +
                "Because he was always spotted.");
        jokes.add("What starts with E, ends with E, and has only 1 letter in it? \n" +
                "An Envelope.");
        jokes.add("Teacher: \"Which book has helped you the most in your life?\" \n" +
                "Student: \"My father's cheque book!\"");
        jokes.add("I was wondering why the ball kept getting bigger and bigger, and then it hit me.");
    }

    public void showMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        for (String s : list) {
            popup.getMenu().add(s);
        }
        popup.show();

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                String displayed = menuItem.toString();
                try {
                    if(displayed.matches("Tell me a Joke")) {
                        ChatMessage cm = new ChatMessage();
                        cm.setId(1);//dummy
                        int  ran = new Random().nextInt(jokes.size() - 1);
                        cm.setMessage(jokes.get(ran));
                        jokes.remove(ran);
                        DateFormat dateFormat = new SimpleDateFormat("HH:mm dd/MM/yy");
                        cm.setDate(dateFormat.format(new Date()));
                        cm.setMe(false);
                        displayMessage(cm);
                        sp.setSelected(false);
                    }else if(displayed.matches("Reset Bot")) {
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    }else{
                        Intent intent = new Intent(getApplicationContext(),ShopsActivity.class);
                        intent.putExtra("productType", displayed.substring(0,displayed.indexOf(" at ")).toLowerCase().replace("&","N"));
                        intent.putExtra("mallName", displayed.substring(displayed.indexOf(" at ")+4));
                        startActivity(intent);
                    }

                }
                catch (StringIndexOutOfBoundsException e){
                    //do nth
                }
                return false;
            }
        });
    }

    private void initControls() {
        messagesContainer = (ListView) findViewById(R.id.messagesContainer);
        messageET = (EditText) findViewById(R.id.messageEdit);
        sendBtn = (ImageButton) findViewById(R.id.btnSpeak);
        sp = (ImageButton) findViewById(R.id.spinner1);

        list = new ArrayList<String>();
        list.add("Tell me a Joke");
        list.add("Reset Bot");

        dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        sp.setAdapter(dataAdapter);

        RelativeLayout container = (RelativeLayout) findViewById(R.id.container);

        loadDummyHistory();

        sendBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                promptSpeechInput();
                return false;
            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageET.getText().toString();
                if (TextUtils.isEmpty(messageText)) {
                    return;
                }

                userLastMsg = messageET.getText().toString();

                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setId(1);//dummy
                chatMessage.setMessage(messageText);
                DateFormat dateFormat = new SimpleDateFormat("HH:mm dd/MM/yy");
                chatMessage.setDate(dateFormat.format(new Date()));
                chatMessage.setMe(true);

                displayMessage(chatMessage);

                String a = messageET.getText().toString();
                messageET.setText("");
                RetrieveFeedTask task = new RetrieveFeedTask();
                task.execute(a);
            }
        });
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"");
        try {
            startActivityForResult(intent, USERSPEAKCODE);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),"",
                    Toast.LENGTH_SHORT).show();
        }
    }


    private void loadDummyHistory(){
        chatHistory = new ArrayList<ChatMessage>();

        ChatMessage msg = new ChatMessage();
        msg.setId(1);
        msg.setMe(false);
        msg.setMessage("Hi. I'm Sma - The Shopping Mall Assistant. Which Mall are you in?");
        DateFormat dateFormat = new SimpleDateFormat("HH:mm dd/MM/yy");
        msg.setDate(dateFormat.format(new Date()));
        chatHistory.add(msg);

        //speak
        latestChatMsg = msg;
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);

        adapter = new ChatAdapter(MainActivity.this, new ArrayList<ChatMessage>());
        messagesContainer.setAdapter(adapter);

        for(int i=0; i<chatHistory.size(); i++) {
            ChatMessage message = chatHistory.get(i);
            displayMessage(message);
        }
    }

    public void displayMessage(ChatMessage message) {
        adapter.add(message);
        adapter.notifyDataSetChanged();
        scroll();

    }

    private void scroll() {
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
    }

    // Create GetText Method
    public String GetText(String query) throws UnsupportedEncodingException {
        String text = "";
        BufferedReader reader = null;
        // Send data
        try {
            // Defined URL  where to send data
            URL url = new URL("https://api.api.ai/v1/query?v=20150910");

            // Send POST data request
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty("Authorization", "Bearer " + "e8332833ad554097a1fc9d7defd7a2f0" );
            conn.setRequestProperty("Content-Type", "application/json");
            //Create JSONObject here
            JSONObject jsonParam = new JSONObject();
            JSONArray queryArray = new JSONArray();
            queryArray.put(query);
            jsonParam.put("query", queryArray);
//            jsonParam.put("name", "order a medium pizza");
            jsonParam.put("lang", "en");
            jsonParam.put("sessionId", "1234567890");
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(jsonParam.toString());
            wr.flush();

            // Get the server response
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = null;

            // Read Server Response
            while ((line = reader.readLine()) != null) {
                // Append server response in string
                sb.append(line + "\n");
            }

            text = sb.toString();

            JSONObject object1 = new JSONObject(text);
            JSONObject object = object1.getJSONObject("result");
            JSONObject fulfillment = null;
            String speech = null;
            fulfillment = object.getJSONObject("fulfillment");
            speech = fulfillment.optString("speech");

            return speech;

        } catch (Exception ex) {
        } finally {
            try {
                reader.close();
            } catch (Exception ex) {
            }
        }

        return null;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE && voiceSetting == true) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // success, create the TTS instance
                mTts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        mTts.speak(latestChatMsg.getMessage().toString(),TextToSpeech.QUEUE_FLUSH,  // Drop all pending entries in the playback queue.
                                null);
                    }
                });
            }else{
                // missing data, install it
                Intent installIntent = new Intent();
                installIntent.setAction(
                        TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }else if (requestCode == USERSPEAKCODE ) {
            if (resultCode == RESULT_OK && null != data) {

                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                messageET.setText(result.get(0).toString());

            }
        }
    }

    public class RetrieveFeedTask extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... voids) {
            String s = null;
            try {
                s = GetText(voids[0]);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            return s;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            String mallName,productType, redinfedProductType;

            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setId(122);//dummy
            chatMessage.setMessage(s);
            DateFormat dateFormat = new SimpleDateFormat("HH:mm dd/MM/yy");
            chatMessage.setDate(dateFormat.format(new Date()));
            chatMessage.setMe(false);
            messageET.setText("");
            displayMessage(chatMessage);

            // speak
            latestChatMsg = chatMessage;
            Intent checkIntent = new Intent();
            checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
            startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);

            if(s.contains("Shops found selling ")){
                Intent i = new Intent(getApplicationContext(),ShopsActivity.class);
                productType =s.substring(s.indexOf("selling ")+ 8, s.indexOf(" at "));
                redinfedProductType = productType.toLowerCase();
                redinfedProductType = productType.replace("&","N");
                mallName =s.substring(s.indexOf("at ")+ 3);
                i.putExtra("productType", redinfedProductType);
                i.putExtra("mallName", mallName);
                startActivity(i);

                ChatMessage cm = new ChatMessage();
                cm.setId(1);//dummy
                cm.setMessage("What else are you looking for?");

                cm.setDate(dateFormat.format(new Date()));
                cm.setMe(false);
                displayMessage(cm);

                list.add(productType +" at "+ mallName);
            }else{
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.audioIO){
            if (voiceSetting == false) {
                SharedPreferences prefs = getSharedPreferences("status", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("status", true);
                editor.apply();
                voiceSetting = true;
                Toast.makeText(this, "Voice Output Enabled", Toast.LENGTH_SHORT).show();
            }else if(voiceSetting == true){
                SharedPreferences prefs = getSharedPreferences("status", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("status", false);
                editor.apply();
                voiceSetting = false;
                Toast.makeText(this, "Voice Output Disabled", Toast.LENGTH_SHORT).show();
            }
        }else if(item.getItemId() == R.id.menuProfile){
            Intent intent = new Intent(getApplicationContext(),MyProfileActivity.class);
            startActivity(intent);
        }else if(item.getItemId() == R.id.menuAbout){
            AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
            builder1.setMessage("Sma is an AI powered ChatBot application that assist Shoppers in Shopping Malls");
            builder1.setCancelable(true);
            AlertDialog alert11 = builder1.create();
            alert11.show();
        }else if(item.getItemId() == R.id.menuContactUs){
            Toast.makeText(this, "Send an email to us", Toast.LENGTH_SHORT).show();
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto","Amar012@hotmail.com", null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Contact Form");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "");
            startActivity(Intent.createChooser(emailIntent, "Send email..."));
        }else if(item.getItemId() == R.id.logOut){
            FirebaseAuth.getInstance().signOut();
            sendtoStart();
        }
        return true;
    }

    public void sendtoStart(){
        startActivity(new Intent(getApplicationContext(),SplashActivity.class));
        finish();
    }
}