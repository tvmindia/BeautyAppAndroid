package com.tech.thrithvam.beautyapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.CalendarContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Booking extends AppCompatActivity {
    DatePicker datePicker;
    TimePicker timePicker;
    DatabaseHandler db= new DatabaseHandler(this);
    Bundle extras;
    Calendar mCalendarOpeningTime;
    Calendar mCalendarClosingTime;
    Calendar daysLimit;
    Calendar finalTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);
        extras = getIntent().getExtras();
        TextView name=(TextView)findViewById(R.id.user_name_view);
        TextView mobile=(TextView)findViewById(R.id.mob_no_view);
        TextView email=(TextView)findViewById(R.id.email_view);
        name.setText(db.GetUserDetail("UserName"));
        email.setText(db.GetUserDetail("Email"));
        mobile.setText(db.GetUserDetail("MobNo"));


        datePicker=(DatePicker)findViewById(R.id.datePicker);
        timePicker=(TimePicker)findViewById(R.id.timePicker);


        //time interval checking---------------------------------------

        mCalendarOpeningTime = Calendar.getInstance();
        mCalendarOpeningTime.set(Calendar.HOUR, 8);
        mCalendarOpeningTime.set(Calendar.MINUTE, 59);
        mCalendarOpeningTime.set(Calendar.AM_PM, Calendar.AM);

        mCalendarClosingTime = Calendar.getInstance();
        mCalendarClosingTime.set(Calendar.HOUR, 5);
        mCalendarClosingTime.set(Calendar.MINUTE, 30);
        mCalendarClosingTime.set(Calendar.AM_PM, Calendar.PM);

        daysLimit=Calendar.getInstance();
        daysLimit.add(Calendar.MONTH, 1);

        finalTime = Calendar.getInstance();
    }
    public void book_button(View view) {
        finalTime.set(datePicker.getYear(),datePicker.getMonth(),datePicker.getDayOfMonth());
        mCalendarOpeningTime.set(datePicker.getYear(),datePicker.getMonth(),datePicker.getDayOfMonth());
        mCalendarClosingTime.set(datePicker.getYear(),datePicker.getMonth(),datePicker.getDayOfMonth());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            finalTime.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
            finalTime.set(Calendar.MINUTE, timePicker.getMinute());
            finalTime.set(Calendar.SECOND, 0);
            finalTime.set(Calendar.MILLISECOND, 0);
        }
        else {
            finalTime.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
            finalTime.set(Calendar.MINUTE, timePicker.getCurrentMinute());
            finalTime.set(Calendar.MILLISECOND, 0);
            finalTime.set(Calendar.SECOND, 0);
        }
        if(finalTime.after(Calendar.getInstance())) {
            if (!finalTime.after(daysLimit)) {
                if (finalTime.after(mCalendarOpeningTime) && finalTime.before(mCalendarClosingTime)) {
                    new Book().execute();

                /*
            Intent intent = new Intent(Intent.ACTION_EDIT);
            intent.setType("vnd.android.cursor.item/event"); //working is to be checked with "intent.setData(CalendarContract.Events.CONTENT_URI);" on failure
            intent.putExtra("beginTime", cal.getTimeInMillis());
            intent.putExtra("allDay", false);
            intent.putExtra("endTime", cal.getTimeInMillis() + 60 * 60 * 1000);
            intent.putExtra("title", "Visit BeautyParlour");
            intent.putExtra("eventLocation", "Location of Beauty parlour");
            intent.putExtra("description", "Booked using" + R.string.app_name);
            startActivity(intent);*/
                } else {
                    Toast.makeText(Booking.this, R.string.time_error, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(Booking.this, R.string.date_error, Toast.LENGTH_LONG).show();
            }
        }
        else {

            Toast.makeText(Booking.this, R.string.time_past_error, Toast.LENGTH_LONG).show();
        }

              /*  Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "sub");
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "text");
        startActivity(emailIntent);*/
    }
    public class Book extends AsyncTask<Void , Void, Void> {
        int status;StringBuilder sb;
        String strJson, postData,formattedTime;
        JSONArray jsonArray4types;
        String msg;
        boolean pass;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            SimpleDateFormat formatted = new SimpleDateFormat("yyyy MM dd HH:mm:ss", Locale.US);
            formattedTime = formatted.format(finalTime.getTime());
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String url =getResources().getString(R.string.url) + "GetServices.asmx/Booking";
            HttpURLConnection c = null;
            try {
                postData = "{\"bookingID\":\"" +"noID" + "\",\"user\":\"" + db.GetUserDetail("Email") + "\",\"serviceCode\":\"" + extras.getString("service") + "\",\"sTypeCode\":\"" + extras.getString("sType") + "\",\"providerCode\":\"" +extras.getString("provider") + "\",\"timing\":\"" + formattedTime + "\",\"bookORcancel\":\"" + "book"+ "\"}";
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
                    case 201: BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                        sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line+"\n");
                        }
                        br.close();
                        int a=sb.indexOf("[");
                        int b=sb.lastIndexOf("]");
                        strJson=sb.substring(a,b+1);
                        strJson="{\"Message\":" + strJson.replace("\\\"","\"") + "}";
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
                jsonArray4types = jsonRootObject.optJSONArray("Message");
                for (int i = 0; i < jsonArray4types.length(); i++) {
                    JSONObject jsonObject = jsonArray4types.getJSONObject(i);
                    msg=jsonObject.optString("msg");
                    pass=jsonObject.optBoolean("pass");
                }
            } catch (JSONException e) {

            }}
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);


            new AlertDialog.Builder(Booking.this).setIcon(android.R.drawable.ic_dialog_alert)//.setTitle("")
                    .setMessage(msg)
                    .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (pass) {

                            }
                        }
                    }).show();
         //    Toast.makeText(Booking.this, postData, Toast.LENGTH_LONG).show();
        }
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
