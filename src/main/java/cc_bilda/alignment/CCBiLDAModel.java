package cc_bilda.alignment;

import beans.LogPrinter;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;

public class CCBiLDAModel {
    int[][] source_doc;//word index array in source language
    int[][] target_doc;//word index array in target language

    int K, M, C;//topic number, document number
    int sourceV, targetV;// source language vocabulary size, target language vocabulary size

    int[][][] source_z;//category-topic label array in source language, corresponds to source_doc
    int[][][] target_z;//category-topic label array in target language, corresponds to target_doc

    double alpha; //doc-topic dirichlet prior parameter
    double source_beta; //topic-word dirichlet prior parameter in source language
    double target_beta; //topic-word dirichlet prior parameter in target language

    int[][] source_nlk;//given category l in source language, count times of topic k. C*K
    int[][] target_nlk;//given category l in target language, count times of topic k. C*K

    int[][] source_nkt;//given topic k, count times of term t in source language. K*sourceV
    int[][] target_nkt;//given topic k, count times of term t in target language. K*targetV

    int [] source_nlkSum;//Sum for each row in source_nlk
    int [] target_nlkSum;//Sum for each row in target_nlk

    int[] source_nktSum;//Sum for each row in source_nkt
    int[] target_nktSum;//Sum for each row in target_nkt

    double[][] theta;//Parameters for doc-topic distribution M*K
    double[][] source_phi;//Parameters for topic-word distribution in source language K*sourceV
    double[][] target_phi;//Parameters for topic-word distribution in target language K*targetV

    int iterations;//Times of iterations
    int saveStep;//The number of iterations between two saving
    int beginSaveIters;//Begin save model at this iteration

    public CCBiLDAModel(CCBiLDAGibbsSampling.modelparameters modelparam) {
        K = modelparam.topicNum;
        alpha = modelparam.alpha;
        source_beta = modelparam.source_beta;
        target_beta = modelparam.target_beta;
        iterations = modelparam.iteration;
        saveStep = modelparam.saveStep;
        beginSaveIters = modelparam.beginSaveIters;
    }

    public void initializeModel(Documents docSet) {
        M = docSet.docs.size();
        C = docSet.docs.size();
        sourceV = docSet.getSourceTermToIndexMap().size();
        targetV = docSet.getTargetTermToIndexMap().size();

        source_nlk = new int[C][K];
        target_nlk = new int[C][K];

        source_nkt = new int[K][sourceV];
        target_nkt = new int[K][targetV];

        source_nlkSum = new int[C];
        target_nlkSum = new int[C];

        source_nktSum = new int[K];
        target_nktSum = new int[K];

        theta = new double[C][K];
        source_phi = new double[K][sourceV];
        target_phi = new double[K][targetV];

        //initialize documents in source language index array
        source_doc = new int[M][];
        for (int m = 0; m < M; m++) {
            int N = docSet.getDocs().get(m).getSourceWordList().size();
            source_doc[m] = new int[N];
            for (int n = 0; n < N; n++) {
                source_doc[m][n] = docSet.getSourceTermToIndexMap().get(docSet.getDocs().get(m).getSourceWordList().get(n));
            }
        }

        //initialize documents in target language index array
        target_doc = new int[M][];
        for (int m = 0; m < M; m++) {
            int N = docSet.getDocs().get(m).getTargetWordList().size();
            target_doc[m] = new int[N];
            for (int n = 0; n < N; n++) {
                target_doc[m][n] = docSet.getTargetTermToIndexMap().get(docSet.getDocs().get(m).getTargetWordList().get(n));
            }
        }

        //initialize topic lable z for each word in source_doc
        source_z = new int[M][][];
        for (int m = 0; m < M; m++) {
            int N = source_doc[m].length;
            source_z[m] = new int[N][2];

            for (int n = 0; n < N; n++) {
                int initTopic = (int) (Math.random() * K);// From 0 to K - 1
                int initCateg = (int) (Math.random() * C);// From 0 to C - 1
                source_z[m][n][0] = initCateg;
                source_z[m][n][1] = initTopic;

                //number of words in category l of source language assigned to topic initTopic add 1
                source_nlk[initCateg][initTopic]++;
                // total number of words in source language assigned to category initCateg add 1
                source_nlkSum[initCateg]++;
                //number of words source_doc[m][n] assigned to topic initTopic add 1
                source_nkt[initTopic][source_doc[m][n]]++;
                // total number of words in source language assigned to topic initTopic add 1
                source_nktSum[initTopic]++;
            }
        }

        //initialize topic lable z for each word in target_doc
        target_z = new int[M][][];
        for (int m = 0; m < M; m++) {
            int N = target_doc[m].length;
            target_z[m] = new int[N][2];

            for (int n = 0; n < N; n++) {
                int initTopic = (int) (Math.random() * K);// From 0 to K - 1
                int initCateg = (int) (Math.random() * C);// From 0 to C - 1
                target_z[m][n][0] = initCateg;
                target_z[m][n][1] = initTopic;

                //number of words in doc m of target language assigned to topic initTopic add 1
                target_nlk[initCateg][initTopic]++;
                // total number of words in target language assigned to category initCateg add 1
                target_nlkSum[initCateg]++;
                //number of terms target_doc[m][n] assigned to topic initTopic add 1
                target_nkt[initTopic][target_doc[m][n]]++;
                // total number of words in target language assigned to topic initTopic add 1
                target_nktSum[initTopic]++;
            }
        }

    }

