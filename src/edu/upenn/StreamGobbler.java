package edu.upenn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cheng on 1/16/15.
 */
class StreamGobbler extends Thread
{
    InputStream is;
    List<String> contents = new ArrayList<String>();
    Boolean verbose = true;
    Logger log;
    StreamGobbler(InputStream is)
    {
        this.is = is;
        this.verbose = false;

    }

    StreamGobbler(InputStream is, Logger log)
    {
        this.is = is;
        this.verbose= true;
        this.log = log;

    }

    public void run()
    {
        try
        {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ( (line = br.readLine()) != null){
                //System.out.println(line);
                contents.add(line);
                if(this.verbose){
                    this.log.log_message(line);
                }
            }
        } catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    public String[] get_contents(){
        return this.contents.toArray(new String[this.contents.size()]);
    }

}