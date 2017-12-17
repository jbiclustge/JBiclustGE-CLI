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
package jbiclustgecli.syntheticdatasets.evaluators;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import jbiclustge.analysis.similarity.BiclusterResultsPairwiseFunctions;
import jbiclustge.methods.algorithms.AbstractBiclusteringAlgorithmCaller;
import jbiclustge.results.biclusters.containers.BiclusterList;
import jbiclustgecli.syntheticdatasets.components.DefaultRecoveryRelevanceResultsContainer;
import pt.ornrocha.fileutils.MTUDirUtils;
import pt.ornrocha.ioutils.writers.MTUWriterUtils;


// TODO: Auto-generated Javadoc
/**
 * The Class DefaultRecoveryRelevanceEvaluationExecutorRuntimeLoadDataset.
 */
public class DefaultRecoveryRelevanceEvaluationExecutorRuntimeLoadDataset extends MethodEvaluationExecutorRuntimeLoadDatatset<DefaultRecoveryRelevanceResultsContainer>{

	
	//private String saveindir=null;
	/** The appendreporttofile. */
	//private String filename=null;
	private String appendreporttofile=null;
	
	/** The appendtoexcelfile. */
	private String appendtoexcelfile=null;
	
	/** The associateddatasetname. */
	private String associateddatasetname=null;
	
	/** The outputresultsdir. */
	private String outputresultsdir=null;
	
	/** The writeonly. */
	private boolean writeonly=false;
	
	/** The wb. */
	private Workbook wb=null;

	
	/**
	 * Instantiates a new default recovery relevance evaluation executor runtime load dataset.
	 *
	 * @param dataset the dataset
	 * @param method the method
	 * @param expectedresults the expectedresults
	 * @param issimplebiclustformat the issimplebiclustformat
	 * @param writeonly the writeonly
	 */
	public DefaultRecoveryRelevanceEvaluationExecutorRuntimeLoadDataset(String dataset, AbstractBiclusteringAlgorithmCaller method,String expectedresults,boolean issimplebiclustformat, boolean writeonly) {
		super(dataset, method, expectedresults,issimplebiclustformat);
		this.writeonly=writeonly;
	}

	
	
	/**
	 * Sets the file to make results report.
	 *
	 * @param savetodir the new file to make results report
	 */
	public void setFileToMakeResultsReport(String savetodir){
		
		String outputdir=FilenameUtils.concat(savetodir, "Results");
		MTUDirUtils.checkDirectory(outputdir);
		

		String filename="results_"+method.getAlgorithmName()+".csv";

		this.appendreporttofile=FilenameUtils.concat(outputdir, filename);
		//this.appendtoexcelfile=FilenameUtils.concat(outputdir, "Results_algorithms.xlsx");
		this.outputresultsdir=outputdir;
	}
	
