package com.example.leehwanwoong.droneappcontroller;

import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    String IP;
    int PORT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        patchEOFException();

        final EditText IPtext = (EditText)findViewById(R.id.editText);
        final EditText PORTtext = (EditText)findViewById(R.id.editText2);
        final Button connectButton = (Button)findViewById(R.id.button);

        connectButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                IP = IPtext.getText().toString();
                PORT = Integer.parseInt(PORTtext.getText().toString());
                String temp = IP+" "+PORT;

                Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
                intent.putExtra("temp", temp);
                startActivity(intent);

                finish();
            }
        });
    }
    private void patchEOFException() {

        System.setProperty("http.keepAlive", "false");

    }
}
