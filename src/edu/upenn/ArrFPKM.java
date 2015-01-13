package edu.upenn;


import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by cheng on 1/7/15.
 */
public class ArrFPKM {
    ArrayList<Double> y_list = new ArrayList<Double>();
    ArrayList<Double> variance_list = new ArrayList<Double>();
    Map<String, Double> cv = null;

    public void append(double y, double sd){
        this.y_list.add(y);
        this.variance_list.add(sd * sd);
    }

    public void append(double y, double conf_lo, double conf_hi){
        this.y_list.add(y);
        this.variance_list.add(((conf_hi-y)/1.959963984540053605343)*((conf_hi-y)/1.959963984540053605343));
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
            string_list.add(iso_name+"\t"+this.y_list.get(i).toString()+"\t"+this.variance_list.get(i).toString()+"\t"+collapsed_cov_mat[i]);
        }
        return(StringUtils.join(string_list,"\n"));
    }

    public Map<String, Double> get_cv(String[] group_var, String[] uniq_group){
        Map<String, Double> group_to_cv_map = new TreeMap<String, Double>();
        Map<String, ArrayList<Double> > grouped_y = new TreeMap<String, ArrayList<Double>>();
        for (String aUniq_group : uniq_group){
            grouped_y.put(aUniq_group, new ArrayList<Double>());
            for (int i = 0; i < group_var.length; i++) {
                if (group_var[i].equals(aUniq_group)){
                    grouped_y.get(aUniq_group).add(this.y_list.get(i));
                }
            }
        }

        StandardDeviation std_stat = new StandardDeviation(true);
        Mean mean_stat = new Mean();
        for(Iterator<Map.Entry<String, ArrayList<Double>>> it = grouped_y.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, ArrayList<Double>> entry = it.next();
            double[] cur_y_arr = ArrayUtils.toPrimitive(entry.getValue().toArray(new Double[entry.getValue().size()]));
            double  std = std_stat.evaluate(cur_y_arr);
            double mean = mean_stat.evaluate(cur_y_arr);
            group_to_cv_map.put(entry.getKey(), std/mean);
        }

        this.cv = group_to_cv_map;
        return group_to_cv_map;
    }

}
