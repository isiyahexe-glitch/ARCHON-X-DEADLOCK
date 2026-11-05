package com.deadlock.archonx;

import androidx.appcompat.app.AppCompatActivity;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    EditText targetNumber;
    Button btnBan;
    ImageView logoImage;
    TextView consoleText;
    ProgressBar loadingBar;
    
    // ඔයාගේ විස්තර
    String GITHUB_USER = "isiyahexe-glitch"; 
    String GITHUB_REPO = "ARCHON-X-DEADLOCK";
    String GITHUB_TOKEN = "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"; 

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        targetNumber = findViewById(R.id.targetNumber);
        btnBan = findViewById(R.id.btnBan);
        logoImage = findViewById(R.id.logoImage);
        consoleText = findViewById(R.id.consoleText);
        loadingBar = findViewById(R.id.loadingBar);

        // 3D Rotation Animation එක (ලෝගෝ එක කැරකෙනවා වගේ)
        ObjectAnimator rotation3D = ObjectAnimator.ofFloat(logoImage, "rotationY", 0f, 360f);
        rotation3D.setDuration(4000); 
        rotation3D.setRepeatCount(ValueAnimator.INFINITE);
        rotation3D.setInterpolator(new LinearInterpolator());
        rotation3D.start();

        btnBan.setOnClickListener(v -> {
            String target = targetNumber.getText().toString();
            if(target.isEmpty()) {
                Toast.makeText(this, "Enter Target Number", Toast.LENGTH_SHORT).show();
                return;
            }
            startHackingProcess(target);
        });
    }

    private void startHackingProcess(String target) {
        btnBan.setEnabled(false);
        loadingBar.setVisibility(View.VISIBLE);
        consoleText.setTextColor(getResources().getColor(android.R.color.holo_green_light));

        // ටර්මිනල් එකේ අකුරු එනවා වගේ පෙන්නන්න
        String[] logs = {
            "> Initializing ARCHON_X...",
            "> Bypassing Firebase Routing...",
            "> Establishing Secure Tunnel...",
            "> Spoofing Headers...",
            "> Injecting Report Payloads...",
            "[!] Target Locked: " + target
        };

        handler.postDelayed(() -> consoleText.setText(logs[0]), 500);
        handler.postDelayed(() -> consoleText.setText(logs[1]), 1200);
        handler.postDelayed(() -> consoleText.setText(logs[2]), 2000);
        handler.postDelayed(() -> consoleText.setText(logs[3]), 2800);
        handler.postDelayed(() -> consoleText.setText(logs[4]), 3500);
        handler.postDelayed(() -> consoleText.setText(logs[5]), 4000);

        // ඇත්තම වැඩේ (GitHub Actions Trigger)
        sendCommandToGitHub(target);
    }

    private void sendCommandToGitHub(String target) {
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
                    handler.removeCallbacksAndMessages(null); // පරණ ටයිපින් නවත්තන්න
                    loadingBar.setVisibility(View.GONE);
                    btnBan.setEnabled(true);

                    if (responseCode == 204 || responseCode == 200) {
                        consoleText.setTextColor(getResources().getColor(android.R.color.holo_green_light));
                        consoleText.setText("[+] SUCCESS! Command Dispatched.\nCloud Engine is executing...\nCheck GitHub Actions Logs for Report Count.");
                        Toast.makeText(MainActivity.this, "Target Locked!", Toast.LENGTH_LONG).show();
                    } else {
                        consoleText.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                        consoleText.setText("[ERROR] GitHub API: " + responseCode + "\nCheck your Token or Internet.");
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    handler.removeCallbacksAndMessages(null);
                    loadingBar.setVisibility(View.GONE);
                    btnBan.setEnabled(true);
                    consoleText.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                    consoleText.setText("[CRITICAL] Connection Failed!\nServer unreachable.");
                });
            }
        }).start();
    }
}
