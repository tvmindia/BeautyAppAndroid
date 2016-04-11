package com.tech.thrithvam.beautyapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImTxtAdapter extends BaseAdapter
{
    DatabaseHandler db;
    ArrayList<String> providerName;
    ArrayList<String> providerCode;
    String serviceCode, serviceTypeCode;
    ArrayList<String> link;
    ArrayList<Boolean> favs;
    Context context;
    ArrayList<String> imageUrl;
    private static LayoutInflater inflater=null;
    //-----------initializing gridview adapter with passed values-----------
    public ImTxtAdapter(Activity mainActivity, ArrayList<String> provider,ArrayList<String> pCode,String sCode, String stCode,ArrayList<String> links,ArrayList<Boolean> fav, ArrayList<String> Img) {
        providerName =provider;
        providerCode=pCode;
        link=links;
        favs=fav;
        serviceTypeCode =stCode;
        serviceCode=sCode;
        context=mainActivity;
        imageUrl =Img;
        inflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        db= new DatabaseHandler(context);
    }

    @Override
    public int getCount() {
        return providerName.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    //--------Holder for holding views in each grid item-------------
    public class Holder
    {
        TextView textView;
        ImageView imgView;
        Button share_button;
        Button fav_button;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

            final Holder holder;
            View rowView=convertView;
        //-----------if it is first time on screen--------------
            if(convertView==null) {
                rowView = inflater.inflate(R.layout.grid_item, null);
                holder = new Holder();
                holder.textView = (TextView) rowView.findViewById(R.id.gridTxt);
                holder.imgView = (ImageView) rowView.findViewById(R.id.gridImg);
                holder.share_button = (Button) rowView.findViewById(R.id.share_button);
                holder.fav_button = (Button) rowView.findViewById(R.id.fav_button);
                rowView.setTag(holder);
            }
            //-------------recycling ---------
        else {
                holder=(Holder)rowView.getTag();
            }
        //----------Set each holder item values----------
        holder.textView.setText(providerName.get(position));
        holder.share_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!link.get(position).equals("null")) {
                    Toast.makeText(context, link.get(position), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, link.get(position));
                    intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Check out this!");
                    context.startActivity(Intent.createChooser(intent, "Share"));
                } else
                    Toast.makeText(context, R.string.no_link_msg, Toast.LENGTH_SHORT).show();
            }
        });
        if(favs.get(position))
            holder.fav_button.setBackgroundResource(R.drawable.fav);
        else
            holder.fav_button.setBackgroundResource(R.drawable.fav_not);
        holder.fav_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              if(db.GetUserDetail("Email").equals("")){
                Toast.makeText(context,R.string.please_login,Toast.LENGTH_LONG).show();
                Intent intentUser = new Intent(context, UserAccount.class);
                context.startActivity(intentUser);
              }
              else{
                if(!favs.get(position)) {
                    new AddToFavorites().execute(providerCode.get(position),"add");
                    holder.fav_button.setBackgroundResource(R.drawable.fav);
                    favs.set(position, true);
                    Toast.makeText(context, R.string.add_fav_msg, Toast.LENGTH_SHORT).show();
                }
                else
                {
                    new AddToFavorites().execute(providerCode.get(position),"remove");
                    holder.fav_button.setBackgroundResource(R.drawable.fav_not);
                    favs.set(position,false);
                    Toast.makeText(context, R.string.remove_fav_msg, Toast.LENGTH_SHORT).show();
                }
              }

            }
        });
        if(!imageUrl.get(position).equals("NOIMAGE")) {                 //image downloading
        String tempurl=context.getResources().getString(R.string.url) + imageUrl.get(position).substring(imageUrl.get(position).indexOf("tempImages"));
            Picasso.with(context)
                    .load(tempurl)
                    .into(holder.imgView)
            ;
        }
        //-----------each grid item listener------------
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                        Intent intent = new Intent(context, BeautyParlour.class);
                        intent.putExtra("provider", providerCode.get(position));
                        intent.putExtra("service",serviceCode);
                        intent.putExtra("type", serviceTypeCode);
                        context.startActivity(intent);
                    }
                });

            return rowView;
        }
    public class AddToFavorites extends AsyncTask<String, Void, Void> {
        int status;
        String postData;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... arg0) {
            String url = context.getResources().getString(R.string.url) + "GetServices.asmx/AddToFavorites";
            HttpURLConnection c = null;
            try {
                postData = "{\"user\":\"" + db.GetUserDetail("Email") + "\",\"sTypeCode\":\"" + serviceTypeCode + "\",\"providerCode\":\"" + arg0[0]+ "\",\"addORremove\":\"" + arg0[1] + "\"}";
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

}
