package cc_bibtm.alignment;

import beans.Category;
import beans.LogPrinter;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class CCBiBTMModel {
    int[][] B_s_doc;//word index array in B^s
    int[][] B_st_doc;//word index array in B^st
    int[][] B_t_doc;//word index array in B^t

    int K, C, M_s, M_st, M_t;//topic number, biterm number in B^s, B^st, B^t
    int sourceV, targetV;// source language vocabulary size, target language vocabulary size

    int[][][] B_s_z;//category-topic label array in B^s
    int[][][] B_st_z;//category-topic label array in B^st
    int[][][] B_t_z;//category-topic label array in B^t

    double alpha; //doc-topic dirichlet prior parameter
    double source_beta; //topic-word dirichlet prior parameter in source language
    double target_beta; //topic-word dirichlet prior parameter in target language
    /*
    int [][] B_s_nbk;//given biterm b in B^s, count times of topic k. M_s*K
    int [][] B_st_nbk;//given biterm b in B^st, count times of topic k. M_st*K
    int [][] B_t_nbk;//given biterm b in B^t, count times of topic k. M_t*K
    */
    int[][] source_nkt;//given topic k, count times of term t in source language. K*sourceV
    int[][] target_nkt;//given topic k, count times of term t in target language. K*targetV

    int[][] B_s_nkbSum;//Sum for each column in B_s_nbk
    int[][] B_st_nkbSum;//Sum for each column in B_st_nbk
    int[][] B_t_nkbSum;//Sum for each column in B_t_nbk

    int[] B_s_nlkSum;//Sum for each column in B_s_nlk
    int[] B_st_nlkSum;//Sum for each column in B_st_nlk
    int[] B_t_nlkSum;//Sum for each column in B_t_nlk

    int[] source_nktSum;//Sum for each row in source_nkt
    int[] target_nktSum;//Sum for each row in target_nkt

    double[][] theta;//Parameters for biterm-topic distribution K for each Category
    double[][] source_phi;//Parameters for topic-word distribution in source language K*sourceV
    double[][] target_phi;//Parameters for topic-word distribution in target language K*targetV

    int iterations;//Times of iterations
    int saveStep;//The number of iterations between two saving
    int beginSaveIters;//Begin save model at this iteration

    List<Category> cateList;

    public CCBiBTMModel(CCBiBTMGibbsSampling.modelparameters modelparam) {
        K = modelparam.topicNum;
        alpha = modelparam.alpha;
        source_beta = modelparam.source_beta;
        target_beta = modelparam.target_beta;
        iterations = modelparam.iteration;
        saveStep = modelparam.saveStep;
        beginSaveIters = modelparam.beginSaveIters;
    }

    public void initializeModel(Corpus cp) {
        cateList = cp.getDocs();
        C = cateList.size();

        M_s = cp.getSourceBiterms().size();
        M_st = cp.getSource_targetBiterms().size();
        M_t = cp.getTargetBiterms().size();

        sourceV = cp.getSourceTermToIndexMap().size();
        targetV = cp.getTargetTermToIndexMap().size();

        //单词主题个数
        source_nkt = new int[K][sourceV];
        target_nkt = new int[K][targetV];
        //双词Category-主题总数
        B_s_nkbSum = new int[C][K];
        B_st_nkbSum = new int[C][K];
        B_t_nkbSum = new int[C][K];
        //双词category总数
        B_s_nlkSum = new int[C];
        B_st_nlkSum = new int[C];
        B_t_nlkSum = new int[C];
        //单词主题总数
        source_nktSum = new int[K];
        target_nktSum = new int[K];
        //隐变量
        theta = new double[C][K];
        source_phi = new double[K][sourceV];
        target_phi = new double[K][targetV];


        //initialize documents in B^s index array
        B_s_doc = new int[M_s][2];
        for (int b = 0; b < M_s; b++) {
            B_s_doc[b][0] = cp.getSourceTermToIndexMap().get(cp.getSourceBiterms().get(b).getWord1());
            B_s_doc[b][1] = cp.getSourceTermToIndexMap().get(cp.getSourceBiterms().get(b).getWord2());
        }

        //initialize documents in B^st index array
        B_st_doc = new int[M_st][2];
        for (int b = 0; b < M_st; b++) {
            B_st_doc[b][0] = cp.getSourceTermToIndexMap().get(cp.getSource_targetBiterms().get(b).getWord1());
            B_st_doc[b][1] = cp.getTargetTermToIndexMap().get(cp.getSource_targetBiterms().get(b).getWord2());
        }

        //initialize documents in B^t index array
        B_t_doc = new int[M_t][2];
        for (int b = 0; b < M_t; b++) {
            B_t_doc[b][0] = cp.getTargetTermToIndexMap().get(cp.getTargetBiterms().get(b).getWord1());
            B_t_doc[b][1] = cp.getTargetTermToIndexMap().get(cp.getTargetBiterms().get(b).getWord2());
        }

        //initialize topic lable z for each word in B_s_doc
        B_s_z = new int[M_s][2][2];
        for (int b = 0; b < M_s; b++) {
            int initTopic = (int) (Math.random() * K);// From 0 to K - 1
            int initCategory = (int) (Math.random() * C);// From 0 to C - 1
            B_s_z[b][0][0] = initCategory;
            B_s_z[b][0][1] = initTopic;

            B_s_z[b][1][0] = initCategory;
            B_s_z[b][1][1] = initTopic;

            //number of terms B_s_doc[b][0/1] assigned to topic initTopic add 1
            source_nkt[initTopic][B_s_doc[b][0]]++;
            source_nkt[initTopic][B_s_doc[b][1]]++;
            // total number of words in source language assigned to topic initTopic add 2
            source_nktSum[initTopic] += 2;
            // total number of biterms in in B^s assigned to topic initTopic add 1
            B_s_nkbSum[initCategory][initTopic]++;
            B_s_nlkSum[initCategory]++;
        }

        //initialize topic lable z for each word in B_st_doc
        B_st_z = new int[M_st][2][2];
        for (int b = 0; b < M_st; b++) {
            int initTopic = (int) (Math.random() * K);// From 0 to K - 1
            int initCategory = (int) (Math.random() * C);// From 0 to C - 1
            B_st_z[b][0][0] = initCategory;
            B_st_z[b][0][1] = initTopic;

            B_st_z[b][1][0] = initCategory;
            B_st_z[b][1][1] = initTopic;

            //number of terms B_st_doc[b][0/1] assigned to topic initTopic add 1
            source_nkt[initTopic][B_st_doc[b][0]]++;
            target_nkt[initTopic][B_st_doc[b][1]]++;
            // total number of words in source/target language assigned to topic initTopic add 1
            source_nktSum[initTopic]++;
            target_nktSum[initTopic]++;
            // total number of biterms in in B^st assigned to topic initTopic add 1
            B_st_nkbSum[initCategory][initTopic]++;
            B_st_nlkSum[initCategory]++;
        }

        //initialize topic lable z for each word in B_t_doc
        B_t_z = new int[M_t][2][2];
        for (int b = 0; b < M_t; b++) {
            int initTopic = (int) (Math.random() * K);// From 0 to K - 1
            int initCategory = (int) (Math.random() * C);// From 0 to C - 1
            B_t_z[b][0][0] = initCategory;
            B_t_z[b][0][1] = initTopic;

            B_t_z[b][1][0] = initCategory;
            B_t_z[b][1][1] = initTopic;

            //number of terms B_t_doc[b][0/1] assigned to topic initTopic add 1
            target_nkt[initTopic][B_t_doc[b][0]]++;
            target_nkt[initTopic][B_t_doc[b][1]]++;
            // total number of words in target language assigned to topic initTopic add 2
            target_nktSum[initTopic] += 2;
            // total number of biterms in in B^t assigned to topic initTopic add 1
            B_t_nkbSum[initCategory][initTopic]++;
            B_t_nlkSum[initCategory]++;
        }
    }

    private int sample_B_s_TopicZ(int b, Map<Integer, Double> source_pi_b) {
        // Sample from p(z_i|z_-i, B) using Gibbs upde rule

        //Remove category-topic label for biterm b
        int oldCategory = B_s_z[b][0][0];
        int oldTopic = B_s_z[b][0][1];
        //B_s_nbk[b][oldTopic]--;
        source_nkt[oldTopic][B_s_doc[b][0]]--;
        source_nkt[oldTopic][B_s_doc[b][1]]--;
        B_s_nkbSum[oldCategory][oldTopic]--;
        B_s_nlkSum[oldCategory]--;
        source_nktSum[oldTopic] -= 2;

        int categorySize = source_pi_b.size();
        int[] categoryIndex = new int[categorySize];
        double[] categoryDistr = new double[categorySize];

        //Compute p(z_i = k|z_-i, B)
        double[][] p = new double[categorySize][K];
        int i=0;
        for(Integer c: source_pi_b.keySet()){
            categoryIndex[i] = c;
            categoryDistr[i++] = source_pi_b.get(c);
        }
        for (int k = 0; k < K; k++) {
            double s = ((source_nkt[k][B_s_doc[b][0]] + source_beta) * (source_nkt[k][B_s_doc[b][1]] + source_beta)) /
                    ((source_nktSum[k] + 1 + sourceV * source_beta) * (source_nktSum[k] + sourceV * source_beta));
            for (int c = 0; c < categorySize; c++) {
                int realIndex = categoryIndex[c];
                p[c][k] = (B_s_nkbSum[realIndex][k] + B_st_nkbSum[realIndex][k] + B_t_nkbSum[realIndex][k] + alpha) /
                        (B_s_nlkSum[realIndex] + B_s_nlkSum[realIndex] + B_t_nlkSum[realIndex] + K * alpha) * s * categoryDistr[c];//need another pi

            }
        }

        //Sample a new topic label for the given biterm b like roulette
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

        //Add new topic label for the given biterm b
        //B_s_nbk[b][newTopic]++;
        source_nkt[newTopic][B_s_doc[b][0]]++;
        source_nkt[newTopic][B_s_doc[b][1]]++;
        B_s_nkbSum[newCategory][newTopic]++;
        B_s_nlkSum[newCategory]++;
        source_nktSum[newTopic] += 2;

        return newCategory * K + newTopic;
    }

    private int sample_B_st_TopicZ(int b, Map<Integer, Double> source_target_pi_b) {
        // Sample from p(z_i|z_-i, B) using Gibbs upde rule

        //Remove category-topic label for biterm b
        int oldCategory = B_st_z[b][0][0];
        int oldTopic = B_st_z[b][0][1];
        //B_st_nbk[b][oldTopic]--;
        source_nkt[oldTopic][B_st_doc[b][0]]--;
        target_nkt[oldTopic][B_st_doc[b][1]]--;
        B_st_nkbSum[oldCategory][oldTopic]--;
        B_st_nlkSum[oldCategory]--;
        source_nktSum[oldTopic]--;
        target_nktSum[oldTopic]--;

        int categorySize = source_target_pi_b.size();
        int[] categoryIndex = new int[categorySize];
        double[] categoryDistr = new double[categorySize];

        //Compute p(z_i = k|z_-i, B)
        double[][] p = new double[categorySize][K];
        int i=0;
        for(Integer c: source_target_pi_b.keySet()){
            categoryIndex[i] = c;
            categoryDistr[i++] = source_target_pi_b.get(c);
        }
        for (int k = 0; k < K; k++) {
            double s = ((source_nkt[k][B_st_doc[b][0]] + source_beta) * (target_nkt[k][B_st_doc[b][1]] + target_beta)) /
                    ((source_nktSum[k] + sourceV * source_beta) * (target_nktSum[k] + targetV * target_beta));
            for (int c = 0; c < categorySize; c++) {
                int realIndex = categoryIndex[c];
                p[c][k] = (B_s_nkbSum[realIndex][k] + B_st_nkbSum[realIndex][k] + B_t_nkbSum[realIndex][k] + alpha) /
                        (B_s_nlkSum[realIndex] + B_st_nlkSum[realIndex] + B_t_nlkSum[realIndex] + K * alpha) * s * categoryDistr[c];

            }
        }

        //Sample a new topic label for the given biterm b like roulette
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

        //Add new topic label for the given biterm b
        //B_st_nbk[b][newTopic]++;
        source_nkt[newTopic][B_st_doc[b][0]]++;
        target_nkt[newTopic][B_st_doc[b][1]]++;
        B_st_nkbSum[newCategory][newTopic]++;
        B_st_nlkSum[newCategory]++;
        source_nktSum[newTopic]++;
        target_nktSum[newTopic]++;

        return newCategory * K + newTopic;
    }

    private int sample_B_t_TopicZ(int b, Map<Integer, Double> target_pi_b) {
        // Sample from p(z_i|z_-i, B) using Gibbs upde rule

        //Remove topic label for biterm b
        int oldCategory = B_t_z[b][0][0];
        int oldTopic = B_t_z[b][0][1];
        //B_t_nbk[b][oldTopic]--;
        target_nkt[oldTopic][B_t_doc[b][0]]--;
        target_nkt[oldTopic][B_t_doc[b][1]]--;
        B_t_nkbSum[oldCategory][oldTopic]--;
        B_t_nlkSum[oldCategory]--;
        target_nktSum[oldTopic] -= 2;

        int categorySize = target_pi_b.size();
        int[] categoryIndex = new int[categorySize];
        double[] categoryDistr = new double[categorySize];

        //Compute p(z_i = k|z_-i, B)
        double[][] p = new double[categorySize][K];
        int i=0;
        for(Integer c: target_pi_b.keySet()){
            categoryIndex[i] = c;
            categoryDistr[i++] = target_pi_b.get(c);
        }
        for (int k = 0; k < K; k++) {
            double s = ((target_nkt[k][B_t_doc[b][0]] + target_beta) * (target_nkt[k][B_t_doc[b][1]] + target_beta)) /
                    ((target_nktSum[k] + 1 + targetV * target_beta) * (target_nktSum[k] + targetV * target_beta));
            for (int c = 0; c < categorySize; c++) {
                int realIndex = categoryIndex[c];
                p[c][k] = (B_s_nkbSum[realIndex][k] + B_st_nkbSum[realIndex][k] + B_t_nkbSum[realIndex][k] + alpha) /
                    (B_s_nlkSum[realIndex] + B_st_nlkSum[realIndex] + B_t_nlkSum[realIndex] + K * alpha) * s * categoryDistr[c];

            }
        }

        //Sample a new topic label for the given biterm b like roulette
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

        //Add new topic label for the given biterm b
        //B_t_nbk[b][newTopic]++;
        target_nkt[newTopic][B_t_doc[b][0]]++;
        target_nkt[newTopic][B_t_doc[b][1]]++;
        B_t_nkbSum[newCategory][newTopic]++;
        B_t_nlkSum[newCategory]++;
        target_nktSum[newTopic] += 2;

        return newCategory * K + newTopic;
    }

    private void updateEstimatedParameters() {
        for (int c = 0; c < C; c++) {
            for (int k = 0; k < K; k++) {
                theta[c][k] = (B_s_nkbSum[c][k] + B_st_nkbSum[c][k] + B_t_nkbSum[c][k] + alpha) /
                        (B_s_nlkSum[c] + B_st_nlkSum[c] + B_t_nlkSum[c] + K * alpha);
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

    public void inferenceModel(Corpus cp, String dataSetType, String disType) throws IOException {
        for (int i = 1; i <= iterations; i++) {
            LogPrinter.println("Iteration " + i);
            //Use Gibbs Sampling to update B_s_z[][],  B_st_z[][], B_t_z[][]
            for (int b = 0; b < M_s; b++) {
                Map<Integer,Double> indexDisMap = cp.getSourceBiterms().get(b).getDisMap();
                int newTC = sample_B_s_TopicZ(b, indexDisMap);
                B_s_z[b][0][0] = newTC / K;
                B_s_z[b][0][1] = newTC % K;

                B_s_z[b][1][0] = newTC / K;
                B_s_z[b][1][1] = newTC % K;
                if((b+1) % 1000000 == 0){
                    LogPrinter.println("Chinese-Chinese Biterm Finished: "+(b+1));
                }
            }

            for (int b = 0; b < M_st; b++) {
                Map<Integer,Double> indexDisMap = cp.getSource_targetBiterms().get(b).getDisMap();
                int newTC = sample_B_st_TopicZ(b, indexDisMap);
                B_st_z[b][0][0] = newTC / K;
                B_st_z[b][0][1] = newTC % K;

                B_st_z[b][1][0] = newTC / K;
                B_st_z[b][1][1] = newTC % K;
                if((b+1) % 1000000 == 0){
                    LogPrinter.println("Chinese-English Biterm Finished: "+(b+1));
                }
            }

            for (int b = 0; b < M_t; b++) {
                Map<Integer,Double> indexDisMap = cp.getTargetBiterms().get(b).getDisMap();
                int newTC = sample_B_t_TopicZ(b, indexDisMap);
                B_t_z[b][0][0] = newTC / K;
                B_t_z[b][0][1] = newTC % K;

                B_t_z[b][1][0] = newTC / K;
                B_t_z[b][1][1] = newTC % K;
                if((b+1) % 1000000 == 0){
                    LogPrinter.println("English-English Biterm Finished: "+(b+1));
                }
            }

            if ( (saveStep == 0 &&  i == iterations) || (saveStep != 0 && i % saveStep == 0) ) {
                //Saving the model
                LogPrinter.println("Saving model at iteration " + i + " ... ");
                //Firstly update parameters
                updateEstimatedParameters();
                //Secondly print model variables
                saveIteratedModel(i, cp, dataSetType, disType);
            }
        }
    }

    public void saveIteratedModel(int iters, Corpus cp, String dataSetType, String disType) throws IOException {
        //BiBTM.params, BiBTM.phi_source, BiBTM.phi_target, BiBTM.theta, BiBTM.tassign_B_s, BiBTM.tassign_B_st, BiBTM.tassign_B_s, BiBTM.twords_source, BiBTM.twords_target, BiBTM.category_topics

        //BiBTM.params
        String modelName = "BiBTM_" + iters + "_topicNum_" + K + disType;
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream
                ("corpus/" + dataSetType + "/exact matching/CC-BiBTM/" + modelName + ".params", false), "utf-8"));
        bw.write("alpha = " + alpha + "\r\n");
        bw.write("source_beta = " + source_beta + "\r\n");
        bw.write("target_beta = " + target_beta + "\r\n");
        bw.write("categoryNum = " + C + "\r\n");
        bw.write("topicNum = " + K + "\r\n");
        bw.write("source_biterm_Num = " + M_s + "\r\n");
        bw.write("source_target_biterm_Num = " + M_st + "\r\n");
        bw.write("target_biterm_Num = " + M_t + "\r\n");
        bw.write("source_termNum = " + sourceV + "\r\n");
        bw.write("target_termNum = " + targetV + "\r\n");
        bw.write("iterations = " + iterations + "\r\n");
        bw.write("saveStep = " + saveStep + "\r\n");
        bw.write("beginSaveIters = " + beginSaveIters + "\r\n");
        bw.flush();
        bw.close();

        //BiBTM.category_topics
        bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream
                ("corpus/" + dataSetType + "/exact matching/CC-BiBTM/" + modelName + ".category_topics", false), "utf-8"));
        for (int i = 0; i < cateList.size(); i++) {
            Category cate = cateList.get(i);
            bw.write(cate.getUrl() + "@#@#@" + cate.getLabel() + "@#@#@");
            for (int j = 0; j < K; j++) {
                if (j == K - 1)
                    bw.write(theta[i][j] + "\r\n");
                else
                    bw.write(theta[i][j] + "\t");
            }
            bw.flush();
        }
        bw.close();

        //BiBTM.twords_source: source_phi[][] K*sourceV
        //BiBTM.twords_target: target_phi[][] K*targetV
        int topNum = 20; //Find the top 20 topic words in each topic

        bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream
                ("corpus/" + dataSetType + "/exact matching/CC-BiBTM/" + modelName + ".twords_source", false), "utf-8"));
        for (int i = 0; i < K; i++) {
            List<Integer> tWordsIndexArray = new ArrayList<Integer>();
            for (int j = 0; j < sourceV; j++) {
                tWordsIndexArray.add(new Integer(j));
            }
            Collections.sort(tWordsIndexArray, new TwordsComparable(source_phi[i]));

            bw.write("topic " + i + "\t:\t");
            for (int t = 0; t < topNum; t++) {
                if (t == topNum - 1)
                    bw.write(cp.getSourceIndexToTermMap().get(tWordsIndexArray.get(t)) + " " + source_phi[i][tWordsIndexArray.get(t)] + "\r\n");
                else
                    bw.write(cp.getSourceIndexToTermMap().get(tWordsIndexArray.get(t)) + " " + source_phi[i][tWordsIndexArray.get(t)] + "\t");
                bw.flush();
            }
        }
        bw.close();

        bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream
                ("corpus/" + dataSetType + "/exact matching/CC-BiBTM/" + modelName + ".twords_target", false), "utf-8"));
        for (int i = 0; i < K; i++) {
            List<Integer> tWordsIndexArray = new ArrayList<Integer>();
            for (int j = 0; j < targetV; j++) {
                tWordsIndexArray.add(new Integer(j));
            }
            Collections.sort(tWordsIndexArray, new TwordsComparable(target_phi[i]));

            bw.write("topic " + i + "\t:\t");
            for (int t = 0; t < topNum; t++) {
                if (t == topNum - 1)
                    bw.write(cp.getTargetIndexToTermMap().get(tWordsIndexArray.get(t)) + " " + target_phi[i][tWordsIndexArray.get(t)] + "\r\n");
                else
                    bw.write(cp.getTargetIndexToTermMap().get(tWordsIndexArray.get(t)) + " " + target_phi[i][tWordsIndexArray.get(t)] + "\t");
                bw.flush();
            }
        }
        bw.close();

        //BiBTM.phi_source
