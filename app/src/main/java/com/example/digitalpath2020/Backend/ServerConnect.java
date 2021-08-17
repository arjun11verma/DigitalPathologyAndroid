/**
 * This is a class for the connection to the Python server
 *
 * @author Arjun Verma
 * @version 1.0
 */

package com.example.digitalpath2020.Backend;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.digitalpath2020.MainActivity;
import com.example.digitalpath2020.R;
import com.example.digitalpath2020.Views.ConfirmCameraView;
import com.example.digitalpath2020.Views.PostUploadView;
import com.example.digitalpath2020.Views.UploadView;

import org.json.JSONException;
import org.json.JSONObject;

public class ServerConnect {
    private RequestQueue queue; // Volley request queue
    private String serverUrl; // Server url
    private MainActivity activity; // Instance of the main activity

    /**
     * Constructor for the ServerConnect class
     *
     * @param activity Instance of the main activity
     */
    public ServerConnect(MainActivity activity) {
        this.activity = activity;
        queue = Volley.newRequestQueue(this.activity);
    }

    /**
     * Makes a post to the Python server using the Android Volley library
     * Determines whether to the post was successful or not
     *
     * @param postObject JSON object to be sent to the server
     */
    public void sendImages(JSONObject postObject) {
        String postUrl = "https://" + serverUrl + ".ngrok.io" + "/stitch_images";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, postUrl, postObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    activity.setImageCurrentTime(response.getString("timeStamp"));
                    if ((response.getString("response")).equals("N")) {
                        activity.changeView(new UploadView(activity, R.layout.post_upload_activity, "Your stitching was NOT successful, please try again.", null));
                    } else {
                        activity.changeView(new UploadView(activity, R.layout.post_upload_activity, "Your stitching was successful! If you think this image is valid, upload it. Otherwise, please take more slide images to generate another stitch.", (String) response.get("imageData")));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                activity.changeView(new UploadView(activity, R.layout.post_upload_activity, "Your upload was NOT successful, please try again.", null));
                System.out.println(error);
            }
        });

        request.setRetryPolicy(new DefaultRetryPolicy(600000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }

    public void sendUpload(final JSONObject postObject) {
        String postUrl = serverUrl + "/upload_image";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, postUrl, postObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                String returnString = "unsuccessfully";
                try {
                    if (response.get("response").equals("Y")) {
                        returnString = "successfully";
                    }
                } catch (JSONException e) {

                }

                try {
                    if (postObject.get("status").equals("Y")) {
                        activity.changeView(new PostUploadView(activity, R.layout.final_upload_activity, "Your image was uploaded " + returnString + ". You can either logout or choose to take more slide images"));
                    } else {
                        activity.getMatList().clear();
                        activity.changeView(new ConfirmCameraView(activity, R.layout.confirm_camera_activity));
                    }
                } catch (JSONException e) {

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    if (postObject.get("status").equals("Y")) {
                        activity.changeView(new PostUploadView(activity, R.layout.final_upload_activity, "Your image was uploaded unsuccessfully. You can either logout or choose to take more slide images"));
                    } else {
                        activity.getMatList().clear();
                        activity.changeView(new ConfirmCameraView(activity, R.layout.confirm_camera_activity));
                    }
                } catch (JSONException e) {

                }
            }
        });

        request.setRetryPolicy(new DefaultRetryPolicy(600000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);
    }

    // Getters and setters for the fields
    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getServerUrl() {
        return serverUrl;
    }
}
