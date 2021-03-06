package andr.nodomain.stswoon.rutorparser;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

//import android.content.Intent;

//https://developer.android.com/studio/run/oem-usb.html do it before test on real devices
public class MainActivity extends AppCompatActivity {

    private String stableUrl = null;
    private List<String> urls = new ArrayList<String>() {{
        //add("http://rutor.org");//only via vpn

        add("http://s.new-rutor.org");
        //filmadd("http://ru-tor.biz");
        //add("http://zarutor.org");
        //add("http://browar.bz");
        //add("http://live-rutor.eu");//http://live-rutor.eu/search/0/0/000/2/film

//
//        //no magnet on search and top
//        add("http://live-rutor.org");
//
//        //no magnet on search, no torrent and magnet on top
//        add("http://main-rutor.org");
//        add("http://therutor.org");
//        add("http://xn--c1avfbif.org");
//
//        //no magnet on search and top not worked
//        add("http://the-rutor.org");

        //blocked
        //add("http://new-ru.org");
        //no magnet links
        //add("http://xrutor.org");
    }};


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

    private void loadAd(AdView inputAdView) {
        final AdView adView = inputAdView == null ? (AdView) findViewById(R.id.adView) : inputAdView;

        if (false) {
            //http://stackoverflow.com/questions/4524752/how-can-i-get-device-id-for-admob
            String android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
            String deviceId = md5(android_id).toUpperCase();
        }

        // Load an ad into the AdMob banner view.
        MobileAds.initialize(this, "ca-app-pub-1891256243789657~9263299320");
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("868243020016207") //http://stackoverflow.com/questions/9681400/android-get-device-id-for-admob
                .addTestDevice("28D42D8E394253AD6540E8E8B16311D6") //http://stackoverflow.com/questions/4524752/how-can-i-get-device-id-for-admob
                .setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);

