package edu.upenn;


import org.apache.commons.lang3.StringUtils;

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

    public double get_mean_fpkm(){
        double sum = (double)0;
        for (int i = 0; i < this.y_list.size(); i++) {
            sum += y_list.get(i);
        }
        return(sum/y_list.size());
    }

    public int get_sample_num(){
        return(this.y_list.size());
    }

    public String get_fpkm_string(String iso_name, String[][] cov_mat){
        ArrayList<String> string_list = new ArrayList<String>();
        String[] collapsed_cov_mat = new String[cov_mat.length];
        for (int i = 0; i < cov_mat.length; i++) {
            collapsed_cov_mat[i] = StringUtils.join(cov_mat[i],"\t");
        }
        for (int i = 0; i < this.y_list.size(); i++) {
            string_list.add(iso_name+"\t"+this.y_list.get(i).toString()+"\t"+this.sd_list.get(i).toString()+"\t"+collapsed_cov_mat[i]);
        }
        return(StringUtils.join(string_list,"\n"));
    }
}
