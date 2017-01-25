package andr.nodomain.stswoon.rutorparser;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

//import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {
    private static final String md5(final String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            //Logger.logStackTrace(TAG,e);
        }
        return "";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadText();
        setContentView(R.layout.activity_main);

        String deviceId = null;
        if (false) { //http://stackoverflow.com/questions/4524752/how-can-i-get-device-id-for-admob
            String android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
            deviceId = md5(android_id).toUpperCase();
            //Log.v(TAG, "is Admob Test Device ? "+deviceId+" "+isTestDevice);
        }

        // Load an ad into the AdMob banner view.
        MobileAds.initialize(this, "ca-app-pub-1891256243789657~9263299320");
        final AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("868243020016207") //http://stackoverflow.com/questions/9681400/android-get-device-id-for-admob
                .addTestDevice("5619A35EB654725CC3114A234B8A4657") //http://stackoverflow.com/questions/4524752/how-can-i-get-device-id-for-admob
//                .addTestDevice(deviceId)
                .setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);



        //http://stackoverflow.com/questions/23880516/disable-remove-ads-from-your-own-app-in-android
        try {
            final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
            String deviceid = tm.getDeviceId(); //http://stackoverflow.com/questions/9681400/android-get-device-id-for-admob
            if ("868243020016207".equals(deviceid)) { //todo check on different device in realease
                final Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        timer.cancel();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adView.setVisibility(View.GONE);
                            }
                        });
                    }
                }, 5000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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
            //startActivity(new Intent(MainActivity.this, SettingActivity.class));
            setContentView(R.layout.settings);
            EditText url = (EditText) findViewById(R.id.torrentUrl);
            if (torrentUrl != null && !torrentUrl.isEmpty()) {
                url.setText(torrentUrl);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onBtnSettingCancelClick(View v) {
        EditText url = (EditText) findViewById(R.id.torrentUrl);
        url.setText("");
        setContentView(R.layout.activity_main);
    }

    public void onBtnSettingOkClick(View v) {
        EditText url = (EditText) findViewById(R.id.torrentUrl);
        torrentUrl = url.getText().toString();
        torrentUrl = torrentUrl == null ? "" : torrentUrl;
        saveUrl(torrentUrl);
        stableUrl = null;
        setContentView(R.layout.activity_main);
    }

    private SharedPreferences sPref;
    private static final String TORRENT_URL = "torrentUrl";
    private String torrentUrl = "";

    private void saveUrl(String url) {
        sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(TORRENT_URL, url);
        ed.commit();
        Toast.makeText(this, makeUtf8("Настройки сохранены"), Toast.LENGTH_SHORT).show();
    }

    private void loadText() {
        sPref = getPreferences(MODE_PRIVATE);
        String url = sPref.getString(TORRENT_URL, "");
        torrentUrl = url;
        torrentUrl = torrentUrl == null ? "" : torrentUrl;
    }

    private String makeUtf8(String s) {
        try {
            s = new String(s.getBytes(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return s;
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

        AsyncTask asyncTask = new MyTask(this);
        asyncTask.execute(searchText);

        if (false) {
            html = "<h1>Hello!</h1>";
            html += "<a href='www.yandex.ru'>link</a>";
            result.loadData(html, "text/html", null);
        }
    }

    private Document connect(String url) {
        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            //result.loadData(e.getMessage(), "text/html", null);
            e.printStackTrace();
        } catch (Exception e) {
            //result.loadData(e.getMessage(), "text/html", null);
            e.printStackTrace();
        }
        return doc;
    }

    private String stableUrl = null;
    private List<String> urls = new ArrayList<String>() {{
        add("http://rutor.org");
        add("http://new-ru.org");
        add("http://xrutor.org");
    }};

    private class MyTask extends AsyncTask<Object, Void, Document> {
        private MainActivity mainActivity;

        public MyTask(MainActivity mainActivity) {
            this.mainActivity = mainActivity;
        }

        @Override
        protected Document doInBackground(Object... params) {
            String postfix = "/search/0/0/000/2/" + params[0];
            Document doc = null;
            if (stableUrl != null) {
                doc = connect(stableUrl + postfix);
                if (doc == null) {
                    stableUrl = null;
                } else {
                    return doc;
                }
            }

            if (torrentUrl != null && !torrentUrl.isEmpty()) {
                doc = connect(torrentUrl + postfix);
                if (doc == null) {
                    //Toast.makeText(mainActivity, makeUtf8("Не удалось загрузить данные по url из настроек"), Toast.LENGTH_SHORT).show();
                } else {
                    stableUrl = torrentUrl;
                    return doc;
                }
            }

            for (String url : urls) {
                doc = connect(url + postfix);
                if (doc != null) {
                    stableUrl = url;
                    return doc;
                }
            }

            return doc;
        }


        @Override
        protected void onPostExecute(Document doc) {
            if (torrentUrl != null && !torrentUrl.isEmpty() && !stableUrl.equals(torrentUrl)) {
                Toast.makeText(mainActivity, makeUtf8("Не удалось загрузить данные по url из настроек"), Toast.LENGTH_SHORT).show();
            }
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
