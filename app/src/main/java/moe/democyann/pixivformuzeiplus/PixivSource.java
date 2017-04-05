package moe.democyann.pixivformuzeiplus;


import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import moe.democyann.pixivformuzeiplus.dbUtil.DbUtil;
import moe.democyann.pixivformuzeiplus.util.ConfigManger;
import moe.democyann.pixivformuzeiplus.util.PixivLike;
import moe.democyann.pixivformuzeiplus.util.PixivTop50;
import moe.democyann.pixivformuzeiplus.util.PixivUser;


/**
 * Created by demo on 4/3/17.
 * Pixiv Source for Muzei
 * Auth:Democyann
 * email:support@democyann.moe
 * github:@democyann
 * blog:https://democyann.moe
 */

public class PixivSource extends RemoteMuzeiArtSource {
    private static final String TAG = "PixivSource";
    private static final String SOURCE_NAME = "PixivSource";
    private static final int MINUTE = 60*1000;

    private static boolean loadflag=false;
    private static String error="";


    private static PixivTop50 pixivtop;    //Top50类
    private static PixivUser pixivUser;    //Pixiv用户推荐类
    private static PixivLike pixivLike;    //Pixiv收藏夹

    private static ConfigManger conf;      //设置管理器
    private DbUtil db;                     //数据库辅助类


    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            loadflag=true;

            Artwork a = null;
            while (true) {

                error="";

                int method=conf.getMethod();
                //每日TOP50模式
                if (method == 0) {
                    try {
                        a = pixivtop.getArtwork();
                    } catch (Exception e) {
                        error = pixivtop.getError();
                        e.printStackTrace();
                    }
                }else if(method == 1){
                    //用户推荐模式
                    try{
                        a = pixivUser.getArtwork();
                    }catch (Exception e){
                        Log.i(TAG, "run: ERROR get User ArtWork");
                        error=pixivUser.getError();
                        Log.i(TAG, "run: ERROR !"+error);
                        e.printStackTrace();
                        try {
                            a = pixivtop.getArtwork();
                        }catch (Exception er){
                            er.printStackTrace();
                            error+=","+pixivtop.getError();
                        }
                    }

                }else{
                    //收藏夹模式
                    try{
                        a = pixivLike.getArtwork();
                    }catch (Exception e){
                        Log.i(TAG, "run: ERROR get Like ArtWork");
                        error=pixivLike.getError();
                        Log.i(TAG, "run: ERROR !"+error);
                        e.printStackTrace();
                        try {
                            a = pixivtop.getArtwork();
                        }catch (Exception er){
                            er.printStackTrace();
                            error+=","+pixivtop.getError();
                        }
                    }
                }
                int i=0;
                if(a!=null) {
                    try {
                         i= db.insertImg(a.toJson().toString());
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                Log.i(TAG, "run: ERROR"+error);
                if(!"".equals(error)){
                    if(error.equals("1100")) error=getString(R.string.u_err);
                    if(error.equals("1005")) error=getString(R.string.login_failed);

                    Artwork t= PixivSource.this.getCurrentArtwork();
                    Artwork p = new Artwork.Builder()
                            .title(t.getTitle())
                            .byline(t.getByline()+"\nERROR:"+error)
                            .imageUri(t.getImageUri())
                            .viewIntent(t.getViewIntent())
                            .token(t.getToken())
                            .build();
                    publishArtwork(p);
                    break;
                }
                if (i > 5) break;
            }
            loadflag=false;
        }
    };



    public PixivSource() {
        super(SOURCE_NAME);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setUserCommands(BUILTIN_COMMAND_ID_NEXT_ARTWORK);
        conf=new ConfigManger(this);
        pixivtop= new PixivTop50(this,getDir());
        pixivUser=new PixivUser(this,getDir());
        pixivLike=new PixivLike(this,getDir());
        db=new DbUtil(this);
    }

    @Override
    protected void onTryUpdate(int i) throws RetryException {

        if(!isEnabledWifi() && conf.isOnlyUpdateOnWifi()){
            scheduleUpdate();
            return;
        }

        Artwork last=getCurrentArtwork();
        Artwork artwork=last;

        String json = db.getImg();
        JSONObject o = null;
        try {
            o = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (o != null) {
            try {
                artwork = Artwork.fromJson(o);
            } catch (JSONException e) {
                e.printStackTrace();

            }
        }

        //进程未启动时则获取新图片
        if(!loadflag) {
            Thread t = new Thread(runnable);
            Log.i(TAG, "onTryUpdate: Thread Start");
            Log.i(TAG, "onTryUpdate: ==========METHOD======"+conf.getMethod());
            t.start();
        }

        //未找到文件则2秒后重新获取下一张图片
        if(artwork!=null) {
            File test = new File(getDir(), artwork.getToken());
            if (!test.exists()) {
                scheduleUpdate(System.currentTimeMillis() + 2 * 1000);
                return;
            }
        }else{
            scheduleUpdate(System.currentTimeMillis() + 5 * 1000);
            return;
        }

        //分享文件前进行授权
        grantUriPermission("net.nurik.roman.muzei", artwork.getImageUri(), Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

        //推送图片
        publishArtwork(artwork);

        //清理无用缓存
        if(artwork!=last){
            try {
                File f= new File(getDir(),last.getToken());
                f.delete();
            }catch (Exception e){
                e.printStackTrace();
            }

        }

        scheduleUpdate();
    }

    private File getDir(){
        Application app = getApplication();
        if(app.getExternalCacheDir()==null){
            return app.getCacheDir();
        }else{
            return app.getExternalCacheDir();
        }
    }


    /***
     * 获取是否开启了 Wifi 网络
     * @return
     */
    private boolean isEnabledWifi() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifi.isConnected();
    }

    /***
     * 设置默认更新时间
     */

    private void scheduleUpdate() {
        int changeInterval = conf.getChangeInterval();
        if (changeInterval > 0) {
            scheduleUpdate(System.currentTimeMillis() + changeInterval * MINUTE);
        }
    }
}
