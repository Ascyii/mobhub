package de.ascyii.mobhub;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private TextView headersText;
    private EditText urlInput;
    private Button fetchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        headersText = findViewById(R.id.headersText);
        urlInput = findViewById(R.id.urlInput);
        fetchButton = findViewById(R.id.fetchButton);

        fetchButton.setOnClickListener(v -> fetchHeaders());
        
        // Auto-fetch on startup
        fetchHeaders();
    }

    private void fetchHeaders() {
        String urlString = urlInput.getText().toString().trim();
        if (urlString.isEmpty()) {
            urlString = "https://www.google.com";
        }
        
        // Ensure URL has protocol
        if (!urlString.startsWith("http://") && !urlString.startsWith("https://")) {
            urlString = "https://" + urlString;
        }
        
        headersText.setText("Fetching headers from:\n" + urlString + "\n\nPlease wait...");
        
        final String finalUrl = urlString;
        Executors.newSingleThreadExecutor().execute(() -> {
            StringBuilder result = new StringBuilder();
            HttpURLConnection connection = null;
            
            try {
                // First test DNS resolution
                URL url = new URL(finalUrl);
                String host = url.getHost();
                
                result.append("=== DNS Resolution ===\n");
                result.append("Host: ").append(host).append("\n");
                
                try {
                    InetAddress address = InetAddress.getByName(host);
                    result.append("IP: ").append(address.getHostAddress()).append("\n\n");
                } catch (Exception e) {
                    result.append("DNS ERROR: ").append(e.getMessage()).append("\n\n");
                    throw e;
                }
                
                // Now try HTTP connection
                result.append("=== HTTP Request ===\n");
                result.append("URL: ").append(finalUrl).append("\n");
                
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("HEAD");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setInstanceFollowRedirects(true);
                connection.setRequestProperty("User-Agent", "MobHub/1.0");
                
                // Force connection
                connection.connect();
                
                int responseCode = connection.getResponseCode();
                result.append("\n=== HTTP Response ===\n");
                result.append("Status: ").append(responseCode).append(" ")
                      .append(connection.getResponseMessage()).append("\n\n");
                
                result.append("=== Response Headers ===\n");
                Map<String, List<String>> headers = connection.getHeaderFields();
                
                for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                    String key = entry.getKey();
                    if (key == null) key = "Status";
                    
                    for (String value : entry.getValue()) {
                        result.append(key).append(": ").append(value).append("\n");
                    }
                }
                
            } catch (java.net.UnknownHostException e) {
                result.append("\n❌ DNS RESOLUTION FAILED\n");
                result.append("Cannot resolve hostname: ").append(e.getMessage()).append("\n\n");
                result.append("Possible causes:\n");
                result.append("- No internet connection\n");
                result.append("- DNS server unavailable\n");
                result.append("- Invalid hostname\n");
            } catch (java.net.ConnectException e) {
                result.append("\n❌ CONNECTION REFUSED\n");
                result.append(e.getMessage()).append("\n\n");
                result.append("Possible causes:\n");
                result.append("- Network restrictions on device\n");
                result.append("- Firewall blocking connection\n");
                result.append("- Server not accepting connections\n");
            } catch (javax.net.ssl.SSLException e) {
                result.append("\n❌ SSL/TLS ERROR\n");
                result.append(e.getMessage()).append("\n\n");
                result.append("Try using http:// instead of https://\n");
            } catch (Exception e) {
                result.append("\n❌ ERROR\n");
                result.append("Type: ").append(e.getClass().getSimpleName()).append("\n");
                result.append("Message: ").append(e.getMessage()).append("\n");
                result.append("\nStack trace:\n");
                for (StackTraceElement element : e.getStackTrace()) {
                    result.append("  ").append(element.toString()).append("\n");
                }
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            
            final String finalResult = result.toString();
            runOnUiThread(() -> headersText.setText(finalResult));
        });
    }
}
