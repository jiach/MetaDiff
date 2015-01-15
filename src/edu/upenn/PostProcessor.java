package edu.upenn;

import java.io.*;
import java.util.List;

/**
 * Created by cheng on 1/14/15.
 */

//TODO: need to finish post processor with the following functions: 1. status_ok check 2. fdr output
public class PostProcessor {
    String[] metatest_out_str_arr;
    BufferedWriter rscript_out;
    Boolean has_group_var;

    public PostProcessor(List<String> metatest_output, String output_fn, Boolean has_group_var){
        this.metatest_out_str_arr = metatest_output.toArray(new String[metatest_output.size()]);
        this.has_group_var = has_group_var;
        try {
            this.rscript_out = new BufferedWriter(new FileWriter(output_fn));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
