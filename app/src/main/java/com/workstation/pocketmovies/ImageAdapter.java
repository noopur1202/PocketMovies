package com.workstation.pocketmovies;

import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ImageAdapter extends ArrayAdapter{

    private List<String> imageUrl;
    private List<String> title;
    private Context ctx;
    private LayoutInflater inflater;

    public ImageAdapter(Activity context, List<String> images, List<String> title)
    {
        super(context, R.layout.single_image);
        this.ctx = context;
        this.imageUrl=images;
        this.title = title;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public Object getItem(int position)
    {
        return null;
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    public void ClearArray(){
        imageUrl.clear();
        title.clear();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;

        if (null == convertView) {
            convertView = inflater.inflate(R.layout.single_image, parent, false);
            holder=new ViewHolder();
            holder.imageView=(ImageView) convertView.findViewById(R.id.movie_image);
            holder.mTextView=(TextView) convertView.findViewById(R.id.movie_name);
            convertView.setTag(holder);
        }else {
            holder=(ViewHolder)convertView.getTag();
        }

        holder.mTextView.setText(title.get(position));

        Picasso
                .with(ctx)
                .load(imageUrl.get(position))
                .fit()
                .into(holder.imageView);

        return convertView;
    }

    static class ViewHolder {
        ImageView imageView;
        TextView mTextView;
    }

}
