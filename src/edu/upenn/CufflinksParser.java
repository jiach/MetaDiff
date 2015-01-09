/**
 * Created by cheng on 1/7/15.
 */

package edu.upenn;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by cheng on 1/7/15.
 */
public class CufflinksParser {

    Map<String, ArrFPKM> dict_arr_fpkm = new TreeMap<String, ArrFPKM>();


    public CufflinksParser() {
    }

    public CufflinksParser(String[] fn){
        this.read_in_file(fn);
    }

    public void read_in_file(String[] fn){
        try {
            for (int i = 0; i < fn.length; i++) {
                System.out.println("Now processing: "+fn[i]);
                BufferedReader in = new BufferedReader(new FileReader(fn[i]));
                String line;
                String[] line_tokens;
                in.readLine();
                while ((line=in.readLine())!=null){
                    line_tokens = line.split("\t");
                    if (this.dict_arr_fpkm.containsKey(line_tokens[0])) {
                        this.dict_arr_fpkm.get(line_tokens[0]).append(Double.parseDouble(line_tokens[9]),Double.parseDouble(line_tokens[10]),Double.parseDouble(line_tokens[11]));
                    }else{
                        this.dict_arr_fpkm.put(line_tokens[0],new ArrFPKM());
                        this.dict_arr_fpkm.get(line_tokens[0]).append(Double.parseDouble(line_tokens[9]),Double.parseDouble(line_tokens[10]),Double.parseDouble(line_tokens[11]));
                    }

                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void write_tmp_file(String output_dir, String[][] cov_mat, String header){

        String mat_fn = output_dir+"/fpkm.mat";

        try {
            PrintWriter mat_fout = new PrintWriter(new File(mat_fn));
            mat_fout.print(header+"\n");
            mat_fout.print(this.get_mat_output_string(cov_mat));
            mat_fout.close();
        } catch (FileNotFoundException e) {
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
}

