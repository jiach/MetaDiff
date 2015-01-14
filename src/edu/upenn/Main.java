/**
 * Created by cheng on 1/7/15.
 */
package edu.upenn;

import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import ubic.basecode.math.MultipleTestCorrection;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args){


        //parsing commandline arguments
        Options options = new Options();
        options.addOption("v", "verbose", false, "print detailed output for development purposes.");
        Option opt_input_file_list = OptionBuilder.withArgName( "input_file_list" ).hasArg().isRequired().withDescription("specify the list of input files and covariates. Refer to README for format details.").create("input_file_list");
        Option opt_output_dir = OptionBuilder.withArgName( "output_dir" ).hasArg().isRequired().withDescription("specify the location where temporary files and final results are stores.").create("output_dir");
        Option opt_method = OptionBuilder.withArgName( "method" ).hasArg().isRequired().withDescription("specify the method with which the input files are generated, method = \"cufflinks\" or \"mmseq\". ").create("method");
        Option opt_fpkm_threshold = OptionBuilder.withArgName("mean_fpkm_threshold").hasArg().withDescription("specify the lowest mean FPKM for the isoform to be considered and analyzed").create("mean_fpkm_threshold");
        Option opt_cv_threshold = OptionBuilder.withArgName( "cv_threshold" ).hasArg().withDescription("specify the highest coefficient of variation of the FPKM for the isoform to pass STATUS check").create("cv_threshold");
        Option opt_num_cores = OptionBuilder.withArgName("num_cores").hasArg().withDescription("specify the number of cores used to run the R script.").create("num_cores");
        options.addOption(opt_input_file_list);
        options.addOption(opt_output_dir);
        options.addOption(opt_method);
        options.addOption(opt_fpkm_threshold);
        options.addOption(opt_cv_threshold);
        options.addOption(opt_num_cores);


        CommandLine line = null;
        Boolean r_parallel = false;
        int num_cores = 0;

        try {
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

        String min_fpkm_mean = "1";
        if (line.hasOption("mean_fpkm_threshold")) {
            min_fpkm_mean = line.getOptionValue("mean_fpkm_threshold");
        }

        String max_cv = "1";
        if (line.hasOption("cv_threshold")){
            max_cv = line.getOptionValue("cv_threshold");
        }

        Boolean verbose = false;

        if (line.hasOption("num_cores")){
            num_cores = Integer.parseInt(line.getOptionValue("num_cores"));
            if (num_cores>1){
                r_parallel=true;
            }
        }

        if (line.hasOption("v")){
            verbose=true;
        }


        //initialize buffered writer for log file
        Path output_path = Paths.get(output_dir);
        String log_file = output_path.resolve("metadiff.log").toString();
        Logger metadiff_log = new Logger(verbose,log_file);
        metadiff_log.log_message("Starting logging to file: " + output_dir + "/metadiff.log");

        //read in the list of inputs.
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

        //read in all the output from files given in the input list.
        CufflinksParser fpkm_parser = null;

        if (method.equals("cufflinks")){
            fpkm_parser = new CufflinksParser(sorted_filename, metadiff_log);
        }else if (method.equals("mmseq")) {
            fpkm_parser = new MmseqParser(sorted_filename, metadiff_log);
        }

        metadiff_log.log_message("Filtering isoforms according to mean_fpkm_threshold = " + min_fpkm_mean);

        fpkm_parser.trim_isoforms(Double.parseDouble(min_fpkm_mean),fpkm_list.get_num_sample());

        metadiff_log.log_message(Long.toString(fpkm_parser.get_num_isoforms()) + " isoforms remaining after filtering");

        if (fpkm_list.has_group_var){
            metadiff_log.log_message("Filtering isoforms according to cv_threshold = 1");
            fpkm_parser.trim_isoform(Double.parseDouble(max_cv),fpkm_list.get_group_var(sorted_sample_id));
            metadiff_log.log_message(Long.toString(fpkm_parser.get_num_isoforms())+" isoforms remaining after filtering");
        }else{
            metadiff_log.log_message("Group variable \"C_group\" not provided, skipping filtering with CV");
        }

        fpkm_parser.write_tmp_file(output_path, fpkm_list.get_cov_mat(sorted_sample_id), fpkm_list.get_cov_header_string());

        RScriptBuilder rscript_builder = new RScriptBuilder();
        String r_script_fn = output_path.resolve("run_metatest.R").toString();
        rscript_builder.write_to_R_script(r_parallel, fpkm_list.get_arr_cov_string(),r_script_fn);


        ProcessBuilder run_rscript = new ProcessBuilder("Rscript", r_script_fn, output_path.resolve("fpkm.mat").toString(),Integer.toString(num_cores)).redirectError(ProcessBuilder.Redirect.to(new File(output_path.resolve("metadiff_r.log").toString())));


        metadiff_log.log_message("Running R script for metatest: \n" + StringUtils.join(run_rscript.command(), " "));

        try {

            Process child = run_rscript.start();
            child.waitFor();

            BufferedReader rscript_out = new BufferedReader(new InputStreamReader(child.getInputStream()));


            BufferedWriter rscript_out_writer = new BufferedWriter(new FileWriter(output_dir+"/metadiff_results.tsv"));

            String s = rscript_out.readLine();
            rscript_out_writer.write(s);
            if (fpkm_list.has_group_var){
                rscript_out_writer.write("\tStatus");
            }
            rscript_out_writer.newLine();

            metadiff_log.log_message("Writing results to file: " + output_dir + "/metadiff_results.tsv");

            if (fpkm_list.has_group_var) {
                while ((s = rscript_out.readLine()) != null) {

                    rscript_out_writer.write(s);
                    String[] str_tokens=s.split("\t");
                    if (fpkm_parser.get_ok_status(str_tokens[0]) & str_tokens[1].equals("0")) {
                        rscript_out_writer.write("\tOK");
                    }else{
                        rscript_out_writer.write("\tFailed");
                    }
                    rscript_out_writer.newLine();
                }
            }else{
                while ((s = rscript_out.readLine()) != null) {

                    rscript_out_writer.write(s);
                    rscript_out_writer.newLine();
                }
            }

            rscript_out_writer.flush();
            rscript_out_writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        metadiff_log.end_logging();
    }

}