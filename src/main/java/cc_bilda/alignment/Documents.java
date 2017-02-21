package cc_bilda.alignment;

import beans.Category;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Documents
{
	ArrayList<Category> docs;
	
	//source zh, target en
	Map<String, Integer> sourceTermToIndexMap;
	Map<String, Integer> targetTermToIndexMap;

	ArrayList<String> sourceIndexToTermMap;
	ArrayList<String> targetIndexToTermMap;

	Map<Integer, Map<Integer,Double>> categoryDisMap;

	Map<String, Integer> categoryUrlMap;

	Map<String,Integer> sourceTermCountMap;
	Map<String,Integer> targetTermCountMap;

	public Documents()
	{
		this.docs = new ArrayList<Category>();
		
		this.sourceTermToIndexMap = new HashMap<String, Integer>();
		this.targetTermToIndexMap = new HashMap<String, Integer>();
		
		this.sourceIndexToTermMap = new ArrayList<String>();
		this.targetIndexToTermMap = new ArrayList<String>();

		this.categoryUrlMap = new HashMap<String, Integer>();
		
		this.sourceTermCountMap = new HashMap<String, Integer>();
		this.targetTermCountMap = new HashMap<String, Integer>();

		this.categoryDisMap = new HashMap<Integer, Map<Integer, Double>>();
	}
	
	
	public void readDocs(String fileName, String dataSetType, String disType) throws IOException
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("corpus/" + dataSetType +
				"/exact matching/CC-BiLDA/" + fileName + "_" +dataSetType), "utf-8"));
		String theLine = "";
		
		while((theLine = br.readLine()) != null)
		{
			String[] line = theLine.split("@#@#@");
			Category cate = new Category(line[0], line[1], line[2]);
			
			line[3] = line[3].substring(1, line[3].length() - 1);
			line[4] = line[4].substring(1, line[4].length() - 1);

			String[] line3x;
			if(line[3].contains(", "))
				line3x = line[3].split(", ");
			else
				line3x = new String[]{line[3]};
			
			String[] line4x;
			if(line[4].contains(", "))
				line4x = line[4].split(", ");
			else
				line4x = new String[]{line[4]};
		
			cate.setSourceWordList(Arrays.asList(line3x));
			cate.setTargetWordList(Arrays.asList(line4x));
			
			setDocs(cate);
			
			//Transfer word to index
			if(!cate.getSourceWordList().isEmpty())
			{
				for(int i = 0; i < cate.getSourceWordList().size(); i++)
				{
					String word = cate.getSourceWordList().get(i);
					if(!this.sourceTermToIndexMap.containsKey(word))
					{
						int newIndex = this.sourceTermToIndexMap.size();
						setSourceTermToIndexMap(word, newIndex);
						
						setSourceIndexToTermMap(word);
						setSourceTermCountMap(word, new Integer(1));
					}
					else
					{
						setSourceTermCountMap(word, this.sourceTermCountMap.get(word) + 1);
					}
				}
			}
			
			if(!cate.getTargetWordList().isEmpty())
			{
				for(int i = 0; i < cate.getTargetWordList().size(); i++)
				{
					String word = cate.getTargetWordList().get(i);
					if(!this.targetTermToIndexMap.containsKey(word))
					{
						int newIndex = this.targetTermToIndexMap.size();
						setTargetTermToIndexMap(word, newIndex);
						
						setTargetIndexToTermMap(word);
						setTargetTermCountMap(word, new Integer(1));
					}
					else
					{
						setTargetTermCountMap(word, this.targetTermCountMap.get(word) + 1);
					}
				}
			}
		}
		br.close();
		br = new BufferedReader(new InputStreamReader(new FileInputStream("corpus/" + dataSetType +
				"/exact matching/CC-BiLDA/" + fileName +"_" +dataSetType + disType), "utf-8"));
		while((theLine = br.readLine())!=null){
			String[] splits = theLine.split("\t");
			String[] category_def = splits[0].split("@#@#@");
			splits[1] = splits[1].substring(1,splits[1].length()-1);
			String[] cat_list = splits[1].split(", ");
			String categoryUrl = category_def[0];
			int categ_index = this.categoryUrlMap.get(categoryUrl);
			Map<Integer, Double> category_disMap = new HashMap<Integer, Double>();
			for(String cat_def: cat_list){
				String[] category_dis = cat_def.split("@#@#@");
				double sim = Double.valueOf(category_dis[1]);
				int index = categoryUrlMap.get(category_dis[0]);
				category_disMap.put(index,sim);
			}
			categoryDisMap.put(categ_index, category_disMap);

		}
	}


	public ArrayList<Category> getDocs() {
		return docs;
	}


	public void setDocs(Category cate) {
		this.docs.add(cate);
		this.categoryUrlMap.put(cate.getUrl(), this.categoryUrlMap.size());
	}


	public Map<String, Integer> getSourceTermToIndexMap() {
		return sourceTermToIndexMap;
	}


	public void setSourceTermToIndexMap(String word, int index) {
		this.sourceTermToIndexMap.put(word, index);
	}


	public Map<String, Integer> getTargetTermToIndexMap() {
		return targetTermToIndexMap;
	}


	public void setTargetTermToIndexMap(String word, int index) {
		this.targetTermToIndexMap.put(word, index);
	}


	public ArrayList<String> getSourceIndexToTermMap() {
		return sourceIndexToTermMap;
	}


	public void setSourceIndexToTermMap(String word) {
		this.sourceIndexToTermMap.add(word);
	}


	public ArrayList<String> getTargetIndexToTermMap() {
		return targetIndexToTermMap;
	}


	public void setTargetIndexToTermMap(String word) {
		this.targetIndexToTermMap.add(word);
	}


	public Map<String, Integer> getSourceTermCountMap() {
		return sourceTermCountMap;
	}


	public void setSourceTermCountMap(String word, int count) {
		this.sourceTermCountMap.put(word, count);
	}


	public Map<String, Integer> getTargetTermCountMap() {
		return targetTermCountMap;
	}


	public void setTargetTermCountMap(String word, int count) {
		this.targetTermCountMap.put(word, count);
	}
	
	
}
