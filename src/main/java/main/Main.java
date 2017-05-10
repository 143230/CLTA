package main;

import cc_bibtm.alignment.CCBiBTMGibbsSampling;
import cc_bilda.alignment.CCBiLDAGibbsSampling;
import org.apache.commons.cli.*;

import java.io.IOException;

/**
 * Created by Administrator on 2017/1/5.
 */
public class Main {
    private static Options opts = new Options();
    private static CommandLineParser parser = new DefaultParser();
    static{
        opts.addOption("h", false, "HELP_DESCRIPTION");
        opts.addOption("k", true, "Topic Number");
        opts.addOption("alpha", true, "Hyper Parameter Alpha");
        opts.addOption("source_beta", true, "Source Beta");
        opts.addOption("target_beta", true, "Target Beta");
        opts.addOption("iter", true, "Iteration Number");
        opts.addOption("savestep", true, "Step to Save");
        opts.addOption("m", true, "Method for training the corpus, one of <CCBiBTM, CCBiLDA>");
        opts.addOption("f", true, "File Name");
        opts.addOption("t", true, "Data Type");
        opts.addOption("avg", false, "Using Average Category Distribution to inference the GibbsSampling.");
        opts.addOption("hier", false, "Using Hierarchy Category Distribution to inference the GibbsSampling.");
    }
    public static void main(String[] args) throws Exception {
        CommandLine cli = parser.parse(opts, args);
        if (cli.getOptions().length > 0) {
            if (cli.hasOption('h')) {
                HelpFormatter hf = new HelpFormatter();
                hf.printHelp("Model Run Options", opts);
            } else {
                if(cli.hasOption("m")){
                    String method = cli.getOptionValue("m");
                    if("CCBiBTM".equals(method))runCCBiBTM(cli);
                    else if("CCBiLDA".equals(method))runCCBiLDA(cli);
                    else System.err.println("Error Method Found, Use BiBTM or BiLDA.");
                }else System.err.println("Error Method Found, Use BiBTM or BiLDA.");
            }
        } else {
            HelpFormatter hf = new HelpFormatter();
            hf.printHelp("Model Run Options", opts);
        }
    }

    private static void runCCBiBTM(CommandLine cli) throws Exception {
        if(cli.hasOption("k")){
            int K = Integer.valueOf(cli.getOptionValue("k")).intValue();
            CCBiBTMGibbsSampling.modelparameters.topicNum = K;
        }
        if(cli.hasOption("alpha")){
            double alpha = Double.valueOf(cli.getOptionValue("alpha")).doubleValue();
            CCBiBTMGibbsSampling.modelparameters.alpha = alpha;
        }
        if(cli.hasOption("source_beta")){
            double source_bata = Double.valueOf(cli.getOptionValue("source_beta")).doubleValue();
            CCBiBTMGibbsSampling.modelparameters.source_beta = source_bata;
        }
        if(cli.hasOption("target_beta")){
            double target_beta = Double.valueOf(cli.getOptionValue("target_beta")).doubleValue();
            CCBiBTMGibbsSampling.modelparameters.target_beta = target_beta;
        }
        if(cli.hasOption("iter")){
            int iteration = Integer.valueOf(cli.getOptionValue("iter")).intValue();
            CCBiBTMGibbsSampling.modelparameters.iteration = iteration;
        }
        if(cli.hasOption("savestep")){
            int savestep = Integer.valueOf(cli.getOptionValue("savestep")).intValue();
            CCBiBTMGibbsSampling.modelparameters.saveStep = savestep;
        }
        String fileName = cli.getOptionValue("f");
        if(fileName == null){
            throw new Exception("Must Specify corpus filename prefix.");
        }
        String dataType = cli.getOptionValue("t");
        if(dataType == null){
            throw new Exception("Must Specify corpus data type.");
        }
        String defalut_distype = ".avg_pi";
        if(cli.hasOption("hier")){
            defalut_distype = ".hier_pi";
        }
        CCBiBTMGibbsSampling.Run(fileName, dataType, defalut_distype);
    }

    private static void runCCBiLDA(CommandLine cli) throws Exception {
        if(cli.hasOption("k")){
            int K = Integer.valueOf(cli.getOptionValue("k")).intValue();
            CCBiLDAGibbsSampling.modelparameters.topicNum = K;
        }
        if(cli.hasOption("alpha")){
            double alpha = Double.valueOf(cli.getOptionValue("alpha")).doubleValue();
            CCBiLDAGibbsSampling.modelparameters.alpha = alpha;
        }
        if(cli.hasOption("source_beta")){
            double source_bata = Double.valueOf(cli.getOptionValue("source_beta")).doubleValue();
            CCBiLDAGibbsSampling.modelparameters.source_beta = source_bata;
        }
        if(cli.hasOption("target_beta")){
            double target_beta = Double.valueOf(cli.getOptionValue("target_beta")).doubleValue();
            CCBiLDAGibbsSampling.modelparameters.target_beta = target_beta;
        }
        if(cli.hasOption("iter")){
            int iteration = Integer.valueOf(cli.getOptionValue("iter")).intValue();
            CCBiLDAGibbsSampling.modelparameters.iteration = iteration;
        }
        if(cli.hasOption("savestep")){
            int savestep = Integer.valueOf(cli.getOptionValue("savestep")).intValue();
            CCBiLDAGibbsSampling.modelparameters.saveStep = savestep;
        }
        String fileName = cli.getOptionValue("f");
        if(fileName == null){
            throw new Exception("Must Specify corpus filename prefix.");
        }
        String dataType = cli.getOptionValue("t");
        if(dataType == null){
            throw new Exception("Must Specify corpus data type.");
        }
        String defalut_distype = ".avg_pi";
        if(cli.hasOption("hier")){
            defalut_distype = ".hier_pi";
        }
        CCBiLDAGibbsSampling.Run(fileName, dataType, defalut_distype);
    }
}
