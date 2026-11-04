package com.deadlock.archonx;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    EditText targetNumber;
    Button btnBan;
    
    String GITHUB_USER = "isiyahexe-glitch"; 
    String GITHUB_REPO = "ARCHONX";
    String GITHUB_TOKEN = "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"; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        targetNumber = findViewById(R.id.targetNumber);
        btnBan = findViewById(R.id.btnBan);

        btnBan.setOnClickListener(v -> {
            String target = targetNumber.getText().toString();
            if(target.isEmpty()) {
                Toast.makeText(this, "Enter Target Number", Toast.LENGTH_SHORT).show();
                return;
            }
            sendCommand(target);
        });
    }

    private void sendCommand(String target) {
        new Thread(() -> {
            try {
                URL url = new URL("https://api.github.com/repos/" + GITHUB_USER + "/" + GITHUB_REPO + "/actions/workflows/ban.yml/dispatches");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + GITHUB_TOKEN);
                conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String jsonInputString = "{\"ref\":\"main\", \"inputs\":{\"target\":\"" + target + "\"}}";
                
                try(OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
                
                int responseCode = conn.getResponseCode();
                runOnUiThread(() -> {
                    if (responseCode == 204 || responseCode == 200) {
                        Toast.makeText(MainActivity.this, "Cloud Attack Launched!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, "GitHub Error: " + responseCode, Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Connection Failed!", Toast.LENGTH_LONG).show());
            }
        }).start();
    }
}