        //http://stackoverflow.com/questions/23880516/disable-remove-ads-from-your-own-app-in-android
        try {
            final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
            String deviceid = tm.getDeviceId(); //http://stackoverflow.com/questions/9681400/android-get-device-id-for-admob
            if ("868243020016207".equals(deviceid)) {
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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadText();
        setContentView(R.layout.activity_main);

        Toast.makeText(this, makeUtf8("Скорее всего приложение в связи с блокировкой не сможет впредь функционировать нормально"), Toast.LENGTH_SHORT).show();

        String deviceId = null;
        if (false) { //http://stackoverflow.com/questions/4524752/how-can-i-get-device-id-for-admob
            String android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
            deviceId = md5(android_id).toUpperCase();
            //Log.v(TAG, "is Admob Test Device ? "+deviceId+" "+isTestDevice);
        }

        loadAd(null);

        WebView result = (WebView) findViewById(R.id.webView);
        String fludLink = "market://details?id=com.delphicoder.flud";//or https://play.google.com/store/apps/details?id=com.delphicoder.flud
        String s = "1. Введите имя торента для поиска. </br> " +
                "2. Нажмите на кнопку 'Поиск'. </br> " +
                "3. Будет выдан результат поиска с сайта Rutor в порядке убывания сидов. </br> " +
                "4. Нажмите 'Скачать' (для скачивания должен быть установлен любой торрент-клиент, например <a href='" + fludLink + "'>Flud</a>).";
        s = "Очень часто закрывают сайты, а свободного времени оперативно исправвлять ошики нет, " +
                "поэтому это, возможно, последняя рабочая версия, кому нужно вот код - " +
                "https://github.com/stswoon/androidRutorParser, правда он не качественный, т.к. все время ушло на борьбу с сайтами." +
                "</br></br>Спасибо, что выбирали мой продукт.</br></br>" + s;
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
        } else if (id == R.id.action_top) {
            setContentView(R.layout.activity_top);
            loadAd((AdView) findViewById(R.id.adTopView));
            loadTop();
            return true;
        } else if (id == R.id.action_search) {
            setContentView(R.layout.activity_main);
            loadAd(null);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onBtnSettingCancelClick(View v) {
        EditText url = (EditText) findViewById(R.id.torrentUrl);
        url.setText("");
        setContentView(R.layout.activity_main);
        loadAd(null);
    }

    public void onBtnSettingOkClick(View v) {
        EditText url = (EditText) findViewById(R.id.torrentUrl);
        torrentUrl = url.getText().toString();
        torrentUrl = torrentUrl == null ? "" : torrentUrl;
        saveUrl(torrentUrl);
        stableUrl = null;
        setContentView(R.layout.activity_main);
        loadAd(null);
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

        AsyncTask asyncTask = new MyTask(this, false);
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



    private class MyTask extends AsyncTask<Object, Void, Document> {
        private MainActivity mainActivity;
        private boolean isTop;

        public MyTask(MainActivity mainActivity, boolean isTop) {
            this.mainActivity = mainActivity;
            this.isTop = isTop;
        }

        @Override
        protected Document doInBackground(Object... params) {
            String postfix = "/search/0/0/0/2/" +  params[0];
            if (isTop) {
                postfix = "/top?p=7d";
            }
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
            if (torrentUrl != null && !torrentUrl.isEmpty() && !torrentUrl.equals(stableUrl)) {
                Toast.makeText(mainActivity, makeUtf8("Не удалось загрузить данные по url из настроек"), Toast.LENGTH_SHORT).show();
            }
            WebView result = isTop ? (WebView) findViewById(R.id.webTopView) : (WebView) findViewById(R.id.webView);
            if (result == null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                result = isTop ? (WebView) findViewById(R.id.webTopView) : (WebView) findViewById(R.id.webView);
            }
            if (isTop) {
                WebSettings webSettings = result.getSettings();
                webSettings.setJavaScriptEnabled(true);
            }

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

            String html = "";
            if (!isTop) {
                boolean torrent = false;
                Elements elements = doc.select("div[id=index]").select("tr[class=gai], tr[class=tum]");
                for (Element element : elements) {
                    String magnet = element.select("td").get(1).select("a[href*=magnet]").outerHtml();
                    magnet = magnet.replaceFirst("<img src=\"/s/i/m.png\" alt=\"M\">", "Скачать");
                    magnet = magnet.replaceFirst("<img src=\"/yh/i/m.png\" alt=\"M\">", "Скачать");
                    if (magnet.isEmpty()) {
                        String href = element.select("td").get(1).select("a[href*=/torrent/]").attr("href");
                        magnet = "<a href='" + stableUrl + href + "'>Скачать torrent</a>";
                        //magnet = element.select("td").get(1).select("a[href*=/torrent/]").outerHtml();
                        //magnet = magnet.replaceFirst(href, stableUrl + href);
                        //magnet = magnet.replaceFirst("<img src=\"/parse/s.rutor.org/i/d.gif\" alt=\"D\">", "Скачать torrent");
                        //magnet = magnet.replaceFirst("<img src=\"/yh/i/d.gif\" alt=\"D\">", "Скачать torrent");
                        if (!torrent) {
                            torrent = true;
                            html += "К сожалению основной сайт не доступен, а запасной сайт не поддерживает magnet ссылки. Поэтому после скачки torrent файла вам вероятно придется вручную добавить скаченный файл в torrent-client. Извините за неудобство. <br/>";
                        }
                    }

                    String name = element.select("td").get(1).select("a[href*=torrent]").html();
                    String size = element.select("td").get(element.select("td").size() - 2).html();
                    String part = magnet + "<span class='size'>" + size + "</span>";

                    String torrentUrlDetails = element.select("td").get(1).select("a[href*=torrent]").attr("href");
                    if (torrentUrlDetails != null && !torrentUrlDetails.isEmpty()) {
                        torrentUrlDetails = stableUrl + torrentUrlDetails;
                        part += "<a class='detailsLink' href='" + torrentUrlDetails + "'>Подробнее</a>";
                    }

                    part += "</br><span class='descr'>" + name + "</span></br>";
                    try {
                        part = new String(part.getBytes(), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    html += part;
                }
                if (elements.isEmpty()) {
                    String s = "Ничего не найдено по запросу. Или сайт заблокирован.";
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
                            "    padding: 5px 20px;\n" +
                            "    text-align: center;\n" +
                            "    text-decoration: none;\n" +
                            "    display: inline-block;\n" +
                            "    font-size: 16px;\n" +
                            "\tmargin-top: 5px;\n" +
                            "   }\n" +
                            "   a.detailsLink {\n" +
                            "     background-color: white; \n" +
                            "     color: #2108e0;\n" +
                            "     text-decoration: underline;  \n" +
                            "   }"+
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
            } else {
                //todo switching from Snapshot to Host GPU - https://forum.ionicframework.com/t/ionic-app-crashing-only-on-android-emulator/33375

                String[] tableName = {"Топ 24", "Зарубежные фильмы", "Наши фильмы", "Науч.-поп. фильмы",
                        "Сериалы", "Мультипликация", "Игры", "Аниме",
                "Музыка", "Софт", "TB", "Спорт и Здоровье",
                        "Юмор", /*"Хозяйство и Быт",*/ "Книги", "Другое"};

                String[] tableId = {"top24", "zarybFilm", "ourFilm", "naychFilm",
                        "series", "mult", "games", "anime",
                        "music", "soft", "tv", "sport",
                        "humor", /*"hoz",*/ "books", "other"};

                boolean torrent = false;
                Elements tables = doc.select("table");
                int tableIndex = -1;
                if (!tables.isEmpty())
                for (int i = 1; i <= 15; ++i) {
                    Element table;
                    try {
                        table = tables.get(i);
                    } catch (IndexOutOfBoundsException e) {
                        throw new RuntimeException("url=" + torrentUrl + "tables.size=" + tables.size(), e);
                    }
                    tableIndex++;
                    html += "<div id='div-"+tableId[tableIndex]+"' " + ((tableIndex==0) ? "class='visible'" : "") + ">";

                    Elements elements = table.select("tr[class=gaitum], tr[class=tum]");
                    for (Element element : elements) {
                        String magnet = element.select("td").get(1).select("a[href*=magnet]").outerHtml();
                        magnet = magnet.replaceFirst("<img src=\"/s/i/m.png\" alt=\"M\">", "Скачать");
                        magnet = magnet.replaceFirst("<img src=\"/yh/i/m.png\" alt=\"M\">", "Скачать");
                        magnet = magnet.replaceFirst("<img src=\"/parse/s.rutor.org/i/m.png\" alt=\"M\">", "Скачать");
                        if (magnet.isEmpty()) {
                            String href = element.select("td").get(1).select("a[href*=/parse/]").attr("href");
                            magnet = element.select("td").get(1).select("a[href*=/parse/]").outerHtml();
                            magnet = magnet.replaceFirst(href, stableUrl + href);
                            magnet = magnet.replaceFirst("<img src=\"/parse/s.rutor.org/i/d.gif\" alt=\"D\">", "Скачать torrent");
                            magnet = magnet.replaceFirst("<img src=\"/yh/i/d.gif\" alt=\"D\">", "Скачать torrent");
                            if (!torrent) {
                                torrent = true;
                            }
                        }
                        String name = element.select("td").get(1).select("a[href*=torrent]").html();
                        String size = element.select("td").get(element.select("td").size() - 2).html();
                        String part = magnet + "<span class='size'>" + size + "</span>";

                        String torrentUrlDetails = element.select("td").get(1).select("a[href*=torrent]").attr("href");
                        if (torrentUrlDetails != null && !torrentUrlDetails.isEmpty()) {
                            torrentUrlDetails = stableUrl + torrentUrlDetails;
                            part += "<a class='detailsLink' href='" + torrentUrlDetails + "'>Подробнее</a>";
                        }

                        part += "</br><span class='descr'>" + name + "</span></br>";
                        try {
                            part = new String(part.getBytes(), "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        html += part;
                    }

                    html += "</div>";
                }
                if (tables.isEmpty()) {
                    String s = makeUtf8("Ничего не найдено.");
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
                    if (torrent) {
                        html = "К сожалению основной сайт не доступен, а запасной сайт не поддерживает magnet ссылки. Поэтому после скачки torrent файла вам вероятно придется вручную добавить скаченный файл в torrent-client. Извините за неудобство. <br/>" + html;
                    }

                    html = "<head>\n" +
                            "  <style type=\"text/css\">\n" +
                            "   a {\n" +
                            "\tbackground-color: #4CAF50; /* Green */\n" +
                            "    border: none;\n" +
                            "    color: white;\n" +
                            "    padding: 5px 20px;\n" +
                            "    text-align: center;\n" +
                            "    text-decoration: none;\n" +
                            "    display: inline-block;\n" +
                            "    font-size: 16px;\n" +
                            "\tmargin-top: 5px;\n" +
                            "   }\n" +
                            "   a.detailsLink {\n" +
                            "     background-color: white; \n" +
                            "     color: #2108e0;\n" +
                            "     text-decoration: underline;  \n" +
                            "   }"+
                            "   span.descr {\n" +
                            "\tborder-radius: 0px 5px 5px 5px;\n" +
                            "\tbackground-color: #00BFFF; /* Blue */\n" +
                            "\tmargin: 5px;\n" +
                            "   }\n" +
                            "   span.size {\t\n" +
                            "   }\n" +
                            "   .controll span {\n" +
                            "\tbackground-color: #D3D3D3; /* Gray */\n" +
                            "\tcursor: pointer;\t\n" +
                            "\tpadding: 2px;\n" +
                            "    margin: 2px;\n" +
                            "\tdisplay: inline-block;" +
                            "   }\n" +
                            "   .controll span.active {\n" +
                            "\tbackground-color: #006FFF; /* Gray */\n" +
                            "   }\n" +
                            "   .data div {\n" +
                            "\tdisplay: none;\n" +
                            "   }\n" +
                            "   .data div.visible {\n" +
                            "\tdisplay: block;\n" +
                            "   }\n" +
                            "  </style>\n" +
                            "  <script>\n" +
                            "\tfunction select(span) {\n" +
                            "\t\tspanControll = document.getElementById(\"controll\");\n" +
                            "\t\tfor (var i = 0; i < spanControll.children.length; ++i) {\n" +
                            "\t\t\tspanControll.children[i].className = \"\";\n" +
                            "\t\t}\n" +
                            "\t\tspan.className = \"active\";\n" +
                            "\t\t\n" +
                            "\t\tdivData = document.getElementById(\"data\");\n" +
                            "\t\tfor (var i = 0; i < divData.children.length; ++i) {\n" +
                            "\t\t\tdivData.children[i].className = \"\";\n" +
                            "\t\t}\n" +
                            "\t\tvar divId = span.id.replace(\"span-\", \"div-\");\n" +
                            "\t\tdocument.getElementById(divId).className = \"visible\";\n" +
                            "\t}\n" +
                            "  </script>\n" +
                            "</head>\n" +
                            "<body>\n" +
                            "<div id=\"controll\" class=\"controll\">\n" +
                            "\t<span class=\"active\" id=\"span-top24\" onclick=\"select(this)\">Топ 24</span> \n" +
                            "\t<span id=\"span-zarybFilm\" onclick=\"select(this)\">Зарубежные фильмы</span> \n" +
                            "\t<span id=\"span-ourFilm\" onclick=\"select(this)\">Наши фильмы</span> \n" +
                            "\t<span id=\"span-naychFilm\" onclick=\"select(this)\">Науч.-поп. фильмы</span> \n" +
                            "\t<span id=\"span-series\" onclick=\"select(this)\">Сериалы</span> \n" +
                            "\t<span id=\"span-tv\" onclick=\"select(this)\">Телевизор</span> \n" +
                            "\t<span id=\"span-mult\" onclick=\"select(this)\">Мультипликация</span> \n" +
                            "\t<span id=\"span-anime\" onclick=\"select(this)\">Аниме</span> \n" +
                            "\t<span id=\"span-music\" onclick=\"select(this)\">Музыка</span> \n" +
                            "\t<span id=\"span-games\" onclick=\"select(this)\">Игры</span> \n" +
                            "\t<span id=\"span-soft\" onclick=\"select(this)\">Софт</span> \n" +
                            "\t<span id=\"span-sport\" onclick=\"select(this)\">Спорт и Здоровье</span> \n" +
                            "\t<span id=\"span-humor\" onclick=\"select(this)\">Юмор</span> \n" +
//                            "\t<span id=\"span-hoz\" onclick=\"select(this)\">Хозяйство и Быт</span> \n" +
                            "\t<span id=\"span-books\" onclick=\"select(this)\">Книги</span> \n" +
                            "\t<span id=\"span-other\" onclick=\"select(this)\">Другое</span> \n" +
                            "</div>\n" +
                            "<hr>\n" +
                            "<div id=\"data\" class=\"data\">\n" +
                            html + "\n" +
                            "</div>\n" +
                            "</body>";
                }
            }

            result.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
        }
    }

    private void loadTop() {
        WebView result = (WebView) findViewById(R.id.webTopView);

        String s = makeUtf8("Пожалуйста, подождите, идет поиск...");
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

        AsyncTask asyncTask = new MyTask(this, true);
        asyncTask.execute("");
    }
}
