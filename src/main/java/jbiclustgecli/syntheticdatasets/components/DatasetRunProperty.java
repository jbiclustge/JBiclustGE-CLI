/************************************************************************** 
 * Copyright 2017
 *
 * University of Minho 
 * 
 * This is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This code is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Public License for more details. 
 * 
 * You should have received a copy of the GNU Public License 
 * along with this code. If not, see http://www.gnu.org/licenses/ 
 *  
 * Created by Orlando Rocha (ornrocha@gmail.com) inside BIOSYSTEMS Group (https://www.ceb.uminho.pt/BIOSYSTEMS)
 */
package jbiclustgecli.syntheticdatasets.components;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.javatuples.Pair;

import pt.ornrocha.fileutils.MTUDirUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class DatasetRunProperty.
 */
public class DatasetRunProperty implements Comparable<DatasetRunProperty> {
	
	
	 /** The name. */
 	private String name=null;
	 
 	/** The datasettoeval 2 expectedres. */
 	private LinkedHashMap<String, String> datasettoeval2expectedres;
	 //private TreeMap<Integer, String> ordereddataset
	
	
	  /**
 	 * Instantiates a new dataset run property.
 	 *
 	 * @param dirpath the dirpath
 	 */
 	public DatasetRunProperty(String dirpath){
		 loadInformationTorun(dirpath);
	  }
	 
	 /**
 	 * Instantiates a new dataset run property.
 	 *
 	 * @param name the name
 	 * @param analysefile2expectedres the analysefile 2 expectedres
 	 */
 	public DatasetRunProperty(String name, LinkedHashMap<String, String> analysefile2expectedres){
		 this.name=name;
		 this.datasettoeval2expectedres=analysefile2expectedres;
	 }
	 
	 /**
 	 * Gets the name.
 	 *
 	 * @return the name
 	 */
 	public String getName() {
		return name;
	}

	/**
	 * Gets the datasets to evaluate 2 expected results.
	 *
	 * @return the datasets to evaluate 2 expected results
	 */
	public LinkedHashMap<String, String> getDatasetsToEvaluate2ExpectedResults() {
		return datasettoeval2expectedres;
	}
	
	/**
	 * Gets the size.
	 *
	 * @return the size
	 */
	public int getSize(){
		return datasettoeval2expectedres.size();
	}
	
	/**
	 * Gets the number rows to analyse.
	 *
	 * @return the number rows to analyse
	 */
	public Integer getNumberRowsToAnalyse(){
		Pattern pat=Pattern.compile("\\w+_(\\d+)$");
		Matcher m=pat.matcher(getName());
		if(m.find()){
			return Integer.parseInt(m.group(1));
		}
		return null;
	}

	/**
	 * Load information torun.
	 *
	 * @param dirpath the dirpath
	 */
	private void loadInformationTorun(String dirpath){
		 datasettoeval2expectedres=new LinkedHashMap<>();
		 this.name=FilenameUtils.getBaseName(dirpath);
		 ArrayList<String> allfiles=MTUDirUtils.getFilePathsInsideDirectory(dirpath);
		 
		 ArrayList<String> datasetpaths=new ArrayList<>();
		 ArrayList<String> expectedpaths=new ArrayList<>();
		 
		 for (int i = 0; i < allfiles.size(); i++) {
			String filepath=allfiles.get(i);
			if(FilenameUtils.getBaseName(filepath).contains("dataset_"))
				datasetpaths.add(filepath);
			else if(FilenameUtils.getBaseName(filepath).contains("expected_"))
				expectedpaths.add(filepath);
		 }
		 organizeInformation(datasetpaths, expectedpaths);
	 }
	 
	 
	 /**
 	 * Organize information.
 	 *
 	 * @param datasetpaths the datasetpaths
 	 * @param expectedpaths the expectedpaths
 	 */
 	private void organizeInformation(ArrayList<String> datasetpaths, ArrayList<String> expectedpaths){
		 
		 Pattern pat=Pattern.compile("dataset_(\\d+)");
		 TreeMap<Integer, Pair<String, String>> ordereddataset =new TreeMap<>();
		 for (int i = 0; i < datasetpaths.size(); i++) {
			String datasetpath=datasetpaths.get(i);
			String datasetname=FilenameUtils.getBaseName(datasetpath);
			Matcher m=pat.matcher(datasetname);
			String id=null;
			if(m.find()){
				id=m.group(1);
			}
			if(id!=null){
				int idnumber=Integer.parseInt(id);
				String getname="expected_"+id;
				for (int j = 0; j < expectedpaths.size(); j++) {
					String expectedname=FilenameUtils.getBaseName(expectedpaths.get(j));
					if(expectedname.equals(getname)){
						Pair<String, String> map=new Pair<String, String>(datasetpath, expectedpaths.get(j));
						ordereddataset.put(idnumber, map);
						//System.out.println(datasetpath+" --> "+expectedpaths.get(j));
						//datasettoeval2expectedres.put(datasetpath, expectedpaths.get(j));	
					}
				}
			}
		 }
		 
		 //System.out.println(ordereddataset);
		 for (Pair<String, String> ma : ordereddataset.values()) {
			datasettoeval2expectedres.put(ma.getValue0(), ma.getValue1());
		 }

	 }
	 
	 
	 /* (non-Javadoc)
 	 * @see java.lang.Comparable#compareTo(java.lang.Object)
 	 */
 	@Override
	public int compareTo(DatasetRunProperty o) {
		
		 Integer thisrowscount=getNumberRowsToAnalyse();
		 Integer otherrowscount=o.getNumberRowsToAnalyse();
		 if(thisrowscount!=null && otherrowscount!=null){
			 if(thisrowscount>otherrowscount)
				 return 1;
			 else if(thisrowscount<otherrowscount)
				 return -1;
			 else
				 return 0;
		 }
		 
		return 0;
	}
	 
	 
	 /**
 	 * The main method.
 	 *
 	 * @param args the arguments
 	 */
 	public static void main(String[] args){
		 DatasetRunProperty prop=new DatasetRunProperty("/home/orocha/discodados/Biclustering/datasets/constant/row_count_500");
		 System.out.println(prop.getName());
	 }

	

	

}
