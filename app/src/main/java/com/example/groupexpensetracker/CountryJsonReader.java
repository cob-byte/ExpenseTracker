package com.example.groupexpensetracker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.JsonIOException;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

public class CountryJsonReader {

    private JSONArray countries;
    private Context context;

    public CountryJsonReader(Context context) throws JSONException {
        this.context = context;
        this.countries = new JSONArray(loadJSONFromAsset(context));
    }

    public int getArraySize(){
        return this.countries.length();
    }

    public String getCountryNameOnIndex(int i) throws JSONException {
        String country = null;
        country = this.countries.getJSONObject(i).getString("name");
        return country;
    }

    public ArrayList<String> getCountryNameList() throws JSONException {
        ArrayList<String> country_list = new ArrayList<String>(getArraySize());
        for(int i = 0; i < getArraySize(); ++i){
            country_list.add(getCountryNameOnIndex(i));
        }
        return country_list;
    }

    public TreeSet<String> getCurrencySet() throws JSONException {
        TreeSet<String> currency_set = new TreeSet<>();
        for(int i = 0; i < getArraySize(); ++i){
            currency_set.add(getCountryCurrencyOnIndex(i));
        }
        return currency_set;
    }

    public String getCountryCurrencyOnIndex(int i) throws JSONException{
        String currency = null;
        currency = this.countries.getJSONObject(i)
                .getJSONArray("currencies")
                .getJSONObject(0)
                .getString("code");
        return currency;
    }


    private String loadJSONFromAsset(Context context) {
        String json = null;
        try {
            InputStream is = context.getAssets().open("country_list.json");

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");


        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}
