package moe.democyann.pixivformuzeiplus;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Exchanger;

import moe.democyann.pixivformuzeiplus.util.Cookie;
import moe.democyann.pixivformuzeiplus.util.Pixiv;
import moe.democyann.pixivformuzeiplus.util.TagFliter;

public class PixivSource extends RemoteMuzeiArtSource{
    private static final String TAG = "PixivSource";
    private static final String SOURCE_NAME = "PixivSource";
    private static Pixiv pixiv=new Pixiv();;
    private static String prevfile;
    private final  int MINUTE=60*1000;
    private static String token="";
    private static List list=null;
    private static long last=0;
    private static JSONArray rall=null;

    private static String pixivid="";
    private static String password="";


    public PixivSource() {
        super(SOURCE_NAME);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setUserCommands(BUILTIN_COMMAND_ID_NEXT_ARTWORK);
    }

    @Override
    public void onTryUpdate(int reason) throws RetryException {
        Log.i(TAG, "onTryUpdate: "+reason);

        if (isOnlyUpdateOnWifi() && !isEnabledWifi()) {
            Log.d(TAG, "no wifi");
            scheduleUpdate();
            return;
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean defaultValue = false,
                v = preferences.getBoolean("is_user", defaultValue);
        Log.i(TAG, "onTryUpdate: "+v);
        Artwork artwork=getCurrentArtwork();
        Uri temp;
        String temptoken;
        if(getCurrentArtwork()!=null)
        {
            temptoken=artwork.getToken();
            temp=artwork.getImageUri();
        }else{
            temp=Uri.parse("");
            temptoken=null;

        }

        Artwork a1= new Artwork.Builder()
                .title(getResources().getString(R.string.loading))
                .byline(getResources().getString(R.string.loading))
                .imageUri(temp)
                .token(temptoken)
                .build();
        publishArtwork(a1);
        scheduleUpdate(0);

        if(pixiv.getCookie()==null || "".equals(pixiv.getCookie().toString())) {
            String cookiestr = getLocalCookie();
            if (!"".equals(cookiestr)) {
                String[] cookarr = cookiestr.split(";");
                Cookie c = new Cookie();
                for (String cr : cookarr) {
                    c.add(cr);
                }
                pixiv.setCookie(c);
                flushToken();
            }
        }

        try{
            if (v) {
                pixivid = getUserName();
                password = getPassword();
                if (!"".equals(pixivid) && !"".equals(password)) {
                    artwork=pixivUserPush();
                } else {
                    Log.i(TAG, "onTryUpdate: 还未设置PixivID及密码");
                    artwork =noPixivUser(getResources().getString(R.string.u_err));
                }
            } else {
                artwork=noPixivUser("");
            }
        }catch (Exception e){
            publishArtwork(getCurrentArtwork());
        }finally {
            if(artwork!=null) {
                publishArtwork(artwork);
                scheduleUpdate(5000);
                try {
                    if (prevfile != null && !"".equals(prevfile)) {
                        Application app = getApplication();
                        final File prev = new File(app.getExternalCacheDir(), prevfile);
                        prev.delete();
                    }
                }catch (Exception e){
                    scheduleUpdate(5000);
                    e.printStackTrace();
                }

                prevfile=artwork.getToken();

            }else{
                scheduleUpdate(5000);
                return;
            }
            setLocalSet("cookie",pixiv.getCookie());
            scheduleUpdate();
        }
    }
    private boolean isOnlyUpdateOnWifi() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean defaultValue = false,
                v = preferences.getBoolean("only_wifi", defaultValue);
        Log.d(TAG, "pref_onlyWifi = " + v);
        return v;
    }
    private boolean isEnabledWifi() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifi.isConnected();
    }

    private String getUserName(){
        SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(this);
        String defaultValue = "",
                v = preferences.getString("pixivid", defaultValue);
        return v;
    }
    private String getPassword(){
        SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(this);
        String defaultValue = "",
                v = preferences.getString("password", defaultValue);
        return v;
    }

    private String getLocalCookie(){
        SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(this);
        String defaultValue = "",
                v = preferences.getString("cookie", defaultValue);
        Log.i(TAG, "getLocalCookie: "+v);
        return v;
    }
    private void setLocalSet(String key,String value){
        Log.i(TAG, "setLocalCookie: ");
        SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor= preferences.edit();
        editor.putString(key,value);
        editor.apply();
    }
    public Artwork noPixivUser(String mser)throws RetryException{
        if(rall==null || rall.length()==0 || (System.currentTimeMillis()-last)>(120*MINUTE)){
            rall=pixiv.getRalllist();
            last=System.currentTimeMillis();
//            setLocalSet("last",String.valueOf(last));
        }

        if(rall!=null && rall.length()>0){
            JSONObject o;
            String user_id = "1";
            String img_id = "1";
            String img_url = "";
            String user_name;
            String illust_title;
            String tags = "";
            while(true) {
                Random r = new Random();
                int i = r.nextInt(rall.length());

                try {
                    o = rall.getJSONObject(i);
                    user_id = o.getString("user_id");
                    img_id = o.getString("illust_id");
                    img_url = o.getString("url");
                    user_name = o.getString("user_name");
                    illust_title = o.getString("title");
                    tags = o.getString("tags");
                } catch (JSONException e) {
                    Log.e(TAG, e.toString(), e);
                    throw new RetryException();
                }

                Log.i(TAG, tags);
                if (getIs_no_R18()) {
                    if (TagFliter.is_r18(tags)) continue;
                }

                if (getIs_check_Tag()) {
                    if (!TagFliter.checkTagAll(getTags(), tags)) break;
                } else {
                    break;
                }
            }

            Application app = getApplication();
            if(app==null){
                Log.e(TAG, "onTryUpdate: APP Error" );
                throw new RetryException();
            }

            File file = new File(app.getExternalCacheDir(),user_id+img_id);
            Uri fileUri =pixiv.downloadImage(img_url,img_id,file);
            if(!mser.equals("")) user_name=mser;
            Artwork artwork = new Artwork.Builder()
                    .title(illust_title)
                    .byline(user_name)
                    .imageUri(fileUri)
                    .token(user_id+img_id)
                    .viewIntent(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.pixiv.net/member_illust.php?mode=medium&illust_id=" + img_id)))
                    .build();
//            publishArtwork(artwork);
//            if (prevfile != null) {
//                final File prev = new File(app.getExternalCacheDir(),prevfile);
//                prev.delete();
//            }else{
//                prevfile=user_id+img_id;
//            }
//            scheduleUpdate();
            return artwork;
        }
        return null;
    }



    private Artwork pixivUserPush() throws RetryException {
        if(token.equals("")) {
            token=pixiv.getToken(pixivid, password,true);
            if("".equals(token)){
                Artwork a = new Artwork.Builder()
                        .imageUri(getCurrentArtwork().getImageUri())
                        .title(getCurrentArtwork().getTitle())
                        .token(getCurrentArtwork().getToken())
                        .byline(getResources().getString(R.string.login_failed))
                        .build();

                prevfile="";
                return a;
            }
        }

        if(!token.equals("")){
            if(list==null|| list.size()==0 || (System.currentTimeMillis()-last)>(120*MINUTE)) {
                list = pixiv.getRcomm();
                last=System.currentTimeMillis();
//                setLocalSet("last",String.valueOf(last));
                Log.i(TAG, "List Update");
                Log.i(TAG, "List Size:"+list.size());
                Log.i(TAG, "time:"+(System.currentTimeMillis()-last));
            }
            if(list!=null && list.size()!=0){
                JSONObject data;
                JSONObject illust;

                while(true) {
                    Random random = new Random();
                    int i = random.nextInt(list.size());
                    String imgid = String.valueOf(list.get(i));
                    data = pixiv.getIllInfo(imgid);
                    if (data == null) {
                        flushToken();
                        return null;
                    }
                    long views;

                    try{
                        illust=data.getJSONObject("illust");
                        views=illust.getLong("total_view");
                        Log.i(TAG, "pixivUserPush Views: "+views);
                    } catch (JSONException e) {
                        Log.e(TAG, e.toString(),e );
                        throw new RetryException();
                    }
                    Log.i(TAG, "pixivUserPush: get"+getViews());
                    if(views<getViews()){
                        Log.i(TAG, "浏览数不足，重新加载"+getViews());
                        continue;
                    }

                    String tags="";
                    try{
                        tags=illust.getString("tags");
                    }catch (Exception e){
                        Log.e(TAG, e.toString(),e );
                        throw new RetryException();
                    }

                    Log.i(TAG, tags);
                    if(getIs_no_R18()){
                        if(TagFliter.is_r18(tags)) continue;
                    }

                    if(getIs_check_Tag()){
                        if(!TagFliter.checkTagAll(getTags(),tags)) break;
                    }else{
                        break;
                    }

                }
                Application app = getApplication();
                if(app==null){
                    Log.e(TAG, "onTryUpdate: APP Error" );
                    throw new RetryException();
                }
                String user_id="1";
                String img_id="1";
                String img_url="";
                String user_name;
                String illust_title;

                JSONObject user;
                JSONObject imgurls;
//                String tag;
                try {
                    user=illust.getJSONObject("user");
                    if(illust.getInt("page_count")>1){
                        imgurls=illust.getJSONArray("meta_pages").getJSONObject(0).getJSONObject("image_urls");
                        img_url=imgurls.getString("original");
                    }else {
                        imgurls = illust.getJSONObject("meta_single_page");
                        img_url=imgurls.getString("original_image_url");
                    }

                    user_id=user.getString("id");
                    img_id=illust.getString("id");

                    user_name=user.getString("name");
                    illust_title=illust.getString("title");
//                    user_id=o.getString("illust_user_id");
//                    img_id=o.getString("illust_id");
//                    img_url=o.getString("url");
//                    user_name=o.getString("user_name");
//                    illust_title=o.getString("illust_title");
//                    tag=o.getString("tags");
                } catch (JSONException e) {
                    Log.e(TAG, e.toString(),e );
                    throw new RetryException();
                }


                File file = new File(app.getExternalCacheDir(),user_id+img_id);
                Uri fileUri =pixiv.downloadImage2(img_url,img_id,file);
                Artwork artwork = new Artwork.Builder()
                        .title(illust_title)
                        .byline(user_name)
                        .imageUri(fileUri)
                        .token(user_id+img_id)
                        .viewIntent(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.pixiv.net/member_illust.php?mode=medium&illust_id=" + img_id)))
                        .build();
                return artwork;
//                publishArtwork(artwork);
//                if (prevfile != null) {
//                    final File prev = new File(app.getExternalCacheDir(),prevfile);
//                    prev.delete();
//                }else{
//                    prevfile=user_id+img_id;
//                }
//                scheduleUpdate();
            }
        }
        return null;
    }


    private void flushToken() throws RetryException {
        Log.i(TAG, "flushToken: ");
        token="";
        token=pixiv.getToken(null,null,false);
        if(token.equals("")){
            token=pixiv.getToken(pixivid,password,true);
        }
        scheduleUpdate(5000);
    }

    private int getChangeInterval() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String defaultValue = getString(R.string.time_default),
                s = preferences.getString("time_change", defaultValue);
        Log.d(TAG, "time_change = \"" + s + "\"");
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            Log.w(TAG, e.toString(), e);
            return 0;
        }
    }

    private void scheduleUpdate() {
        int changeInterval = getChangeInterval();
        if (changeInterval > 0) {
            scheduleUpdate(System.currentTimeMillis() + changeInterval * MINUTE);
        }
    }

    private boolean getIs_no_R18(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean defaultValue = true,
                v = preferences.getBoolean("is_no_r18", defaultValue);
        return v;
    }
    private long getViews(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String defaultValue = "0",
                s = preferences.getString("views", defaultValue);
        long v=0;
        try{
            v=Long.valueOf(s);
        }catch (Exception e){
            Log.e(TAG, "getViews: ", e);
        }
        return v;
    }

    private String getTags(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String defaultValue = "",
                v = preferences.getString("tags", defaultValue);
        return v;
    }

    private boolean getIs_check_Tag(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean defaultValue = false,
                v = preferences.getBoolean("is_tag", defaultValue);
        return v;
    }

}
