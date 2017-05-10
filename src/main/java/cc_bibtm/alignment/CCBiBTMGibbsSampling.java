package cc_bibtm.alignment;

import beans.LogPrinter;

public class CCBiBTMGibbsSampling
{
	public static class modelparameters
	{
		public static int topicNum = 20;
		public static double alpha = (double) 50 / (double) topicNum;
		public static double source_beta = 0.1;
		public static double target_beta = 0.1;

		public static int iteration = 300;
		public static int saveStep = 100;
		public static int beginSaveIters = 300;
	}

	public static void Run(String filename, String datatype, String distype) throws Exception {

		modelparameters bibtmparameters = new modelparameters();
		LogPrinter.setOutPath("corpus/" + datatype + "/exact matching/CC-BiBTM/" + bibtmparameters.topicNum + "topic_log"+distype+".txt");
		Corpus cp = new Corpus();
		LogPrinter.println("Reading Docs.");
		LogPrinter.println("Running " + datatype + " on Topic Number: "+bibtmparameters.topicNum);
		cp.readDocs(filename, datatype, distype);
		LogPrinter.println("source_wordMap size " + cp.getSourceTermToIndexMap().size());
		LogPrinter.println("target_wordMap size " + cp.getTargetTermToIndexMap().size());
		LogPrinter.println("source biterm size "+cp.getSourceBiterms().size());
		LogPrinter.println("source-target biterm size "+cp.getSource_targetBiterms().size());
		LogPrinter.println("target biterm size "+ cp.getTargetBiterms().size());
		CCBiBTMModel bm = new CCBiBTMModel(bibtmparameters);
		LogPrinter.println("1 Initialize the model ...");
		bm.initializeModel(cp);
		LogPrinter.println("2 Learning and Saving the model ...");
		bm.inferenceModel(cp, datatype, distype);
		LogPrinter.println("Done!");
	}
}
