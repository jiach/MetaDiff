package edu.upenn;


import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Created by cheng on 1/7/15.
 */
public class InputList {
    String[] list_header;
    Map<String, String> dict_sample_to_fn = new TreeMap<String, String>();
    Map<String, String[]> dict_sample_to_covariates = new TreeMap<String, String[]>();
    Boolean has_group_var = false;
    int group_var_index = -1;
    

    public void set_header(String headerline){
        String[] line_tokens = headerline.toUpperCase().split("\t");
        this.list_header=line_tokens;
        if (Arrays.asList(line_tokens).contains("C_GROUP")){
            this.has_group_var=true;
            this.group_var_index = Arrays.asList(line_tokens).indexOf("C_GROUP")-2;
        }else{
            this.has_group_var=false;
        }
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
        for (String sample_id : sample_ids) {
            arr_fn.add(dict_sample_to_fn.get(sample_id));
        }
        return(arr_fn.toArray(new String[arr_fn.size()]));
    }

    public String[][] get_cov_mat(String[] sample_id) {
        List<String[]> cov_mat = new ArrayList<String[]>();
        for (String aSample_id : sample_id) {
            cov_mat.add(this.get_covariate(aSample_id));
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

    public String[] get_group_var(String[] sample_ids){
        String[] group_var = new String[this.get_num_sample()];
        if(!this.has_group_var){
            System.err.println("No group variable \"C_group\" is defined, now exiting!");
            System.exit(1);
        }else{
            for (int i = 0; i < this.get_num_sample(); i++) {
                group_var[i] = this.dict_sample_to_covariates.get(sample_ids[i])[this.group_var_index];
            }
        }
        return(group_var);
    }

}