    private int sampleSourceTopicZ(int m, int n, Map<Integer, Double> category_disMap) {
        // Sample from p(z_i|z_-i, w) using Gibbs upde rule

        //Remove category topic label for w_{m,n}
        int oldCateg = source_z[m][n][0];
        int oldTopic = source_z[m][n][1];
        source_nlk[oldCateg][oldTopic]--;
        source_nlkSum[oldCateg]--;
        source_nkt[oldTopic][source_doc[m][n]]--;
        source_nktSum[oldTopic]--;


        int categorySize = category_disMap.size();
        int[] categoryIndex = new int[categorySize];
        double[] categoryDistr = new double[categorySize];

        int i = 0;
        for (Integer c : category_disMap.keySet()) {
            categoryIndex[i] = c;
            categoryDistr[i++] = category_disMap.get(c);
        }

        //Compute p(z_i = k|z_-i, w)
        double[][] p = new double[categorySize][K];
        for (int k = 0; k < K; k++) {
            double s = (source_nkt[k][source_doc[m][n]] + source_beta) / (source_nktSum[k] + sourceV * source_beta);
            for (int c = 0; c < categorySize; c++) {
                int realIndex = categoryIndex[c];
                p[c][k] = (source_nlk[realIndex][k] + target_nlk[realIndex][k] + alpha) /
                        (source_nlkSum[realIndex] + target_nlkSum[realIndex] + K * alpha) * s * categoryDistr[c];
            }
        }

        //Sample a new topic label for w_{m, n} like roulette
        //Compute cumulated probability for p
        for (int k = 1; k < K * categorySize; k++) {
            p[k / K][k % K] += p[(k - 1) / K][(k - 1) % K];
        }

        double u = Math.random() * p[categorySize - 1][K - 1]; //p[] is unnormalised
        int newTopic = K - 1, newCategory = categoryIndex[categorySize - 1];
        for (int newTC = 0; newTC < K * categorySize; newTC++) {
            if (u < p[newTC / K][newTC % K]) {
                newCategory = categoryIndex[newTC / K];
                newTopic = newTC % K;
                break;
            }
        }

        //Add new topic label for w_{m, n}
        source_nlk[newCategory][newTopic]++;
        source_nlkSum[newCategory]++;
        source_nkt[newTopic][source_doc[m][n]]++;
        source_nktSum[newTopic]++;

        return newCategory * K + newTopic;
    }

