package edu.upenn;

import org.apache.commons.math3.util.FastMath;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by cheng on 1/9/15.
 */
public class MmseqParser extends CufflinksParser{

    public MmseqParser(String[] fn, Logger log){
        this.read_in_file(fn,log);
    }

    public void read_in_file(String[] fn, Logger log){
        try {
            for (int i = 0; i < fn.length; i++) {
                log.log_message("processing file: "+fn[i]);
                BufferedReader in = new BufferedReader(new FileReader(fn[i]));
                String line;
                String[] line_tokens;
                in.readLine();
                in.readLine();
                while ((line=in.readLine())!=null){
                    line_tokens = line.split("\t");
                    double y = Double.parseDouble(line_tokens[1]);
                    double sd = Double.parseDouble(line_tokens[2]);
                    if (this.dict_arr_fpkm.containsKey(line_tokens[0])) {
                        this.dict_arr_fpkm.get(line_tokens[0]).append(y, sd, true);
                    }else{
                        this.dict_arr_fpkm.put(line_tokens[0],new ArrFPKM());
                        this.dict_arr_fpkm.get(line_tokens[0]).append(y, sd, true);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}