	/**
	 * Sets the associated dataset name.
	 *
	 * @param name the new associated dataset name
	 */
	public void setAssociatedDatasetName(String name){
		this.associateddatasetname=name;
	}
	
	
	/* (non-Javadoc)
	 * @see jbiclustgecli.syntheticdatasets.evaluators.MethodEvaluationExecutorRuntimeLoadDatatset#buildContainerResults(jbiclustge.results.biclusters.containers.BiclusterList)
	 */
	@Override
	protected DefaultRecoveryRelevanceResultsContainer buildContainerResults(BiclusterList obtainedresults) throws Exception {
		
		double recovery=0.0;
		double relevance=0.0;
		
		
		if(obtainedresults!=null && obtainedresults.size()>0){
			recovery=BiclusterResultsPairwiseFunctions.getRecoveryScore(expectedresults, obtainedresults);
			relevance=BiclusterResultsPairwiseFunctions.getRelevanceScore(expectedresults, obtainedresults);
			System.out.println("Method "+method.getAlgorithmName()+" recovery: "+recovery+ "  relevance: "+relevance);
		
		//LogMessageCenter.getLogger().toClass(getClass()).addInfoMessage("Method "+method.getAlgorithmName()+" recovery: "+recovery+ "  relevance: "+relevance);
		    String filename="Biclusters_obtained_"+method.getAlgorithmName()+"_"+associateddatasetname;
		    obtainedresults.writeBiclusterListToBiclustRPackageFormat(outputresultsdir, filename, "csv");
			
			if(appendreporttofile!=null){
		    	synchronized (obtainedresults) {
					MTUWriterUtils.appendDataTofile(appendreporttofile, getReport(recovery, relevance, obtainedresults));
				}
		    	
		    }
		   /* if(appendtoexcelfile!=null)
		    	saveToExcelFile(associateddatasetname, recovery, relevance, obtainedresults.getMethodRunningTime());*/
		
		    
		}
		else{
			if(appendreporttofile!=null){
				if(obtainedresults!=null)
					MTUWriterUtils.appendDataTofile(appendreporttofile, associateddatasetname+"\tWithout valid bicluster results (list biclusters=0) "+obtainedresults.getMethodRunningTime()+"\n");
				else
					MTUWriterUtils.appendDataTofile(appendreporttofile, associateddatasetname+"\tWithout valid bicluster results (list biclusters=0)\n");
		    }
		}
		
		if(appendreporttofile!=null && writeonly)
			return null;
		else
			return new DefaultRecoveryRelevanceResultsContainer(recovery, relevance, obtainedresults);
	}
	
	
	/**
	 * Save to excel file.
	 *
	 * @param dataset the dataset
	 * @param recovery the recovery
	 * @param relevance the relevance
	 * @param runtime the runtime
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private synchronized void saveToExcelFile(String dataset, Double recovery, Double relevance, String runtime) throws IOException{
		File excelfile=new File(appendtoexcelfile);
		if(!excelfile.exists())
			initExcelFile();
		
		FileInputStream document = new FileInputStream(excelfile);
		wb=new XSSFWorkbook(document);
		addInfoToExcelFile(dataset, recovery, relevance, runtime);
		document.close();
		FileOutputStream fos = new FileOutputStream(appendtoexcelfile);
		wb.write(fos);
		fos.close();
		wb.close();

	}
	
	
	/**
	 * Adds the info to excel file.
	 *
	 * @param dataset the dataset
	 * @param recovery the recovery
	 * @param relevance the relevance
	 * @param runtime the runtime
	 */
	private synchronized void addInfoToExcelFile(String dataset, Double recovery, Double relevance, String runtime){
		String methodname=method.getAlgorithmName();
		Sheet sh=null;
		if(wb.getSheet(methodname)==null){
			sh=wb.createSheet(methodname);
			Row r0=sh.createRow(0);
			r0.createCell(0).setCellValue("Dataset Name");
			r0.createCell(1).setCellValue("Recovery");
			r0.createCell(2).setCellValue("Relevance");
			r0.createCell(3).setCellValue("Runtime");
		}
		else
			sh=wb.getSheet(methodname);
		
		
		int lastrow=sh.getPhysicalNumberOfRows();

		Row r1=sh.createRow(lastrow);
		
		r1.createCell(0).setCellValue(dataset);
		if(recovery!=null && relevance!=null){
			r1.createCell(1).setCellValue(recovery);
			r1.createCell(2).setCellValue(relevance);
		}
		else
			r1.createCell(1).setCellValue("Without solution");
			
		if(runtime!=null)
			r1.createCell(3).setCellValue(runtime);
		
	}
	
	
	/**
	 * Inits the excel file.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void initExcelFile() throws IOException{
		wb=new XSSFWorkbook();
		
		FileOutputStream fos = new FileOutputStream(appendtoexcelfile);
		wb.write(fos);
		fos.close();
		wb.close();
	}
	
	/**
	 * Gets the report.
	 *
	 * @param recovery the recovery
	 * @param relevance the relevance
	 * @param obtainedresults the obtainedresults
	 * @return the report
	 */
	private String getReport(double recovery, double relevance, BiclusterList obtainedresults){
		StringBuilder str=new StringBuilder();
		if(associateddatasetname!=null)
			str.append(associateddatasetname+"\t");
		
			str.append("recovery ");
			str.append(recovery+"\t");
			str.append("relevance ");
			str.append(relevance+"\t");
			str.append("Run time ");
			str.append(obtainedresults.getMethodRunningTime()+"\n");

		return str.toString();
	}

}
