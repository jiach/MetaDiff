package edu.upenn;

import java.util.ArrayList;

/**
 * Created by cheng on 1/7/15.
 */
public class ArrFPKM {
    ArrayList<Double> y_list = new ArrayList<Double>();
    ArrayList<Double> sd_list = new ArrayList<Double>();

    public void append(double y, double sd){
        this.y_list.add(y);
        this.sd_list.add(sd);
    }

    public void append(double y, double conf_lo, double conf_hi){
        this.y_list.add(y);
        this.sd_list.add((conf_hi-conf_lo)/2/1.959963984540053605343);
    }
}
