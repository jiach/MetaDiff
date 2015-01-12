package edu.upenn;


import com.sun.deploy.util.ArrayUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by cheng on 1/7/15.
 */
public class InputList {
    String[] list_header;
    Map<String, String> dict_sample_to_fn = new TreeMap<String, String>();
    Map<String, String[]> dict_sample_to_covariates = new TreeMap<String, String[]>();

    public void set_header(String headerline){
        String[] line_tokens = headerline.toUpperCase().split("\t");
        this.list_header=line_tokens;
    }

    public void append_sample(String bodyline){
        String[] line_tokens = bodyline.split("\t");
        this.dict_sample_to_fn.put(line_tokens[0],line_tokens[1]);
        this.dict_sample_to_covariates.put(line_tokens[0],ArrayUtils.subarray(line_tokens,2,line_tokens.length+1));
    }

    public String[] get_covariate(String sample_id){
        return(dict_sample_to_covariates.get(sample_id));
    }


    public String[] get_sample_ids(){
        return(dict_sample_to_fn.keySet().toArray(new String[dict_sample_to_fn.size()]));
    }

    public String[] get_list_fn(String[] sample_ids){
        List<String> arr_fn= new ArrayList<String>();
        for (int i = 0; i < sample_ids.length; i++) {
            arr_fn.add(dict_sample_to_fn.get(sample_ids[i]));
        }
        return(arr_fn.toArray(new String[arr_fn.size()]));
    }

    public String[][] get_cov_mat(String[] sample_id) {
        List<String[]> cov_mat = new ArrayList<String[]>();
        for (int i = 0; i < sample_id.length; i++) {
            cov_mat.add(this.get_covariate(sample_id[i]));
        }
        return (cov_mat.toArray(new String[cov_mat.size()][]));
    }

    public String get_cov_header_string(){
        return(StringUtils.join(ArrayUtils.subarray(this.list_header,2,this.list_header.length+1),"\t"));
    }

    public String[] get_arr_cov_string(){
        return(ArrayUtils.subarray(this.list_header,2,this.list_header.length+1));
    }

    public int get_num_sample(){
        return(this.dict_sample_to_fn.size());
    }

//TODO:    public String[] get_group_cov(String[] sample_id){}
}
