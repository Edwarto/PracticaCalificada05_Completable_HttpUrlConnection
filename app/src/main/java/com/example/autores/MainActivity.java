package com.example.autores;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private ArrayList<Book> mBooks;
    private RVAdapter mAdapter;
    private RequestQueue mRequestQueue;

    private static  final  String BASE_URL="https://www.googleapis.com/books/v1/volumes?q=";

    private EditText search_edit_text;
    private Button search_button;
    private ProgressBar loading_indicator;
    private TextView error_message;

    private  RadioGroup radioGroup;
    private RadioButton all;
    private RadioButton eng;
    private RadioButton spn;
    private String lng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        search_edit_text=findViewById(R.id.search_box);
        search_button= findViewById(R.id.search_buttton);
        loading_indicator=findViewById(R.id.loading_indicator);
        error_message= findViewById(R.id.message_display);

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mBooks = new ArrayList<>();
        mRequestQueue = Volley.newRequestQueue(this);


        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBooks.clear();
                searchBook();
            }
        }); }


    private boolean Read_network_state(Context context)
    {    boolean is_connected;
        ConnectivityManager cm =(ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        is_connected=info!=null&&info.isConnectedOrConnecting();
        return is_connected;
    }

    public void searchBook() {
        String search_query = search_edit_text.getText().toString();

        boolean is_connected = Read_network_state(this);
        if(!is_connected)
        {
            mRecyclerView.setVisibility(View.INVISIBLE);
            error_message.setVisibility(View.VISIBLE);
            return;
        }

        if(search_query.equals(""))
        {
            Toast.makeText(this,"Please enter your query",Toast.LENGTH_SHORT).show();
            return;
        }
        String final_query=search_query.replace(" ","+");
        Uri uri=Uri.parse(BASE_URL+final_query);
        Uri.Builder buider = uri.buildUpon();

        parseJson(buider.toString());
        Log.d("QUERY",buider.toString());
    }

    private void parseJson(String key) {
        radioGroup = findViewById(R.id.radio_group);


        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View radioButton = radioGroup.findViewById(checkedId);
                int index = radioGroup.indexOfChild(radioButton);
                switch (index) {
                    case 0:
                        lng = "";
                        Toast.makeText(getApplicationContext(),lng,Toast.LENGTH_LONG);
                        break;
                    case 1:
                        lng = "en";
                        Toast.makeText(getApplicationContext(),lng,Toast.LENGTH_LONG);
                        break;
                    case 2:
                        lng = "es";
                        Toast.makeText(getApplicationContext(),lng,Toast.LENGTH_LONG);
                        break;
                    default:
                        lng = "";
                        break;

                }
            }
        });

        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, key.toString(), null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        all = findViewById(R.id.radio_button_all);
                        eng = findViewById(R.id.radio_button_eng);
                        spn = findViewById(R.id.radio_button_spn);

                        String title ="";
                        String author ="";
                        String publishedDate = "NoT Available";
                        String description = "No Description";
                        int pageCount = 1000;
                        String categories = "No categories Available ";
                        String buy ="";

                        String language = "";

                        String price = "NOT_FOR_SALE";
                        try {
                            JSONArray items = response.getJSONArray("items");

                            for (int i = 0 ; i< items.length() ;i++){
                                JSONObject item = items.getJSONObject(i);
                                JSONObject volumeInfo = item.getJSONObject("volumeInfo");



                                try{
                                    title = volumeInfo.getString("title");

                                    JSONArray authors = volumeInfo.getJSONArray("authors");
                                    if(authors.length() == 1){
                                        author = authors.getString(0);
                                    }else {
                                        author = authors.getString(0) + "|" +authors.getString(1);
                                    }


                                    publishedDate = volumeInfo.getString("publishedDate");
                                    pageCount = volumeInfo.getInt("pageCount");




                                            JSONObject saleInfo = item.getJSONObject("saleInfo");
                                            JSONObject listPrice = saleInfo.getJSONObject("listPrice");
                                            price = listPrice.getString("amount") + " " + listPrice.getString("currencyCode");
                                            description = volumeInfo.getString("description");
                                            buy = saleInfo.getString("buyLink");
                                            categories = volumeInfo.getJSONArray("categories").getString(0);
                                            language = volumeInfo.getString("language");
                                            Toast.makeText(getApplicationContext(),language,Toast.LENGTH_LONG);



                                }catch (Exception e){

                                }
                                String thumbnail = volumeInfo.getJSONObject("imageLinks").getString("thumbnail");

                                String previewLink = volumeInfo.getString("previewLink");
                                String url = volumeInfo.getString("infoLink");


                                mBooks.add(new Book(title , author , publishedDate , description ,categories
                                        ,thumbnail,buy,previewLink,price,pageCount,url,language));


                                mAdapter = new RVAdapter(MainActivity.this , mBooks);
                                mRecyclerView.setAdapter(mAdapter);
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("TAG" , e.toString());

                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        mRequestQueue.add(request);
    }


    public class GetBook extends AsyncTask<String,Void,String>{

        private WeakReference<TextView> mTextTitle;
        private WeakReference<TextView> mTextAuthor;

        public GetBook(TextView mTextTitle, TextView mTextAuthor) {
            this.mTextTitle = new WeakReference<>(mTextTitle);
            this.mTextAuthor = new WeakReference<>(mTextAuthor);
        }

        @Override
        protected String doInBackground(String... strings) {
            return NetUtilities.getBookInfo(strings[0]);
        }

        @Override
        protected void onPostExecute(String s){
            super.onPostExecute(s);
            try{
                JSONObject jsonObject = new JSONObject(s);
                JSONArray itemsArray = jsonObject.getJSONArray("items");
                int i = 0;
                String title = null;
                String author = null;
                while(i < itemsArray.length() && (title == null && author == null)){
                    JSONObject book = itemsArray.getJSONObject(i);
                    JSONObject volumeInfo = book.getJSONObject("volumeInfo");
                    try{
                        title = volumeInfo.getString("title");
                        author = volumeInfo.getString("authors");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    i++;
                }
                if(title != null && author != null){
                    mTextTitle.get().setText(title);
                    mTextAuthor.get().setText(author);
                }else{
                    mTextTitle.get().setText("No existen resultados para la consulta");
                    mTextAuthor.get().setText("");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}