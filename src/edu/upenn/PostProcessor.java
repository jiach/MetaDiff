package edu.upenn;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by cheng on 1/14/15.
 */

//TODO: need to finish post processor with the following functions: 1. status_ok check 2. fdr output
public class PostProcessor {
    private String[] metatest_out_str_arr;
    private BufferedWriter rscript_out;
    private Boolean has_group_var;

    public PostProcessor(String[] metatest_output, String output_fn, Boolean has_group_var){
        this.metatest_out_str_arr = metatest_output;
        this.has_group_var = has_group_var;
        try {
            this.rscript_out = new BufferedWriter(new FileWriter(output_fn));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write_to_results(CufflinksParser parser){

        try {
//            int[]  col_idx = this.get_col_idx_of_pvals(this.metatest_out_str_arr[0]);
//
//            String[] isoform_names = new String[this.metatest_out_str_arr.length-1];
//            double[][] p_vals = new double[col_idx.length][];
//            String[] p_val_names = new String[col_idx.length];
//
//            for (int i = 0; i < col_idx.length; i++) {
//                p_val_names[i] = this.metatest_out_str_arr[0].split("\t")[col_idx[i]];
//            }
//
//            for (int i = 1; i < this.metatest_out_str_arr.length; i++) {
//                String[] line_tokens = this.metatest_out_str_arr[i].split("\t");
//                isoform_names[i] = line_tokens[0];
//                for (int j = 0; j < col_idx.length; j++) {
//                    p_vals[j][i-1] = Double.parseDouble(line_tokens[j]);
//                }
//            }
//
////            try {
////                Map<String, Map<String, Double>> pval_isoform_fdr = FDR.get_fdr(isoform_names, p_val_names,p_vals);
////            } catch (UnequalLengthException e) {
////                e.printStackTrace();
////            }

            if(this.has_group_var){
                this.rscript_out.write(this.metatest_out_str_arr[0]+"\tStatus");
                this.rscript_out.newLine();
                for (int i = 1; i < this.metatest_out_str_arr.length; i++) {
                    String[] line_tokens = this.metatest_out_str_arr[i].split("\t");

                    if( (parser.get_ok_status(line_tokens[0])) & line_tokens[1].equals("0")){
                        this.rscript_out.write(this.metatest_out_str_arr[i]+"\tOK");
                        this.rscript_out.newLine();
                    }else{
                        this.rscript_out.write(this.metatest_out_str_arr[i]+"\tFailed");
                        this.rscript_out.newLine();
                    }
                }
            }else{
                for(String aLine:this.metatest_out_str_arr){
                    this.rscript_out.write(aLine);
                    this.rscript_out.newLine();
                }
            }
            this.rscript_out.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close(){
        try {
            this.rscript_out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public int[] get_col_idx_of_pvals(String header){
//        String[] header_tokens = header.split("\t");
//        List<Integer> col_idx_list = new ArrayList<Integer>();
//        for (int i = 0; i < header_tokens.length; i++) {
//            if(header_tokens[i].startsWith("pttest_") || (header_tokens[i].startsWith("pBartlett_"))) col_idx_list.add(i);
//        }
//        return ArrayUtils.toPrimitive(col_idx_list.toArray(new Integer[col_idx_list.size()]));
//    }
//
//    public String get_fdr_str(Map<String, Double> fdr, String[] p_val_names) throws UnequalLengthException {
//        String[] fdr_str_arr = new String[fdr.values().size()];
//        if (fdr_str_arr.length != p_val_names.length) throw new UnequalLengthException("p_val_names length: " + Integer.toString(p_val_names.length) + "\t fdr length: "+Integer.toString(fdr.values().size()));
//        for (int i = 0; i < fdr_str_arr.length; i++) {
//            fdr_str_arr[i] = fdr.get(p_val_names[i]);
//        }
//    }
}
