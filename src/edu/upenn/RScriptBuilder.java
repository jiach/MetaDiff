package edu.upenn;

import org.apache.commons.lang3.StringUtils;

import java.io.*;

/**
 * Created by cheng on 1/9/15.
 */
public class RScriptBuilder {

    private String r_script_pt1 = "library(metatest)\n" +
            "library(plyr)\n" +
            "library(doParallel)\n" +
            "\n" +
            "args <- commandArgs(trailingOnly = TRUE)\n" +
            "file_in <- args[1]\n" +
            "nodes <- as.numeric(args[2])\n" +
            "data_mat <- read.table(file_in, header=T, sep = '\\t')\n" +
            "\n" +
            "run_metatest<-function(mat){\n" +
            "  tryCatch({  meta_obj <- metatest(formula = y~";

    private String r_script_pt2 = ", variance = variance, data=mat)\n" +
            "              res_vec <- c(meta_obj$convergence, meta_obj$tval, meta_obj$dfttest, meta_obj$pttest, meta_obj$bartLLR, meta_obj$pBartlett)\n" +
            "              cov_name <- dimnames(meta_obj$coefficients)[[1]]\n" +
            "              names_vec <- c('Convergence', paste('tval_',cov_name,sep=''), 'dfttest', paste('pttest_',cov_name,sep=''), paste('bartLLR_',cov_name,sep=''), paste('pBartlett_',cov_name,sep=''))\n" +
            "              names(res_vec)<-names_vec\n" +
            "              return(res_vec)}, error = function(e) {\n" +
            "                warning(paste('Metatest failed at Feature: ', mat[1,1],sep=''))\n" +
            "                return(NULL)})}" +
            "\n" +
            "cl <- makeCluster(nodes)\n" +
            "registerDoParallel(cl)\n" +
            "df_res <- ddply(data_mat, .(Feature), .fun = run_metatest, .parallel = T, .paropts = list(.packages='metatest'))\n" +
            "stopCluster(cl)\n" +
            "\n" +
            "write.table(df_res, file = \"\", row.names = F, quote=F, sep='\\t')\n";

    private String r_script_sing_pt1 = "library(metatest)\n" +
            "library(plyr)\n" +
            "\n" +
            "args <- commandArgs(trailingOnly = TRUE)\n" +
            "file_in <- args[1]\n" +
            "\n" +
            "data_mat <- read.table(file_in, header=T, sep = '\\t')\n" +
            "\n" +
            "run_metatest<-function(mat){\n" +
            "  tryCatch({  meta_obj <- metatest(formula = y~";

    private String r_script_sing_pt2 = ", variance = variance, data=mat)\n" +
            "              res_vec <- c(meta_obj$convergence, meta_obj$tval, meta_obj$dfttest, meta_obj$pttest, meta_obj$bartLLR, meta_obj$pBartlett)\n" +
            "              cov_name <- dimnames(meta_obj$coefficients)[[1]]\n" +
            "              names_vec <- c('Convergence', paste('tval_',cov_name,sep=''), 'dfttest', paste('pttest_',cov_name,sep=''), paste('bartLLR_',cov_name,sep=''), paste('pBartlett_',cov_name,sep=''))\n" +
            "              names(res_vec)<-names_vec\n" +
            "              return(res_vec)}, error = function(e) {\n" +
            "                warning(paste('Metatest failed at Feature: ', mat[1,1],sep=''))\n" +
            "                return(NULL)})}" +
            "\n" +
            "df_res <- ddply(data_mat, .(Feature), .fun = run_metatest)\n" +
            "write.table(df_res, file = \"\", row.names = F, quote=F, sep='\\t')";

    public RScriptBuilder(){
    }

    public String get_R_script(Boolean parallel, String[] list_of_cov_names){
        String[] el_formula = new String[list_of_cov_names.length];

        for (int i = 0; i < list_of_cov_names.length; i++) {
            if (list_of_cov_names[i].startsWith("C_")) {
                el_formula[i] = "factor("+list_of_cov_names[i]+")";
            } else {
                el_formula[i] = list_of_cov_names[i];
            }
        }

        if (parallel){
            return(r_script_pt1+StringUtils.join(el_formula,"+")+r_script_pt2);
        } else{
            return(r_script_sing_pt1+StringUtils.join(el_formula,"+")+r_script_sing_pt2);
        }

    }

    public void write_to_R_script(Boolean parallel, String[] list_of_cov_names, String r_script_location){

        BufferedWriter r_script_fout;
        try {
            r_script_fout = new BufferedWriter(new FileWriter(r_script_location));
            r_script_fout.write(this.get_R_script(parallel, list_of_cov_names));
            r_script_fout.flush();
            r_script_fout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
