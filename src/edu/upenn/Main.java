/**
 * Created by cheng on 1/7/15.
 */
package edu.upenn;

import org.apache.commons.cli.*;

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

        try {
            // parse the command line arguments

            CommandLineParser parser = new GnuParser();
            CommandLine line = parser.parse(options, args );

            String input_list_fn = line.getOptionValue("input_file_list");
            String output_dir = line.getOptionValue("output_dir");
            String method = line.getOptionValue("method");
            String min_fpkm_mean = line.getOptionValue("mean_fpkm_threshold");

            System.out.println(input_list_fn);
            System.out.println(output_dir);
            System.out.println(method);
            System.out.println(min_fpkm_mean);

        }
        catch( ParseException exp ) {
            System.out.println( "Unexpected exception:" + exp.getMessage() );
        }



        CufflinksParser cuff_parser = new CufflinksParser();
        cuff_parser.printme();
    }

}
