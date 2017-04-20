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
 * Created by demo on 3/31/17.
 */

public class Pixiv {
    private static Cookie cookie;
    private static String token;
    private static String userid;
    private String restring;

    private final String INDEX_URL = "http://www.pixiv.net";
    private final String POST_KEY_URL = "https://accounts.pixiv.net/login?lang=zh&source=pc&view_type=page&ref=wwwtop_accounts_index";
    private final String LOGIN_URL = "https://accounts.pixiv.net/api/login?lang=zh";
    private final String RECOMM_URL = "http://www.pixiv.net/rpc/recommender.php?type=illust&sample_illusts=auto&num_recommendations=500&tt=";
    private final String ILLUST_URL="http://www.pixiv.net/rpc/illust_list.php?verbosity=&exclude_muted_illusts=1&illust_ids=";

    private final String DETA_URL="https://app-api.pixiv.net/v1/illust/detail?illust_id=";

    private final String RALL_URL="http://www.pixiv.net/ranking.php?mode=daily&content=illust&p=1&format=json";

    private final String BOOK_URL="https://app-api.pixiv.net/v1/user/bookmarks/illust?restrict=public&user_id=";

    private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) "
            + "Chrome/42.0.2311.152 Safari/537.36";

    private Map<String,String> basepre;
    private final String TAG="PixivUtil";
    private Pattern pattern;
    private Matcher matcher;
    private String PostKey="";
    private String error="0";

    public Pixiv(){
        this.cookie=new Cookie();
        basepre= new HashMap<String,String>();
        basepre.put("User-Agent", USER_AGENT);
        basepre.put("Accept-Encoding", "gzip,deflate,sdch");
        basepre.put("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        Log.i(TAG, "Pixiv: init");
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    /***
     * 获取错误代码
     * @return 错误代码
     */
    public String getError(){
        return error;
    }

    /***
     * 设置Cookie
     * @param cookie
     */
    public void setCookie(Cookie cookie){
        this.cookie=cookie;
    }

    /***
     * 获取字符串类型的Cookie
     * @return
     */
    public String getCookie(){
        return cookie.toString();
    }

    public void setToken(String token){
        this.token =token;
    }

    /***
     * 获取POST ID 私有
     */
    private void getPostKey(){

        error="0";

        HttpUtil postkeyurl=new HttpUtil(POST_KEY_URL,cookie);
        if(postkeyurl.checkURL()) {
            restring = postkeyurl.getData(basepre);
            if (restring.equals("ERROR")) {
                Log.e(TAG, "Post Key get Filed");
                error="1001";
                return;
            }
            cookie=postkeyurl.getCookie();
            pattern=Pattern.compile("name=\"post_key\"\\svalue=\"([a-z0-9]{32})\"",Pattern.DOTALL);
            matcher= pattern.matcher(restring);
            if (matcher.find()) {
                PostKey = matcher.group(1);
            } else {
                Log.e(TAG, "Post Key Not Find");
                error="1002";
                return;
            }
        }else{
            Log.e(TAG, "URL Error");
            error="1003";
            return;
        }
    }

    /***
     * 登录 Pixiv 私有
     * @param pixiv_id 用户名/邮箱/ID
     * @param password 密码
     * @return 成功返回 OK 失败返回 ERROR
     */
    private boolean login(String pixiv_id,String password){
        getPostKey();
        if(PostKey.equals("")){
            return false;
        }
        HttpUtil login_url=new HttpUtil(LOGIN_URL,cookie);
        if(login_url.checkURL()){
            basepre.put("Accept", "application/json, text/javascript, */*; q=0.01");
            restring= login_url.postData(basepre,"pixiv_id="+pixiv_id+"&password="+password+"&captcha=&g_recaptcha_response=&post_key="
                    + PostKey
                    + "&source=pc&ref=wwwtop_accounts_index&return_to=http://www.pixiv.net/");

            if (restring.equals("ERROR")) {
                Log.e(TAG, "Login Filed");
                error="1004";
                return false;
            }
            cookie=login_url.getCookie();

            try {

                restring=restring.replaceFirst("null","");
                Log.i(TAG, "======LOGIN restart:"+restring);
                JSONObject json= new JSONObject(restring);
                JSONObject obj= json.getJSONObject("body");
                if(obj.isNull("success")){
                    Log.i(TAG, json.getString("message"));
                    error="1005";
                    return false;
                }else{
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                error="1006";
                return false;
            }
        }else{
            Log.e(TAG, "URL Error");
            error="1007";
            return false;
        }
    }


    /***
     * 登录并获取tooken
     * @param pixiv_id 用户名
     * @param password 密码
     * @param login 是否登录
     * @return
     */
    public String getToken(String pixiv_id,String password,boolean login){
        boolean re;
        if(login) {
            re = login(pixiv_id, password);
        }else{
            re=true;
        }
        if(!re){
            token="";
            return "";
        }else{
            HttpUtil index = new HttpUtil(INDEX_URL,cookie);
            index.checkURL();
            basepre.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            restring=index.getData(basepre);
            if(restring.equals("ERROR")){
                error="1008";
                return "";
            }
            cookie=index.getCookie();
            pattern = Pattern.compile("pixiv.context.token\\s=\\s\"([a-z0-9]{32})\"", Pattern.DOTALL);
            matcher = pattern.matcher(restring);
            if (matcher.find()) {
                token = matcher.group(1);

            }else{
                Log.e(TAG, "Not Find Token");
                error="1009";
                return "";
            }
            pattern = Pattern.compile("pixiv.user.id\\s=\\s\"(\\d+)\"", Pattern.DOTALL);
            matcher = pattern.matcher(restring);
            if (matcher.find()) {
                userid = matcher.group(1);
                Log.i(TAG, "USER_ID: "+ userid);
            }

            return token;
        }
    }

    /***
     * 获取推荐列表
     * @return 推荐列表
     */
    public List getRcomm(){
        List list= new ArrayList();
        Log.i(TAG, "getRcomm: TOKEN:"+token);
        Log.i(TAG, "getRcomm: COOKIE:"+cookie);
        HttpUtil recomm=new HttpUtil(RECOMM_URL+token,cookie);
        recomm.checkURL();
        Map<String,String> recprer=basepre;
        recprer.put("Referer", "http://www.pixiv.net/recommended.php");
        recprer.put("Accept", "application/json, text/javascript, */*; q=0.01");
        restring=recomm.getData(recprer);
        if(restring.equals("ERROR")){
            error="1021";
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
            error="1022";
            return null;
        }
        return list;
    }


    /***
     * 获取每日 TOP 50 列表
     * @return
     */
    public JSONArray getRalllist(){
        HttpUtil rall= new HttpUtil(RALL_URL,new Cookie());
        rall.checkURL();
        basepre.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        restring=rall.getData(basepre);
        if(restring.equals("ERROR")){
            Log.e(TAG, "getRalllist: get RALL ERROR");
            error="1023";
            return null;
        }
        JSONArray arr=null;
        restring=restring.replaceFirst("null","");
//        Log.i(TAG, restring.substring(50000,restring.length()));
        try {
            JSONObject o = new JSONObject(restring);
            arr= o.getJSONArray("contents");
        } catch (JSONException e) {
            error="1024";
            Log.e(TAG, e.toString(), e);
        }
        return arr;
    }


    public List getBooklist(){
        ArrayList list= new ArrayList();
        HttpUtil book;
        String tempurl=BOOK_URL+userid;

        for(int j=0;j<9;j++) {
            book = new HttpUtil(tempurl, cookie);
            book.checkURL();

            Map<String, String> recprer = basepre;
            recprer.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            restring = book.getData(recprer);
            if (restring.equals("ERROR")) {
                error = "1041";
                return null;
            }

            try {
                restring = restring.replaceFirst("null", "");
                Log.i(TAG, restring);
                JSONObject o = new JSONObject(restring);
                JSONArray ill = o.getJSONArray("illusts");
                for (int i = 0; i < ill.length(); i++) {
                    JSONObject t = ill.getJSONObject(i);
                    list.add(t.get("id"));
                }
                tempurl=o.getString("next_url");
                if(o.isNull("next_url")){
                    break;
                }

            } catch (JSONException e) {
                Log.e(TAG, e.toString(), e);
                error = "1042";
                return null;
            }

        }

        return list;
    }

    /***
     * 获取作品信息方式1（R18作品会获取失败）
     * @param id 作品ID
     * @return
     */
    private JSONObject getIllInfo1(String id){
        HttpUtil illust=new HttpUtil(DETA_URL+id,cookie);
        illust.checkURL();
        Map<String,String> recprer=basepre;
//        recprer.put("Referer", "http://www.pixiv.net/recommended.php");
        recprer.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
//        recprer.put("Authorization","Bearer "+token);
        restring=illust.getData(recprer);
        if(restring.equals("ERROR")){
            error="1031";
            return null;
        }
        try {
            restring=restring.replaceFirst("null","");
            Log.i(TAG, restring);
            JSONObject o = new JSONObject(restring);
            return o;
        } catch (JSONException e) {
            Log.e(TAG, e.toString(),e );
            error="1032";
            return null;
        }
    }

    /***
     * 获取作品信息方式2
     * @param id 作品ID
     * @return
     */
    private JSONObject getIllInfo2(String id){
        Log.i(TAG, "getIllInfo: "+token);
        HttpUtil illust=new HttpUtil(ILLUST_URL+id+"&tt="+token,cookie);
        illust.checkURL();
        Map<String,String> recprer=basepre;
        recprer.put("Referer", "http://www.pixiv.net/recommended.php");
        recprer.put("Accept", "application/json, text/javascript, */*; q=0.01");
        restring=illust.getData(recprer);
        if(restring.equals("ERROR")){
            error="1033";
            return null;
        }
        try {
            restring=restring.replaceFirst("null","");
            Log.i(TAG, restring);
            JSONArray arr = new JSONArray(restring);
            return arr.getJSONObject(0);
        } catch (JSONException e) {
            Log.e(TAG, e.toString(),e );
            error="1034";
            return null;
        }
    }

    /***
     * 获取作品信息方式
     * @param id 作品ID
     * @return
     */
    public ImgInfo getIllInfo(String id){

        ImgInfo o = new ImgInfo();

        String img_url="";  //图像地址
        String img_name=""; //图像标题
        String img_id="";   //图像ID
        String user_id="";  //作者ID
        String user_name="";//作者名称
        String tags="";     //图像标签
        boolean r18=false;  //R18标志

        int view=0;         //浏览数量

        //=============解析信息1=============

        JSONObject temp=getIllInfo1(id);

        if(temp==null) {
            return null;
        }

        try {

            JSONObject ill=temp.getJSONObject("illust");
            JSONObject imgurls;

            //获取浏览数量
            view=ill.getInt("total_view");

            //根据不同的页数获取图片地址
            if(ill.getInt("page_count")>1){
                imgurls=ill.getJSONArray("meta_pages").getJSONObject(0).getJSONObject("image_urls");
                img_url=imgurls.getString("original");
            }else {
                imgurls = ill.getJSONObject("meta_single_page");
                img_url=imgurls.getString("original_image_url");
            }

            CharSequence c= "limit_r18";

            if(img_url.contains(c)){
                r18=true;
            }else{
                r18=false;
            }

            JSONObject user = ill.getJSONObject("user");
            user_id=user.getString("id");
            user_name=user.getString("name");
            img_id=ill.getString("id");
            img_name=ill.getString("title");
            tags=ill.getString("tags");


        } catch (JSONException e) {
            e.printStackTrace();
            error+=",1035";
            return null;
        }

        //=================解析信息2=============
        if(r18){
            temp=getIllInfo2(id);
            if(temp==null) {
                return null;
            }

            try {

                user_id=temp.getString("illust_user_id");
                img_id=temp.getString("illust_id");
                img_url=temp.getString("url");
                user_name=temp.getString("user_name");
                img_name=temp.getString("illust_title");
                tags=temp.getString("tags");

            } catch (JSONException e) {
                error+=",1036";
                e.printStackTrace();
                return null;
            }
        }

        o.setImg_id(img_id);
        o.setImg_name(img_name);
        o.setImg_url(img_url);
        o.setUser_id(user_id);
        o.setUser_name(user_name);
        o.setR18(r18);
        o.setTags(tags);
        o.setView(view);

        return o;
    }


    /***
     * 下载图片
     * @param imgurl 图片地址
     * @param workid 作品ID
     * @param file   存储位置
     * @param x      true 进行地址转换，false 不转换
     * @return  图片文件 Uri
     */
    public Uri downloadImage(String imgurl, String workid, File file,boolean x){
        String smail=imgurl;
        String ref="http://www.pixiv.net/member_illust.php?mode=medium&illust_id=" + workid;


        String big=smail;

        if(x) {
            pattern = Pattern.compile("^(https?://.+?/c/)[0-9]+x[0-9]+(/img-master.+)$");
            matcher = pattern.matcher(imgurl);
            if (matcher.find()) {
                big = matcher.group(1) + "1200x1200" + matcher.group(2);
            }
        }

        HttpUtil download = new HttpUtil(big,null);
        download.checkURL();

        if(download.downloadImg(ref,USER_AGENT,file)){
            return Uri.parse("file://" + file.getAbsolutePath());
        }else{
            return null;
        }
    }




}
