package beans;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2016/12/23.
 */
public class LogPrinter {
    private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
    private static BufferedWriter bw = null;
    public static void setOutPath(String path) throws FileNotFoundException, UnsupportedEncodingException {
        bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), "utf-8"));
    }
    public static void print(String content) {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        System.out.print(sdf.format(date)+" "+content);
        if(bw != null) {
            try {
                bw.write(sdf.format(date) + " " + content);
                bw.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void println(String content){
        print(content+"\r\n");
    }

    public static void main(String[] args) {
        print("Reading Docs");
    }
}
