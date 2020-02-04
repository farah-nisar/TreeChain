package com.treechain.treechain;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Logger;

public class TreeProjectActivity extends AppCompatActivity {

    private String ServerURL = "http://10.0.2.2:5000/api/v1/projects/";
    private String ServerPlantTree = "http://10.0.2.2:5000/api/v1/tree";
    private String ServerVerifyTree = "http://10.0.2.2:5000/api/v1/verify";
    private Logger logger = Logger.getLogger(TreeProjectActivity.class.getName());
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int ACTION_PLANT = 2;
    static final int ACTION_VERIFY = 3;
    private int currentAction = 0;
    private int projectID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        projectID = getIntent().getIntExtra("PROJECT_ID", 1);


        setContentView(R.layout.activity_tree_project);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String queryURL = ServerURL + String.valueOf(projectID);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, queryURL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                logger.info(response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                logger.warning(error.getLocalizedMessage());
            }
        });


        MapsActivity.queue.add(jsonObjectRequest);


        // Fetch project information
        Button fab = findViewById(R.id.button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent(ACTION_PLANT);
                currentAction = ACTION_PLANT;
            }
        });
        fab = findViewById(R.id.button2);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent(ACTION_VERIFY);
                currentAction = ACTION_VERIFY;
            }
        });
    }


    private void plantTree() {
        logger.info("Plant a Tree");
        postServer(ServerPlantTree);
    }

    private void verifyTree() {
        logger.info("Verify a Tree");
        startVerifyActivity(projectID);
        postServer(ServerVerifyTree);
    }

    private void postServer(String url) {
        JSONObject request = new JSONObject();
        try {
            request.put("x", -31);
            request.put("y", 54);
            request.put("id", projectID);
            request.put("num", 1);
        } catch (JSONException e) {
            logger.warning(e.getLocalizedMessage());
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, request, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Context context = getApplicationContext();
                CharSequence text = "Tree Added";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();


                logger.info(response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                logger.warning(error.getLocalizedMessage());
            }
        });

        MapsActivity.queue.add(jsonObjectRequest);
    }

    private void dispatchTakePictureIntent(int action) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra("ACTION", action);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        logger.info("OnActivity " + String.valueOf(requestCode) + " " + String.valueOf(resultCode));
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (currentAction == ACTION_PLANT) {
                plantTree();
            } else if (currentAction == ACTION_VERIFY) {
                verifyTree();
            }
        }
    }

    public void startVerifyActivity(int id) {
        Intent intent = new Intent(this, VerifyActivity.class);
        intent.putExtra("PROJECT_ID", id);
        startActivity(intent);

    }
}
