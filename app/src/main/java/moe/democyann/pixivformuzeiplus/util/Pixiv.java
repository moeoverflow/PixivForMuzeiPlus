package moe.democyann.pixivformuzeiplus.util;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by democyann on 2017/1/12.
 */

public class Pixiv {
    private static Cookie cookie;
    private static String token;
    private String restring;

    private final String INDEX_URL = "http://www.pixiv.net";
    private final String POST_KEY_URL = "https://accounts.pixiv.net/login?lang=zh&source=pc&view_type=page&ref=wwwtop_accounts_index";
    private final String LOGIN_URL = "https://accounts.pixiv.net/api/login?lang=zh";
    private final String RECOMM_URL = "http://www.pixiv.net/rpc/recommender.php?type=illust&sample_illusts=auto&num_recommendations=500&tt=";
    private final String ILLUST_URL="http://www.pixiv.net/rpc/illust_list.php?verbosity=&exclude_muted_illusts=1&illust_ids=";

    private final String DETA_URL="https://app-api.pixiv.net/v1/illust/detail?illust_id=";

    private final String RALL_URL="http://www.pixiv.net/ranking.php?mode=daily&content=illust&p=1&format=json";


    private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) "
            + "Chrome/42.0.2311.152 Safari/537.36";

    private Map<String,String> basepre;
    private final String TAG="PixivUtil";
    private Pattern pattern;
    private Matcher matcher;
    private String PostKey="";
    public Pixiv(){
        this.cookie=new Cookie();
        basepre= new HashMap<String,String>();
        basepre.put("User-Agent", USER_AGENT);
        basepre.put("Accept-Encoding", "gzip,deflate,sdch");
        basepre.put("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        Log.i(TAG, "Pixiv: init");
    }

    public void setCookie(Cookie cookie){
        this.cookie=cookie;
    }

    public String getCookie(){
        return cookie.toString();
    }

    private void getPostKey(){
        HttpUtil postkeyurl=new HttpUtil(POST_KEY_URL,cookie);
        if(postkeyurl.checkURL()) {
            restring = postkeyurl.getData(basepre);
            if (restring.equals("ERROR")) {
                Log.e(TAG, "Post Key get Filed");
                return;
            }
            cookie=postkeyurl.getCookie();
            pattern=Pattern.compile("name=\"post_key\"\\svalue=\"([a-z0-9]{32})\"",Pattern.DOTALL);
            matcher= pattern.matcher(restring);
            if (matcher.find()) {
                PostKey = matcher.group(1);
            } else {
                Log.e(TAG, "Post Key Not Find");
                return;
            }
        }else{
            Log.e(TAG, "URL Error");
            return;
        }
    }

    private String login(String pixiv_id,String password){
        getPostKey();
        if(PostKey.equals("")){
            return "ERROR";
        }
        HttpUtil login_url=new HttpUtil(LOGIN_URL,cookie);
        if(login_url.checkURL()){
            basepre.put("Accept", "application/json, text/javascript, */*; q=0.01");
            restring= login_url.postData(basepre,"pixiv_id="+pixiv_id+"&password="+password+"&captcha=&g_recaptcha_response=&post_key="
                    + PostKey
                    + "&source=pc&ref=wwwtop_accounts_index&return_to=http://www.pixiv.net/");

            if (restring.equals("ERROR")) {
                Log.e(TAG, "Login Filed");
                return "ERROR";
            }
            cookie=login_url.getCookie();

            try {

                restring=restring.replaceFirst("null","");
                Log.i(TAG, restring);
                JSONObject json= new JSONObject(restring);
                if(json.getBoolean("error")){
                    Log.i(TAG, json.getString("message"));
                    return "ERROR";
                }else{
                    return "OK";
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return "ERROR";
            }
        }else{
            Log.e(TAG, "URL Error");
            return "ERROR";
        }
    }

    public String getToken(String pixiv_id,String password,boolean login){
        String re;
        if(login) {
           re = login(pixiv_id, password);
        }else{
            re="OK";
        }
        if(!re.equals("OK")){
            token="";
            return "";
        }else{
            HttpUtil index = new HttpUtil(INDEX_URL,cookie);
            index.checkURL();
            basepre.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            restring=index.getData(basepre);
            if(restring.equals("ERROR")){
                return "";
            }
            cookie=index.getCookie();
            pattern = Pattern.compile("pixiv.context.token\\s=\\s\"([a-z0-9]{32})\"", Pattern.DOTALL);
            matcher = pattern.matcher(restring);
            if (matcher.find()) {
                token = matcher.group(1);

            }else{
                Log.e(TAG, "Not Find Token");
                return "";
            }
            return token;
        }
    }

    public List getRcomm(){
        List list= new ArrayList();
        HttpUtil recomm=new HttpUtil(RECOMM_URL+token,cookie);
        recomm.checkURL();
        Map<String,String> recprer=basepre;
        recprer.put("Referer", "http://www.pixiv.net/recommended.php");
        recprer.put("Accept", "application/json, text/javascript, */*; q=0.01");
        restring=recomm.getData(recprer);
        if(restring.equals("ERROR")){
            return null;
        }

        try {
            restring=restring.replaceFirst("null","");
            Log.i(TAG, restring);
            JSONObject o= new JSONObject(restring);
            JSONArray arr= o.getJSONArray("recommendations");
            for(int i=0;i<arr.length();i++) {
                list.add(arr.getInt(i));
            }
        } catch (JSONException e) {
            Log.e(TAG, e.toString(),e );
            return null;
        }
        return list;
    }

    public JSONObject getIllInfo(String id){
        HttpUtil illust=new HttpUtil(DETA_URL+id,cookie);
        illust.checkURL();
        Map<String,String> recprer=basepre;
//        recprer.put("Referer", "http://www.pixiv.net/recommended.php");
        recprer.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
//        recprer.put("Authorization","Bearer "+token);
        restring=illust.getData(recprer);
        if(restring.equals("ERROR")){
            return null;
        }
        try {
            restring=restring.replaceFirst("null","");
            Log.i(TAG, restring);
            JSONObject o = new JSONObject(restring);
            return o;
        } catch (JSONException e) {
            Log.e(TAG, e.toString(),e );
            return null;
        }
    }

    public JSONObject getIllInfo2(String id){
        Log.i(TAG, "getIllInfo: "+token);
        HttpUtil illust=new HttpUtil(ILLUST_URL+id+"&tt="+token,cookie);
        illust.checkURL();
        Map<String,String> recprer=basepre;
        recprer.put("Referer", "http://www.pixiv.net/recommended.php");
        recprer.put("Accept", "application/json, text/javascript, */*; q=0.01");
        restring=illust.getData(recprer);
        if(restring.equals("ERROR")){
            return null;
        }
        try {
            restring=restring.replaceFirst("null","");
            Log.i(TAG, restring);
            JSONArray arr = new JSONArray(restring);
            return arr.getJSONObject(0);
        } catch (JSONException e) {
            Log.e(TAG, e.toString(),e );
            return null;
        }
    }

    public Uri downloadImage(String imgurl,String workid,File file){
        String smail=imgurl;
        String ref="http://www.pixiv.net/member_illust.php?mode=medium&illust_id=" + workid;

        pattern=Pattern.compile("^(https?://.+?/c/)[0-9]+x[0-9]+(/img-master.+)$");
        matcher=pattern.matcher(imgurl);
        String big=smail;
        if(matcher.find()){
            big=matcher.group(1)+ "1200x1200"+matcher.group(2);
        }
        HttpUtil download = new HttpUtil(big,null);
        download.checkURL();

        if(download.downloadImg(ref,USER_AGENT,file)){
            return Uri.parse("file://" + file.getAbsolutePath());
        }else{
            return null;
        }
    }

    public Uri downloadImage2(String imgurl,String workid,File file){

        String ref="http://www.pixiv.net/member_illust.php?mode=medium&illust_id=" + workid;

        HttpUtil download = new HttpUtil(imgurl,null);
        download.checkURL();

        if(download.downloadImg(ref,USER_AGENT,file)){
            return Uri.parse("file://" + file.getAbsolutePath());
        }else{
            return null;
        }
    }


    public JSONArray getRalllist(){
        HttpUtil rall= new HttpUtil(RALL_URL,new Cookie());
        rall.checkURL();
        basepre.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        restring=rall.getData(basepre);
        if(restring.equals("ERROR")){
            Log.e(TAG, "getRalllist: get RALL ERROR");
            return null;
        }
        JSONArray arr=null;
        restring=restring.replaceFirst("null","");
//        Log.i(TAG, restring.substring(50000,restring.length()));
        try {
            JSONObject o = new JSONObject(restring);
            arr= o.getJSONArray("contents");
        } catch (JSONException e) {
            Log.e(TAG, e.toString(), e);
        }
        return arr;
    }

}
