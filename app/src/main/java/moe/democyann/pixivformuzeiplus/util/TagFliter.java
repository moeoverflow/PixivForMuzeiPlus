package moe.democyann.pixivformuzeiplus.util;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Democyann on 2017/1/14.
 */

public class TagFliter {

    private static final String TAG="TagFilter";

    public static boolean is_r18(String str){
        Log.i(TAG, "R18 Checking");
        boolean flag=false;
        Pattern pattern = Pattern.compile("(R-18|R18|r18|r-18)");
        Matcher matcher=pattern.matcher(str);
        if(matcher.find()){
            flag=true;
            Log.i(TAG, "Is R18 Image");
        }
        return flag;
    }

    private static boolean checkTag(String tag,String str){
        boolean flag=false;
        CharSequence cs = tag;
        if(str.contains(cs)){
            flag=true;
        }
        return flag;
    }

    public static boolean checkTagAll(String tag,String str){
        boolean flag=false;
        Log.i(TAG, "Tag Checking");
        String[] arr=tag.split(",");
        for(String itm:arr){
            Log.i(TAG, "checkTagAll: "+itm);
            if("".equals(itm)) continue;
            if(checkTag(itm,str)){
                flag=true;
                Log.i(TAG, "Find Tag "+itm);
                break;
            }
        }
        return flag;
    }
}
