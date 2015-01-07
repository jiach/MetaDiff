package edu.upenn;


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
        this.dict_sample_to_fn.put(line_tokens[0],line_tokens[2:]);

    }
}
