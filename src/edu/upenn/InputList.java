package edu.upenn;


import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
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

    public void setHeader(String headerline){
        String[] line_tokens = headerline.split("\t");
        this.list_header=line_tokens;
    }

    public void appendSample(String bodyline){
        String[] line_tokens = bodyline.split("\t");
        this.dict_sample_to_fn.put(line_tokens[0],line_tokens[1]);
        this.dict_sample_to_covariates.put(line_tokens[0],ArrayUtils.subarray(line_tokens,2,line_tokens.length+1));
    }

    public String[] getCovariates(String sample_id){
        return(dict_sample_to_covariates.get(sample_id));
    }


    public String[] getSampleIDs(){
        return(dict_sample_to_fn.keySet().toArray(new String[dict_sample_to_fn.size()]));
    }

    public String[] getListFilename(String[] sample_ids){
        List<String> arr_fn= new ArrayList<String>();
        for (int i = 0; i < sample_ids.length; i++) {
            arr_fn.add(dict_sample_to_fn.get(sample_ids[i]));
        }
        return(arr_fn.toArray(new String[arr_fn.size()]));
    }
//
//    public void write_tmp_file(String output_dir){
//
//        String cov_fn = output_dir+"/cov.mat";
//
//        try {
//            PrintWriter cov_fout = new PrintWriter(new File(cov_fn));
//            cov_fout.print(StringUtils.join(list_header,"\t")+"\n");
//
//            cov_fout.print(this.get_mat_output_string());
//            cov_fout.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//    }
}
