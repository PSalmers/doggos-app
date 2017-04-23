package ca.patsworld.freshdoggos;

import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class HTTPRequestMaker {

    void sendJsonToServer(final JSONObject jsonData) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run()
            {
                makePostRequest(jsonData);
            }
        });
        thread.start();
    }

    private void makePostRequest(JSONObject jsonData) {
        HttpURLConnection connection = null;
        try {
            connection = getHttpURLConnection();
            sendRequest(jsonData, connection);
            getResponse(connection);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @NonNull
    private static HttpURLConnection getHttpURLConnection() throws IOException {
        URL url = new URL("http://10.0.2.2:3000/");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        return connection;
    }

    private static void sendRequest(JSONObject jsonData, HttpURLConnection connection) throws IOException {
        DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
        outputStream.writeBytes(jsonData.toString());
        Log.v("JSON being sent: ", jsonData.toString());
        outputStream.flush();
        outputStream.close();
    }

    private static void getResponse(HttpURLConnection connection) throws IOException {
        InputStream inputStream = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        StringBuilder response = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            response.append(line);
            response.append('\n');
        }
        reader.close();
        String responseStr = response.toString();
        Log.d("Server response", responseStr);
    }
}
