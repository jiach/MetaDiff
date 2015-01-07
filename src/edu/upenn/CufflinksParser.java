/**
 * Created by cheng on 1/7/15.
 */

package edu.upenn;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by cheng on 1/7/15.
 */
public class CufflinksParser {

    Map<String, ArrFPKM> dict_arr_fpkm = new TreeMap<String, ArrFPKM>();

    public CufflinksParser(String[] fn){
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

    public void printme(){
        System.out.println(this.dict_arr_fpkm.size());
    }

}

