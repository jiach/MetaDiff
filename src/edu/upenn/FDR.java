package edu.upenn;

import cern.colt.list.DoubleArrayList;
import ubic.basecode.math.MultipleTestCorrection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by jiacheng on 1/14/2015.
 */
public class FDR {
    public FDR(){
    }

    public static Map<String, Double> get_fdr(String[] isoform_names, double[] p_val) throws UnequalLengthException {

        if (isoform_names.length!=p_val.length){
            throw new UnequalLengthException("isoform_names: "+Integer.toString(isoform_names.length)+"\tp_val: "+Integer.toString(p_val.length));
        }
        DoubleArrayList arr_fdr = MultipleTestCorrection.benjaminiHochberg(new DoubleArrayList(p_val));
        Map<String, Double> isoform_fdr = new HashMap<String, Double>();
        for (int i = 0; i < arr_fdr.size(); i++) {
            isoform_fdr.put(isoform_names[i], arr_fdr.getQuick(i));
        }
        return isoform_fdr;
    }

    public static Map<String, Map<String, Double>> get_fdr(String[] isoform_names, String[] p_val_names, double[][] p_val) throws UnequalLengthException {

        if (isoform_names.length!=p_val[0].length){
            throw new UnequalLengthException("isoform_names: "+Integer.toString(isoform_names.length)+"\tp_val: "+Integer.toString(p_val[0].length));
        } else if (p_val_names.length != p_val.length){
            throw new UnequalLengthException("isoform_names: "+Integer.toString(p_val_names.length)+"\tp_val: "+Integer.toString(p_val.length));
        }

        Map<String, Map<String, Double>> isoform_fdr = new HashMap<String, Map<String, Double>>();

        for (int i = 0; i < p_val_names.length; i++) {
            isoform_fdr.put(p_val_names[i],FDR.get_fdr(isoform_names,p_val[i]));
        }

        return isoform_fdr;
    }

}