    private int sampleTargetTopicZ(int m, int n, Map<Integer, Double> category_disMap) {
        // Sample from p(z_i|z_-i, w) using Gibbs upde rule

        //Remove topic label for w_{m,n}
        int oldCateg = target_z[m][n][0];
        int oldTopic = target_z[m][n][1];
        target_nlk[oldCateg][oldTopic]--;
        target_nlkSum[oldCateg]--;
        target_nkt[oldTopic][target_doc[m][n]]--;
        target_nktSum[oldTopic]--;

        int categorySize = category_disMap.size();
        int[] categoryIndex = new int[categorySize];
        double[] categoryDistr = new double[categorySize];

        int i = 0;
        for (Integer c : category_disMap.keySet()) {
            categoryIndex[i] = c;
            categoryDistr[i++] = category_disMap.get(c);
        }

        //Compute p(z_i = k|z_-i, w)
        double[][] p = new double[categorySize][K];
        for (int k = 0; k < K; k++) {
            double s = (target_nkt[k][target_doc[m][n]] + target_beta) / (target_nktSum[k] + targetV * target_beta);
            for (int c = 0; c < categorySize; c++) {
                int realIndex = categoryIndex[c];
                p[c][k] = (source_nlk[realIndex][k] + target_nlk[realIndex][k] + alpha) /
                        (source_nlkSum[realIndex] + target_nlkSum[realIndex] + K * alpha) * s * categoryDistr[c];
            }
        }

        //Sample a new topic label for w_{m, n} like roulette
        //Compute cumulated probability for p
        for (int k = 1; k < K * categorySize; k++) {
            p[k / K][k % K] += p[(k - 1) / K][(k - 1) % K];
        }

        double u = Math.random() * p[categorySize - 1][K - 1]; //p[] is unnormalised
        int newTopic = K - 1, newCategory = categoryIndex[categorySize - 1];
        for (int newTC = 0; newTC < K * categorySize; newTC++) {
            if (u < p[newTC / K][newTC % K]) {
                newCategory = categoryIndex[newTC / K];
                newTopic = newTC % K;
                break;
            }
        }

        //Add new topic label for w_{m, n}
        target_nlk[newCategory][newTopic]++;
        target_nlkSum[newCategory]++;
        target_nkt[newTopic][target_doc[m][n]]++;
        target_nktSum[newTopic]++;

        return newCategory * K + newTopic;
    }

    private void updateEstimatedParameters() {
        for (int c = 0; c < C; c++) {
            for (int k = 0; k < K; k++) {
                theta[c][k] = (source_nlk[c][k] + target_nlk[c][k] + alpha) / (source_nlkSum[c] + target_nlkSum[c] + K * alpha);
            }
        }

        for (int k = 0; k < K; k++) {
            for (int t = 0; t < sourceV; t++) {
                source_phi[k][t] = (source_nkt[k][t] + source_beta) / (source_nktSum[k] + sourceV * source_beta);
            }
        }

        for (int k = 0; k < K; k++) {
            for (int t = 0; t < targetV; t++) {
                target_phi[k][t] = (target_nkt[k][t] + target_beta) / (target_nktSum[k] + targetV * target_beta);
            }
        }
    }

