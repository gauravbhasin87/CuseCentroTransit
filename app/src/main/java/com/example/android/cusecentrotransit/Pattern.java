package com.example.android.cusecentrotransit;

import java.util.ArrayList;

/**
 * Created by Gaurav on 5/7/2015.
 */
public class Pattern {
    private String pid;
    private String ln;
    private String rtdir;
    private ArrayList<Pt> pts;

    public Pattern(){
        pts =  new ArrayList<Pt>();
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getLn() {
        return ln;
    }

    public void setLn(String ln) {
        this.ln = ln;
    }

    public String getRtdir() {
        return rtdir;
    }

    public void setRtdir(String rtdir) {
        this.rtdir = rtdir;
    }

    public ArrayList<Pt> getPts() {
        return pts;
    }

    public void setPts(ArrayList<Pt> pts) {
        this.pts = pts;
    }
}

class Pt{
    public String seq;
    public String lat;
    public String lon;
    public String typ;
    public String stpid;
    public String stpnm;
    public String pdist;
}
