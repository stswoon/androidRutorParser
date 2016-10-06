package android.nodomain.stswoon.rutorparser;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static android.provider.ContactsContract.CommonDataKinds.Website.URL;

public class MainActivity extends AppCompatActivity {
    // Remove the below line after defining your own ad unit ID.
    private static final String TOAST_TEXT = "Test ads are being shown. "
            + "To show live ads, replace the ad unit ID in res/values/strings.xml with your own ad unit ID.";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load an ad into the AdMob banner view.
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);

        // Toasts the test ad message on the screen. Remove this after defining your own ad unit ID.
        Toast.makeText(this, TOAST_TEXT, Toast.LENGTH_LONG).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onSearchButtonClick(View v) {
        EditText search = (EditText) findViewById(R.id.editText);
        WebView result = (WebView) findViewById(R.id.webView);
        result.loadData("Put 'film' in search", "text/html", null);

        String searchText = null;
        try {
            searchText = new String(search.getText().toString().getBytes(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        AsyncTask asyncTask = new MyTask();
        asyncTask.execute(searchText);

        if (false) {
            String html = "<h1>Hello!</h1>";
            html += "<a href='www.yandex.ru'>link</a>";
            result.loadData(html, "text/html", null);
        }
    }


    private class MyTask extends AsyncTask<Object, Void, Document> {
        @Override
        protected Document doInBackground(Object... params) {
            Document doc = null;
            try {
                doc = Jsoup.connect("http://new-ru.org/search/0/0/000/2/" + params[0]).get();
            } catch (IOException e) {
                //result.loadData(e.getMessage(), "text/html", null);
                e.printStackTrace();
            } catch (Exception e) {
                //result.loadData(e.getMessage(), "text/html", null);
                e.printStackTrace();
            }
            return doc;
        }


        @Override
        protected void onPostExecute(Document doc) {
            WebView result = (WebView) findViewById(R.id.webView);
            if (false) {
                String html = "<h1>Hello!</h1>";
                html += "<a href='www.yandex.ru'>link</a>";
                result.loadData(html, "text/html", null);
            }
            String html = "";
            Elements elements = doc.select("div[id=index]").select("tr[class=gai]");
            for (Element element : elements) {
                String magnet = element.select("td").get(1).select("a[href*=magnet]").outerHtml();
                magnet = magnet.replaceFirst("<img src=\"/s/i/m.png\" alt=\"M\">", "Download");
                String name = element.select("td").get(1).select("a[href*=torrent]").html();
                String part = magnet + "</br>" + name + "</br></br>";
                try {
                    part = new String(part.getBytes(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                html += part;
            }
            if (elements.isEmpty()) {
                html = "Nothing found";
            }
            html = "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "   <head>\n" +
                    "      <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n" +
                    "      <title>HTML Document</title>\n" +
                    "   </head>\n" +
                    "   <body>\n" +
                    html + "\n" +
                    "  </body>\n" +
                    "</html>\n";
            result.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
        }
    }
}
