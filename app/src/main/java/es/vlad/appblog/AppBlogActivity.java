package es.vlad.appblog;

import android.app.ListActivity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class AppBlogActivity extends ListActivity {

    public final static int NUMBER_OF_POST = 20;
    public final static String TAG = AppBlogActivity.class.getSimpleName();
    public final static String URL_JSON = "http://itvocationalteacher.blogspot.com/feeds/posts/default?alt=json";
    protected String[] mBlogPostTitles;
    JSONObject mBlogData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_blog);

        if (isNetworkAvailable(this)) {
            GetBlogPostTask getBlogPostTask = new GetBlogPostTask();
            getBlogPostTask.execute();
        } else {
            Toast.makeText(this, R.string.no_network, Toast.LENGTH_LONG).show();
        }
        if (mBlogPostTitles != null) {
            findViewById(R.id.textViewNoDataFound).setVisibility(View.INVISIBLE);
        }
    }

    public boolean isNetworkAvailable(Context context) {
        boolean isAvailable = false;
        ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
            Log.i("isNetworkAvailable", "isAvailable:" + isAvailable);
            return isAvailable;
        } else {
            Log.i("isNetworkAvailable", "isAvailable:" + isAvailable);
            return isAvailable;
        }
    }

    public void updateList() {
        if (mBlogData != null) {
            Log.i("updateList", "mBlogData" + mBlogData.toString());
            try {
                JSONObject jsonFeed = mBlogData.getJSONObject("feed");
                JSONArray jsonAentry;
                jsonAentry = jsonFeed.getJSONArray("entry");
                mBlogPostTitles = new String[jsonAentry.length()];
                for (int x = 0; x < jsonAentry.length(); x++) {
                    JSONObject jsonPost = (JSONObject) jsonAentry.get(x);
                    JSONObject jsonTitle = (JSONObject) jsonPost.get("title");
                    String title = Html.fromHtml(jsonTitle.getString("$t")).toString();
                    mBlogPostTitles[x] = title;
                    ArrayAdapter<String> arAd = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, mBlogPostTitles);
                    setListAdapter(arAd);
                    findViewById(R.id.textViewNoDataFound).setVisibility(View.INVISIBLE);
                }
            } catch (JSONException e) {
                Log.e(TAG, "JSONException caught:", e);
            }
        } else Log.i("updateList", "mBlogData is null");
    }

    public class GetBlogPostTask extends AsyncTask<Object, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(Object[] params) {
            int responseCode = -1;
            JSONObject jsonAnswer = null;
            try {
                URL blogFeedUrl = new URL(URL_JSON);
                HttpURLConnection connection = (HttpURLConnection) blogFeedUrl.openConnection();
                connection.connect();
                responseCode = connection.getResponseCode();
                Log.i(TAG, "responseCode:" + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    String answerStringBuilder = stringBuilder.toString();
                    Log.i(TAG, "answerStringBuilder:" + answerStringBuilder);
                    jsonAnswer = new JSONObject(answerStringBuilder);
                } else {
                    Log.i(TAG, "HttpURLConnection FAIL. responseCode:" + responseCode);
                }
            } catch (MalformedURLException e) {
                Log.e(TAG, "MalformedURLException caught:", e);
            } catch (IOException e) {
                Log.e(TAG, "IOException caught:", e);
            } catch (Exception e) {
                Log.e(TAG, "Exception caught:", e);
            }
            return jsonAnswer;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            mBlogData = result;
            updateList();
        }
    }
}
