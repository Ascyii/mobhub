package de.ascyii.mobhub;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.view.Gravity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Base64;
import android.util.TypedValue;
import android.view.ViewGroup;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.Executors;
import org.json.JSONObject;

public class MainActivity extends Activity {
    private EditText urlInput;
    private EditText usernameInput;
    private EditText passwordInput;
    private TextView contentDisplay;
    private TextView statusText;
    private TextView lastFileText;
    
    private static final String CREDENTIALS_FILE = "webdav_credentials.json";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create main layout
        ScrollView scrollView = new ScrollView(this);
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(40, 40, 40, 40);
        mainLayout.setBackgroundColor(Color.parseColor("#1a1a2e"));

        // Title
        TextView title = new TextView(this);
        title.setText("📁 WebDAV File Viewer");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
        title.setTextColor(Color.parseColor("#00ff88"));
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 10);
        mainLayout.addView(title);

        // Last file indicator
        lastFileText = new TextView(this);
        lastFileText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        lastFileText.setTextColor(Color.parseColor("#888888"));
        lastFileText.setGravity(Gravity.CENTER);
        lastFileText.setPadding(0, 0, 0, 20);
        mainLayout.addView(lastFileText);

        // URL Input
        TextView urlLabel = createLabel("WebDAV File URL:");
        mainLayout.addView(urlLabel);
        
        urlInput = createInput("https://example.com/webdav/file.txt");
        mainLayout.addView(urlInput);

        // Username Input
        TextView userLabel = createLabel("Username:");
        mainLayout.addView(userLabel);
        
        usernameInput = createInput("your-username");
        mainLayout.addView(usernameInput);

        // Password Input
        TextView passLabel = createLabel("Password:");
        mainLayout.addView(passLabel);
        
        passwordInput = createInput("");
        passwordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | 
                                   android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mainLayout.addView(passwordInput);

        // Buttons Layout
        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        buttonLayoutParams.setMargins(0, 30, 0, 30);
        buttonLayout.setLayoutParams(buttonLayoutParams);

        // Fetch Button
        Button fetchButton = new Button(this);
        fetchButton.setText("🚀 FETCH");
        fetchButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        fetchButton.setTextColor(Color.WHITE);
        fetchButton.setBackgroundColor(Color.parseColor("#00ff88"));
        fetchButton.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams fetchParams = new LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
        );
        fetchParams.setMargins(0, 0, 10, 0);
        fetchButton.setLayoutParams(fetchParams);
        fetchButton.setOnClickListener(v -> fetchWebDavFile());
        buttonLayout.addView(fetchButton);

        // Save Button
        Button saveButton = new Button(this);
        saveButton.setText("💾 SAVE");
        saveButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        saveButton.setTextColor(Color.WHITE);
        saveButton.setBackgroundColor(Color.parseColor("#ff6b6b"));
        saveButton.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams saveParams = new LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
        );
        saveParams.setMargins(10, 0, 0, 0);
        saveButton.setLayoutParams(saveParams);
        saveButton.setOnClickListener(v -> saveCredentials());
        buttonLayout.addView(saveButton);

        mainLayout.addView(buttonLayout);

        // Clear Button
        Button clearButton = new Button(this);
        clearButton.setText("🗑️ Clear Saved Credentials");
        clearButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        clearButton.setTextColor(Color.parseColor("#888888"));
        clearButton.setBackgroundColor(Color.parseColor("#2a2a3e"));
        LinearLayout.LayoutParams clearParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        clearParams.setMargins(0, 0, 0, 20);
        clearButton.setLayoutParams(clearParams);
        clearButton.setOnClickListener(v -> clearCredentials());
        mainLayout.addView(clearButton);

        // Status Text
        statusText = new TextView(this);
        statusText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        statusText.setTextColor(Color.parseColor("#ffd700"));
        statusText.setGravity(Gravity.CENTER);
        statusText.setPadding(0, 0, 0, 20);
        mainLayout.addView(statusText);

        // Content Display
        TextView contentLabel = createLabel("File Contents:");
        mainLayout.addView(contentLabel);
        
        contentDisplay = new TextView(this);
        contentDisplay.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        contentDisplay.setTextColor(Color.parseColor("#e0e0e0"));
        contentDisplay.setBackgroundColor(Color.parseColor("#0f0f1e"));
        contentDisplay.setPadding(20, 20, 20, 20);
        contentDisplay.setTypeface(Typeface.MONOSPACE);
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        contentParams.setMargins(0, 10, 0, 0);
        contentDisplay.setLayoutParams(contentParams);
        mainLayout.addView(contentDisplay);

        scrollView.addView(mainLayout);
        setContentView(scrollView);

        // Load saved credentials and auto-fetch on startup
        loadCredentials();
    }

    private TextView createLabel(String text) {
        TextView label = new TextView(this);
        label.setText(text);
        label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        label.setTextColor(Color.parseColor("#00d9ff"));
        label.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 20, 0, 8);
        label.setLayoutParams(params);
        return label;
    }

    private EditText createInput(String hint) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.parseColor("#666666"));
        input.setBackgroundColor(Color.parseColor("#16213e"));
        input.setPadding(20, 20, 20, 20);
        return input;
    }

    private void saveCredentials() {
        try {
            JSONObject credentials = new JSONObject();
            credentials.put("url", urlInput.getText().toString());
            credentials.put("username", usernameInput.getText().toString());
            credentials.put("password", passwordInput.getText().toString());
            credentials.put("timestamp", System.currentTimeMillis());

            File file = new File(getFilesDir(), CREDENTIALS_FILE);
            FileWriter writer = new FileWriter(file);
            writer.write(credentials.toString());
            writer.close();

            statusText.setText("✅ Credentials saved locally!");
            statusText.postDelayed(() -> statusText.setText(""), 3000);
        } catch (Exception e) {
            statusText.setText("❌ Failed to save: " + e.getMessage());
        }
    }

    private void loadCredentials() {
        try {
            File file = new File(getFilesDir(), CREDENTIALS_FILE);
            if (!file.exists()) {
                lastFileText.setText("No saved credentials");
                return;
            }

            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
            reader.close();

            JSONObject credentials = new JSONObject(json.toString());
            String url = credentials.getString("url");
            String username = credentials.getString("username");
            String password = credentials.getString("password");

            urlInput.setText(url);
            usernameInput.setText(username);
            passwordInput.setText(password);

            // Extract filename from URL for display
            String fileName = url.substring(url.lastIndexOf('/') + 1);
            lastFileText.setText("📄 Last file: " + fileName);

            // Auto-fetch the last file
            statusText.setText("🔄 Auto-loading last file...");
            statusText.postDelayed(() -> fetchWebDavFile(), 500);

        } catch (Exception e) {
            lastFileText.setText("No saved credentials");
        }
    }

    private void clearCredentials() {
        try {
            File file = new File(getFilesDir(), CREDENTIALS_FILE);
            if (file.exists()) {
                file.delete();
                urlInput.setText("");
                usernameInput.setText("");
                passwordInput.setText("");
                contentDisplay.setText("");
                lastFileText.setText("No saved credentials");
                statusText.setText("🗑️ Credentials cleared");
                statusText.postDelayed(() -> statusText.setText(""), 3000);
            } else {
                statusText.setText("No credentials to clear");
                statusText.postDelayed(() -> statusText.setText(""), 3000);
            }
        } catch (Exception e) {
            statusText.setText("❌ Error clearing: " + e.getMessage());
        }
    }

    private void fetchWebDavFile() {
        String urlString = urlInput.getText().toString().trim();
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        if (urlString.isEmpty()) {
            statusText.setText("❌ Please enter a URL");
            return;
        }

        statusText.setText("⏳ Fetching file...");
        contentDisplay.setText("");

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                
                // Add Basic Auth if credentials provided
                if (!username.isEmpty() && !password.isEmpty()) {
                    String credentials = username + ":" + password;
                    String encodedAuth = Base64.encodeToString(
                        credentials.getBytes(), 
                        Base64.NO_WRAP
                    );
                    connection.setRequestProperty("Authorization", "Basic " + encodedAuth);
                }

                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                int responseCode = connection.getResponseCode();
                
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream())
                    );
                    StringBuilder content = new StringBuilder();
                    String line;
                    
                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\n");
                    }
                    reader.close();
                    
                    String fileContent = content.toString();
                    updateUI("✅ File loaded successfully!", fileContent);
                } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    updateUI("🔒 Authentication failed - check credentials", "");
                } else {
                    updateUI("❌ Error: HTTP " + responseCode, "");
                }
                
                connection.disconnect();
            } catch (Exception e) {
                updateUI("❌ Error: " + e.getMessage(), "");
            }
        });
    }

    private void updateUI(String status, String content) {
        new Handler(Looper.getMainLooper()).post(() -> {
            statusText.setText(status);
            contentDisplay.setText(content.isEmpty() ? "No content to display" : content);
        });
    }
}
