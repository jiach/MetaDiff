/**
 * Created by cheng on 1/7/15.
 */

package edu.upenn;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by cheng on 1/7/15.
 */
public class CufflinksParser {

    Map<String, ArrFPKM> dict_arr_fpkm = new TreeMap<String, ArrFPKM>();
    String[] uniq_group = null;

    public CufflinksParser() {
    }

    public CufflinksParser(String[] fn, Logger log){
        this.read_in_file(fn, log);
    }

    public void read_in_file(String[] fn, Logger log){
        try {
            for (int i = 0; i < fn.length; i++) {
                log.log_message("processing file: "+fn[i]);
                BufferedReader in = new BufferedReader(new FileReader(fn[i]));
                String line;
                String[] line_tokens;
                in.readLine();
                while ((line=in.readLine())!=null){
                    line_tokens = line.split("\t");
                    double y = Double.parseDouble(line_tokens[9]);
                    double sd = Double.parseDouble(line_tokens[11]) - Double.parseDouble(line_tokens[9]);
                    if (this.dict_arr_fpkm.containsKey(line_tokens[0])) {
                        this.dict_arr_fpkm.get(line_tokens[0]).append(y, sd, false);
                    }else{
                        this.dict_arr_fpkm.put(line_tokens[0],new ArrFPKM());
                        this.dict_arr_fpkm.get(line_tokens[0]).append(y, sd, false);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }


    public void write_tmp_file(Path output_dir, String[][] cov_mat, String header){

        String mat_fn = output_dir.resolve("fpkm.mat").toString();

        try {
            BufferedWriter mat_fout = new BufferedWriter(new FileWriter(mat_fn));
            mat_fout.write("Feature\ty\tvariance\t"+header+"\n");
            mat_fout.write(this.get_mat_output_string(cov_mat));
            mat_fout.flush();
            mat_fout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void trim_isoforms(Double min_fpkm_mean, int sample_num){

        for(Iterator<Map.Entry<String, ArrFPKM>> it = this.dict_arr_fpkm.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, ArrFPKM> entry = it.next();
            if ((entry.getValue().get_mean_fpkm()<min_fpkm_mean) || (entry.getValue().get_sample_num()!=sample_num)) {
                it.remove();
            }
        }
    }

    public void trim_isoform(Double max_cv, String[] group_var){
        Set<String> unique_group_var_set = new HashSet<String>(Arrays.asList(group_var));
        String[] unique_group_var = unique_group_var_set.toArray(new String[unique_group_var_set.size()]);
        this.uniq_group = unique_group_var;

        for(Iterator<Map.Entry<String, ArrFPKM>> it = this.dict_arr_fpkm.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, ArrFPKM> entry = it.next();
            Map<String, Double> cur_cv = entry.getValue().get_cv(group_var, unique_group_var);

            Boolean remove_isoform = false;
            for (Double aCV_val : cur_cv.values()){
                if(aCV_val.isInfinite() || aCV_val.isNaN() || (aCV_val>1)){
                    remove_isoform = true;
                }else{
                    entry.getValue().set_status_ok(max_cv);
                }
            }

            if (remove_isoform){
                it.remove();
            }
        }

    }

    public Boolean get_ok_status(String isoform_name){
        return this.dict_arr_fpkm.get(isoform_name).status_ok;
    }

    public String get_mat_output_string(String[][] cov_mat){
        ArrayList<String> string_list = new ArrayList<String>();

        for(Iterator<Map.Entry<String,ArrFPKM>> it = this.dict_arr_fpkm.entrySet().iterator();it.hasNext();){
            Map.Entry<String, ArrFPKM> entry = it.next();
            string_list.add(entry.getValue().get_fpkm_string(entry.getKey(),cov_mat));
        }
        return(StringUtils.join(string_list.toArray(new String[string_list.size()]),"\n"));
    }
    public long get_num_isoforms(){
        return(dict_arr_fpkm.size());
    }

    //TODO: public get_cv(String[] group_cov) {}
}

