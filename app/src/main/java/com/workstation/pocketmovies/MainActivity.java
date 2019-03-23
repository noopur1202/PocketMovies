package com.workstation.pocketmovies;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    ImageAdapter mImageAdapter;
    List<String> image, title, description, release, rating, imdbId;
    GridView mGridView;
    ImageView settingsIcon, listIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGridView = (GridView) findViewById(R.id.grid_main);
        settingsIcon = (ImageView) findViewById(R.id.settings_icon);
        listIcon = (ImageView) findViewById(R.id.list_icon);

        image = new ArrayList<String>();
        title = new ArrayList<String>();

        UpdateScreen();

        mImageAdapter = new ImageAdapter(this, image,title);
        mGridView.setAdapter(mImageAdapter);

        listIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mGridView.getNumColumns()==1){
                    listIcon.setImageResource(R.mipmap.list);
                    mGridView.setNumColumns(2);
                }else {
                    listIcon.setImageResource(R.mipmap.grid1);
                    mGridView.setNumColumns(1);
                }
            }
        });

        settingsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(MainActivity.this, settingsIcon);
                popup.getMenuInflater().inflate(R.menu.menu_main, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        return true;
                    }
                });
                popup.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        Drawable yourdrawable = menu.getItem(0).getIcon(); // change 0 with 1,2 ...
        yourdrawable.mutate();
        yourdrawable.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sort_rating) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void UpdateScreen() {
        FetchDetails categoryFetch = new FetchDetails();
        categoryFetch.execute();
    }

    public class FetchDetails extends AsyncTask<Void, Void, List<String>> {

        private final String LOG_TAG = FetchDetails.class.getSimpleName();

        private List<String> getdataFromJson(String json_string) throws JSONException {
            JSONObject jsonObject = new JSONObject(json_string);
            JSONArray jsonArray = jsonObject.getJSONArray("Search");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject categoryList = jsonArray.getJSONObject(i);
                title.add(categoryList.getString("Title"));
                image.add(categoryList.getString("Poster"));
                imdbId.add(categoryList.getString("imdbID"));
            }
            Log.v(LOG_TAG, "titles_categories" + title);
            Log.v(LOG_TAG, "posters" + image);

            return title;
        }

        @Override
        protected List<String> doInBackground(Void... voids) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String json_string = null;

            try {
                final String BASE_URL = "http://www.omdbapi.com/?s=Lord&page=1&apikey=9cfa95f8";

                URL url = new URL(BASE_URL);
                Log.v(LOG_TAG, "url" + BASE_URL);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                if (inputStream == null) {
                    json_string = null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "/n");
                }

                if (buffer.length() == 0) {
                    json_string = null;
                }

                json_string = buffer.toString();
                Log.v(LOG_TAG, "json_string" + json_string);
            } catch (IOException e) {
                Log.e(LOG_TAG, "error", e);
                json_string = null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "error closing stream", e);
                    }
                }
            }
            try {
                return getdataFromJson(json_string);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<String> result) {
            super.onPostExecute(result);
            mImageAdapter.clear();
            mImageAdapter.notifyDataSetChanged();
            for (String r : result) {
                mImageAdapter.add(r);
            }
        }
    }
}
