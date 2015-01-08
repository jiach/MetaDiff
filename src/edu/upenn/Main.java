/**
 * Created by cheng on 1/7/15.
 */
package edu.upenn;

import org.apache.commons.cli.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

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
            fpkm_list.setHeader(curLine);

            while((curLine=in.readLine())!=null){
                fpkm_list.appendSample(curLine);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] sorted_sample_id = fpkm_list.getSampleIDs();
        String[] sorted_filename = fpkm_list.getListFilename(sorted_sample_id);


        CufflinksParser cuff_parser = new CufflinksParser(sorted_filename);
        System.out.println("Trimming Isoforms according to mean_fpkm_threshold = "+min_fpkm_mean);
        cuff_parser.trim_isoforms(Double.parseDouble(min_fpkm_mean));
        System.out.println(Long.toString(cuff_parser.get_num_isoforms())+" isoforms remaining after trimming");
        cuff_parser.write_tmp_file(output_dir);
        //        CufflinksParser cuff_parser = new CufflinksParser();
        //        cuff_parser.printme();
    }

}

