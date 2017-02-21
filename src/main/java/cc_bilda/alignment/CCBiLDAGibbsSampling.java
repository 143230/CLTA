package cc_bilda.alignment;

import beans.LogPrinter;

import java.io.IOException;


public class CCBiLDAGibbsSampling
{
	public static class modelparameters
	{
		public static int topicNum = 40;
		public static double alpha = (double) 50 / (double) topicNum;
		public static double source_beta = 0.1;
		public static double target_beta = 0.1;

		public static int iteration = 200;
		public static int saveStep = 0;
		public static int beginSaveIters = 200;
	}

	public static void Run(String filename, String datatype, String distype) throws IOException {
		modelparameters bildaparameters = new modelparameters();
		LogPrinter.setOutPath("corpus/" + datatype + "/exact matching/CC-BiLDA/" + bildaparameters.topicNum + "topic_log" + distype + ".txt");
		LogPrinter.println("Reading Docs.");
		LogPrinter.println("Running "+ datatype+ " on Topic Number: "+bildaparameters.topicNum);
		Documents docSet = new Documents();
		docSet.readDocs(filename, datatype, distype);
		LogPrinter.println("source_wordMap size " + docSet.getSourceTermToIndexMap().size());
		LogPrinter.println("target_wordMap size " + docSet.getTargetTermToIndexMap().size());
		CCBiLDAModel bim = new CCBiLDAModel(bildaparameters);
		LogPrinter.println("1 Initialize the model ...");
		bim.initializeModel(docSet);
		LogPrinter.println("2 Learning and Saving the model ...");
		bim.inferenceModel(docSet, datatype, distype);
		LogPrinter.println("3 Output the final model ...");
		LogPrinter.println("Done!");
	}
	
	public static void main(String[] args) throws IOException
	{
//		Run("TextPairs(for BiLDA)", "web site directory", ".avg_pi");
//		Run("TextPairs(for BiLDA)", "product catalogue", ".avg_pi");
//		Run("MergedTextPairs(for BiLDA)", "different_cmt-cn_conference-en", ".avg_pi");
//		Run("MergedTextPairs(for BiLDA)", "different_conference-cn_confOf-en", ".avg_pi");
//		Run("MergedTextPairs(for BiLDA)", "different_conference-cn_iasted-en", ".avg_pi");
//		Run("MergedTextPairs(for BiLDA)", "different_conference-cn_sigkdd-en", ".avg_pi");
//		Run("MergedTextPairs(for BiLDA)", "different_iasted-cn_sigkdd-en", ".avg_pi");
//		Run("MergedTextPairs(for BiLDA)", "same_cmt", ".avg_pi");
//		Run("MergedTextPairs(for BiLDA)", "same_conference", ".avg_pi");
//		Run("MergedTextPairs(for BiLDA)", "same_confOf", ".avg_pi");
//		Run("MergedTextPairs(for BiLDA)", "same_iasted", ".avg_pi");
		Run("MergedTextPairs(for BiLDA)", "same_sigkdd", ".avg_pi");

		/*modelparameters bildaparameters = new modelparameters();
		
		LogPrinter.println("Reading Docs.");
		LogPrinter.println("Running product catalogue on Topic Number: "+bildaparameters.topicNum);
		Documents docSet = new Documents();
		docSet.readDocs("TextPairs(for BiLDA)_product catalogue", "product catalogue");
		LogPrinter.println("source_wordMap size " + docSet.getSourceTermToIndexMap().size());
		LogPrinter.println("target_wordMap size " + docSet.getTargetTermToIndexMap().size());
		CCBiLDAModel bim = new CCBiLDAModel(bildaparameters);
		LogPrinter.println("1 Initialize the model ...");
		bim.initializeModel(docSet);
		LogPrinter.println("2 Learning and Saving the model ...");
		bim.inferenceModel(docSet, "product catalogue");
		LogPrinter.println("3 Output the final model ...");
		LogPrinter.println("Done!");*/
		
		/*Documents docSet = new Documents();
		LogPrinter.println("Running web site directory on Topic Number: "+bildaparameters.topicNum);
		docSet.readDocs("TextPairs(for BiLDA)_web site directory", "web site directory");
		LogPrinter.println("source_wordMap size " + docSet.getSourceTermToIndexMap().size());
		LogPrinter.println("target_wordMap size " + docSet.getTargetTermToIndexMap().size());
		CCBiLDAModel bim = new CCBiLDAModel(bildaparameters);
		LogPrinter.println("1 Initialize the model ...");
		bim.initializeModel(docSet);
		LogPrinter.println("2 Learning and Saving the model ...");
		bim.inferenceModel(docSet, "web site directory");
		LogPrinter.println("3 Output the final model ...");
		LogPrinter.println("Done!");*/

	}
}
