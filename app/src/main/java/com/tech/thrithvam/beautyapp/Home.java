package com.tech.thrithvam.beautyapp;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Home extends AppCompatActivity {
    List<String> spinnerArray;
    List<String> spinnerArray4sType;
    ArrayAdapter<String> adapter;
    ArrayAdapter<String> adapter4sType;
    Spinner serviceItems;
    Spinner serviceTypes;
    private ProgressDialog pDialog;
    Dictionary<String, String> serviceAndCode=new Hashtable<String, String>();
    Dictionary<String, String> sTypeAndCode=new Hashtable<String, String>();
    JSONArray jsonArray;
    JSONArray jsonArray4types;
    boolean serviceLoadedFlag =false;
    boolean sTypeLoadedFlag =false;
    boolean first_Time=true;
    boolean serverError=false;
    HorizontalScrollView offersHorizontal;
    LinearLayout linr;
    DatabaseHandler db= new DatabaseHandler(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        serviceItems = (Spinner) findViewById(R.id.serviceSpinner);
        serviceTypes = (Spinner) findViewById(R.id.sTypeSpinner);
        spinnerArray4sType = new ArrayList<String>();
        spinnerArray = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerArray);
        adapter4sType = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerArray4sType);
        offersHorizontal = (HorizontalScrollView) findViewById(R.id.offersHorizontal);
        linr=new LinearLayout(Home.this);
        pDialog = new ProgressDialog(Home.this);
        if(!isOnline())
        {                                                       //exit from app if no connectivity
          //  pDialog = new ProgressDialog(Home.this);
            pDialog.setMessage(getResources().getString(R.string.turnNetOn));// "Please turn on internet and come again....");
            pDialog.setCancelable(false);
            pDialog.show();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            }, 3000);
        }
        else{
            new GetServices().execute();            //Load from server if connectivity is 'on'
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                      // Showing progress dialog
                   /* if (pDialog.isShowing())
                        pDialog.dismiss();*/
                    pDialog.setMessage(getResources().getString(R.string.wait));
                    pDialog.setCancelable(false);
                    if((!serviceLoadedFlag || !sTypeLoadedFlag) && !serverError)
                        pDialog.show();
                }
            }, 1000);

        }
        //prepare serviceSpinner listener
        serviceItems.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int pos, long arg3) {
                if (!isOnline()) {                                                       //exit from app if no connectivity
                    pDialog = new ProgressDialog(Home.this);
                    pDialog.setMessage(getResources().getString(R.string.turnNetOn));
                    //   pDialog.setCancelable(false);
                    pDialog.show();
                  /*  Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.addCategory(Intent.CATEGORY_HOME);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    }, 3000);*/
                } else {
                    new GetServiceTypes().execute(serviceAndCode.get(serviceItems.getSelectedItem()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                Toast.makeText(getApplicationContext(), "Nothing selected :P", Toast.LENGTH_LONG).show();
            }
        });
    } ////////////onCreate end

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


    //-----------------Get services-------------------------------
    private class GetServices extends AsyncTask<Void, Void, Void> {
        int status;
        StringBuilder sb;
        String strJson;
        @Override
        protected void onPreExecute() {             //start to show progress bar<please wait>
            super.onPreExecute();
        /*    pDialog = new ProgressDialog(Home.this);     // Showing progress dialog
           if (pDialog.isShowing())
                        pDialog.dismiss();
            pDialog.setMessage(getResources().getString(R.string.wait));
            pDialog.setCancelable(false);
            pDialog.show();*/
        }
         @Override
        protected Void doInBackground(Void... arg0) {           //loading JSONs from server in background
            String url=getResources().getString(R.string.url)+"GetServices.asmx/GetServicesList";
            HttpURLConnection c = null;
           // String strJson="{\"Services\":";//[{\"Code\":\"HC\",\"Name\":\"Hair Cut\",\"CreatedDate\":\"\\/Date(1445858760000)\\/\",\"CreatedBy\":\"Admin\"},{\"Code\":\"HCL\",\"Name\":\"Hair Colouring\",\"CreatedDate\":\"\\/Date(1445858760000)\\/\",\"CreatedBy\":\"Admin\"}]}";
            try {
                URL u = new URL(url);
                c = (HttpURLConnection) u.openConnection();
                c.setRequestMethod("POST");
                c.setRequestProperty("Content-type", "application/json");// charset=utf-8");
                c.setRequestProperty("Content-length", "0");
                c.setUseCaches(false);
                c.setAllowUserInteraction(false);
                c.setConnectTimeout(5000);
                c.setReadTimeout(5000);
                c.connect();
                status = c.getResponseCode();
                switch (status) {
                    case 200:                               //no break; ???!!!
                    case 201:
                        BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                        sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line+"\n");
                        }
                        br.close();
                        int a=sb.indexOf("[");
                        int b=sb.lastIndexOf("]");
                        strJson=sb.substring(a,b+1);//.toString();
                        strJson="{\"Services\":" + strJson.replace("\\\"","\"") + "}";
                }
            } catch (IOException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }  finally {
                if (c != null) {
                    try {
                        c.disconnect();
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
             if(strJson!=null){
 //for spinner parsing JSON
            try {
                JSONObject jsonRootObject = new JSONObject(strJson);
                jsonArray = jsonRootObject.optJSONArray("Services");//Get the instance of JSONArray that contains JSONObjects
                for (int i = 0; i < jsonArray.length(); i++) {          //Iterate the jsonArray and print the info of JSONObjects
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    serviceAndCode.put(jsonObject.optString("Name"), jsonObject.optString("Code"));
                    String serviceName = jsonObject.optString("Name");
                    spinnerArray.add(serviceName);                                                      //serviceSpinner populate
                }
                serviceLoadedFlag =true;
            } catch (JSONException e) {
            }}
             else{
                 serviceLoadedFlag =false;}
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
          /*  if (pDialog.isShowing())
                pDialog.dismiss();*/
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            serviceItems.setAdapter(adapter);
            if(!serviceLoadedFlag){
                if (pDialog.isShowing())
                    pDialog.dismiss();
                Toast.makeText(Home.this,status,Toast.LENGTH_LONG).show();
                Toast.makeText(getApplicationContext(), "Server Error!!", Toast.LENGTH_LONG).show();
                serverError=true;}

            }
    }
    //-----------------Get styles based on services---------------------------
    private class GetServiceTypes extends AsyncTask<String, Void, Void> {
        int status;
        StringBuilder sb;
        String strJson;
        String postData;
        @Override
        protected void onPreExecute() {             //start to show progress bar<please wait>
            super.onPreExecute();
            adapter4sType.clear();                       //clearing spinner for service types
            serviceTypes.setAdapter(adapter4sType);
            sTypeAndCode.put("Any", "ANY");
            adapter4sType.add("Any");
          /*  pDialog = new ProgressDialog(Home.this);     // Showing progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            pDialog.setMessage(getResources().getString(R.string.wait)+"for service types");//remove string!!
            pDialog.setCancelable(false);
            pDialog.show();*/
        }
        @Override
        protected Void doInBackground(String... sCode) {
                 String url=getResources().getString(R.string.url)+"GetServices.asmx/GetServiceTypes";
            HttpURLConnection c = null;
            try {
                postData="{\"ServiceCode\":\""+sCode[0]+"\"}";
                URL u = new URL(url);
                c = (HttpURLConnection) u.openConnection();
                c.setRequestMethod("POST");
                c.setRequestProperty("Content-type", "application/json");
                c.setRequestProperty("Content-length", Integer.toString(postData.length()));
                c.setDoInput(true);
                c.setDoOutput(true);
                c.setUseCaches(false);
                // c.setAllowUserInteraction(false);
                c.setConnectTimeout(5000);
                c.setReadTimeout(5000);
                // c.setRequestProperty("Content-Language", "en-US");
                DataOutputStream wr = new DataOutputStream (c.getOutputStream ());
                wr.writeBytes(postData);
                wr.flush();
                wr.close();
             // c.connect();
                status = c.getResponseCode();
                switch (status) {
                    case 200:                               //no break; ???
                    case 201:
                        BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                        sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line+"\n");
                        }
                        br.close();
                        int a=sb.indexOf("[");
                        int b=sb.lastIndexOf("]");
                        strJson=sb.substring(a,b+1);
                        strJson="{\"ServiceTypes\":" + strJson.replace("\\\"","\"") + "}";
                }
            } catch (IOException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }  finally {
                if (c != null) {
                    try {
                        c.disconnect();
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            //for spinner parsing JSON
            if(strJson!=null)
            {try {
                JSONObject jsonRootObject = new JSONObject(strJson);
                jsonArray4types = jsonRootObject.optJSONArray("ServiceTypes");
                for (int i = 0; i < jsonArray4types.length(); i++) {
                    JSONObject jsonObject = jsonArray4types.getJSONObject(i);
                    sTypeAndCode.put(jsonObject.optString("Name"), jsonObject.optString("Code"));
                    String stypeName = jsonObject.optString("Name");//.toString();
                    spinnerArray4sType.add(stypeName);
                }
            } catch (JSONException ignored) {
            }}
                return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (first_Time) {
                new GetOffers().execute();
                new GetNotifications().execute();
                first_Time = false;
            }

            adapter4sType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            serviceTypes.setAdapter(adapter4sType);
            sTypeLoadedFlag=true;
            if (pDialog.isShowing())
                pDialog.dismiss();
        }
    }
    //-----------------Get offers-------------------------------
    private class GetOffers extends AsyncTask<Void, Void, Void> {
        int status;
        StringBuilder sb;
        String strJson; JSONArray jsonArray4offers;
        ArrayList<String> urls=new ArrayList<String>();
        ArrayList<String> providerCode=new ArrayList<String>();
        ArrayList<String> offerCode=new ArrayList<String>();


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected Void doInBackground(Void... arg0) {           //loading JSONs from server in background
            String url=getResources().getString(R.string.url)+"GetServices.asmx/GetOffers";
            HttpURLConnection c = null;
            try {
                URL u = new URL(url);
                c = (HttpURLConnection) u.openConnection();
                c.setRequestMethod("POST");
                c.setRequestProperty("Content-type", "application/json");// charset=utf-8");
                c.setRequestProperty("Content-length", "0");
                c.setUseCaches(false);
                c.setAllowUserInteraction(false);
                c.setConnectTimeout(5000);
                c.setReadTimeout(5000);
                c.connect();
                status = c.getResponseCode();
                switch (status) {
                    case 200:                               //no break; ???!!!
                    case 201:
                        BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                        sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line+"\n");
                        }
                        br.close();
                        int a=sb.indexOf("[");
                        int b=sb.lastIndexOf("]");
                        strJson=sb.substring(a,b+1);
                        strJson="{\"Offers\":" + strJson.replace("\\\"","\"").replace("\\\\","\\")+ "}";
                }


            } catch (IOException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }  finally {
                if (c != null) {
                    try {
                        c.disconnect();
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            if(strJson!=null){
                //for spinner parsing JSON
                try {
                    JSONObject jsonRootObject = new JSONObject(strJson);
                    jsonArray4offers = jsonRootObject.optJSONArray("Offers");
                    for (int i = 0; i < jsonArray4offers.length(); i++) {
                        JSONObject jsonObject = jsonArray4offers.getJSONObject(i);
                        urls.add(jsonObject.optString("OfferImage", "NOIMAGE"));
                        providerCode.add(jsonObject.optString("ServiceProviderCode"));
                        offerCode.add(jsonObject.optString("ServiceTypeCode")); //to be changed to offercode
                    }

                } catch (JSONException e) {
                }}
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            offersHorizontal.removeAllViews();
            //Toast.makeText(Home.this,providerCode.size(), Toast.LENGTH_LONG).show();
            int size=(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 185, getResources().getDisplayMetrics());//converting dp into pixels
            //String provider, offercode;
            for (int i = 0; i < urls.size(); i++)
            {

                String tempurl=getResources().getString(R.string.url)+urls.get(i).substring(urls.get(i).indexOf("tempImages"));
                ImageView offerImg=new ImageView(Home.this);
                //offerImg.setPadding(10, 10, 0, 0);
                Picasso.with(Home.this)
                        .load(tempurl)
                        .placeholder(R.drawable.loadingimage)
                        .resize(size, offersHorizontal.getHeight())
                        .into(offerImg);
                final String provider=providerCode.get(i);
                final String offercode=offerCode.get(i);
                offerImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Home.this, BeautyParlour.class);
                        intent.putExtra("provider", provider);
                        intent.putExtra("type",offercode);
                        startActivity(intent);
                    }
                });
                linr.addView(offerImg);
            }
            offersHorizontal.addView(linr);
        }
    }
    //-----------------Get Notifications-------------------------------
    private class GetNotifications extends AsyncTask<Void, Void, Void> {
        int status;
        StringBuilder sb;
        String strJson, postData; JSONArray jsonArray4Notifications;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            db.flushNotifications(); //deleting old notification IDs

        }
        @Override
        protected Void doInBackground(Void... arg0) {           //loading JSONs from server in background
           String url=getResources().getString(R.string.url)+"GetServices.asmx/GetNotifications";
            HttpURLConnection c = null;
            try {
                postData="{\"notIDs\":\"" + db.getNotIDs() +  "\"}";
                URL u = new URL(url);
                c = (HttpURLConnection) u.openConnection();
                c.setRequestMethod("POST");
                c.setRequestProperty("Content-type", "application/json");
                c.setRequestProperty("Content-length", Integer.toString(postData.length()));
                c.setDoInput(true);
                c.setDoOutput(true);
                c.setUseCaches(false);
                c.setConnectTimeout(5000);
                c.setReadTimeout(5000);
                DataOutputStream wr = new DataOutputStream (c.getOutputStream ());
                wr.writeBytes(postData);
                wr.flush();
                wr.close();
                // c.connect();
                status = c.getResponseCode();
                switch (status) {
                    case 200:                               //no break; ???!!!
                    case 201:
                        BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                        sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line+"\n");
                        }
                        br.close();
                        int a=sb.indexOf("[");
                        int b=sb.lastIndexOf("]");
                        strJson=sb.substring(a,b+1);
                        strJson="{\"Notifications\":" + strJson.replace("\\\"", "\"")+ "}";
                }


            } catch (MalformedURLException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }  finally {
                if (c != null) {
                    try {
                        c.disconnect();
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            if(strJson!=null){
                try {
                    JSONObject jsonRootObject = new JSONObject(strJson);
                    jsonArray4Notifications = jsonRootObject.optJSONArray("Notifications");
                    for (int i = 0; i < jsonArray4Notifications.length(); i++) {
                        JSONObject jsonObject = jsonArray4Notifications.getJSONObject(i);
                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(Home.this);
                        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
                        mBuilder.setContentTitle(jsonObject.optString("Heading"));
                        mBuilder.setContentText(jsonObject.optString("Description"));
                        String ex_Date=jsonObject.optString("ExpiryDate");
                        db.insertNotIDs(jsonObject.optString("NotificationID"),ex_Date.replace("\\/Date(", "").replace(")\\/", ""));
                        mNotificationManager.notify(i, mBuilder.build());
                    }

                } catch (JSONException e) {
                }}
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
          //  Toast.makeText(Home.this,strJson,Toast.LENGTH_LONG).show();
            //Toast.makeText(Home.this,postData,Toast.LENGTH_LONG).show();
        }
    }

    public void goSearch(View view) {
        if(serviceLoadedFlag) {
            Intent intent = new Intent(this, SearchGrid.class);
            intent.putExtra("service", serviceAndCode.get(serviceItems.getSelectedItem()));//passing service and type codes to next activity
            intent.putExtra("type", sTypeAndCode.get(serviceTypes.getSelectedItem()));
           /* intent.putExtra("serviceName", serviceItems.getSelectedItem().toString());
            intent.putExtra("typeName", serviceTypes.getSelectedItem().toString());*/
            startActivity(intent);
        }
        else {Toast.makeText(getApplicationContext(), "Server Error!!", Toast.LENGTH_LONG).show();}
        //for testing
        /*Intent intent = new Intent(Home.this, BeautyParlour.class);
        intent.putExtra("provider", "A0001");
        intent.putExtra("type", "LGH");
        startActivity(intent);*/
    }
    @Override
    public void onBackPressed()
    {
        new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle(R.string.exit)
                .setMessage(R.string.exit_q)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        //clear cache
                        Toast.makeText(Home.this,"Cache Memory Cleared!!",Toast.LENGTH_SHORT).show();
                        try {
                            File dir = getApplicationContext().getCacheDir();
                            if (dir != null && dir.isDirectory()) {
                                deleteDir(dir);
                            }
                        } catch (Exception e) {}
                    finish();

                }
    }).setNegativeButton(R.string.no, null).show();

    }
    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
    return dir.delete();}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);
        MenuItem refresh = menu.findItem(R.id.home);
        refresh.setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.user:
                Intent intentUser = new Intent(this, UserAccount.class);
                startActivity(intentUser);
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}