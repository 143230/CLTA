package beans;

import java.util.ArrayList;
import java.util.List;

public class Category 
{
	private String url;
	private String label;
	private String labelLanguage;
	private List<Biterm> sourceBiterms;
	private List<Biterm> source_targetBiterms;
	private List<Biterm> targetBiterms;
	
	//for lda
	private List<String> sourceWordList;
	private List<String> targetWordList;
	
	public Category(String url, String label, String labelLanguage)
	{
		this.url = url;
		this.label = label;
		this.labelLanguage = labelLanguage;
		
		this.sourceBiterms = new ArrayList<Biterm>();
		this.source_targetBiterms = new ArrayList<Biterm>();
		this.targetBiterms = new ArrayList<Biterm>();
		
		this.sourceWordList = new ArrayList<String>();
		this.targetWordList = new ArrayList<String>();
	}

	
	public String getUrl() 
	{
		return url;
	}

	public String getLabel()
	{
		return label;
	}

	public String getLabelLanguage()
	{
		return labelLanguage;
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

	public List<String> getSourceWordList() {
		return sourceWordList;
	}

	public void setSourceWordList(List<String> sourceWordList) {
		this.sourceWordList.addAll(sourceWordList);
	}

	public List<String> getTargetWordList() {
		return targetWordList;
	}

	public void setTargetWordList(List<String> targetWordList) {
		this.targetWordList.addAll(targetWordList);
	}
	
	
}
