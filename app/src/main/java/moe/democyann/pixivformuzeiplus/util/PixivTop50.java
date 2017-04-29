package moe.democyann.pixivformuzeiplus.util;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;
import java.util.Random;

import moe.democyann.pixivformuzeiplus.dbUtil.DbUtil;

/**
 * Created by demo on 4/3/17.
 * Pixiv 每日Top 50 作品获取类
 */

public class PixivTop50 {

    private static final String TAG  = "TOP 50";
    private JSONArray rall;
    private long last;
    private Context context;
    private Pixiv pixiv;
    private DbUtil db;
    private ConfigManger conf;
    private File dir;

    private static final int MINUTE=1000*60;
    private String error="";
    private int cont=0;

    public String getError() {
        return error;
    }

    public PixivTop50(Context context,File dir){
        this.context=context;
        this.dir=dir;
        pixiv=new Pixiv();
        db= new DbUtil(context);
        conf=new ConfigManger(context);
    }

    private void listUpdate(){

        try {
            last = Long.valueOf(db.getInfo("last"));
        }catch (Exception e){
            e.printStackTrace();
        }

        //列表为空，超时则重新获取
        if(rall==null||rall.length()==0||(System.currentTimeMillis()-last)>(120*MINUTE)){
            rall=pixiv.getRalllist();
            last=System.currentTimeMillis();
            db.setInfo("last",String.valueOf(last));

            Log.i(TAG, "getArtwork: Internet [+]"+rall.toString());
            Log.i(TAG, "getArtwork: ERROR:" + pixiv.getError());
            db.setInfo("rallList",rall.toString());
        }
    }


    public Artwork getArtwork() throws RemoteMuzeiArtSource.RetryException {

        Artwork artwork = null;

        try {
            Log.i(TAG, "getArtwork: DB"+db.getInfo("rallList"));
            rall=new JSONArray(db.getInfo("rallList"));
        } catch (JSONException e) {
            rall=null;
            e.printStackTrace();
        }

        listUpdate();

        if(rall!=null&&rall.length()>0){
            JSONObject o=null;
            String user_id = "1";
            String img_id = "1";
            String img_url = "";
            String user_name;
            String illust_title;
            String tags = "";

            Random r= new Random();
            cont=0;
            while(true){
                int i=r.nextInt(rall.length());

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
                    error=pixiv.getError();
                    throw new RemoteMuzeiArtSource.RetryException();
                }

                if(cont>=5){
                    break;
                }

                if(conf.getIs_check_Tag()){
                    if(TagFliter.checkTagAll(conf.getTage(),tags)){
                        cont++;
                        continue;
                    }
                }

                break;
            }


//            Random ra = new Random();
            int rn=r.nextInt(1000);
            File file = new File(dir,user_id+img_id+rn);
            Uri fileUri=pixiv.downloadImage(img_url,img_id,file,true);
//            if(!mess.equals("")) user_name=mess;
            if(fileUri==null){
                error="2001";
                throw new RemoteMuzeiArtSource.RetryException();
            }
            Uri f = FileProvider.getUriForFile(context, "moe.democyann.pixivformuzeiplus.fileprovider", file);
//            context.grantUriPermission("net.nurik.roman.muzei", f, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Log.i(TAG, "getArtwork: file"+f.toString());
            Log.i(TAG, "getArtwork: uri"+fileUri.toString());
            artwork= new Artwork.Builder()
                    .title(illust_title)
                    .byline(user_name)
                    .imageUri(f)
                    .token(user_id+img_id+rn)
                    .viewIntent(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.pixiv.net/member_illust.php?mode=medium&illust_id=" + img_id)))
                    .build();
        }
        return artwork;
    }

}
