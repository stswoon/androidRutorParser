package andr.nodomain.stswoon.rutorparser;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

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

public class MainActivity extends AppCompatActivity {
    // Remove the below line after defining your own ad unit ID.
    private static final String TOAST_TEXT = "Test ads are being shown. "
            + "To show live ads, replace the ad unit ID in res/values/strings.xml with your own ad unit ID.";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load an ad into the AdMob banner view.
        MobileAds.initialize(this, "ca-app-pub-1891256243789657~9263299320");
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);

        // Toasts the test ad message on the screen. Remove this after defining your own ad unit ID.
        //Toast.makeText(this, TOAST_TEXT, Toast.LENGTH_LONG).show();


        WebView result = (WebView) findViewById(R.id.webView);
        String fludLink = "market://details?id=com.delphicoder.flud";//or https://play.google.com/store/apps/details?id=com.delphicoder.flud
        String s = "1. Введите имя торента для поиска. </br> " +
                "2. Нажмите на кнопку Поиск. </br> " +
                "3. Будет выдан результат поиска с сайта Rutor в порядке убывания сидов. </br> " +
                "4. Нажмите 'Скачать' (для скачивания должен быть установлен любой торрент-клиент, например <a href='" + fludLink + "'>Flud</a>)";
        try {
            s = new String(s.getBytes(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "   <head>\n" +
                "      <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n" +
                "      <title>HTML Document</title>\n" +
                "   </head>\n" +
                "   <body>\n" +
                s + "\n" +
                "  </body>\n" +
                "</html>\n";
        result.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return false;//true;
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


        String s = "Пожалуйста, подождите, идет поиск...";
        try {
            s = new String(s.getBytes(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "   <head>\n" +
                "      <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n" +
                "      <title>HTML Document</title>\n" +
                "   </head>\n" +
                "   <body>\n" +
                s + "\n" +
                "  </body>\n" +
                "</html>\n";
        result.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);

        String searchText = null;
        try {
            searchText = new String(search.getText().toString().getBytes(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        AsyncTask asyncTask = new MyTask();
        asyncTask.execute(searchText);

        if (false) {
            html = "<h1>Hello!</h1>";
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
            if (doc == null) {
                try {
                    doc = Jsoup.connect("http://xrutor.org/search/0/0/000/2/" + params[0]).get();
                } catch (IOException e) {
                    //result.loadData(e.getMessage(), "text/html", null);
                    e.printStackTrace();
                } catch (Exception e) {
                    //result.loadData(e.getMessage(), "text/html", null);
                    e.printStackTrace();
                }
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

            if (doc == null) {
                String s = "Ошибка сети. Или возможно сайт rutor не доступен.";
                try {
                    s = new String(s.getBytes(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                result.loadDataWithBaseURL(null, s, "text/html", "UTF-8", null);
                return;
            }

            boolean torrent = false;
            String html = "";
            Elements elements = doc.select("div[id=index]").select("tr[class=gai], tr[class=tum]");
            for (Element element : elements) {
                String magnet = element.select("td").get(1).select("a[href*=magnet]").outerHtml();
                magnet = magnet.replaceFirst("<img src=\"/s/i/m.png\" alt=\"M\">", "Скачать");
                if (magnet.isEmpty()) {
                    String href = element.select("td").get(1).select("a[href*=/parse/]").attr("href");
                    magnet = element.select("td").get(1).select("a[href*=/parse/]").outerHtml();
                    magnet = magnet.replaceFirst(href, "http://xrutor.org" + href);
                    magnet = magnet.replaceFirst("<img src=\"/parse/s.rutor.org/i/d.gif\" alt=\"D\">", "Скачать torrent");
                    if (!torrent) {
                        torrent = true;
                        html += "К сожалению основной сайт не доступен, а запасной сайт не поддерживает magnet ссылки. Поэтому после скачки torrent файла вам вероятно придется вручную добавить скаченный файл в torrent-client. Извините за неудобство. <br/>";
                    }
                }
                String name = element.select("td").get(1).select("a[href*=torrent]").html();
                String size = element.select("td").get(element.select("td").size() - 2).html();
                String part = magnet + "<span class='size'>" + size + "</span>";
                part += "</br><span class='descr'>" + name + "</span></br>";
                try {
                    part = new String(part.getBytes(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                html += part;
            }
            if (elements.isEmpty()) {
                String s = "Ничего не найдено.";
                try {
                    s = new String(s.getBytes(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                html = "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "   <head>\n" +
                        "      <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n" +
                        "      <title>HTML Document</title>\n" +
                        "   </head>\n" +
                        "   <body>\n" +
                        s + "\n" +
                        "  </body>\n" +
                        "</html>\n";

            } else {
                html = "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "   <head>\n" +
                        "      <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n" +
                        "      <title>HTML Document</title>\n" +

                        "  <style type=\"text/css\">\n" +
                        "   a {\n" +
                        "\tbackground-color: #4CAF50; /* Green */\n" +
                        "    border: none;\n" +
                        "    color: white;\n" +
                        "    padding: 5px 32px;\n" +
                        "    text-align: center;\n" +
                        "    text-decoration: none;\n" +
                        "    display: inline-block;\n" +
                        "    font-size: 16px;\n" +
                        "\tmargin-top: 5px;\n" +
                        "   }\n" +
                        "   span.descr {\n" +
                        "\tborder-radius: 0px 5px 5px 5px;\n" +
                        "\tbackground-color: #00BFFF; /* Blue */\n" +
                        "\tmargin: 5px;\n" +
                        "   }\n" +
                        "  </style>\n" +

                        "   </head>\n" +
                        "   <body>\n" +
                        html + "\n" +
                        "  </body>\n" +
                        "</html>\n";
            }
            result.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
        }
    }
}
