/*
    I, Minh Dang - 000746305 certify that all of this materials is my own work. No other person's work has been copied and used without due acknowledgement.
 */

package dang.mohawk.ca;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
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
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    /*
        Static class SearchItem models a movie item return from the json request
     */
    static public class SearchItem{

        public String Title; // Title of the movie
        public String Year; // Year released of the movie
        public String imdbID; // Imdb ID of the movie
        public String Type; // Type of the movie
        public String Poster; // Poster url of the movie

        //Override toString method to display only the year and title of the movie
        @Override
        public String toString() {return Year + " - " +  Title;}

    }

    /*
       Static class Search models a whole Search Json string object returned from the request
    */
    static public class Search{
        public ArrayList<SearchItem> Search; //arraylist of all search item
        public String totalResults; //number of search item returned
        public String Response; //response of the request

    }



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Assign onClick method to the ListView
        ListView myMovies = findViewById(R.id.moviesList);
        myMovies.setOnItemClickListener(this);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //When user click on any movie in the result list
        //Get the name and the year of the selected movie and send it to MovieDetails Activity
        String selectedMovie = parent.getItemAtPosition(position).toString();
        String[] split = selectedMovie.split("-", 2);

        //Start new intent of MovieDetails class to show the details of the selected movie
        Intent myIntent = new Intent (this, MovieDetails.class);

        //Send the strings to the new activity
        myIntent.putExtra("name", split[1].trim());
        myIntent.putExtra("year", split[0].trim());

        //Start the activity
        startActivity(myIntent);

    }

    //This method gets the user input from the input fields and build a string request to get all the related movies from the API
    public void getMovies(View view) {

        //Get input
        EditText movieName = findViewById(R.id.movieName);
        String name = movieName.getText().toString().trim();

        EditText movieYear = findViewById(R.id.yearMovie);
        String year = movieYear.getText().toString().trim();

        //Validate user input
        //If user enter empty string make a Toast to prompt users to enter the movie's name and year
        if(name.equals("") || year.equals("")){
            String namePrompt = getString(R.string.prompt_movie_name);
            String yearPrompt = getString(R.string.prompt_movie_year);

            if(name.equals("")){
                Toast.makeText(this, namePrompt, Toast.LENGTH_SHORT).show();
            }
            if(year.equals("")){
                Toast.makeText(this, yearPrompt, Toast.LENGTH_SHORT).show();
            }
        }else{
            //Build the http string request
            String urlString = "http://www.omdbapi.com/?apikey=ad9c4cec";
            urlString += "&s=" + name + "&y=" + year;

            //execute the request calling async DownloadTask method
            new DownloadTask().execute(urlString);
        }

    }

    //This asynchronous class include the methods that make the request out the url string to get the json response from the API
    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {

            //string variable that holds the results string
            String results = "";

            // Connect and get the result from the response
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
                //Use Gson object to get the results into an instance of Search class
                Gson gson = new Gson();
                Search resultList = gson.fromJson(s, Search.class);

                //Use ArrayAdapter to render the result to the ListView
                ListView listView = findViewById(R.id.moviesList);
                ArrayAdapter<SearchItem> adapter = new ArrayAdapter<SearchItem>(MainActivity.this,
                        android.R.layout.simple_list_item_1, resultList.Search);
                listView.setAdapter(adapter);
            }catch(NullPointerException e){
                //Use Toast to inform the users if no results found
                Toast.makeText(MainActivity.this, getString(R.string.no_movies_found), Toast.LENGTH_SHORT).show();
            }

        }
    }
}
