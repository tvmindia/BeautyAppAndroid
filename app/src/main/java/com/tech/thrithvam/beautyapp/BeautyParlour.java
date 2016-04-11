package com.tech.thrithvam.beautyapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

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
import java.util.logging.Level;
import java.util.logging.Logger;

public class BeautyParlour extends AppCompatActivity {
    Bundle extras;
    boolean detailsLoadedFlag=false;
    boolean otherTypesLoaded =false;
    boolean otherServicesLoaded =false;
    private ProgressDialog pDialog;
    TextView SP_name, serviceName, descriptionText,StypeDetails,providerAddress;
    JSONArray jsonArray;
    private static LayoutInflater inflater=null;
    String serviceTypeCode;

    DatabaseHandler db= new DatabaseHandler(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beauty_parlour);
        extras = getIntent().getExtras();
        inflater = ( LayoutInflater )this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        pDialog=new ProgressDialog(BeautyParlour.this);
        //Toast.makeText(getApplicationContext(), extras.getString("provider"), Toast.LENGTH_LONG).show();
        //Toast.makeText(getApplicationContext(), extras.getString("type"), Toast.LENGTH_LONG).show();
        SP_name=(TextView)findViewById(R.id.sp_name);
        serviceName=(TextView)findViewById(R.id.serviceName);
        descriptionText =(TextView)findViewById(R.id.description);
        StypeDetails =(TextView)findViewById(R.id.stypeDet);
        providerAddress=(TextView)findViewById(R.id.address);
        pDialog = new ProgressDialog(this);
        //see whether reached scroll bottom
        final ScrollView SPDetails=(ScrollView)findViewById(R.id.ScrlViewOfSPDetails);
        final TextView otherTypeLabel=(TextView)findViewById(R.id.otherTypeLabel);
        SPDetails.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if(!otherTypesLoaded){
                 int diff = (otherTypeLabel.getBottom()-(SPDetails.getHeight()+SPDetails.getScrollY()));
                 if( diff <= 0 )
                { //  Toast.makeText(BeautyParlour.this, "Bottom has been reached",Toast.LENGTH_LONG).show();
                    new GetDetailsOfOtherStyles().execute();
                    otherTypesLoaded =true;
                } // super.onScrollChanged(l, t, oldl, oldt);
            }
            }
        });
        final TextView otherServiceLabel=(TextView)findViewById(R.id.otherServicesLabel);
        SPDetails.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if(!otherServicesLoaded){
                    int diff = (otherServiceLabel.getBottom()-(SPDetails.getHeight()+SPDetails.getScrollY()));
                    if( diff <= 0 )
                    {  // Toast.makeText(BeautyParlour.this, "Bottom has been reached",Toast.LENGTH_LONG).show();
                        new GetDetailsOfOtherServices().execute();
                        otherServicesLoaded =true;
                    }
                }
            }
        });


        if (!isOnline()) {                                                       //exit from app if no connectivity

            pDialog.setMessage(getResources().getString(R.string.turnNetOn));// "Please turn on internet and come again....");
            pDialog.setCancelable(false);
            pDialog.show();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    finish();           //goes to last screen
                }
            }, 2000);
        } else {
             new GetServiceProviderDetails().execute();            //Load from server if connectivity is 'on'

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    // Showing progress dialog
                   /* if (pDialog.isShowing())
                        pDialog.dismiss();*/
                    pDialog.setMessage(getResources().getString(R.string.wait));
                    pDialog.setCancelable(false);
                    if (!detailsLoadedFlag)
                        pDialog.show();
                }
            }, 1000);
        }

    }


    private class GetServiceProviderDetails extends AsyncTask<Void, Void, Void> {
        int status;
        StringBuilder sb;
        String strJson;
        String postData;
        String sp_name, service_name, description,imageurl4provider,imageurl4style,sTypeDetails, providerAdrs;
        @Override
        protected void onPreExecute() {             //start to show progress bar<please wait>
            super.onPreExecute();
          }
        @Override
        protected Void doInBackground(Void... arg) {
            String url=getResources().getString(R.string.url)+"GetServices.asmx/GetServiceProviderDetails";
            HttpURLConnection c = null;
            try {
                postData="{\"ProviderCode\":\"" + extras.getString("provider")  + "\",\"serviceCode\":\"" + extras.getString("service") + "\",\"sTypeCode\":\"" + extras.getString("type") + "\"}";
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
                        strJson="{\"ServiceProviderDetails\":" + strJson.replace("\\\"","\"").replace("\\\\","\\") + "}";
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
            if(strJson!=null)
            {try {
                JSONObject jsonRootObject = new JSONObject(strJson);
                jsonArray = jsonRootObject.optJSONArray("ServiceProviderDetails");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    sp_name=jsonObject.optString("ProviderName");
                    service_name=jsonObject.optString("ServiceName");
                    serviceTypeCode=jsonObject.optString("ServiceTypeCode");
                    description =jsonObject.optString("Description");
                    sTypeDetails=jsonObject.optString("ServiceTypeName")+"\nRate: "+jsonObject.getDouble("Rate");
                    providerAdrs=jsonObject.optString("Contact1")+"\n"+jsonObject.optString("Contact2")+"\nEmail: "+jsonObject.optString("Email");
                    imageurl4provider=jsonObject.optString("ProviderImage","NOIMAGE");
                    imageurl4style=jsonObject.optString("StyleImg","NOIMAGE");
                }
            } catch (JSONException e) {

            }}

            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            SP_name.setText(sp_name);
            serviceName.setText(service_name);
            StypeDetails.setText(sTypeDetails);
            descriptionText.setText(description);
            providerAddress.setText(providerAdrs.replace("\nnull",""));
            final ProgressBar pgrs=(ProgressBar)findViewById(R.id.progressBar);
            pgrs.getIndeterminateDrawable().setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);
            // Toast.makeText(getApplicationContext(), imageurl, Toast.LENGTH_LONG).show();
            if(!imageurl4provider.equals("NOIMAGE")) {
            ImageView bpImg = (ImageView)findViewById(R.id.bpImg);
            String tempurl=getResources().getString(R.string.url)+imageurl4provider.substring(imageurl4provider.indexOf("tempImages"));
           //     Toast.makeText(getApplicationContext(),tempurl, Toast.LENGTH_LONG).show();
            Picasso.with(BeautyParlour.this)
                        .load(tempurl)
                        //.error(R.drawable.handledn)
                        .into(bpImg, new Callback() {
                            @Override
                            public void onSuccess() {
                                pgrs.setVisibility(View.GONE);
                            }
                            @Override
                            public void onError() {
                                Toast.makeText(getApplicationContext(), "Image Loading Error!!!", Toast.LENGTH_LONG).show();
                            }
                        });
            }
            else {
                pgrs.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "No image available!!!", Toast.LENGTH_LONG).show();
            }
            if(!imageurl4style.equals("NOIMAGE")) {
                ImageView styleImg = (ImageView)findViewById(R.id.s_type_image);
                String tempurl=getResources().getString(R.string.url)+imageurl4style.substring(imageurl4style.indexOf("tempImages"));
                Picasso.with(BeautyParlour.this)
                        .load(tempurl).placeholder(R.drawable.loadingimage)
                        .into(styleImg);
            }
            detailsLoadedFlag=true;
            if (pDialog.isShowing())
                pDialog.dismiss();
        }
    }
    //get more details about available styles
    private class GetDetailsOfOtherStyles extends AsyncTask<Void, Void, Void> {
        int status;
        StringBuilder sb;
        String strJson;
        String postData;
        ArrayList<String> imageurl,sTypeDetails,sTypeCode;
        LinearLayout otherTypes;
        @Override
        protected void onPreExecute() {             //start to show progress bar<please wait>
            super.onPreExecute();
            otherTypes=(LinearLayout)findViewById(R.id.otherStyles);
            imageurl=new ArrayList<>();
            sTypeDetails=new ArrayList<>();
            sTypeCode=new ArrayList<>();
        }
        @Override
        protected Void doInBackground(Void... arg) {
            String url=getResources().getString(R.string.url)+"GetServices.asmx/GetDetailsOfItems";
            HttpURLConnection c = null;
            try {
                postData="{\"ProviderCode\":\"" + extras.getString("provider")+ "\",\"serviceCode\":\"" + extras.getString("service") + "\",\"sTypeCode\":\"" + serviceTypeCode + "\",\"req_type\":\"" + "s_type" + "\"}";
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
                        strJson="{\"ItemDetails\":" + strJson.replace("\\\"","\"").replace("\\\\","\\") + "}";
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
            if(strJson!=null)
            {try {
                JSONObject jsonRootObject = new JSONObject(strJson);
                jsonArray = jsonRootObject.optJSONArray("ItemDetails");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    sTypeDetails.add(jsonObject.optString("ServiceTypeName")+"\nRate: "+jsonObject.optString("Rate"));
                    sTypeCode.add(jsonObject.optString("ServiceTypeCode"));
                    imageurl.add(jsonObject.optString("StyleImg", "NOIMAGE"));

                }
            } catch (JSONException e) {

            }}

            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            int i;
                for(i=0;i<sTypeDetails.size();i++)
                {
                View Details= inflater.inflate(R.layout.item_details, null);
                TextView textView=(TextView) Details.findViewById(R.id.item_details);
                textView.setText(sTypeDetails.get(i));
                ImageView imageView=(ImageView)Details.findViewById(R.id.itemImg);
                String tempurl=getResources().getString(R.string.url)+imageurl.get(i).substring(imageurl.get(i).indexOf("tempImages"));
                Picasso.with(BeautyParlour.this)
                        .load(tempurl)
                        .placeholder(R.drawable.loadingimage)
                        .into(imageView);
                final int finalI = i;
                Details.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(BeautyParlour.this, BeautyParlour.class);
                        intent.putExtra("provider", extras.getString("provider"));
                        intent.putExtra("service", extras.getString("service"));
                        intent.putExtra("type", sTypeCode.get(finalI));
                        startActivity(intent);
                        finish();
                        }
                         });
                otherTypes.addView(Details);
                }
            if (i==0) //no other types are available
            {
                TextView textView=new TextView(BeautyParlour.this);
                textView.setText(R.string.no_other_type);
                otherTypes.addView(textView);
            }
        }
    }
    //get details about other available services
    private class GetDetailsOfOtherServices extends AsyncTask<Void, Void, Void> {
        int status;
        StringBuilder sb;
        String strJson;
        String postData;
        ArrayList<String> imageurl,serviceDetails, serviceCode;
        LinearLayout otherServices;
        @Override
        protected void onPreExecute() {             //start to show progress bar<please wait>
            super.onPreExecute();
            otherServices=(LinearLayout)findViewById(R.id.otherServices);
            imageurl=new ArrayList<>();
            serviceDetails=new ArrayList<>();
            serviceCode=new ArrayList<>();
        }
        @Override
        protected Void doInBackground(Void... arg) {
            String url=getResources().getString(R.string.url)+"GetServices.asmx/GetDetailsOfItems";
            HttpURLConnection c = null;
            try {
                postData="{\"ProviderCode\":\"" + extras.getString("provider")+ "\",\"serviceCode\":\"" + extras.getString("service") + "\",\"sTypeCode\":\"" + serviceTypeCode + "\",\"req_type\":\"" + "service" + "\"}";
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
                        strJson="{\"ServiceDetails\":" + strJson.replace("\\\"","\"").replace("\\\\","\\") + "}";
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
            if(strJson!=null)
            {try {
                JSONObject jsonRootObject = new JSONObject(strJson);
                jsonArray = jsonRootObject.optJSONArray("ServiceDetails");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    serviceDetails.add(jsonObject.optString("ServiceName"));
                    serviceCode.add(jsonObject.optString("ServiceCode"));
                    imageurl.add(jsonObject.optString("StyleImg", "NOIMAGE"));
                }
            } catch (JSONException e) {

            }}

            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            int i;
            for(i=0;i<serviceDetails.size();i++)
            {
                View Details= inflater.inflate(R.layout.item_details, null);
                TextView textView=(TextView) Details.findViewById(R.id.item_details);
                textView.setText(serviceDetails.get(i));
                ImageView imageView=(ImageView)Details.findViewById(R.id.itemImg);
                String tempurl=getResources().getString(R.string.url)+imageurl.get(i).substring(imageurl.get(i).indexOf("tempImages"));
                Picasso.with(BeautyParlour.this)
                        .load(tempurl)
                        .placeholder(R.drawable.loadingimage)
                        .into(imageView);
                final int finalI = i;
                Details.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(BeautyParlour.this, BeautyParlour.class);
                        intent.putExtra("provider", extras.getString("provider"));
                        intent.putExtra("service",serviceCode.get(finalI) );
                        intent.putExtra("type", "ANY");
                        startActivity(intent);
                        finish();
                    }
                });

                otherServices.addView(Details);
            }
            if (i==0) //no other types are available
            {
                TextView textView=new TextView(BeautyParlour.this);
                textView.setText(R.string.no_other_services);
                otherServices.addView(textView);
            }
        }
    }


    public void viewMap(View view) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + ("10.313498, 76.335608")));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
    }
    public void book(View view) {
        if(db.GetUserDetail("Email").equals("")){
            Toast.makeText(BeautyParlour.this,R.string.please_login,Toast.LENGTH_LONG).show();
            Intent intentUser = new Intent(BeautyParlour.this, UserAccount.class);
            startActivity(intentUser);
        }
        else {
            Intent intent = new Intent(BeautyParlour.this,Booking.class);
            intent.putExtra("provider",extras.getString("provider"));
            intent.putExtra("sType", serviceTypeCode);
            intent.putExtra("service", extras.getString("service"));
            startActivity(intent);}

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
              //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
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

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
    @Override
    public void onBackPressed()
    {
       this.finish();

    }
}