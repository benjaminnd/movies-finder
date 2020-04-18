package dang.mohawk.ca;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MovieDetails extends AppCompatActivity {

    /*
     Static class Movie model a single movie json object returned from the API
     */
    static public class Movie{
        public String Title; //Title of the movie
        public String Year; //Year of the movie
        public String Actors; //Actors of the movie
        public String Poster; //Poster URL of the movie
        public String Plot; //Plot of the movie

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        //Get the movie name and year passed from MainActivity
        Intent myIntent = getIntent();
        String movieName = myIntent.getStringExtra("name");
        String movieYear = myIntent.getStringExtra("year");

        //Buil the request string to get the single result from the API
        String url = "http://www.omdbapi.com/?apikey=ad9c4cec" + "&t=" + movieName + "&y=" + movieYear;

        //execute the request calling async DownloadTask method
        new DownloadTask().execute(url);
    }

    //This asynchronous class include the methods that make the request out the url string to get the json response from the API
    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String results = "";

            try {
                URL url = new URL(strings[0]);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                int statusCode = connection.getResponseCode();
                if(statusCode == 200){
                    InputStream is = new BufferedInputStream(connection.getInputStream());
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(is, "UTF-8"));
                    String line = null;

                    while ((line = br.readLine()) != null){
                        results += line;
                    }
                }else {
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return results;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try{
                //Use Gson object to get the response from the API into Movie instance
                Gson gson = new Gson();
                MovieDetails.Movie result = gson.fromJson(s, MovieDetails.Movie.class);

                //Display the movie details
                TextView nameToShow = findViewById(R.id.title);
                TextView yearToShow = findViewById(R.id.year);
                TextView plotToShow = findViewById(R.id.plot);
                TextView castToShow = findViewById(R.id.cast);
                nameToShow.setText(result.Title);
                yearToShow.setText(result.Year);
                plotToShow.setText(result.Plot);
                castToShow.setText(result.Actors);

                //Use the DownloadImageTask class to display the poster from it's url
                new DownloadImageTask().execute(result.Poster);

            }catch(NullPointerException e){
                Toast.makeText(MovieDetails.this, "No movies found!", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        protected Bitmap doInBackground(String... urls) {

            // Use the URL Connection interface to download the URL
            Bitmap bmp = null;

            Log.d("debug", "do background " + urls[0]);
            try {
                URL url = null;
                url = new URL(urls[0]);
                bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());

            } catch (MalformedURLException e) {
            } catch (IOException e) {
            }

            Log.d("debug", "done ");
            return bmp;
        }

        // Display the image in UI
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                // Find the ImageView object to use
                ImageView imageView = findViewById(R.id.thumbnail);
                imageView.setImageBitmap(result);
            }
        }

    }
}
