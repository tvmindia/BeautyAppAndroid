package com.tech.thrithvam.beautyapp;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class UserAccount extends AppCompatActivity {
    //variables for all
    RelativeLayout loginWidgets;
    RelativeLayout userWidgets;
    RelativeLayout signUpWidgets;
    int height,width, initial;
    Cipher cipher;
    DatabaseHandler db= new DatabaseHandler(this);
    //variables for sign up
    boolean nameValid=false;
    boolean password1Valid=false;
    boolean password2Valid=false;
    boolean mobValid=false;
    boolean emailValid=false;
    boolean genderValid=false;
    EditText name, password, password2, mobileNo, email, emailLogin, passwordLogin,e1,e2,e3,e4;
    String gender="";
    //variables for user details edit
    boolean changingPassword=false;
    //variables for booking details
    boolean bookings;
    private static LayoutInflater inflater=null;
    LinearLayout bookingDetailsLinear;
    LinearLayout visitedCancelledLinear;
    ScrollView bookingDetailsScroll;
    ScrollView visitedDetailsScroll;
    Button visited_can;
    Button booked_button;
   // int vButton, bScroll;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_account);
        //-----------------animations----------------------------------
        loginWidgets=(RelativeLayout)findViewById(R.id.Login);
        userWidgets =(RelativeLayout)findViewById(R.id.UserDetails);
        signUpWidgets=(RelativeLayout)findViewById(R.id.sign_up_details);
        signUpWidgets.setVisibility(View.GONE);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        height = displaymetrics.heightPixels;
        width=displaymetrics.widthPixels;
        initial=(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics());//converting 15dp into pixels
        //--------------------Login check----------------------
        if(db.GetUserDetail("UserName")!="")
        {
           // Toast.makeText(UserAccount.this,db.GetUserDetail("UserName"),Toast.LENGTH_LONG).show();
            loginWidgets.setVisibility(View.GONE);
            TextView name=(TextView)findViewById(R.id.user_name_view);
            TextView mobile=(TextView)findViewById(R.id.mob_no_view);
            TextView email=(TextView)findViewById(R.id.email_view);
            name.setText(db.GetUserDetail("UserName"));
            email.setText(db.GetUserDetail("Email"));
            mobile.setText(db.GetUserDetail("MobNo"));
            userWidgets.setVisibility(View.VISIBLE);
            new GetBookedDetails().execute();
        }
        else {
            userWidgets.setVisibility(View.GONE);
        }
        //--------------adding Encryption methods--------------------
        DESKeySpec keySpec;
        try {
            keySpec = new DESKeySpec("Thrithvam".getBytes("UTF8"));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey key = keyFactory.generateSecret(keySpec);
            cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            //byte[] cleartext = plainTextPassword.getBytes("UTF8");
//          String encryptedPwd = base64encoder.encode(cipher.doFinal(cleartext)); // Base64.encodeToString(cipher.doFinal(cleartext),Base64.DEFAULT)
        } catch (InvalidKeyException | UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
      /*  // DECODE encryptedPwd String
        byte[] encrypedPwdBytes = base64decoder.decodeBuffer(encryptedPwd);
        Cipher cipher = Cipher.getInstance("DES");// cipher is not thread safe
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] plainTextPwdBytes = (cipher.doFinal(encrypedPwdBytes));//Base64.decode(encryptedPwd, Base64.DEFAULT) of the android.util.Base64 */




        //--------------------sign_up validations-------------------------
        //name=====
        name=(EditText)findViewById(R.id.user_name_signup);
        name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {}
                else {
                    name.setText(name.getText().toString().trim());
                    if(name.length()<4 || name.length()>30)
                    {
                        name.setError(getResources().getString(R.string.name_error_msg));
                        nameValid=false;
                    }
                    else {
                        nameValid=true;
                    }
                }
            }
        });
        //password====
        password=(EditText)findViewById(R.id.password1_signup);
        password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {}
                else {
                    password.setText(password.getText().toString().trim());
                    //length
                    if(password.length()<8 || password.length()>20)
                    {
                        password.setError(getResources().getString(R.string.password_error_msg_1));
                        password1Valid=false;
                    }
                    else {
                        password1Valid=true;//  password.setError(null);
                    }
                    //matching
                    if(!password2.getText().toString().equals(password.getText().toString()))
                    {
                        password2.setError(getResources().getString(R.string.password_error_msg_2));
                        password2Valid=false;
                    }
                    else {
                        password2Valid=true;
                        password2. setError(null);
                    }
                }
            }
        });
        password2=(EditText)findViewById(R.id.password2_signup);
        password2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {}
                else {
                    if(!password2.getText().toString().equals(password.getText().toString()))
                    {
                        password2.setError(getResources().getString(R.string.password_error_msg_2));
                        password2Valid=false;
                    }
                    else {
                        password2Valid=true;
                    }
                }
            }
        });
        //mobile number=====
        mobileNo=(EditText)findViewById(R.id.mob_no_signup);
        mobileNo.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {}
                else {
                    mobileNo.setText(mobileNo.getText().toString().trim());
                    if(mobileNo.length()<4 || mobileNo.length()>20)
                    {
                        mobileNo.setError(getResources().getString(R.string.mobile_no_error_msg));
                        mobValid=false;
                    }
                    else {
                        mobValid=true;
                    }
                }
            }
        });
        //e-mail=====
        email=(EditText)findViewById(R.id.email_signup);
        email.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {}
                else {
                    email.setText(email.getText().toString().trim());
                    if(email.length()<7 || email.length()>40 || !android.util.Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches())
                    {
                        email.setError(getResources().getString(R.string.email_error_msg));
                        emailValid=false;
                    }
                    else {
                        emailValid=true;
                    }
                }
            }
        });

        //----------------------------Login user--------------------------------
        emailLogin=(EditText)findViewById(R.id.emailLogin);
        passwordLogin=(EditText)findViewById(R.id.passwordLogin);
        //-----------------------------booking-------------------------------------
        inflater = ( LayoutInflater )this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        bookingDetailsLinear=new LinearLayout(UserAccount.this);
        bookingDetailsLinear.setOrientation(LinearLayout.VERTICAL);
        visitedCancelledLinear=new LinearLayout(UserAccount.this);
        visitedCancelledLinear.setOrientation(LinearLayout.VERTICAL);
        visited_can=(Button)findViewById(R.id.visited_items_button);
        bookingDetailsScroll=(ScrollView)findViewById(R.id.booked_items_scrollview);
        visitedDetailsScroll=(ScrollView)findViewById(R.id.visited_items_scrollview);
        visitedDetailsScroll.setVisibility(View.GONE);
        booked_button=(Button)findViewById(R.id.booked_items_button);
        booked_button.setEnabled(false);booked_button.setTextColor(Color.WHITE);

    }
    public void login(View view) {
    if(emailLogin.getText().toString().equals("")||passwordLogin.getText().toString().equals(""))
    {
        Toast.makeText(UserAccount.this,R.string.login_error_msg,Toast.LENGTH_LONG).show();
    }
    else if(!android.util.Patterns.EMAIL_ADDRESS.matcher(emailLogin.getText().toString()).matches()){
        Toast.makeText(UserAccount.this,R.string.email_error_msg,Toast.LENGTH_LONG).show();
    }
    else
    {
            new UserLogin().execute();
    }
    }
    public void logout(View view) {
        new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle(R.string.logout)
                .setMessage(R.string.logout_q)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db.UserLogout();
                        bookingDetailsLinear.removeAllViews();
                        loginWidgets.setVisibility(View.VISIBLE);
                        ObjectAnimator Anim1 = ObjectAnimator.ofFloat(loginWidgets, "y",-height, initial);
                        ObjectAnimator Anim2 = ObjectAnimator.ofFloat(userWidgets, "y", initial, height);
                        Anim1.setDuration(1000);
                        Anim2.setDuration(1000);
                        Anim1.start();
                        Anim2.start();

                    }
                }).setNegativeButton(R.string.no, null).show();

    }
    public void sign_up(View view) {
        password1Valid=false;
        password2Valid = false;
        nameValid=false;
        mobValid=false;
        email.setText(emailLogin.getText().toString());
        ObjectAnimator Anim1 = ObjectAnimator.ofFloat(loginWidgets, "y",initial, height);
        ObjectAnimator Anim2 = ObjectAnimator.ofFloat(signUpWidgets, "y", -height, initial);
        Anim1.setDuration(1000);
        Anim2.setDuration(1000);
        Anim1.start();
        signUpWidgets.setVisibility(View.VISIBLE);
        Anim2.start();
    }
    public void sign_up_filled(View view) {
        name.requestFocus();
        password.requestFocus();
        password2.requestFocus();
        mobileNo.requestFocus();
        email.requestFocus();
        name.clearFocus();
        password.clearFocus();
        password2.clearFocus();
        mobileNo.clearFocus();
        email.clearFocus();
        RadioGroup rButtons=(RadioGroup)findViewById(R.id.gender);
        switch (rButtons.getCheckedRadioButtonId())
        {
            case R.id.male:
                gender="MALE";
                genderValid=true;
                break;
            case R.id.female:
                gender="FEMALE";
                genderValid=true;
                break;
            default:Toast.makeText(UserAccount.this, R.string.gender_error_msg,Toast.LENGTH_LONG).show();
                genderValid=false;
        }
        if(nameValid && password1Valid && password2Valid && mobValid && emailValid &&genderValid) {
                        new AddUsers().execute();
        }
    }
    //-------------------adding a user to database----------------------------
    public class AddUsers extends AsyncTask<Void , Void, Void> {
        int status;StringBuilder sb;
        String strJson, postData, userName,passwordString,mobileString,emailString,genderString;
        JSONArray jsonArray4types;
        String msg;
        boolean pass;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            userName=name.getText().toString();
            passwordString=password.getText().toString();
            mobileString=mobileNo.getText().toString();
            emailString=email.getText().toString();
            genderString=gender;
            //----------encrypting password
            byte[] cleartext;
            try {
                cleartext = passwordString.getBytes("UTF8");
                passwordString= Base64.encodeToString(cipher.doFinal(cleartext), Base64.DEFAULT);
            } catch (UnsupportedEncodingException | BadPaddingException | IllegalBlockSizeException e) {
                e.printStackTrace();
            }

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String url =getResources().getString(R.string.url) + "GetServices.asmx/AddUsers";
            HttpURLConnection c = null;
            try {
                postData = "{\"username\":\"" +userName + "\",\"password\":\"" + passwordString + "\",\"mobile\":\"" + mobileString+ "\",\"email\":\"" +emailString + "\",\"gender\":\"" + genderString+ "\"}";
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
            new AlertDialog.Builder(UserAccount.this).setIcon(android.R.drawable.ic_dialog_alert)//.setTitle("")
                    .setMessage(msg)
                    .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(pass) {
                                ObjectAnimator Anim1 = ObjectAnimator.ofFloat(loginWidgets, "y", height, initial);
                                ObjectAnimator Anim2 = ObjectAnimator.ofFloat(signUpWidgets, "y", initial, -height);
                                Anim1.setDuration(1000);
                                Anim2.setDuration(1000);
                                Anim1.start();
                                Anim2.start();

                                name.setText("");
                                password.setText("");
                                password2.setText("");
                                mobileNo.setText("");
                                email.setText("");
                                emailLogin.setText("");
                                passwordLogin.setText("");
                            }
                        }
                    }).setCancelable(false)
                    .show();
        }
    }

    //------------------------Login--------------------------------------------
    public class UserLogin extends AsyncTask<Void , Void, Void> {
        int status;StringBuilder sb;
        String strJson, postData, userName,passwordString,mobileString,emailString,genderString;
        JSONArray jsonArray4types;
        String msg;
        boolean pass;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            db.UserLogout();            //refresh user details locally stored
            emailString=emailLogin.getText().toString();
            passwordString=passwordLogin.getText().toString();
            //----------encrypting password-----------------
            byte[] cleartext;
            try {
                cleartext = passwordString.getBytes("UTF8");
                passwordString= Base64.encodeToString(cipher.doFinal(cleartext), Base64.DEFAULT);
            } catch (UnsupportedEncodingException | BadPaddingException | IllegalBlockSizeException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String url =getResources().getString(R.string.url) + "GetServices.asmx/UserLogin";
            HttpURLConnection c = null;
            try {
                postData = "{\"email\":\"" +emailString + "\",\"password\":\"" + passwordString + "\"}";
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
                    userName=jsonObject.optString("user_name");
                    mobileString=jsonObject.optString("mobile_no");
                    genderString=jsonObject.optString("gender");

                }
            } catch (JSONException e) {

            }}
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if(!pass) {
                new AlertDialog.Builder(UserAccount.this).setIcon(android.R.drawable.ic_dialog_alert)//.setTitle("")
                        .setMessage(msg)
                        .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                passwordLogin.setText("");
                            }
                        }).setCancelable(false).show();
            }
            else {
                //hide keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(passwordLogin.getWindowToken(), 0);//.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);}

                //animation
                ObjectAnimator Anim1 = ObjectAnimator.ofFloat(loginWidgets, "y", initial, -height);
                ObjectAnimator Anim2 = ObjectAnimator.ofFloat(userWidgets, "y", height, initial);
                Anim1.setDuration(1000);
                Anim2.setDuration(1000);
                //set user details screen
                TextView name=(TextView)findViewById(R.id.user_name_view);
                TextView mobile=(TextView)findViewById(R.id.mob_no_view);
                TextView email=(TextView)findViewById(R.id.email_view);
                name.setText(userName);
                email.setText(emailString);
                mobile.setText(mobileString);

                Anim1.start();
                userWidgets.setVisibility(View.VISIBLE);
                Anim2.start();
                Toast.makeText(UserAccount.this,msg,Toast.LENGTH_SHORT).show();
                passwordLogin.setText("");
                //database acquiring user details
                db.UserLogin(userName, emailString, passwordString, mobileString, genderString);
                // booking details
                new GetBookedDetails().execute();

            }
        }
    }

    public void changeAccountDetails(View view) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(UserAccount.this);
        alert.setTitle(R.string.change_details);
        ScrollView scrl=new ScrollView(UserAccount.this);
        LinearLayout linr= new LinearLayout(UserAccount.this);
        linr.setOrientation(LinearLayout.VERTICAL);
        final TextView t1=new TextView(UserAccount.this);
        final TextView t2 = new TextView(UserAccount.this);
        t1.setText(R.string.password);
        linr.addView(t1);
        final EditText oldPassword=new EditText(UserAccount.this);
        oldPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        oldPassword.setHint(R.string.old_password);
        linr.addView(oldPassword);
        e1 = new EditText(UserAccount.this);
        e2 = new EditText(UserAccount.this);
        e1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        e2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        e1.setHint(R.string.new_password);
        e2.setHint(R.string.password_again);
        password1Valid=false;
        password2Valid = false;
        nameValid=false;
        mobValid=false;
        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(20);
        e1.setFilters(FilterArray);
        e1.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                } else {
                    e1.setText(e1.getText().toString().trim());
                    //length
                    if (e1.length() < 8 || e1.length() > 20) {
                        e1.setError(getResources().getString(R.string.password_error_msg_1));
                        password1Valid = false;
                    } else {
                        password1Valid = true;
                    }
                    //matching
                    if (!e2.getText().toString().equals(e1.getText().toString())) {
                        e2.setError(getResources().getString(R.string.password_error_msg_2));
                        password2Valid = false;
                    } else {
                        password2Valid = true;
                        e2.setError(null);
                    }
                }
            }
        });
        e2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                } else {
                    if (!e2.getText().toString().equals(e1.getText().toString())) {
                        e2.setError(getResources().getString(R.string.password_error_msg_2));
                        password2Valid = false;
                    } else {
                        password2Valid = true;
                    }
                }
            }
        });
        linr.addView(e1);
        linr.addView(e2);
        t2.setText(R.string.user_name);
        linr.addView(t2);
        e3 = new EditText(UserAccount.this);
        e3.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                } else {
                    e3.setText(e3.getText().toString().trim());
                    if (e3.length() < 4 || e3.length() > 30) {
                        e3.setError(getResources().getString(R.string.name_error_msg));
                        nameValid = false;
                    } else {
                        nameValid = true;
                    }
                }
            }
        });
        InputFilter[] FilterArray1 = new InputFilter[1];
        FilterArray1[0] = new InputFilter.LengthFilter(30);
        e3.setFilters(FilterArray1);
        e3.setSingleLine(true);
        linr.addView(e3);
        final TextView t3 = new TextView(UserAccount.this);
        t3.setText(R.string.mob_no);
        linr.addView(t3);
        e4 = new EditText(UserAccount.this);
        e4.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                } else {
                    e4.setText(e4.getText().toString().trim());
                    if (e4.length() < 4 || e4.length() > 20) {
                        e4.setError(getResources().getString(R.string.mobile_no_error_msg));
                        mobValid = false;
                    } else {
                        mobValid = true;
                    }
                }
            }
        });
        InputFilter[] FilterArray2 = new InputFilter[1];
        FilterArray2[0] = new InputFilter.LengthFilter(20);
        e4.setFilters(FilterArray2);
        e4.setInputType(InputType.TYPE_CLASS_PHONE);
        linr.addView(e4);
        final TextView t4 = new TextView(UserAccount.this);
        final String deleteText=getResources().getString(R.string.delete_account) +" " +db.GetUserDetail("Email");
        t4.setText(deleteText);
        t4.setTextColor(Color.BLUE);
        t4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//              alert.dis;
                AlertDialog.Builder alertDelete = new AlertDialog.Builder(UserAccount.this);
                alertDelete.setTitle(deleteText);
                ScrollView scrl = new ScrollView(UserAccount.this);
                LinearLayout linr = new LinearLayout(UserAccount.this);
                linr.setOrientation(LinearLayout.VERTICAL); //1 is for vertical orientation
                final TextView t1 = new TextView(UserAccount.this);
                t1.setText(R.string.password);
                linr.addView(t1);
                final EditText PasswordForDelete = new EditText(UserAccount.this);
                PasswordForDelete.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                linr.addView(PasswordForDelete);
                scrl.addView(linr);
                alertDelete.setView(scrl);

                alertDelete.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String olderPass = "";
                        byte[] cleartext;
                        try {
                            cleartext = PasswordForDelete.getText().toString().getBytes("UTF8");
                            olderPass= Base64.encodeToString(cipher.doFinal(cleartext), Base64.DEFAULT);
                        } catch (UnsupportedEncodingException | BadPaddingException | IllegalBlockSizeException e) {
                            e.printStackTrace();
                        }
                        if(olderPass.equals(db.GetUserDetail("Password"))){
                            new EditUserDetails().execute("delete");
                            db.UserLogout();
                            finish();
                        }
                        else {
                            Toast.makeText(UserAccount.this,R.string.password_incorrect_msg,Toast.LENGTH_LONG).show();
                            UserAccount.this.changeAccountDetails(new View(getApplicationContext()));
                        }

                    }
                });
                alertDelete.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Cancelled.
                    }
                });
                alertDelete.show();
            }

        });
        t4.setGravity(Gravity.CENTER);
        linr.addView(t4);
        e3.setText(db.GetUserDetail("UserName"));
        e4.setText(db.GetUserDetail("MobNo"));


        scrl.addView(linr);
        scrl.setPadding(20,20,20,20);
        alert.setView(scrl);
        alert.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                oldPassword.requestFocus();
                e1.requestFocus();
                e2.requestFocus();
                e3.requestFocus();
                e4.requestFocus();

                oldPassword.clearFocus();
                e1.clearFocus();
                e2.clearFocus();
                e3.clearFocus();
                e4.clearFocus();
                if(oldPassword.getText().toString().equals("")&&e1.getText().toString().equals("")&&e2.getText().toString().equals(""))
                {
                    changingPassword=false;
                }
                else{
                    changingPassword=true;
                }
               /* if(e3.getText().toString().equals(db.GetUserDetail("UserName")))
                    nameValid=true;
                if(e4.getText().toString().equals(db.GetUserDetail("MobNo")))
                    mobValid=true;*/

                if(!changingPassword){
                    if (nameValid && mobValid){
                        new EditUserDetails().execute("update");
                    }
                    else {
                        Toast.makeText(UserAccount.this,R.string.enter_valid_information,Toast.LENGTH_LONG).show();
                        UserAccount.this.changeAccountDetails(new View(getApplicationContext()));
                    }
                }
                else
                 {
                 if(password1Valid)
                 {
                            if(password2Valid){
                                String olderPass = "";
                                byte[] cleartext;
                                try {
                                    cleartext = oldPassword.getText().toString().getBytes("UTF8");
                                    olderPass= Base64.encodeToString(cipher.doFinal(cleartext), Base64.DEFAULT);
                                } catch (UnsupportedEncodingException | BadPaddingException | IllegalBlockSizeException e) {
                                    e.printStackTrace();
                                }
                                        if(olderPass.equals(db.GetUserDetail("Password"))){
                                            new EditUserDetails().execute("update");
                                        }
                                        else {
                                            Toast.makeText(UserAccount.this,R.string.password_incorrect_msg,Toast.LENGTH_LONG).show();
                                            UserAccount.this.changeAccountDetails(new View(getApplicationContext()));
                                        }
                              }
                            else{
                            Toast.makeText(UserAccount.this,R.string.password_error_msg_2,Toast.LENGTH_LONG).show();
                            UserAccount.this.changeAccountDetails(new View(getApplicationContext()));
                             }
                 }
                 else {
                  Toast.makeText(UserAccount.this,R.string.password_error_msg_1,Toast.LENGTH_LONG).show();
                  UserAccount.this.changeAccountDetails(new View(getApplicationContext()));
                 }
                }

            }
        });
        alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Cancelled.
            }
        });
        alert.show();

       /* db.UserLogout();
        loginWidgets.setVisibility(View.VISIBLE);
        ObjectAnimator Anim1 = ObjectAnimator.ofFloat(loginWidgets, "y",-height, initial);
        ObjectAnimator Anim2 = ObjectAnimator.ofFloat(userWidgets, "y", initial, height);
        Anim1.setDuration(1000);
        Anim2.setDuration(1000);
        Anim1.start();
        Anim2.start();*/
    }
    public class EditUserDetails extends AsyncTask<String , Void, Void> {
        int status;StringBuilder sb;
        String strJson, postData, userName,passwordString,mobileString,emailString,genderString;
        JSONArray jsonArray4types;
        String msg;
        boolean pass;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            userName=e3.getText().toString();
            passwordString=e1.getText().toString();
            mobileString=e4.getText().toString();
            emailString=db.GetUserDetail("Email");
            genderString=db.GetUserDetail("Gender");
            //----------encrypting password
            if(changingPassword) {
                byte[] cleartext;
                try {
                    cleartext = passwordString.getBytes("UTF8");
                    passwordString = Base64.encodeToString(cipher.doFinal(cleartext), Base64.DEFAULT);
                } catch (UnsupportedEncodingException | BadPaddingException | IllegalBlockSizeException e) {
                    e.printStackTrace();
                }
            }
            else passwordString=db.GetUserDetail("Password");

        }

        @Override
        protected Void doInBackground(String... arg0) {
            String url =getResources().getString(R.string.url) + "GetServices.asmx/EditUser";
            HttpURLConnection c = null;
            try {
                postData = "{\"username\":\"" +userName + "\",\"password\":\"" + passwordString + "\",\"mobile\":\"" + mobileString+ "\",\"email\":\"" +emailString + "\",\"type\":\"" + arg0[0]+ "\"}";
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


            new AlertDialog.Builder(UserAccount.this).setIcon(android.R.drawable.ic_dialog_alert)//.setTitle("")
                    .setMessage(msg)
                    .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (pass) {
                                db.UserLogout();
                                db.UserLogin(userName, emailString, passwordString, mobileString, genderString);
                                Intent intent=new Intent(UserAccount.this,UserAccount.class);
                                startActivity(intent);
                                finish();
                            }
                        }
                    }).setCancelable(false)
                    .show();
               // Toast.makeText(UserAccount.this,postData,Toast.LENGTH_LONG).show();
        }
    }
    public void seeBookedItems(View view) {
        booked_button.setBackgroundColor(Color.parseColor("#5e26b2"));
        booked_button.setTextColor(Color.WHITE);
        visited_can.setBackgroundColor(Color.parseColor("#FF707070"));
        visited_can.setEnabled(true);
        booked_button.setEnabled(false);
        ObjectAnimator Anim1 = ObjectAnimator.ofFloat(bookingDetailsScroll, "x", -width, 0);
        ObjectAnimator Anim2 = ObjectAnimator.ofFloat(visitedDetailsScroll, "x",0, width);
        Anim1.setDuration(300);
        Anim2.setDuration(300);
        Anim1.start();
        Anim2.start();
    }
    //------------------------show booked items-----------------------------------
    public class GetBookedDetails extends AsyncTask<Void , Void, Void> {
        int status;StringBuilder sb;
        String strJson, postData;
        JSONArray jsonArray4types;
        ArrayList<String> imageurl,bookingID,bookingDetails,providerName,serviceCode,sTypeCode,providerCode,bookingTime,contact1,contact2,bookedTime,rate,cancelledTime;
        ArrayList<Boolean> active;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            imageurl=new ArrayList<>();
            bookingID=new ArrayList<>();
            bookingDetails=new ArrayList<>();
            serviceCode=new ArrayList<>();
            sTypeCode=new ArrayList<>();
            providerCode=new ArrayList<>();
            providerName=new ArrayList<>();
            bookingTime =new ArrayList<>();
            contact1=new ArrayList<>();
            contact2=new ArrayList<>();
            bookedTime=new ArrayList<>();
            rate=new ArrayList<>();
            active=new ArrayList<>();
            cancelledTime=new ArrayList<>();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String url =getResources().getString(R.string.url) + "GetServices.asmx/GetBookingDetails";
            HttpURLConnection c = null;
            try {
                postData = "{\"Email\":\"" +db.GetUserDetail("Email")+ "\"}";
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
                        if(strJson.equals("[]")) break;
                        strJson="{\"Message\":" + strJson.replace("\\\"","\"") + "}";
                }
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
            if(strJson!=null && !strJson.equals("[]"))
            {bookings=true;
                try {
                JSONObject jsonRootObject = new JSONObject(strJson);
                jsonArray4types = jsonRootObject.optJSONArray("Message");
                for (int i = 0; i < jsonArray4types.length(); i++) {
                    JSONObject jsonObject = jsonArray4types.getJSONObject(i);
                    bookingID.add(jsonObject.optString("BookingID"));
                    bookingDetails.add(jsonObject.optString("ServiceName")+" : "+jsonObject.optString("ServiceTypeName"));
                    providerName.add(jsonObject.optString("ProviderName"));
                    bookingTime.add(jsonObject.optString("BookingTime").replace("\\/Date(", "").replace(")\\/", ""));
                    serviceCode.add(jsonObject.optString("ServiceCode"));
                    sTypeCode.add(jsonObject.optString("ServiceTypeCode"));
                    providerCode.add(jsonObject.optString("ServiceProviderCode"));
                    contact1.add(jsonObject.optString("Contact1"));
                    contact2.add(jsonObject.optString("Contact2"));
                    rate.add(jsonObject.optString("Rate"));
                    bookedTime.add(jsonObject.optString("BookedTime").replace("\\/Date(", "").replace(")\\/", ""));
                    cancelledTime.add(jsonObject.optString("CanceledTime").replace("\\/Date(", "").replace(")\\/", ""));

                    active.add(jsonObject.optBoolean("Active"));
                    imageurl.add(jsonObject.optString("StyleImg", "NOIMAGE"));

                }
            } catch (JSONException e) {

            }}
            else {
                bookings=false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
           //         Toast.makeText(UserAccount.this,strJson,Toast.LENGTH_LONG).show();
          //  Toast.makeText(UserAccount.this,Boolean.toString(bookings),Toast.LENGTH_LONG).show();
            final SimpleDateFormat formatted = new SimpleDateFormat("HH:mm    dd-MM-yyyy", Locale.US);
        //    String formattedTime = formatted.format(finalTime.getTime());
          // Calendar cal=Calendar.getInstance();

            int i=0;
            for(;i<bookingID.size();i++)
            {
                View Details= inflater.inflate(R.layout.booked_item, null);
                TextView textView=(TextView) Details.findViewById(R.id.item_details);
                textView.setText(bookingDetails.get(i));
                TextView textView2=(TextView) Details.findViewById(R.id.providerName);
                textView2.setText(providerName.get(i));
                TextView textView3=(TextView) Details.findViewById(R.id.bookingTime);
                final Calendar cal=Calendar.getInstance();
                cal.setTimeInMillis(Long.parseLong(bookingTime.get(i)));
                textView3.setText(formatted.format(cal.getTime()));
                ImageView imageView=(ImageView)Details.findViewById(R.id.itemImg);
                String tempurl=getResources().getString(R.string.url)+imageurl.get(i).substring(imageurl.get(i).indexOf("tempImages"));
                Picasso.with(UserAccount.this)
                        .load(tempurl)
                        .placeholder(R.drawable.loadingimage)
                        .into(imageView);
                final int finalI = i;
                Details.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final AlertDialog.Builder alert = new AlertDialog.Builder(UserAccount.this);
                        alert.setTitle(R.string.booking_details);
                        ScrollView scrl = new ScrollView(UserAccount.this);
                        LinearLayout linr = new LinearLayout(UserAccount.this);
                        linr.setOrientation(LinearLayout.VERTICAL);

                        final TextView t1 = new TextView(UserAccount.this);
                        final TextView t2 = new TextView(UserAccount.this);
                        final TextView t3 = new TextView(UserAccount.this);
                        final TextView t4 = new TextView(UserAccount.this);
                        final TextView t5 = new TextView(UserAccount.this);
                        final TextView t6 = new TextView(UserAccount.this);
                        final TextView t7 = new TextView(UserAccount.this);
                        final TextView t8 = new TextView(UserAccount.this);


                        t1.setTextSize(20);
                        t2.setTextSize(20);
                        t3.setTextSize(20);
                        t4.setTextSize(20);
                        t5.setTextSize(20);
                        t6.setTextSize(20);
                        t7.setTextSize(20);

                        t1.setTextColor(Color.BLACK);
                        t2.setTextColor(Color.BLACK);
                        t3.setTextColor(Color.BLUE);
                        t4.setTextColor(Color.BLACK);
                        t5.setTextColor(Color.BLUE);
                        t6.setTextColor(Color.BLUE);
                        t7.setTextColor(Color.BLACK);

                        t1.setText(bookingDetails.get(finalI));
                        t2.setText(getResources().getString(R.string.place) + ": " + providerName.get(finalI));
                        if(active.get(finalI)&&cal.after(Calendar.getInstance()))
                                 {t3.setText(getResources().getString(R.string.time) + ": " + formatted.format(cal.getTime()) + " ⏰");
                        t3.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Intent.ACTION_EDIT);
                                intent.setType("vnd.android.cursor.item/event"); //working is to be checked with "intent.setData(CalendarContract.Events.CONTENT_URI);" on failure
                                intent.putExtra("beginTime", cal.getTimeInMillis());
                                intent.putExtra("allDay", false);
                                //  intent.putExtra("endTime", cal.getTimeInMillis() + 60 * 60 * 1000);
                                intent.putExtra("title", "Visit: " + providerName.get(finalI));
                                intent.putExtra("eventLocation", "Location of Beauty parlour");
                                intent.putExtra("description", "Booked using " + getResources().getString(R.string.app_name));
                                startActivity(intent);
                            }
                        });}
                        else {
                            t3.setTextColor(Color.BLACK);
                            t3.setText(getResources().getString(R.string.time) + ": " + formatted.format(cal.getTime()));
                        }
                        Calendar cal2 = Calendar.getInstance();
                        if(active.get(finalI))
                            {
                                cal2.setTimeInMillis(Long.parseLong(bookedTime.get(finalI)));
                                t4.setText(getResources().getString(R.string.booked_at) + ": " + formatted.format(cal2.getTime()));
                            }
                        else{
                            cal2.setTimeInMillis(Long.parseLong(cancelledTime.get(finalI)));
                            t4.setText(getResources().getString(R.string.cancelled_at) + ": " + formatted.format(cal2.getTime()));
                        }
                        t5.setText(getResources().getString(R.string.contact) + ": " + contact1.get(finalI) + " ☎");
                        t5.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Uri number = Uri.parse("tel:" + contact1.get(finalI));
                                Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
                                startActivity(callIntent);
                            }
                        });
                        t6.setText(getResources().getString(R.string.contact2) + ": " + contact2.get(finalI) + " ☎");
                        t6.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Uri number = Uri.parse("tel:" + contact2.get(finalI));
                                Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
                                startActivity(callIntent);
                            }
                        });
                        t7.setText(getResources().getString(R.string.rate) + ": " + rate.get(finalI));
                        t8.setText(getResources().getString(R.string.booking_id) + ": " + bookingID.get(finalI));

                        linr.addView(t1);
                        linr.addView(t2);
                        linr.addView(t3);
                        linr.addView(t4);
                        linr.addView(t5);
                        if (!contact2.get(finalI).equals("null"))
                            linr.addView(t6);
                        linr.addView(t7);
                        linr.addView(t8);

                        scrl.addView(linr);
                        scrl.setPadding(20, 20, 20, 20);
                        alert.setView(scrl);
                        alert.setPositiveButton(R.string.more_details, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Intent intent = new Intent(UserAccount.this, BeautyParlour.class);
                                intent.putExtra("provider", providerCode.get(finalI));
                                intent.putExtra("service", serviceCode.get(finalI));
                                intent.putExtra("type", sTypeCode.get(finalI));
                                startActivity(intent);
                            }
                        });
                        alert.setNegativeButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Cancelled.
                            }
                        });
                        alert.show();

                    }
                });
                //cancel booking---------------------------------------------------
                Button cancel=(Button)Details.findViewById(R.id.cancel_booking);
                if(active.get(finalI)&&cal.after(Calendar.getInstance()))
                {
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        new AlertDialog.Builder(UserAccount.this).setIcon(android.R.drawable.ic_dialog_alert).setTitle(R.string.cancel)
                                .setMessage(R.string.cancel_booking_q)
                                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        new CancelBooking().execute(bookingID.get(finalI));
                                    }
                                }).setNegativeButton(R.string.no, null).show();
                    }
                });
                bookingDetailsLinear.addView(Details);}
                else {
                    cancel.setVisibility(View.GONE);
                    visitedCancelledLinear.addView((Details));
                }
            }
            if (i==0) //no other types are available
            {
                TextView textView=new TextView(UserAccount.this);
                textView.setText(R.string.no_bookings);
                bookingDetailsLinear.addView(textView);
                visitedCancelledLinear.addView(textView);
            }
            bookingDetailsScroll.addView(bookingDetailsLinear);
            visitedDetailsScroll.addView(visitedCancelledLinear);
            ProgressBar progressBar=(ProgressBar)findViewById(R.id.progressBar_bookings);
            progressBar.setVisibility(View.GONE);

        }
    }

    //----------------------cancel a booking----------------------------
    public class CancelBooking extends AsyncTask<String , Void, Void> {
        int status;StringBuilder sb;
        String strJson, postData;//,formattedTime;
        JSONArray jsonArray4types;
        String msg;
        boolean pass;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        //    SimpleDateFormat formatted = new SimpleDateFormat("yyyy MM dd HH:mm:ss", Locale.US);
       //     formattedTime = formatted.format(finalTime.getTime());
        }

        @Override
        protected Void doInBackground(String... arg0) {
            String url =getResources().getString(R.string.url) + "GetServices.asmx/Booking";
            HttpURLConnection c = null;
            try {
                postData = "{\"bookingID\":\"" + arg0[0] + "\",\"user\":\"" + " "+ "\",\"serviceCode\":\"" + " " + "\",\"sTypeCode\":\"" + " " + "\",\"providerCode\":\"" +" " + "\",\"timing\":\"" + "12-09-2015 05:00" + "\",\"bookORcancel\":\"" + "cancel"+ "\"}";//dummy time
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

            Toast.makeText(UserAccount.this, postData, Toast.LENGTH_LONG).show();
            new AlertDialog.Builder(UserAccount.this).setIcon(android.R.drawable.ic_dialog_alert)//.setTitle("")
                    .setMessage(msg)
                    .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (pass) {
                                Intent intent=new Intent(UserAccount.this,UserAccount.class);
                                startActivity(intent);
                                finish();
                            }
                        }
                    }).show();
            //    Toast.makeText(Booking.this, postData, Toast.LENGTH_LONG).show();
        }
    }
    public void view_visited_cancelled(View view) {
        visitedDetailsScroll.setVisibility(View.VISIBLE);
        booked_button.setBackgroundColor(Color.parseColor("#FF707070"));
        visited_can.setBackgroundColor(Color.parseColor("#5e26b2"));
        visited_can.setTextColor(Color.WHITE);
        visited_can.setEnabled(false);
        booked_button.setEnabled(true);
        ObjectAnimator Anim1 = ObjectAnimator.ofFloat(bookingDetailsScroll, "x",0, -width);
        ObjectAnimator Anim2 = ObjectAnimator.ofFloat(visitedDetailsScroll, "x", width, 0);
        Anim1.setDuration(300);
        Anim2.setDuration(300);
        Anim1.start();
        Anim2.start();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);
        MenuItem refresh = menu.findItem(R.id.user);
        refresh.setVisible(false);
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
