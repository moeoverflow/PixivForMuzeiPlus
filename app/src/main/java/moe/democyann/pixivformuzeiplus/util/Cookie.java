package moe.democyann.pixivformuzeiplus.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by demo on 3/31/17.
 */

public class Cookie {
    private Map<String, String> cookie;
    public Cookie(){
        cookie=new HashMap<String, String>();
    }
    public Cookie(String str){
        cookie=new HashMap<String, String>();

        if(!"".equals(str)){
            String[] cooarr=str.split(";");
            for(String c:cooarr){
                add(c);
            }
        }

    }
    public boolean add(String key,String value){
        cookie.put(key, value);
        return true;
    }

    public boolean add(String str){
        if(str.indexOf("=")==-1) return false;
        String[] arr=str.split("=");
        this.add(arr[0], arr[1]);
        return true;
    }

    public void remove(String key){
        cookie.remove(key);
    }

    public String toString(){
        StringBuffer temp = new StringBuffer();
        for(String key:cookie.keySet()){
            temp.append(key).append("=").append(cookie.get(key)).append(";");
        }
        return temp.toString();
    }
}
