package com.tech.thrithvam.beautyapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SearchGrid extends AppCompatActivity {
    DatabaseHandler db= new DatabaseHandler(this);
    boolean openPopup=false;
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
    JSONArray jsonArray4providers;
    boolean serviceLoadedFlag =false;
    boolean sTypeLoadedFlag =false;
    boolean resultsLoadedFlag=false;
    boolean serverError =false;
    Transparent popup;
    Bundle extras;
    GridView gridView;
    ArrayList<String> gridArray4provider = new ArrayList<>();
    ArrayList<String> gridArray4pCode = new ArrayList<>();
    ArrayList<String> gridArray4image = new ArrayList<>();
    ArrayList<String> gridArray4link = new ArrayList<>();
    ArrayList<Boolean> gridArray4fav= new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_grid);
        extras= getIntent().getExtras();
        gridView=(GridView) findViewById(R.id.gridView);
        pDialog=new ProgressDialog(SearchGrid.this);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // Showing progress dialog
                   /* if (pDialog.isShowing())
                        pDialog.dismiss();*/
                pDialog.setMessage(getResources().getString(R.string.wait));
                pDialog.setCancelable(false);
                if((!serviceLoadedFlag || !sTypeLoadedFlag || !resultsLoadedFlag)&&!serverError)
                    pDialog.show();
            }
        }, 1000);
        //searchResults
         new GetSearchResults().execute();
        //Popup
        // disable ScrollView of popup in portrait mode
       /* if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            ScrollView scrollView = (ScrollView) findViewById(R.id.ScrlView);
            scrollView.setVerticalScrollBarEnabled(false);
        }*/
        popup = (Transparent) findViewById(R.id.popup_window);

        final Button btn=(Button)findViewById(R.id.handle);
        btn.setOnTouchListener(new View.OnTouchListener() {
            boolean firstMove=true;
            private int dx = 0;
            private int dy = 0;
            private static final int MAX_CLICK_DURATION = 200;
            private long startClickTime;
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    //    dx = (int) motionEvent.getX();
                        dy = (int) motionEvent.getY();
                        startClickTime = Calendar.getInstance().getTimeInMillis();

                        break;
                    case MotionEvent.ACTION_UP:
                        long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                        if(clickDuration < MAX_CLICK_DURATION)
                        {
                            //click event has occurred
                            if (!openPopup) {
                                openPopup = true;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                                    popup.animate().translationY(0).setDuration(1000);  //sliding the drawer
                                    btn.animate().translationY(popup.getHeight()).setDuration(1000);

                                } else {
                                    popup.setVisibility(View.VISIBLE);
                                }

                            } else if (openPopup) {
                                openPopup = false;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                                    popup.animate().translationY(-popup.getHeight()).setDuration(1000); //sliding back
                                    btn.animate().translationY(0).setDuration(1000);
                                } else {
                                    popup.setVisibility(View.GONE);
                                }                                           //hide for older versions

                            }

                        }

//                        int x = (int) motionEvent.getX();
                        int y = (int) motionEvent.getY();
