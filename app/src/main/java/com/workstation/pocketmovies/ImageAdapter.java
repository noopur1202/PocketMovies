package com.workstation.pocketmovies;

import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private final int displayMovie = 0;
    private final int displayLoading = 1;
    private Context context;
    private ArrayList<MovieModel> movies;


    public ImageAdapter(Context context, ArrayList<MovieModel> movies) {
        this.context = context;
        this.movies = movies;
    }



    @Override
    public int getItemViewType(int position) {
        return movies.get(position) == null ? displayLoading : displayMovie;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == displayMovie) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_image, parent, false);
            return new MovieViewHolder(view);
        } else if (viewType == displayLoading) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.progress_bar_layout, parent, false);
            return new LoadingViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MovieViewHolder) {
            MovieModel movie = movies.get(position);
            MovieViewHolder movieViewHolder = (MovieViewHolder) holder;

            Picasso.with(context).load(movie.getPoster()).fit().into(movieViewHolder.poster);
            movieViewHolder.title.setText(movie.getTitle());
            movieViewHolder.description.setText(movie.getPlot());
        } else if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {

        ImageView poster;
        TextView title, description;

        public MovieViewHolder(View itemView) {
            super(itemView);
            poster = (ImageView) itemView.findViewById(R.id.movie_image);
            title = (TextView) itemView.findViewById(R.id.movie_name);
            description = (TextView) itemView.findViewById(R.id.movie_description);
        }
    }

    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public LoadingViewHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar);
        }
    }
}