    public void inferenceModel(Documents docSet, String dataSetType, String disType) throws IOException {

        for (int i = 1; i <= iterations; i++) {
            LogPrinter.println("Iteration " + i);

            //Use Gibbs Sampling to update source_z[][] and target_z[][]
            for (int m = 0; m < M; m++) {
                int N1 = docSet.getDocs().get(m).getSourceWordList().size();
                int N2 = docSet.getDocs().get(m).getTargetWordList().size();

                Map<Integer, Double> category_disMap = docSet.categoryDisMap.get(m);

                for (int n1 = 0; n1 < N1; n1++) {
                    // Sample from p(z_i|z_-i, w)
                    int newTC = sampleSourceTopicZ(m, n1, category_disMap);
                    source_z[m][n1][0] = newTC / K;
                    source_z[m][n1][1] = newTC % K;
                }

                for (int n2 = 0; n2 < N2; n2++) {
                    // Sample from p(z_i|z_-i, w)
                    int newTC = sampleTargetTopicZ(m, n2, category_disMap);
                    target_z[m][n2][0] = newTC / K;
                    target_z[m][n2][1] = newTC % K;
                }
                if((m+1) % 1000 == 0){
                    LogPrinter.println("Document Iterate Finished: "+(m+1));
                }
            }

            if ( (saveStep == 0 &&  i == iterations) || (saveStep != 0 && i % saveStep == 0) ) {
                //Saving the model
                LogPrinter.println("Saving model at iteration " + i + " ... ");
                //Firstly update parameters
                updateEstimatedParameters();
                //Secondly print model variables
                saveIteratedModel(i, docSet, dataSetType, disType);
            }
        }
    }

    public void saveIteratedModel(int iters, Documents docSet, String dataSetType, String disType) throws IOException {
        //BiLDA.params, BiLDA.phi_source, BiLDA.phi_target, BiLDA.theta, BiLDA.tassign_source, BiLDA.tassign_target, BiLDA.twords_source, BiLDA.twords_target

        //BiLDA.params
        String modelName = "BiLDA_" + iters + "_topicNum_" + K + disType;
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream
                ("corpus/" + dataSetType + "/exact matching/CC-BiLDA/" + modelName + ".params", false), "utf-8"));
        bw.write("alpha = " + alpha + "\r\n");
        bw.write("source_beta = " + source_beta + "\r\n");
        bw.write("target_beta = " + target_beta + "\r\n");
        bw.write("topicNum = " + K + "\r\n");
        bw.write("CategoryNum = " + C + "\r\n");
        bw.write("docNum (category num) = " + M + "\r\n");
        bw.write("source_termNum = " + sourceV + "\r\n");
        bw.write("target_termNum = " + targetV + "\r\n");
        bw.write("iterations = " + iterations + "\r\n");
        bw.write("saveStep = " + saveStep + "\r\n");
        bw.write("beginSaveIters = " + beginSaveIters + "\r\n");
        bw.flush();
        bw.close();

        //BiLDA.theta M*K (for each category)
        bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream
                ("corpus/" + dataSetType + "/exact matching/CC-BiLDA/" + modelName + ".theta", false), "utf-8"));
        for (int i = 0; i < C; i++) {
            for (int j = 0; j < K; j++) {
                if (j == K - 1)
                    bw.write(theta[i][j] + "\r\n");
                else
                    bw.write(theta[i][j] + "\t");
                bw.flush();
            }
        }
        bw.close();

        //BiLDA.twords_source: source_phi[][] K*sourceV
        //BiLDA.twords_target: target_phi[][] K*targetV
        int topNum = 20; //Find the top 20 topic words in each topic

        bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream
                ("corpus/" + dataSetType + "/exact matching/CC-BiLDA/" + modelName + ".twords_source", false), "utf-8"));
        for (int i = 0; i < K; i++) {
            List<Integer> tWordsIndexArray = new ArrayList<Integer>();
            for (int j = 0; j < sourceV; j++) {
                tWordsIndexArray.add(new Integer(j));
            }
            Collections.sort(tWordsIndexArray, new TwordsComparable(source_phi[i]));

            bw.write("topic " + i + "\t:\t");
            for (int t = 0; t < topNum; t++) {
                if (t == topNum - 1)
                    bw.write(docSet.getSourceIndexToTermMap().get(tWordsIndexArray.get(t)) + " " + source_phi[i][tWordsIndexArray.get(t)] + "\r\n");
                else
                    bw.write(docSet.getSourceIndexToTermMap().get(tWordsIndexArray.get(t)) + " " + source_phi[i][tWordsIndexArray.get(t)] + "\t");
                bw.flush();
            }
        }
        bw.close();

        bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream
                ("corpus/" + dataSetType + "/exact matching/CC-BiLDA/" + modelName + ".twords_target", false), "utf-8"));
        for (int i = 0; i < K; i++) {
            List<Integer> tWordsIndexArray = new ArrayList<Integer>();
            for (int j = 0; j < targetV; j++) {
                tWordsIndexArray.add(new Integer(j));
            }
            Collections.sort(tWordsIndexArray, new TwordsComparable(target_phi[i]));

            bw.write("topic " + i + "\t:\t");
            for (int t = 0; t < topNum; t++) {
                if (t == topNum - 1)
                    bw.write(docSet.getTargetIndexToTermMap().get(tWordsIndexArray.get(t)) + " " + target_phi[i][tWordsIndexArray.get(t)] + "\r\n");
                else
                    bw.write(docSet.getTargetIndexToTermMap().get(tWordsIndexArray.get(t)) + " " + target_phi[i][tWordsIndexArray.get(t)] + "\t");
                bw.flush();
            }
        }
        bw.close();

        //BiLDA.phi_source
