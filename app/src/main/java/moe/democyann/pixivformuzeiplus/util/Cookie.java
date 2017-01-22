package moe.democyann.pixivformuzeiplus.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Democyann on 2017/1/11.
 */

public class Cookie {
    private Map<String, String> cookie;
    public Cookie(){
        cookie=new HashMap<String, String>();
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
