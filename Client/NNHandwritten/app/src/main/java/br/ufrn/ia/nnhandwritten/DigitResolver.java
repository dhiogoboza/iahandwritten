package br.ufrn.ia.nnhandwritten;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dhiogoboza on 20/06/17.
 */

public class DigitResolver {


    private static final String TAG = "DigitResolver";

    private static String serverIp;
    private static String serverPort;


    public static int resolve(Context context, final int[] image_data, final TextView textViewDigit, final TextView textViewMatch) {

        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "http://" + serverIp + ":" + serverPort;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        String[] responseSplit = response.split(",");

                        textViewDigit.setText(responseSplit[0]);
                        textViewMatch.setText(responseSplit[1]);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Request error", error);

                        textViewDigit.setText("X");
                        textViewMatch.setText("Request error");
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();

                StringBuilder imageDataStr = new StringBuilder();

                for (int i = 0; i < image_data.length; i++) {
                    imageDataStr.append(image_data[i]);
                }

                Log.d(TAG, "imageDataStr.toString(): " + imageDataStr.toString());

                params.put("image", imageDataStr.toString());

                return params;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);

        queue.start();

        return 0;
    }

    public static void init(String serverIp, String serverPort) {
        DigitResolver.serverIp = serverIp;
        DigitResolver.serverPort = serverPort;
    }
}
