package com.example.android.sunshine.app;

/**
 * Created by Chandra on 20-09-2015.
 */

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.text.format.Time;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {
    public final static String EXTRA_MESSAGE = "com.example.android.sunshine.app.MESSAGE";
    private ArrayAdapter<String> mForecastAdapter;

    public ForecastFragment() {
    }

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment,menu);
    }

    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if (id == R.id.action_refresh){
            FetchWeatherTask weatherTask=new FetchWeatherTask();
            weatherTask.execute("560035,IN");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ArrayList<String> weekForecast = new ArrayList<String>();
        weekForecast.add("Today Cloudy 88/68");
        weekForecast.add("Tommorow Sunny 55/68");
        weekForecast.add("Weds Rainy 88/68");
        weekForecast.add("Thurs Sunny 88/77");
        weekForecast.add("Fri Sunny 98/68");
        weekForecast.add("Sat Windy 88/54");
        weekForecast.add("Sun Foggy 65/45");

        mForecastAdapter =
                new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast,
                        R.id.list_item_forecast_textview, weekForecast);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);
        FetchWeatherTask weatherTask=new FetchWeatherTask();
        weatherTask.execute("560035,IN");
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            public void onItemClick(AdapterView<?> adapterView, View view,int i, long l ){

                String forecast = mForecastAdapter.getItem(i);
                /*Toast.makeText(getActivity(),forecast,Toast.LENGTH_SHORT).show();*/
                Intent intent = new Intent(getActivity(),DetailActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, forecast);
                startActivity(intent);
            }
        });

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.

        return rootView;
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        private String forecastJsonStr = null;

        private final String LogTag = FetchWeatherTask.class.getSimpleName();

        private String getReadableDateString(long time){
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        private String formatHighLows(double high, double low) {
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);
            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        @Override
        protected void onPostExecute(String[] result) {
            if(result!= null){
                mForecastAdapter.clear();
                mForecastAdapter.addAll(result);
                }
        }


        private String[] getWeatherFromJson(String forecastJsonStr1,int days )throws JSONException{


            final String day_list = "list";
            final String day_temp = "temp";
            final String day_max = "max";
            final String day_min = "min";
            final String day_desp = "main";
            final String day_weather = "weather";

            JSONObject weatherJson = new JSONObject(forecastJsonStr1);
            JSONArray weatherArray = weatherJson.getJSONArray(day_list);

            Time dayTime = new Time();
            dayTime.setToNow();

            int julianday = Time.getJulianDay(System.currentTimeMillis(),dayTime.gmtoff);

            dayTime = new Time();

            String[] weatherForecast = new String[days];

            for(int i = 0;i<weatherArray.length();i++){

                String day;
                String highlow;
                String descrip;

                long dateTime;

                dateTime = dayTime.setJulianDay(julianday+i);

                day = getReadableDateString(dateTime);

                JSONObject weather = weatherArray.getJSONObject(i);
                JSONObject temp = weather.getJSONObject(day_temp);
                JSONArray dayWeather = weather.getJSONArray(day_weather);
                JSONObject dayDesc = dayWeather.getJSONObject(0);

                double high = temp.getDouble(day_max);
                double low = temp.getDouble(day_min);
                descrip = dayDesc.getString(day_desp);

                highlow = formatHighLows(high, low);

                weatherForecast[i] = day + "-" + descrip + "-" + highlow;
            }
            for(String s : weatherForecast){
                Log.v(LogTag,"Forecast" + s);
            }
            return weatherForecast;
        }

        protected String[] doInBackground(String... params){

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;


            // Will contain the raw JSON response as a string.

            String format = "JSON";
            String unit = "metric";
            int days = 7;
            String appid  = "2de143494c0b295cca9337e1e96b00e0";


            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                /*URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=560035,IN&mode=JSON&units=metric&cnt=7&appid=bd82977b86bf27fb59a04b61b657fb6f");*/
                final String baseURL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String query = "q";
                final String format_param = "mode";
                final String unit_param = "units";
                final String days_param = "cnt";
                final String appid_param = "appid";

                Uri buildURL = Uri.parse(baseURL).buildUpon().
                        appendQueryParameter(query,params[0]).
                        appendQueryParameter(format_param,format).
                        appendQueryParameter(unit_param,unit).
                        appendQueryParameter(days_param,Integer.toString(days)).
                        appendQueryParameter(appid_param,appid).build();

                URL url = new URL(buildURL.toString());
                Log.v(LogTag, " Built URl " + url);

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();

                return getWeatherFromJson(forecastJsonStr,7);

            } catch (IOException e) {
                Log.e(LogTag, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;

            }
            catch (JSONException e) {
                Log.e(LogTag, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;

            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LogTag, "Error closing stream", e);
                    }
                }
            }
        }

    }

}