//                        int left = (x - dx);
                        int top = (y - dy);
                        if(top<-5&& openPopup)
                        {
                            openPopup = false;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                                popup.animate().translationY(-popup.getHeight()).setDuration(1000); //sliding back
                                btn.animate().translationY(0).setDuration(1000);
                            } else {
                                popup.setVisibility(View.GONE);
                            }                                           //hide for older versions


                            //mPopupWindow.update(left, top, -1, -1);
                        }
                        else if(top>2)
                        {
                            openPopup =true;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                                popup.animate().translationY(0).setDuration(1000);  //sliding the drawer
                                btn.animate().translationY(popup.getHeight()).setDuration(1000);

                            } else {
                                popup.setVisibility(View.VISIBLE);
                            }
                        }

                       // Toast.makeText(getApplicationContext(), "x: " + Integer.toString(left) + " y: " + Integer.toString(top), Toast.LENGTH_LONG).show();
                        break;
                  /*  case MotionEvent.ACTION_MOVE:
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {

                            int j = (int) motionEvent.getY();
                            if(j>dy&&firstMove)
                            {
                            firstMove=false;
                            popup.animate().translationY(-popup.getHeight()+j+dy);  //sliding the drawer
                            btn.animate().translationY(dy+j);
                            }
                            else if(j>dy&&!firstMove)
                            {
                                popup.animate().translationY(j+dy);  //sliding the drawer
                                btn.animate().translationY(dy+j);
                            }
                        }*/
                }
                if(openPopup)
                {btn.setText(R.string.close);}
                else {btn.setText(R.string.searchtext);}
                return true;
            }

        });

     /*   btn.setOnDragListener(new View.OnDragListener(){
            @Override
            public boolean onDrag(View v,  DragEvent event){
                switch(event.getAction())
                {
                    case DragEvent.ACTION_DRAG_STARTED:
                        Toast.makeText(getApplicationContext(), "ACTION_DRAG_STARTED", Toast.LENGTH_LONG).show();
                        break;
                    case DragEvent.ACTION_DRAG_ENTERED:
                        Toast.makeText(getApplicationContext(), "ACTION_DRAG_ENTERED", Toast.LENGTH_LONG).show();
                        break;
                    case DragEvent.ACTION_DRAG_EXITED :
                        Toast.makeText(getApplicationContext(), "ACTION_DRAG_EXITED ", Toast.LENGTH_LONG).show();
                        break;
                    case DragEvent.ACTION_DRAG_LOCATION  :
                        Toast.makeText(getApplicationContext(), "ACTION_DRAG_LOCATION", Toast.LENGTH_LONG).show();
                        int right=btn.getRight();
                        int left=btn.getLeft();
                        int top=btn.getTop();
                        int bottom=btn.getBottom();
                        System.out.println("Start Touch "+right+" "+top+" "+left+" "+bottom);

                        break;
                    case DragEvent.ACTION_DRAG_ENDED   :
                        Toast.makeText(getApplicationContext(), "ACTION_DRAG_ENDED ", Toast.LENGTH_LONG).show();
                        break;
                    case DragEvent.ACTION_DROP:
                        Toast.makeText(getApplicationContext(), "ACTION_DROP", Toast.LENGTH_LONG).show();
                        break;
                    default: break;
                }
                return true;
            }
        });*/


        popup.setOnTouchListener(new View.OnTouchListener() {
            private int dx = 0;
            private int dy = 0;
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dx = (int) motionEvent.getX();
                        dy = (int) motionEvent.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        int x = (int) motionEvent.getX();
                        int y = (int) motionEvent.getY();
                        int left = (x - dx);
                        int top = (y - dy);
                    if(top<-5)
                    {
                        openPopup = false;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                            popup.animate().translationY(-popup.getHeight()).setDuration(1000); //sliding back
                            btn.animate().translationY(0).setDuration(1000);
                        } else {
                            popup.setVisibility(View.GONE);
                        }                                           //hide for older versions

                    }
               //     Toast.makeText(getApplicationContext(), "x: " + Integer.toString(left) + " y: " + Integer.toString(top), Toast.LENGTH_LONG).show();
                        break;
                }
                if(openPopup)
                {btn.setText(R.string.close);}
                else {btn.setText(R.string.searchtext);}
                return true;
            }
        });

        //sliding window items
        serviceItems = (Spinner) findViewById(R.id.serviceSpinner);
        serviceTypes = (Spinner) findViewById(R.id.sTypeSpinner);
        spinnerArray4sType = new ArrayList<String>();
        spinnerArray = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerArray);
        adapter4sType = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerArray4sType);
        new GetServices().execute();
        serviceItems.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int pos, long arg3) {
                if (!isOnline()) {                                                       //exit from app if no connectivity
                 //   pDialog = new ProgressDialog(SearchGrid.this);
                    pDialog.setMessage(getResources().getString(R.string.turnNetOn));
                    //   pDialog.setCancelable(false);
                    pDialog.show();
                } else {
                    new GetServiceTypes().execute(serviceAndCode.get(serviceItems.getSelectedItem()));
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                Toast.makeText(getApplicationContext(), "Nothing selected :P", Toast.LENGTH_LONG).show();
            }
        });

        //Search results

        }

    private class GetServices extends AsyncTask<Void, Void, Void> {
        int status;
        StringBuilder sb;
        String strJson;
        @Override
        protected void onPreExecute() {             //start to show progress bar<please wait>
            super.onPreExecute();
    //        pDialog = new ProgressDialog(SearchGrid.this);     // Showing progress dialog
           /* if (pDialog.isShowing())
                pDialog.dismiss();
            pDialog.setMessage(getResources().getString(R.string.wait));
            pDialog.setCancelable(false);
            pDialog.show();*/
        }
        @Override
        protected Void doInBackground(Void... arg0) {           //loading JSONs from server in background
            String url=getResources().getString(R.string.url)+"GetServices.asmx/GetServicesList";
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
                        strJson=sb.substring(a,b+1);//.toString();
                        strJson="{\"Services\":" + strJson.replace("\\\"","\"") + "}";
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
                serviceLoadedFlag =true;
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

                } catch (JSONException e) {
                }}
            else{
                serviceLoadedFlag =false;}
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (pDialog.isShowing())
                pDialog.dismiss();
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            serviceItems.setAdapter(adapter);
            serviceItems.setSelection(adapter.getPosition(extras.getString("serviceName")));
            if(!serviceLoadedFlag){
                if (pDialog.isShowing())
                    pDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Server Error!!", Toast.LENGTH_LONG).show(); finish();
            serverError=true;}

        }
    }

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
     //       pDialog = new ProgressDialog(SearchGrid.this);     // Showing progress dialog
          /*  if (pDialog.isShowing())
                pDialog.dismiss();
            pDialog.setMessage(getResources().getString(R.string.wait));
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
            } catch (JSONException e) {

            }}
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            adapter4sType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            serviceTypes.setAdapter(adapter4sType);
            serviceTypes.setSelection(adapter4sType.getPosition(extras.getString("typeName")));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                popup.animate().translationY(-popup.getHeight()).setDuration(1000);
            }
            else
            {popup.setVisibility(View.GONE);} //make the popup slide out of screen
            sTypeLoadedFlag=true;
            if (pDialog.isShowing())
                pDialog.dismiss();
        }
    }

    //Search results
    private class GetSearchResults extends AsyncTask<Void, Void, Void> {
        int status;
        StringBuilder sb;
        String strJson;
        String postData;

        @Override
        protected void onPreExecute() {             //start to show progress bar<please wait>
            super.onPreExecute();
  //          pDialog = new ProgressDialog(SearchGrid.this);     // Showing progress dialog
           /* if (pDialog.isShowing())
                pDialog.dismiss();
            pDialog.setMessage(getResources().getString(R.string.wait));
            pDialog.setCancelable(false);
            pDialog.show();*/
        }
        @Override
        protected Void doInBackground(Void... arg0) {           //loading JSONs from server in background
            String url = getResources().getString(R.string.url)+"GetServices.asmx/GetSearchResults";
            HttpURLConnection c = null;
            try {
                postData = "{\"ServiceCode\":\"" + extras.getString("service") + "\",\"sTypeCode\":\"" + extras.getString("type")+ "\",\"user\":\"" + db.GetUserDetail("Email") + "\"}";
                URL u = new URL(url);
                c = (HttpURLConnection) u.openConnection();
                c.setRequestMethod("POST");
                c.setRequestProperty("Content-type", "application/json; charset=utf-16");
                c.setRequestProperty("Content-length", Integer.toString(postData.length()));
                c.setDoInput(true);
                c.setDoOutput(true);
                c.setUseCaches(false);
                // c.setAllowUserInteraction(false);
                c.setConnectTimeout(5000);
                c.setReadTimeout(5000);
               // c.setRequestProperty("Content-Language", "en-US");
                DataOutputStream wr = new DataOutputStream(c.getOutputStream());
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
                            sb.append(line + "\n");
                        }
                        br.close();
                        int a = sb.indexOf("[");
                        int b = sb.lastIndexOf("]");
                        strJson = sb.substring(a, b + 1);
                        strJson = "{\"SearchResults\":" + strJson.replace("\\\"", "\"").replace("\\\\", "\\") + "}";
                }
            } catch (MalformedURLException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (c != null) {
                    try {
                        c.disconnect();
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            if(strJson!=null)
            {try {
                JSONObject jsonRootObject = new JSONObject(strJson);
                jsonArray4providers = jsonRootObject.optJSONArray("SearchResults");
                for (int i = 0; i < jsonArray4providers.length(); i++) {
                    JSONObject jsonObject = jsonArray4providers.getJSONObject(i);
                    gridArray4provider.add(jsonObject.optString("ProviderName"));
                    gridArray4pCode.add(jsonObject.optString("ProviderCode"));
                    gridArray4link.add(jsonObject.optString("Link", "NOLINK"));
                    gridArray4fav.add(jsonObject.optBoolean("Favorite"));
                    gridArray4image.add(jsonObject.optString("StyleImg", "NOIMAGE"));
                }
            } catch (JSONException e) {

            }}
        return null;
    }
    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        resultsLoadedFlag=true;
        if (pDialog.isShowing())
            pDialog.dismiss();
      //  Toast.makeText(getApplicationContext(), postData, Toast.LENGTH_LONG).show();
     //   Toast.makeText(getApplicationContext(), Integer.toString(status), Toast.LENGTH_LONG).show();
       // Toast.makeText(getApplicationContext(), strJson, Toast.LENGTH_LONG).show();
        gridView.setAdapter(new ImTxtAdapter(SearchGrid.this, gridArray4provider, gridArray4pCode, extras.getString("service"),extras.getString("type"),gridArray4link, gridArray4fav,gridArray4image));
    }
}

    //Add to favorites
   /* public class AddToFavorites extends AsyncTask<String, Void, Void> {
        int status;
        String postData;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... arg0) {
            String url = getResources().getString(R.string.url) + "GetServices.asmx/AddToFavorites";
            HttpURLConnection c = null;
            try {
                postData = "{\"user\":\"" + "user@mail.com" + "\",\"sTypeCode\":\"" + extras.getString("type") + "\",\"providerCode\":\"" + arg0[0] + "\"}";
                URL u = new URL(url);
                c = (HttpURLConnection) u.openConnection();
                c.setRequestMethod("POST");
                c.setRequestProperty("Content-type", "application/json; charset=utf-16");
                c.setRequestProperty("Content-length", Integer.toString(postData.length()));
                c.setDoInput(true);
                c.setDoOutput(true);
                c.setUseCaches(false);
                c.setConnectTimeout(5000);
                c.setReadTimeout(5000);
                DataOutputStream wr = new DataOutputStream(c.getOutputStream());
                wr.writeBytes(postData);
                wr.flush();
                wr.close();
                status = c.getResponseCode();
                switch (status) {
                    case 200:
                    case 201:
                }
            } catch (MalformedURLException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (c != null) {
                    try {
                        c.disconnect();
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
         }
    }
*/

        public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
    public void goSearch(View view) {
        Intent intent = new Intent(this, SearchGrid.class);
        intent.putExtra("service", serviceAndCode.get(serviceItems.getSelectedItem()));//passing service and type codes to next activity
        intent.putExtra("type",sTypeAndCode.get(serviceTypes.getSelectedItem()));
        intent.putExtra("serviceName", serviceItems.getSelectedItem().toString());
        intent.putExtra("typeName", serviceTypes.getSelectedItem().toString());
        startActivity(intent);
       // new AddToFavorites().execute();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                Intent intent = new Intent(this, Home.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                return true;
            case R.id.user:
                Intent intentUser = new Intent(this, UserAccount.class);
                startActivity(intentUser);

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onBackPressed()
    {
        this.finish();

    }
}
