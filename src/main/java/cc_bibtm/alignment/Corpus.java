package cc_bibtm.alignment;

import beans.Biterm;
import beans.Category;
import beans.LogPrinter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;


public class Corpus 
{
	List<Category> docs;
	//source zh, target en
	List<Biterm> sourceBiterms;
	List<Biterm> source_targetBiterms;
	List<Biterm> targetBiterms;
	Map<Biterm, Map<Integer,Double>> bitermToPiMap;
	Map<String, Integer> categoryUrlIndexMap;

	Map<String, Integer> sourceTermToIndexMap;//source word -> index
	Map<String, Integer> targetTermToIndexMap;//target word -> index

	ArrayList<String> sourceIndexToTermMap;//source dictionary
	ArrayList<String> targetIndexToTermMap;//target dictionary

	Map<String,Integer> sourceTermCountMap;//source word count
	Map<String,Integer> targetTermCountMap;//target word count

	public Corpus()
	{
		this.docs = new ArrayList<Category>();
		
		this.sourceBiterms = new ArrayList<Biterm>();
		this.source_targetBiterms = new ArrayList<Biterm>();
		this.targetBiterms = new ArrayList<Biterm>();
		this.bitermToPiMap = new HashMap<Biterm, Map<Integer,Double>>();

		this.categoryUrlIndexMap = new HashMap<String, Integer>();
		
		this.sourceTermToIndexMap = new HashMap<String, Integer>();
		this.targetTermToIndexMap = new HashMap<String, Integer>();
		
		this.sourceIndexToTermMap = new ArrayList<String>();
		this.targetIndexToTermMap = new ArrayList<String>();
		
		this.sourceTermCountMap = new HashMap<String, Integer>();
		this.targetTermCountMap = new HashMap<String, Integer>();
	}
	
