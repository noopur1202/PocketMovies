package com.workstation.pocketmovies;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class MovieResponseModel
{

    @SerializedName("Search")
    public List<Datum> data = new ArrayList<>();

    public class Datum {

        @SerializedName("Type")
        public Integer Type;
        @SerializedName("Year")
        public Integer Year;
        @SerializedName("imdbID")
        public Integer imdbID;
        @SerializedName("Poster")
        public Integer Poster;
        @SerializedName("Title")
        public Integer Title;
    }
}