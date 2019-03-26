package com.workstation.pocketmovies;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    ImageAdapter mImageAdapter;
    RecyclerView mGridView;
    private SearchView searchView;
    private String latestQuery;
    private ProgressDialog progressDialog;
    public SearchResultModel searchResult;
    private int pagesLoaded;
    private boolean reachedEnd;
    public static ArrayList<MovieModel> movies;
    public static ArrayList<MovieModel> temp;
    public TextView message;
    private OnLoadMoreListener mOnLoadMoreListener;
    public static boolean gridView;
    FloatingActionButton sortFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGridView = (RecyclerView) findViewById(R.id.grid_recycler);
        sortFab = (FloatingActionButton) findViewById(R.id.sort_fab);
        movies = new ArrayList<>();

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(),1, LinearLayoutManager.VERTICAL,false);
        mGridView.setLayoutManager(gridLayoutManager);

        message = (TextView) findViewById(R.id.message);

        mImageAdapter = new ImageAdapter(this, movies);
        mGridView.setAdapter(mImageAdapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        setupLazyLoad();

        sortFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(MainActivity.this, sortFab);
                popup.getMenuInflater().inflate(R.menu.sort_menu, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.sort_rating:
                                sortByRating();
                                mImageAdapter.notifyDataSetChanged();
                                return true;

                            case R.id.sort_release:
                                sortByRelease();
                                mImageAdapter.notifyDataSetChanged();
                                return true;

                            default:
                                return true;
                        }
                    }
                });
                popup.show();
            }
        });
    }

    private void sortByRelease()
    {
        Collections.sort(movies, new Comparator<MovieModel>(){
            @Override
            public int compare(MovieModel obj1, MovieModel obj2) {
                // ## Ascending order
                return obj1.getReleased().compareToIgnoreCase(obj2.getReleased());
              }
        });
    }

    private void sortByRating()
    {
        Collections.sort(movies, new Comparator<MovieModel>(){
            @Override
            public int compare(MovieModel obj1, MovieModel obj2) {
                // ## Ascending order
                return obj1.getImdbRating().compareToIgnoreCase(obj2.getImdbRating());
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                MainActivity.this.latestQuery = query;
                getData(query, true);
                if (progressDialog != null && !progressDialog.isShowing()) {
                    progressDialog.show();
                }
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        };
        searchView.setOnQueryTextListener(queryTextListener);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_grid:
                if (gridView){
                    item.setIcon(R.mipmap.grid1);
                    mGridView.setLayoutManager(new GridLayoutManager(this, 1));
                    gridView = false;
                }else {
                    item.setIcon(R.mipmap.list);
                    mGridView.setLayoutManager(new GridLayoutManager(this, 3));
                    gridView = true;
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void getData(final String query, boolean newQuery) {
        if (newQuery) {
            movies.clear();
            mImageAdapter.notifyDataSetChanged();
            pagesLoaded = 0;
            reachedEnd = false;
            NetworkFactory.Factory.getInstance().search(query, "movie", 1).enqueue(new Callback<SearchResultModel>() {
                @Override
                public void onResponse(Call<SearchResultModel> call, Response<SearchResultModel> response) {
                    searchResult = response.body();
                    if (searchResult.getResponse().equals("True")) {
                        //Movie Found
                        pagesLoaded = 1;
                        getMovies();
                        message.setVisibility(View.GONE);
                        mGridView.setVisibility(View.VISIBLE);
                    } else {
                        //Movie not found
                        progressDialog.dismiss();
                        message.setText(R.string.no_results);
                        message.setVisibility(View.VISIBLE);
                        mGridView.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailure(Call<SearchResultModel> call, Throwable t) {
                    Log.e("LOG TAG", "Failure : " + t.getMessage());
                    progressDialog.dismiss();
                    message.setText(R.string.server_error);
                    message.setVisibility(View.VISIBLE);
                    mGridView.setVisibility(View.GONE);
                }
            });
        } else {
            if (!reachedEnd) {
                pagesLoaded++;
                NetworkFactory.Factory.getInstance().search(query, "movie", pagesLoaded).enqueue(new Callback<SearchResultModel>() {
                    @Override
                    public void onResponse(Call<SearchResultModel> call, Response<SearchResultModel> response) {
                        searchResult = response.body();
                        if (searchResult.getResponse().equals("True")) {
                            //Movie Found
                            getMovies();
                        } else {
                            //Reached End
                            movies.remove(movies.size() - 1);
                            reachedEnd = true;
                            mImageAdapter.notifyItemRemoved(movies.size());
                            mImageAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(Call<SearchResultModel> call, Throwable t) {
                        movies.remove(movies.size() - 1);
                        mImageAdapter.notifyItemRemoved(movies.size());
                        mImageAdapter.notifyDataSetChanged();
                    }
                });
            }
        }
    }

    public void getMovies() {
        final int[] count = {0};
        for (int i = 0; i < searchResult.getSearch().size(); i++) {
            String imdbId = searchResult.getSearch().get(i).getImdbID();
            NetworkFactory.Factory.getInstance().getMovie(imdbId).enqueue(new Callback<MovieModel>() {
                @Override
                public void onResponse(Call<MovieModel> call, Response<MovieModel> response) {
                    movies.add(response.body());
                    Log.d("LOG TAG", movies.get(movies.size() - 1).getTitle());
                    count[0]++;
                    isDataFetchComplete(count[0]);
                }

                @Override
                public void onFailure(Call<MovieModel> call, Throwable t) {
                    Log.e("LOG TAG", "Failure : " + t.getMessage());
                    count[0]++;
                    isDataFetchComplete(count[0]);
                }
            });
        }
    }

    private void isDataFetchComplete(int count) {
        if (searchResult.getResponse().equals("True") && count == searchResult.getSearch().size()) {
            progressDialog.dismiss();
            for (int i = 0; i < movies.size(); i++) {
                if (movies.get(i) == null) {
                    movies.remove(i);
                    mImageAdapter.notifyItemRemoved(i);
                }
            }

            mImageAdapter.notifyDataSetChanged();
        }
    }

    private void setupLazyLoad() {

        setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (!reachedEnd) {
                    movies.add(null);
                    mImageAdapter.notifyItemInserted(movies.size() - 1);
                    getData(latestQuery, false);
                }
            }
        });
    }

    public void setOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener) {
        this.mOnLoadMoreListener = mOnLoadMoreListener;
    }
}
