package edu.upenn;

import cern.colt.list.DoubleArrayList;
import org.apache.commons.lang3.ArrayUtils;
import ubic.basecode.math.MultipleTestCorrection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jiacheng on 1/14/2015.
 */
public class FDR {
    public FDR(){
    }

    public static Map<String, Double> get_fdr(List<String> isoform_names, List<Double> p_val) throws UnequalLengthException {

        if (isoform_names.size()!=p_val.size()){
            throw new UnequalLengthException("isoform_names: "+Integer.toString(isoform_names.size())+"\tp_val: "+Integer.toString(p_val.size()));
        }
        DoubleArrayList arr_fdr = MultipleTestCorrection.benjaminiHochberg(new DoubleArrayList(ArrayUtils.toPrimitive(p_val.toArray(new Double[p_val.size()]))));
        Map<String, Double> isoform_fdr = new HashMap<String, Double>();
        for (int i = 0; i < arr_fdr.size(); i++) {
            isoform_fdr.put(isoform_names.get(i), arr_fdr.getQuick(i));
        }
        return isoform_fdr;
    }
}
