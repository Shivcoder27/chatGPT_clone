package com.example.chatgpt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
   RecyclerView recyclerView ;
   TextView welcomeTextView;
   EditText messageEditText;
   ImageButton sendbutton;
   List<Message> messageList;
   MessageAdaptor messageAdaptor ;
    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         messageList = new ArrayList<>();

        recyclerView =findViewById(R.id.recycler_view);
        welcomeTextView = findViewById(R.id.welcome_text);
        messageEditText = findViewById(R.id.message_edit_text);
        sendbutton = findViewById(R.id.send_btn);

        messageAdaptor = new MessageAdaptor(messageList);
        recyclerView.setAdapter(messageAdaptor);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);

        sendbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String question = messageEditText.getText().toString();
                addToChat(question,Message.SENT_BY_ME);
                messageEditText.setText("");
                callApi(question);
                welcomeTextView.setVisibility(View.GONE);
            }
        });

    }
    void addToChat(String message,String sentBy){
       runOnUiThread(new Runnable() {
           @Override
           public void run() {
               messageList.add(new Message(message,sentBy));
               messageAdaptor.notifyDataSetChanged();
               recyclerView.smoothScrollToPosition(messageAdaptor.getItemCount());
           }
       });
    }
    void addResponse(String response){
        addToChat(response,Message.SENT_BY_BOT);
    }
    void callApi(String question){
        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put("model","text-davinci-003");
            jsonObject.put("prompt",question);
            jsonObject.put("max_tokens",4000);
            jsonObject.put("temperature",0);

        }catch (JSONException e){
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(jsonObject.toString(),JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/completions")
                .header("Authorization","Bearer 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")
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
     addResponse("failed to load response "+e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if(response.isSuccessful()){
                            JSONObject jsonObject = null;
                            try {
                                jsonObject = new JSONObject(response.body().string());
                                JSONArray jsonArray = jsonObject.getJSONArray("choices");
                                String result = jsonArray.getJSONObject(0).getString("text");
                                addResponse(result.trim());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }else{
                            addResponse("failed to load response due to"+ response.body().string());
                        }
            }
        });
    }
}