package moe.democyann.pixivformuzeiplus.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource;

import java.io.File;
import java.util.List;
import java.util.Random;

import moe.democyann.pixivformuzeiplus.dbUtil.DbUtil;

/**
 * Created by demo on 4/3/17.
 * pixiv 收藏夹作品获取类
 */

public class PixivLike {
    private static final String TAG  = "Pixiv Like";
    private static final int MINUTE=1000*60;

    private Context context;      //应用程序上下文
    private Pixiv pixiv;          //Pixiv操作类
    private long last;            //上次列表更新时间
    private DbUtil db;            //数据库操作类
    private ConfigManger conf;    //配置管理器
    private File dir;             //文件路径
    private static List list=null;     //推荐列表

    private static String token="";  //登录Token
    private static Cookie cookie;  //登录Cookie
    private static String userid="";
    private int cont=0;


    private String error="";

    public PixivLike(Context context,File dir){
        this.context=context;
        this.dir=dir;
        pixiv=new Pixiv();
        db= new DbUtil(context);
        conf=new ConfigManger(context);
    }

    public String getError() {
        return error;
    }

    public void listUpdate(){
        try {
            last = Long.valueOf(db.getInfo("last"));
        }catch (Exception e){
            e.printStackTrace();
        }

        //列表为空，超时则重新获取
        if(list==null||list.size()<=0||(System.currentTimeMillis()-last)>(60*MINUTE)){
            list=pixiv.getBooklist();
            last=System.currentTimeMillis();
            db.setInfo("last",String.valueOf(last));
            Log.i(TAG, "listUpdate: Internet List Update");
            db.setInfo("likeList",conf.listToString(list));
        }
    }

    public Artwork getArtwork()throws RemoteMuzeiArtSource.RetryException {
        Artwork artwork;

        if("".equals(conf.getUsername()) ||"".equals(conf.getPassword())){
            error="1100";
            throw new RemoteMuzeiArtSource.RetryException();
        }

        //如果没有Cookie，首先尝试从本地获取Cookie缓存
        if(pixiv.getCookie()==null || "".equals(pixiv.getCookie())){
            String coostr = db.getInfo("cookie");
            cookie=new Cookie(coostr);
            pixiv.setCookie(cookie);
        }

        //首先尝试本地获取Token，如没有则重新登录获取
        token=db.getInfo("token");
        if("".equals(token) || !conf.getUsername().equals(db.getInfo("username")) ){
            pixiv.setCookie(new Cookie());
            token=pixiv.getToken(conf.getUsername(),conf.getPassword(),true);
            if(!"".equals(token)){
                //登录成功后，写入本地Token和Cookie
                Log.i(TAG, "getArtwork: ==============="+pixiv.getCookie()+pixiv.getUserid());
                db.setInfo("token",token);
                db.setInfo("cookie",pixiv.getCookie());
                db.setInfo("username",conf.getUsername());
                db.setInfo("userid",pixiv.getUserid());
                db.setInfo("likeList","");
            }else{
                error=pixiv.getError();

                //出现登录失败则清空所有登录记录
                db.setInfo("token","");
                db.setInfo("cookie","");
                db.setInfo("likeList","");
                cookie=new Cookie();
                pixiv.setCookie(cookie);
                throw new RemoteMuzeiArtSource.RetryException();
            }
        }

        Log.i(TAG, "getArtwork: TOKEN:"+token);

        userid=db.getInfo("userid");
        if(!"".equals(userid) && userid!=null){
            Log.i(TAG, "getArtwork: USERID"+userid);
            pixiv.setUserid(userid);
        }
        //获取本地推荐列表

        list=conf.stringToList(db.getInfo("likeList"));

        listUpdate();

        Log.i(TAG, "getArtwork: List SIZE:"+list.size());

        if(list.size()==0){

            Log.i(TAG, "getArtwork: TOKEN 过期重新获取");
            token=pixiv.getToken(conf.getUsername(),conf.getPassword(),false);
            listUpdate();

            if(list.size()==0){
                Log.i(TAG, "getArtwork: TOKEN 过期重新获取(LOGIN)");
                token=pixiv.getToken(conf.getUsername(),conf.getPassword(),true);
                listUpdate();

                if(list.size()==0) {
                    error = pixiv.getError();
                    throw new RemoteMuzeiArtSource.RetryException();
                }
            }

            db.setInfo("token",token);
            db.setInfo("cookie",pixiv.getCookie());
            db.setInfo("userid",pixiv.getUserid());
            db.setInfo("likeList",conf.listToString(list));

            Log.i(TAG, "getArtwork: "+conf.listToString(list));

//            listUpdate();
        }

        ImgInfo info;
        Random r = new Random();
        while(true) {
            int i = r.nextInt(list.size());

            info = pixiv.getIllInfo(String.valueOf(list.get(i)));

            //信息获取失败直接返回
            if (info == null) {
                error = pixiv.getError();
                db.setInfo("likeList", "");
                throw new RemoteMuzeiArtSource.RetryException();
            }

//            if(conf.getIs_check_Tag()){
//                if(TagFliter.checkTagAll(conf.getTage(),info.getTags())){
//                    continue;
//                }
//            }
//
//            if(conf.getView()>info.getView()){
//                continue;
//            }

            if(cont>=5){
                break;
            }

            if(list.size()>50 && conf.getIs_autopx()){
                Log.i(TAG, "getArtwork: =========PX=======");
                Log.i(TAG, "getArtwork: D:"+conf.getPx());
                Log.i(TAG, "getArtwork: I:"+info.getPx());
                double max=conf.getPx()+0.2;
                double min=conf.getPx()-0.1;
                if(info.getPx()>max || info.getPx()<min){
                    Log.i(TAG, "getArtwork: PX retry");
                    cont++;
                    continue;
                }
            }
//
            if(conf.getIs_no_R18() && info.isR18()){
                cont++;
                continue;
            }

            break;
        }

//        Random ra = new Random();
        int rn = r.nextInt(1000);
        File file = new File(dir,info.getUser_id()+info.getImg_id()+rn);
        Uri uri=pixiv.downloadImage(info.getImg_url(),info.getImg_id(),file,true);
        if(uri==null){
            error="2001";
            throw new RemoteMuzeiArtSource.RetryException();
        }
        Uri f = FileProvider.getUriForFile(context, "moe.democyann.pixivformuzeiplus.fileprovider", file);
        artwork= new Artwork.Builder()
                .title(info.getImg_name())
                .byline(info.getUser_name())
                .imageUri(f)
                .token(info.getUser_id()+info.getImg_id()+rn)
                .viewIntent(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.pixiv.net/member_illust.php?mode=medium&illust_id=" + info.getImg_id())))
                .build();

        return artwork;
    }


}
