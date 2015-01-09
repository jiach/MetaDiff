/**
 * Created by cheng on 1/7/15.
 */
package edu.upenn;

import org.apache.commons.cli.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Main {

    public static void main(String[] args){

        Options options = new Options();
        options.addOption("v", "verbose", false, "print detailed output for development purposes.");
        Option opt_input_file_list = OptionBuilder.withArgName( "input_file_list" ).hasArg().isRequired().withDescription("specify the list of input files and covariates. Refer to README for format details.").create("input_file_list");
        Option opt_output_dir = OptionBuilder.withArgName( "output_dir" ).hasArg().isRequired().withDescription("specify the location where temporary files and final results are stores.").create("output_dir");
        Option opt_method = OptionBuilder.withArgName( "method" ).hasArg().isRequired().withDescription("specify the method with which the input files are generated. 0-Cufflinks, 1-MMSEQ. ").create("method");
        Option opt_fpkm_threshold = OptionBuilder.withArgName( "mean_fpkm_threshold" ).hasArg().isRequired().withDescription("specify the lowest mean fpkm for the isoform to be considered and analyzed").create("mean_fpkm_threshold");

        options.addOption(opt_input_file_list);
        options.addOption(opt_output_dir);
        options.addOption(opt_method);
        options.addOption(opt_fpkm_threshold);

        CommandLine line = null;
        try {
            // parse the command line arguments

            CommandLineParser parser = new GnuParser();
            line = parser.parse(options, args );
        } catch( ParseException exp ) {
            System.out.println( "Unexpected exception:" + exp.getMessage() );
        }
        String input_list_fn = line.getOptionValue("input_file_list");
        String output_dir = line.getOptionValue("output_dir");
        String method = line.getOptionValue("method");
        String min_fpkm_mean = line.getOptionValue("mean_fpkm_threshold");

//        System.out.println(input_list_fn);
//        System.out.println(output_dir);
//        System.out.println(method);
//        System.out.println(min_fpkm_mean);

        InputList fpkm_list = new InputList();

        try {
            BufferedReader in = new BufferedReader(new FileReader(input_list_fn));
            String curLine = in.readLine();
            fpkm_list.set_header(curLine);

            while((curLine=in.readLine())!=null){
                fpkm_list.append_sample(curLine);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] sorted_sample_id = fpkm_list.get_sample_ids();
        String[] sorted_filename = fpkm_list.get_list_fn(sorted_sample_id);

        RScriptBuilder rscript_builder = new RScriptBuilder();
        String r_script_fn = output_dir+"/run_metatest.R";
        rscript_builder.write_to_R_script(fpkm_list.get_arr_cov_string(),r_script_fn);

        CufflinksParser fpkm_parser = null;

        if (method.equals("0")){
            fpkm_parser = new CufflinksParser(sorted_filename);
        }else if (method.equals("1")) {
            fpkm_parser = new MmseqParser(sorted_filename);
        }

        System.out.println("Trimming Isoforms according to mean_fpkm_threshold = "+min_fpkm_mean);
        fpkm_parser.trim_isoforms(Double.parseDouble(min_fpkm_mean),fpkm_list.get_num_sample());
        System.out.println(Long.toString(fpkm_parser.get_num_isoforms())+" isoforms remaining after trimming");
        fpkm_parser.write_tmp_file(output_dir, fpkm_list.get_cov_mat(sorted_sample_id), fpkm_list.get_cov_header_string());

        String r_script_cmd = "Rscript "+r_script_fn+" "+output_dir+"/fpkm.mat"+" "+output_dir+"/metadiff_results.tsv";
        System.out.println("Running R script for metatest: \n"+r_script_cmd);
        System.out.println("Please be patient.");
        try {
            Process child = Runtime.getRuntime().exec(r_script_cmd);
            child.waitFor();
            if (child.exitValue()==0){
                System.out.println("Rscript exited without errors.");
            } else {
                System.out.println("Rscript exited with errors.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
