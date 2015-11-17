package edu.upenn;

import cern.colt.list.DoubleArrayList;
import org.apache.commons.lang3.StringUtils;
import ubic.basecode.math.MultipleTestCorrection;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cheng on 1/14/15.
 */

//TODO: need to finish post processor with the following functions: fdr output
public class PostProcessor {
    private String[] metatest_out_str_arr;
    private BufferedWriter rscript_out;
    private Boolean has_group_var;
    private Map<String, Boolean> ok_status;
    private Map<String, double[]> ok_fdr;
    private List<Integer> pval_column_idx;
    public PostProcessor(String[] metatest_output, String output_fn, Boolean has_group_var){
        this.metatest_out_str_arr = metatest_output;
        this.has_group_var = has_group_var;
        try {
            this.rscript_out = new BufferedWriter(new FileWriter(output_fn));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write_to_results(CufflinksParser parser, Logger logger){

//        System.out.println(StringUtils.join(this.metatest_out_str_arr,"\n"));

        this.generate_fdr_columns(parser);
        if (this.ok_status == null || this.ok_fdr == null){
            logger.log_message("Error generate fdr columns. The output from Rscript may not be formatted correctly. Check error messages in metadiff_r.log under the result directory.");
            return;
        }

        try {
            String[] extra_columns_header = new String[this.pval_column_idx.size()+1];
            String[] header_tokens = this.metatest_out_str_arr[0].split("\t");
            for (int i = 0; i < extra_columns_header.length-1; i++) {
                extra_columns_header[i] = header_tokens[this.pval_column_idx.get(i)].replaceFirst("p", "fdr_");
            }
            extra_columns_header[extra_columns_header.length-1] = "Status";

            String combined_line = this.metatest_out_str_arr[0]+"\t"+ StringUtils.join(extra_columns_header,"\t");

            rscript_out.write(combined_line);
            rscript_out.newLine();

            String na_line = StringUtils.repeat("\tNA",this.pval_column_idx.size());

            for (int i = 1; i < this.metatest_out_str_arr.length; i++) {
                String[] tokens = this.metatest_out_str_arr[i].split("\t");
                if (this.ok_status.get(tokens[0])) {
                    rscript_out.write(this.metatest_out_str_arr[i]+"\t"+this.join_double_arr(this.ok_fdr.get(tokens[0]))+"OK");
                    rscript_out.newLine();
                } else {
                    rscript_out.write(this.metatest_out_str_arr[i]+na_line+"\tFailed");
                    rscript_out.newLine();
                }
            }

            this.rscript_out.flush();
            this.rscript_out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void generate_fdr_columns(CufflinksParser parser){
        if (this.metatest_out_str_arr.length<1){
            this.ok_fdr = null;
            this.ok_status = null;
            return;
        }
        String header_line = this.metatest_out_str_arr[0];
        String[] header_tokens = header_line.split("\t");
        this.pval_column_idx = new ArrayList<Integer>();

        // discover columns that contain p-values to be adjusted
        for (int i = 0; i < header_tokens.length; i++) {
            if (header_tokens[i].startsWith("pttest_") || header_tokens[i].startsWith("pBartlett_")){
                this.pval_column_idx.add(i);
            }
        }

        if (this.pval_column_idx.isEmpty()){
            this.ok_fdr = null;
            this.ok_status = null;
            return;
        }

        // add p-values to a <feature_id, p_val> map, for each column, add one such map to the list;
        List<Map<String, Double>> list_raw_pval = new ArrayList<Map<String, Double>>();
        for (int i = 0; i < this.pval_column_idx.size(); i++) {
            list_raw_pval.add(new HashMap<String, Double>());
        }

        // contains whether the trascript is ok.
        this.ok_status = new HashMap<String, Boolean>();

        for (int i = 1; i < this.metatest_out_str_arr.length; i++) {
            String[] tokens = metatest_out_str_arr[i].split("\t");
            boolean this_ok;
            if (this.has_group_var) {
                if (parser.get_ok_status(tokens[0]) && tokens[1].equals("0")) {
                    this_ok = true;

                } else {
                    this_ok = false;
                }
            } else {
                if (tokens[1].equals("0")) {
                    this_ok = true;
                } else {
                    this_ok = false;
                }
            }

            this.ok_status.put(tokens[0], this_ok);

            if (this_ok) {
                for (int j = 0; j < this.pval_column_idx.size(); j++) {
                    list_raw_pval.get(j).put(tokens[0], Double.parseDouble(tokens[this.pval_column_idx.get(j)]));
                }
            }
        }

        // contains all the feature_ids that have ok status, use this order to get_fdr
        String[] ok_feature_id = list_raw_pval.get(0).keySet().toArray(new String[list_raw_pval.get(0).keySet().size()]);

        // contains all the fdr for all the ok_feature_id.
        this.ok_fdr = new HashMap<String, double[]>();
        for (int i = 0; i < ok_feature_id.length; i++) {
            this.ok_fdr.put(ok_feature_id[i], new double[list_raw_pval.size()]);
        }

        for (int i = 0; i < list_raw_pval.size(); i++) {
            double[] raw_pval = new double[ok_feature_id.length];

            for (int j = 0; j < ok_feature_id.length; j++) {
                raw_pval[j] = list_raw_pval.get(i).get(ok_feature_id[j]);
            }

            double[] fdr = this.get_fdr(raw_pval); //contains fdr in the order of the ok_feature_id

//            this.print_void_dbl_arr(fdr);

            for (int j = 0; j < ok_feature_id.length; j++) {
                this.ok_fdr.get(ok_feature_id[j])[i] = fdr[j];
            }
        }
    }

    private String join_double_arr(double[] arr){
        String out = "";
        for (int i = 0; i < arr.length; i++) {
            out = out+Double.toString(arr[i])+"\t";
        }
        return out;
    }

    private void print_void_dbl_arr(double[] arr){
        String out = "";
        for (int i = 0; i < arr.length; i++) {
            out = out+Double.toString(arr[i])+"\t";
        }
        System.out.println(out);
    }

    public double[] get_fdr(double [] pval){
        DoubleArrayList fdr = MultipleTestCorrection.benjaminiHochberg(new DoubleArrayList(pval));
        double[] fdr_arr = new double[fdr.size()];

        for (int i = 0; i < fdr.size(); i++) {
            fdr_arr[i] = fdr.getQuick(i);
        }
        return fdr_arr;
    }

}