	//dataSetType:"web site directory" or "product catalogue"
	public void readDocs(String fileName, String dataSetType, String disType) throws Exception
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("corpus/" + dataSetType + "/exact matching/CC-BiBTM/"
				+ fileName+"_"+dataSetType), "utf-8"));
		String theLine = "";
		int line_readed = 0;
		while((theLine = br.readLine()) != null)
		{
			String[] line = theLine.split("@#@#@");
			if(++line_readed%2000==0){
				LogPrinter.println("Finished Readed "+line_readed+" lines Category.");
			}
			line[3] = line[3].substring(1, line[3].length() - 1);
			line[4] = line[4].substring(1, line[4].length() - 1);
			line[5] = line[5].substring(1, line[5].length() - 1);
			
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
			
			String[] line5x;
			if(line[5].contains(", "))
				line5x = line[5].split(", ");
			else
				line5x = new String[]{line[5]};
	
			List<Biterm> list3xy = new ArrayList<Biterm>();
			List<Biterm> list4xy = new ArrayList<Biterm>();
			List<Biterm> list5xy = new ArrayList<Biterm>();
			
			for(int i = 0; i < line3x.length; i++)
			{
				String[] line3xy = line3x[i].split("\t");
				Biterm bm = new Biterm(line3xy[0], line3xy[1], "ZH_ZH");
				list3xy.add(bm);
			}
			
			for(int i = 0; i < line4x.length; i++)
			{
				String[] line4xy = line4x[i].split("\t");
				Biterm bm = new Biterm(line4xy[0], line4xy[1], "ZH_EN");
				list4xy.add(bm);
			}
			
			for(int i = 0; i < line5x.length; i++)
			{
				String[] line5xy = line5x[i].split("\t");
				Biterm bm = new Biterm(line5xy[0], line5xy[1], "EN_EN");
				list5xy.add(bm);
			}
			
			Category cate = new Category(line[0], line[1], line[2]);
			cate.setSourceBiterms(list3xy);
			cate.setSource_targetBiterms(list4xy);
			cate.setTargetBiterms(list5xy);
			
			setDocs(cate);
			setSourceBiterms(cate.getSourceBiterms());
			setSource_targetBiterms(cate.getSource_targetBiterms());
			setTargetBiterms(cate.getTargetBiterms());
			
			//Transfer word to index
			if(!cate.getSourceBiterms().isEmpty())//duo ci yi ju
			{
				for(int i = 0; i < cate.getSourceBiterms().size(); i++)
				{
					Biterm bm = cate.getSourceBiterms().get(i);
					String word1 = bm.getWord1();
					String word2 = bm.getWord2();

					if(!this.sourceTermToIndexMap.containsKey(word1))
					{
						int newIndex = this.sourceTermToIndexMap.size();
						setSourceTermToIndexMap(word1, newIndex);
						
						setSourceIndexToTermMap(word1);
						setSourceTermCountMap(word1, new Integer(1));
					}
					else
					{
						setSourceTermCountMap(word1, this.sourceTermCountMap.get(word1) + 1);
					}
					
					if(!this.sourceTermToIndexMap.containsKey(word2))
					{
						int newIndex = this.sourceTermToIndexMap.size();
						setSourceTermToIndexMap(word2, newIndex);
						
						setSourceIndexToTermMap(word2);
						setSourceTermCountMap(word2, new Integer(1));
					}
					else
					{
						setSourceTermCountMap(word2, this.sourceTermCountMap.get(word2) + 1);
					}
				}
			}
			
			if(!cate.getSource_targetBiterms().isEmpty())
			{
				for(int i = 0; i < cate.getSource_targetBiterms().size(); i++)
				{
					Biterm bm = cate.getSource_targetBiterms().get(i);
					//word1 zh, word2 en
					String word1 = bm.getWord1();
					String word2 = bm.getWord2();

					if(!this.sourceTermToIndexMap.containsKey(word1))
					{
						int newIndex = this.sourceTermToIndexMap.size();
						setSourceTermToIndexMap(word1, newIndex);
						
						setSourceIndexToTermMap(word1);
						setSourceTermCountMap(word1, new Integer(1));
					}
					else
					{
						setSourceTermCountMap(word1, this.sourceTermCountMap.get(word1) + 1);
					}
					
					if(!this.targetTermToIndexMap.containsKey(word2))
					{
						int newIndex = this.targetTermToIndexMap.size();
						setTargetTermToIndexMap(word2, newIndex);
						
						setTargetIndexToTermMap(word2);
						setTargetTermCountMap(word2, new Integer(1));
					}
					else
					{
						setTargetTermCountMap(word2, this.targetTermCountMap.get(word2) + 1);
					}
				}
			}
			
			if(!cate.getTargetBiterms().isEmpty())
			{
				for(int i = 0; i < cate.getTargetBiterms().size(); i++)
				{
					Biterm bm = cate.getTargetBiterms().get(i);
					String word1 = bm.getWord1();
					String word2 = bm.getWord2();

					if(!this.targetTermToIndexMap.containsKey(word1))
					{
						int newIndex = this.targetTermToIndexMap.size();
						setTargetTermToIndexMap(word1, newIndex);
						
						setTargetIndexToTermMap(word1);
						setTargetTermCountMap(word1, new Integer(1));
					}
					else
					{
						setTargetTermCountMap(word1, this.targetTermCountMap.get(word1) + 1);
					}
					
					if(!this.targetTermToIndexMap.containsKey(word2))
					{
						int newIndex = this.targetTermToIndexMap.size();
						setTargetTermToIndexMap(word2, newIndex);
						
						setTargetIndexToTermMap(word2);
						setTargetTermCountMap(word2, new Integer(1));
					}
					else
					{
						setTargetTermCountMap(word2, this.targetTermCountMap.get(word2) + 1);
					}
				}
			}
		}
		LogPrinter.println("Finished Readed "+line_readed+" lines Category.");
		br = new BufferedReader(new InputStreamReader(new FileInputStream("corpus/" + dataSetType + "/exact matching/CC-BiBTM/"
				+ fileName+"_"+dataSetType + disType), "utf-8"));
		LogPrinter.println("Start Reading Category Distribution for "+dataSetType);
		line_readed =0;
		while((theLine = br.readLine())!=null){
			String[] splits = theLine.split("\t");
			String[] biterm_parts = splits[0].split("@#@#@");
			Biterm bm = new Biterm(biterm_parts[0],biterm_parts[1],biterm_parts[2]);
			splits[1] = splits[1].substring(1,splits[1].length()-1);
			String[] categoryList = splits[1].split(", ");
			Map<Integer, Double> indexToDisMap = new HashMap<Integer,Double>();
			for(String category:categoryList){
				String[] parts = category.split("@#@#@");
				Double dis = Double.valueOf(parts[1]);
				int index = categoryUrlIndexMap.get(parts[0]);
				indexToDisMap.put(index, dis);
			}
			this.bitermToPiMap.put(bm,indexToDisMap);
			if(++line_readed % 100000 == 0){
				LogPrinter.println("Finished Readed Biterm-Category Distribution :"+line_readed);
			}
		}
		for(Biterm bm:this.sourceBiterms){
			Map<Integer,Double> indexToDisMap = this.bitermToPiMap.get(bm);
			bm.setIndexDis(indexToDisMap);
		}
		for(Biterm bm:this.source_targetBiterms){
			Map<Integer,Double> indexToDisMap = this.bitermToPiMap.get(bm);
			bm.setIndexDis(indexToDisMap);
		}
		for(Biterm bm:this.targetBiterms){
			Map<Integer,Double> indexToDisMap = this.bitermToPiMap.get(bm);
			bm.setIndexDis(indexToDisMap);
		}
	}

	public List<Category> getDocs() {
		return docs;
	}

	public void setDocs(Category cate) {
		this.docs.add(cate);
		this.categoryUrlIndexMap.put(cate.getUrl(),this.categoryUrlIndexMap.size());
	}

	public List<Biterm> getSourceBiterms() {
		return sourceBiterms;
	}

	public void setSourceBiterms(List<Biterm> sourceBiterms) {
		this.sourceBiterms.addAll(sourceBiterms);
	}

	public List<Biterm> getSource_targetBiterms() {
		return source_targetBiterms;
	}

	public void setSource_targetBiterms(List<Biterm> source_targetBiterms) {
		this.source_targetBiterms.addAll(source_targetBiterms);
	}

	public List<Biterm> getTargetBiterms() {
		return targetBiterms;
	}

	public void setTargetBiterms(List<Biterm> targetBiterms) {
		this.targetBiterms.addAll(targetBiterms);
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
