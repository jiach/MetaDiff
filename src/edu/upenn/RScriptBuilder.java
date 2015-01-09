package edu.upenn;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * Created by cheng on 1/9/15.
 */
public class RScriptBuilder {

    private String r_script_pt1 = "library(metatest)\n" +
            "library(plyr)\n" +
            "library(doParallel)\n" +
            "args <- commandArgs(trailingOnly = TRUE)\n" +
            "file_in <- args[1]\n" +
            "file_out <- args[2]\n" +
            "data_mat <- read.table(file_in, header=T, sep = '\\t')[1:640,]\n" +
            "run_metatest<-function(mat){\n" +
            "  meta_obj <- metatest(formula = y~";

    private String r_script_pt2 = ", variance = variance, data=mat)\n" +
            "  res_vec <- c(meta_obj$convergence, meta_obj$coefficients, meta_obj$se, meta_obj$tval, meta_obj$dfttest, meta_obj$pttest, meta_obj$LLR, meta_obj$pLLR, meta_obj$bartLLR, meta_obj$bartscale, meta_obj$pBartlett, meta_obj$ppermtest)\n" +
            "  cov_name <- dimnames(meta_obj$coefficients)[[1]]\n" +
            "  names_vec <- c('Convergence', paste('coef_',cov_name,sep=''), paste('se_',cov_name,sep=''), paste('tval_',cov_name,sep=''), 'dfttest', paste('pttest_',cov_name,sep=''),paste('LLR_',cov_name,sep=''), paste('pLLR_',cov_name,sep=''), paste('bartLLR_',cov_name,sep=''), paste('bartscale',cov_name,sep=''), paste('pBartlett_',cov_name,sep=''), paste('ppermtest_',cov_name,sep=''))\n" +
            "  names(res_vec)<-names_vec\n" +
            "  return(res_vec)\n" +
            "}\n" +
            "nodes <- detectCores()\n" +
            "fileConn<-file(paste(file_out, '.log', sep = ''))\n" +
            "writeLines(c(paste('Running Rscript with ',nodes,' cores.',sep='')), fileConn)\n" +
            "close(fileConn)\n" +
            "\n" +
            "cl <- makeCluster(nodes)\n" +
            "registerDoParallel(cl)\n" +
            "df_res <- ddply(data_mat, .(Isoform), .fun = run_metatest, .parallel = T, .paropts = list(.packages='metatest'))\n" +
            "stopCluster(cl)\n" +
            "write.table(df_res, file = file_out, row.names = F, quote=F)";

    public RScriptBuilder(){
    }

    public String get_R_script(String[] list_of_cov_names){
        String[] el_formula = new String[list_of_cov_names.length];

        for (int i = 0; i < list_of_cov_names.length; i++) {
            if (list_of_cov_names[i].startsWith("C_")) {
                el_formula[i] = "factor("+list_of_cov_names[i]+")";
            } else {
                el_formula[i] = list_of_cov_names[i];
            }
        }

        return(r_script_pt1+StringUtils.join(el_formula,"+")+r_script_pt2);
    }

    public void write_to_R_script(String[] list_of_cov_names, String r_script_location){

        PrintWriter r_script_fout;
        try {
            r_script_fout = new PrintWriter(new File(r_script_location));
            r_script_fout.print(this.get_R_script(list_of_cov_names));
            r_script_fout.flush();
            r_script_fout.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
