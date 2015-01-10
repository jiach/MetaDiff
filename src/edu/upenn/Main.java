/**
 * Created by cheng on 1/7/15.
 */
package edu.upenn;

import org.apache.commons.cli.*;

import java.io.*;

public class Main {

    public static void main(String[] args){

        Options options = new Options();
        options.addOption("v", "verbose", false, "print detailed output for development purposes.");
        Option opt_input_file_list = OptionBuilder.withArgName( "input_file_list" ).hasArg().isRequired().withDescription("specify the list of input files and covariates. Refer to README for format details.").create("input_file_list");
        Option opt_output_dir = OptionBuilder.withArgName( "output_dir" ).hasArg().isRequired().withDescription("specify the location where temporary files and final results are stores.").create("output_dir");
        Option opt_method = OptionBuilder.withArgName( "method" ).hasArg().isRequired().withDescription("specify the method with which the input files are generated. 0-Cufflinks, 1-MMSEQ. ").create("method");
        Option opt_fpkm_threshold = OptionBuilder.withArgName( "mean_fpkm_threshold" ).hasArg().isRequired().withDescription("specify the lowest mean fpkm for the isoform to be considered and analyzed").create("mean_fpkm_threshold");
        Option opt_num_cores = OptionBuilder.withArgName("num_cores").hasArg().withDescription("specify the number of cores used to run the R script.").create("num_cores");
        options.addOption(opt_input_file_list);
        options.addOption(opt_output_dir);
        options.addOption(opt_method);
        options.addOption(opt_fpkm_threshold);
        options.addOption(opt_num_cores);

        CommandLine line = null;
        Boolean r_parallel = false;
        int num_cores = 0;

        try {
            // parse the command line arguments

            CommandLineParser parser = new GnuParser();
            line = parser.parse(options, args );
        } catch( ParseException exp ) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "java -jar MetaDiffJ.jar", options );
            System.exit(0);
        }
        String input_list_fn = line.getOptionValue("input_file_list");
        String output_dir = line.getOptionValue("output_dir");
        String method = line.getOptionValue("method");
        String min_fpkm_mean = line.getOptionValue("mean_fpkm_threshold");
        if (line.hasOption("num_cores")){
            num_cores = Integer.parseInt(line.getOptionValue("num_cores"));
            if (num_cores>1){
                r_parallel=true;
            }
        }

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
        rscript_builder.write_to_R_script(r_parallel, fpkm_list.get_arr_cov_string(),r_script_fn);

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

        String r_script_cmd = "Rscript "+r_script_fn+" "+output_dir+"/fpkm.mat";
        if (r_parallel){
            r_script_cmd = r_script_cmd+" "+Integer.toString(num_cores);
        }
        System.out.println("Running R script for metatest: \n"+r_script_cmd);
        System.out.println("Please be patient.");
        try {
            Process child = Runtime.getRuntime().exec(r_script_cmd);
            child.waitFor();

            BufferedReader rscript_out = new BufferedReader(new
                    InputStreamReader(child.getInputStream()));

            BufferedReader rscript_log = new BufferedReader(new
                    InputStreamReader(child.getErrorStream()));

            BufferedWriter rscript_out_writer = new BufferedWriter(new FileWriter(output_dir+"/metadiff_results.tsv"));

            String s = null;

            System.out.println("Writing results to file: "+output_dir+"/metadiff_results.tsv");
            while ((s = rscript_out.readLine()) != null) {

                rscript_out_writer.write(s);
                rscript_out_writer.newLine();
            }

            rscript_out_writer.flush();
            rscript_out_writer.close();
            BufferedWriter rscript_log_writer = new BufferedWriter(new FileWriter(output_dir+"/metadiff_rscript.log"));

            System.out.println("Writing log to file: "+output_dir+"/metadiff_rscript.log");
            while ((s = rscript_log.readLine()) != null) {

                rscript_log_writer.write(s);
                rscript_log_writer.newLine();
            }

            rscript_log_writer.flush();
            rscript_log_writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}