package com.zxhl.entity;

/**
 * Created by Administrator on 2018/2/1.
 */

public class LinkMan {

    public String LinkManName = "";
    public String LinkManMobile = "";
    public String Email = "";

    public LinkMan()
    {
    }

    public LinkMan(String LinkManName, String LinkManMobile, String Email)
    {
        this.LinkManName = LinkManName;
        this.LinkManMobile = LinkManMobile;
        this.Email = Email;
    }
    public String getLinkManName()
    {
        return LinkManName;
    }

    public void setLinkManName(String LinkManName)
    {
        this.LinkManName = LinkManName;
    }

    public String getLinkManMobile()
    {
        return LinkManMobile;
    }

    public void setLinkManMobile(String LinkManMobile)
    {
        this.LinkManMobile = LinkManMobile;
    }
    public String getEmail()
    {
        return Email;
    }

    public void setEmail(String Email)
    {
        this.Email = Email;
    }

}
