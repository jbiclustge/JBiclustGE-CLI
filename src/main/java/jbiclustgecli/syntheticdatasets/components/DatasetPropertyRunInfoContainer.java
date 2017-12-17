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
import java.util.Collections;

// TODO: Auto-generated Javadoc
/**
 * The Class DatasetPropertyRunInfoContainer.
 */
public class DatasetPropertyRunInfoContainer {
	
	
	
	 /** The setstoanalyse. */
 	private ArrayList<DatasetRunProperty> setstoanalyse;
	
	
	 
	 /**
 	 * Instantiates a new dataset property run info container.
 	 */
 	public DatasetPropertyRunInfoContainer(){
		 this.setstoanalyse=new ArrayList<>();
	 }
	 
	 
	 /**
 	 * Instantiates a new dataset property run info container.
 	 *
 	 * @param dirstoload the dirstoload
 	 */
 	public DatasetPropertyRunInfoContainer(ArrayList<String> dirstoload){
		 loadInformationToExecuteInDirectory(dirstoload);
	 }
	
	
	
	 /**
 	 * Load information to execute in directory.
 	 *
 	 * @param dirstoload the dirstoload
 	 */
 	private void loadInformationToExecuteInDirectory(ArrayList<String> dirstoload){
		 
		 setstoanalyse=new ArrayList<>();
		 for (int i = 0; i < dirstoload.size(); i++) {
			 String path=dirstoload.get(i);
				DatasetRunProperty runfiles=new DatasetRunProperty(path);
				if(runfiles.getSize()>0)
					setstoanalyse.add(runfiles);
		 }
		 
		 Collections.sort(setstoanalyse);
		 
	 }
	

	/**
	 * Gets the setstoanalyse.
	 *
	 * @return the setstoanalyse
	 */
	public ArrayList<DatasetRunProperty> getSetstoanalyse() {
		return setstoanalyse;
	}
	
	/**
	 * Prints the.
	 */
	public void print(){
		
		if(setstoanalyse!=null){
			for (DatasetRunProperty datasetRunProperty : setstoanalyse) {
				System.out.println(datasetRunProperty.getName()+" run "+datasetRunProperty.getSize()+" files");
			}
		}
		else
			System.out.println("None to execute");
	}


	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		
		/*String dir="/home/orocha/discodados/Biclustering/datasets/constant";
		DatasetPropertyRunInfoContainer2 run=new DatasetPropertyRunInfoContainer2(dir);
		
		ArrayList<DatasetRunProperty> files=run.getSetstoanalyse();
		for (DatasetRunProperty datasetRunProperty : files) {
			System.out.println(datasetRunProperty.getName()+" run "+datasetRunProperty.getSize()+" files");
			//System.out.println(datasetRunProperty.getNumberRowsToAnalyse());
		}*/
	}

}