//		bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream
//				(dataSetType + "/exact matching/BiLDA/" + modelName + ".phi_source", true), "utf-8"));
//		for (int i = 0; i < K; i++)
//		{
//			for (int j = 0; j < sourceV; j++)
//			{
//				if(j == sourceV - 1)
//					bw.write(source_phi[i][j] + "\r\n");	
//				else
//					bw.write(source_phi[i][j] + "\t");
//				bw.flush();
//			}
//		}
//		bw.close();

        //BiLDA.phi_target
//		bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream
//				(dataSetType + "/exact matching/BiLDA/" + modelName + ".phi_target", true), "utf-8"));
//		for (int i = 0; i < K; i++)
//		{
//			for (int j = 0; j < targetV; j++)
//			{
//				if(j == targetV - 1)
//					bw.write(target_phi[i][j] + "\r\n");	
//				else
//					bw.write(target_phi[i][j] + "\t");
//				bw.flush();
//			}
//		}
//		bw.close();

        //BiLDA.tassign_source
//		bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream
//				(dataSetType + "/exact matching/BiLDA/" + modelName + ".tassign_source", true), "utf-8"));
//		for(int m = 0; m < M; m++)
//		{
//			for(int n = 0; n < source_doc[m].length; n++)
//			{
//				if(n == source_doc[m].length - 1)
//					bw.write(source_doc[m][n] + ":" + source_z[m][n] + "\r\n");
//				else
//					bw.write(source_doc[m][n] + ":" + source_z[m][n] + "\t");
//				bw.flush();
//			}
//		}
//		bw.close();

        //BiLDA.tassign_target
//		bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream
//				(dataSetType + "/exact matching/BiLDA/" + modelName + ".tassign_target", true), "utf-8"));
//		for(int m = 0; m < M; m++)
//		{
//			for(int n = 0; n < target_doc[m].length; n++)
//			{
//				if(n == target_doc[m].length - 1)
//					bw.write(target_doc[m][n] + ":" + target_z[m][n] + "\r\n");
//				else
//					bw.write(target_doc[m][n] + ":" + target_z[m][n] + "\t");
//				bw.flush();
//			}
//		}
//		bw.close();

    }

    public class TwordsComparable implements Comparator<Integer> {
        public double[] sortProb; // Store probability of each word in topic k

        public TwordsComparable(double[] sortProb) {
            this.sortProb = sortProb;
        }

        public int compare(Integer o1, Integer o2) {
            // TODO Auto-generated method stub
            //Sort topic word index according to the probability of each word in topic k
            if (sortProb[o1] > sortProb[o2]) return -1;
            else if (sortProb[o1] < sortProb[o2]) return 1;
            else return 0;
        }
    }
}
