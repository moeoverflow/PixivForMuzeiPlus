package moe.democyann.pixivformuzeiplus.util;

/**
 * Created by demo on 4/3/17.
 * 图像信息实体
 */

public class ImgInfo {
    private String img_url="";  //图像地址
    private String img_name=""; //图像标题
    private String img_id="";   //图像ID
    private String user_id="";  //作者ID
    private String user_name="";//作者名称
    private String tags="";     //图像标签
    private boolean r18=false;  //R18标志
    private double px=0.5;      //图片宽高比

    private int view=0;         //浏览数量

    public double getPx() {
        return px;
    }

    public void setPx(double px) {
        this.px = px;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public String getImg_name() {
        return img_name;
    }

    public void setImg_name(String img_name) {
        this.img_name = img_name;
    }

    public String getImg_id() {
        return img_id;
    }

    public void setImg_id(String img_id) {
        this.img_id = img_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public boolean isR18() {
        return r18;
    }

    public void setR18(boolean r18) {
        this.r18 = r18;
    }

    public int getView() {
        return view;
    }

    public void setView(int view) {
        this.view = view;
    }
}
