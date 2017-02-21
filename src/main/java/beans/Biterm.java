package beans;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Biterm
{
	private String word1;
	private String word2;
	private Lang language;
	private enum Lang{
		ZH_ZH,EN_EN,ZH_EN
	}
	Map<String, Double> categoryDis;
	Map<Integer,Double> indexDis;
	
	public Biterm(String word1, String word2, String language)
	{
		this.word1 = word1;
		this.word2 = word2;
		if("ZH_ZH".equals(language)) {
			this.language = Lang.ZH_ZH;
		}else if("EN_EN".equals(language)){
			this.language = Lang.EN_EN;
		}else {
			this.language = Lang.ZH_EN;
		}
	}
	
	public String getWord1()
	{
		return word1;
	}
	
	public String getWord2()
	{
		return word2;
	}

	public void setCategoryDis(Map<String,Double> map){
		this.categoryDis = map;
	}

	public Map<String,Double> getCategoryDis(){return this.categoryDis;}

	public void setIndexDis(Map<Integer,Double> disMap){
		this.indexDis = disMap;
	}
	public Map<Integer,Double> getDisMap(){
		return this.indexDis;
	}

	public String getLang(){
		switch (this.language){
			case ZH_ZH:return "ZH_ZH";
			case EN_EN:return "EN_EN";
			case ZH_EN:return "ZH_EN";
		}
		return null;
	}
	
	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append(word1 + "\t" + word2+"\t"+getLang());
		
		return buf.toString();
	}
	
	@Override
	public boolean equals(Object o)
	{
		Biterm other = (Biterm) o;
		if(!this.language.equals(other.language))return false;
		if(this.word1.equals(other.getWord1()) && this.word2.equals(other.getWord2()))
			return true;
		else if(this.word1.equals(other.getWord2()) && this.word2.equals(other.getWord1()))
			return true;
		else
			return false;
	}
	
	@Override
	public int hashCode()
    {
    	return this.word1.hashCode() + this.word2.hashCode();
    }
	
}
