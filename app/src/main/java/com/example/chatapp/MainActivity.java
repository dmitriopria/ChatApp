package com.example.chatapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private static int SIGN_IN_CODE = 1;
    private RelativeLayout activity_main;
    private FirebaseListAdapter<Message> adapter;
    private FloatingActionButton sendBtn;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_IN_CODE) {
            if (requestCode == RESULT_OK) {
                Snackbar.make(activity_main, "You are authorized", Snackbar.LENGTH_LONG).show();
                displayAllMessages();
            } else {
                Snackbar.make(activity_main, "You are not authorized", Snackbar.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity_main = findViewById(R.id.activity_main);
        sendBtn = findViewById(R.id.btnSend);
        sendBtn.setOnClickListener(v -> {
            EditText textField = findViewById(R.id.messageField);
            if(textField.getText().toString() == "")
                return;
            FirebaseDatabase.getInstance().getReference().push().setValue(
                    new Message(FirebaseAuth.getInstance().getCurrentUser().getEmail(),
                            textField.getText().toString()));
            textField.setText("");
        });

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), SIGN_IN_CODE);
        } else {
            Snackbar.make(activity_main, "You are authorized", Snackbar.LENGTH_LONG).show();
            displayAllMessages();
        }
    }

    private void displayAllMessages() {
        ListView messages = findViewById(R.id.list_of_messages);
        FirebaseListOptions.Builder<Message> builder = new FirebaseListOptions.Builder<>();
        builder
                .setLayout(R.layout.list_item)
                .setQuery(FirebaseDatabase.getInstance().getReference(), Message.class)
                .setLifecycleOwner(this.getLifecycle());

        adapter = new FirebaseListAdapter<Message>(builder.build()) {
            @Override
            protected void populateView(@NonNull View v, @NonNull Message model, int position) {
                TextView mess_user, mess_time, mess_text;
                mess_user = v.findViewById(R.id.message_user);
                mess_time = v.findViewById(R.id.message_time);
                mess_text = v.findViewById(R.id.message_text);

                mess_user.setText(model.getUserName());
                mess_text.setText(model.getTextMessage());
                mess_time.setText(DateFormat.format("dd-mm-yyyy HH:mm:ss", model.getMessageTime()));
            }
        };
        messages.setAdapter(adapter);
    }
}