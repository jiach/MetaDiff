package edu.upenn;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by cheng on 1/13/15.
 */
public class Logger {
    BufferedWriter metadiffj_log_writer = null;
    Boolean verbose;
    DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    public Logger(Boolean verbose, String log_file_name){
        try {
            this.metadiffj_log_writer = new BufferedWriter(new FileWriter(log_file_name));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.verbose = verbose;
    }

    public void log_message(String message){
        Date today = Calendar.getInstance().getTime();
        message = "["+this.df.format(today)+"]:\t"+message;

        if (verbose) {
            System.err.println(message);
        }
        try {
            this.metadiffj_log_writer.write(message);
            this.metadiffj_log_writer.newLine();
            this.metadiffj_log_writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void end_logging(){
        try {
            this.metadiffj_log_writer.flush();
            this.metadiffj_log_writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void r_log(String message){
        message = "[R Script]:\t"+message;
        if(this.verbose){
            System.err.println(message);
        }

        try {
            this.metadiffj_log_writer.write(message);
            this.metadiffj_log_writer.newLine();
            this.metadiffj_log_writer.flush();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