//		bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream
//				(dataSetType + "/exact matching/BiBTM/" + modelName + ".phi_source", true), "utf-8"));
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

        //BiBTM.phi_target
//		bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream
//				(dataSetType + "/exact matching/BiBTM/" + modelName + ".phi_target", true), "utf-8"));
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

        //BiBTM.tassign_B_s
//		bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream
//				(dataSetType + "/exact matching/BiBTM/" + modelName + ".tassign_B_s", true), "utf-8"));
//		for(int b = 0; b < M_s; b++)
//		{
//			bw.write(B_s_doc[b][0] + ":" + B_s_z[b][0] + "\t" + B_s_doc[b][1] + ":" + B_s_z[b][1] + "\r\n");
//		}
//		bw.close();

        //BiBTM.tassign_B_st
//		bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream
//				(dataSetType + "/exact matching/BiBTM/" + modelName + ".tassign_B_st", true), "utf-8"));
//		for(int b = 0; b < M_st; b++)
//		{
//			bw.write(B_st_doc[b][0] + ":" + B_st_z[b][0] + "\t" + B_st_doc[b][1] + ":" + B_st_z[b][1] + "\r\n");
//		}
//		bw.close();

        //BiBTM.tassign_B_t
//		bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream
//				(dataSetType + "/exact matching/BiBTM/" + modelName + ".tassign_B_t", true), "utf-8"));
//		for(int b = 0; b < M_t; b++)
//		{
//			bw.write(B_t_doc[b][0] + ":" + B_t_z[b][0] + "\t" + B_t_doc[b][1] + ":" + B_t_z[b][1] + "\r\n");
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
