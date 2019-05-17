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

package jbiclustgecli.cli.executemodules.byconfig;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;

import jbiclustge.datatools.expressiondata.dataset.ExpressionData;
import jbiclustge.enrichmentanalysistools.common.EnrichmentAnalyserProcessor;
import jbiclustge.enrichmentanalysistools.common.EnrichmentAnalysisResultList;
import jbiclustge.propertiesmodules.PropertyLabels;
import jbiclustge.propertiesmodules.readers.ExpressionDatasetModuleLoader;
import jbiclustge.propertiesmodules.readers.GSEAModuleLoader;
import jbiclustge.reporters.BiclusteringGSEATaskCSVReporter;
import jbiclustge.results.biclusters.BiclusterOutputFormat;
import jbiclustge.results.biclusters.containers.BiclusterList;
import jbiclustge.utils.props.AlgorithmProperties;
import jbiclustgecli.cli.executemodules.CommandLineExecuter;
import pt.ornrocha.fileutils.MTUDirUtils;
import pt.ornrocha.logutils.MTULogLevel;
import pt.ornrocha.logutils.messagecomponents.LogMessageCenter;

// TODO: Auto-generated Javadoc
/**
 * The Class RunEnrichmentAnalysisWithMethodResults.
 */
public class RunEnrichmentAnalysisWithMethodResults extends CommandLineExecuter{
	
	/** The props. */
	private Properties props=null;
    
    /** The filestoanalyse. */
    private ArrayList<String> filestoanalyse=null;
    
    /** The listofresults. */
    private ArrayList<BiclusterList> listofresults=null;
    
    /** The gseaprocessor. */
    private EnrichmentAnalyserProcessor gseaprocessor;
    
    /** The pvalues. */
    private ArrayList<Double> pvalues;
    
    /** The adjustedpvalues. */
    private boolean adjustedpvalues=false;
    
    /** The analyseddataset. */
    private ExpressionData analyseddataset;

	/**
	 * Instantiates a new run enrichment analysis with method results.
	 *
	 * @param propertiesfile the propertiesfile
	 * @throws FileNotFoundException the file not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public RunEnrichmentAnalysisWithMethodResults(String propertiesfile) throws FileNotFoundException, IOException{
		this.props=AlgorithmProperties.loadProperties(propertiesfile);
		
		
	}
    

	/* (non-Javadoc)
	 * @see cli.executemodules.CommandLineExecuter#needsConfiguration()
	 */
	@Override
	protected boolean needsConfiguration() {
		return true;
	}

	/* (non-Javadoc)
	 * @see cli.executemodules.CommandLineExecuter#configure()
	 */
	@Override
	protected boolean configure() {

		
		
		String mainfolder=props.getProperty(PropertyLabels.ANALYSERESULTINFOLDER);
	
		if(mainfolder==null || mainfolder.isEmpty())
			return false;
		else{
			
			try {
			
			filestoanalyse=new ArrayList<>();
			listofresults=new ArrayList<>();
			
			ExpressionDatasetModuleLoader loadexpressiondataset=ExpressionDatasetModuleLoader.load(props);
			analyseddataset=loadexpressiondataset.getExpressionDataset();
			
			
			ArrayList<String> possibleignore=new ArrayList<>();
			possibleignore.add("Results");
			possibleignore.add("GSEA_Analysis");
			possibleignore.add("heatmaps");
			possibleignore.add("parallelcoordinates");
			
			ArrayList<String> withinfolders=MTUDirUtils.getLastDirectoryInTree(mainfolder, possibleignore);
		
			ArrayList<String> mappeddirs=new ArrayList<>();
			
			for (int i = 0; i < withinfolders.size(); i++) {
				
				ArrayList<String> fileswithin=MTUDirUtils.getFilePathsInsideDirectory(withinfolders.get(i), true, false);
				
				String parentpath=withinfolders.get(i);
				
				for (int j = 0; j < fileswithin.size(); j++) {
					
					String basename=FilenameUtils.getBaseName(fileswithin.get(j));
					

					if(basename.toLowerCase().equals(BiclusterOutputFormat.JBiclustGE_CSV.getName().toLowerCase()) || 
						basename.toLowerCase().equals(BiclusterOutputFormat.JBiclustGE_JSON.getName().toLowerCase()) ||
						basename.toLowerCase().equals(BiclusterOutputFormat.Rbiclust_TXT.getName().toLowerCase())){
						
					if(!mappeddirs.contains(parentpath)){
						filestoanalyse.add(fileswithin.get(j));
						BiclusterList biclusters=getListBiclusters(fileswithin.get(j), basename);
						biclusters.setAnalysedDataset(analyseddataset);
						listofresults.add(biclusters);
						mappeddirs.add(parentpath);
						}	
					}
				}
			}
			System.out.println(filestoanalyse);
			if(filestoanalyse.size()>0){
				System.out.println(props);
				GSEAModuleLoader loadgseaprops=GSEAModuleLoader.load(props);
				gseaprocessor=loadgseaprops.getGseaprocessor();
				pvalues=loadgseaprops.getPvalues();
				adjustedpvalues=loadgseaprops.isUseadjustedpvalues();
				
				
					
			}
			
		} catch (Exception e) {
					LogMessageCenter.getLogger().addCriticalErrorMessage("Error loading information: ", e);
				}	
			
			
			
			
		}
		return true;
	}
	
	
	/**
	 * Gets the list biclusters.
	 *
	 * @param filepath the filepath
	 * @param basename the basename
	 * @return the list biclusters
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private BiclusterList getListBiclusters(String filepath, String basename) throws IOException{
		
		if(basename.toLowerCase().equals(BiclusterOutputFormat.JBiclustGE_CSV.getName().toLowerCase()))
			return BiclusterList.importBiclusterListFromJBiclustGEFormat(filepath);
		else if(basename.toLowerCase().equals(BiclusterOutputFormat.JBiclustGE_JSON.getName().toLowerCase()))
			return BiclusterList.imporBiclusterListFromJBiclustGEJSONFile(filepath);
		else{
			return null;
		}
		
		
	}
	

	/* (non-Javadoc)
	 * @see cli.executemodules.CommandLineExecuter#runExecuter()
	 */
	@Override
	protected void runExecuter() throws Exception {
		
		if(gseaprocessor!=null){
			
		  String mainsavedir=null;
		  if(props.containsKey(PropertyLabels.OUTPUTDIRECTORY) && !props.getProperty(PropertyLabels.OUTPUTDIRECTORY).isEmpty())
			  mainsavedir=props.getProperty(PropertyLabels.OUTPUTDIRECTORY);
			
			
		  for (int i = 0; i < listofresults.size(); i++) {
			 

			  	EnrichmentAnalyserProcessor currentprocessor=gseaprocessor.copyWorkingInstance();
			  	currentprocessor.setBiclusteringResultsToAnalyse(listofresults.get(i));  
			  	currentprocessor.run();
				
			  	EnrichmentAnalysisResultList resultsgsea=currentprocessor.getEnrichmentAnalysisResults();
				  
			  	BiclusteringGSEATaskCSVReporter reporter=new BiclusteringGSEATaskCSVReporter(listofresults.get(i), resultsgsea);
			  	reporter.setEnrichmentAnalysisPvalueTresholds(pvalues);
			  	reporter.useAdjustedpvalues(adjustedpvalues);
			  	
			  	String saveto=null;
			  	
			  	if(mainsavedir!=null){
			  		String origdirpath=FilenameUtils.getFullPathNoEndSeparator(filestoanalyse.get(i));
			  		String origdirname=FilenameUtils.getBaseName(origdirpath);
			  		saveto=FilenameUtils.concat(mainsavedir, origdirname+"_GSEA");
			  			
			  	}
			  	else{
			  		String origdirpath=FilenameUtils.getFullPath(filestoanalyse.get(i));
			  		saveto=FilenameUtils.concat(origdirpath, "GSEA_Analysis");
			  	}
			  	
			  	MTUDirUtils.checkandsetDirectory(saveto);
			  	reporter.writetodirectory(saveto);
			  	
			  	LogMessageCenter.getLogger().addInfoMessage("Report files saved to: "+saveto);
			  		  
			  }

		 }
		
		
	}
	
	
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] args) throws Exception {
		/*EnhancedPropertiesWithSubGroups props=getGSEABaseTemplateProperties();
		props.store(new FileWriter("/home/orocha/discodados/ApenasTrabalho/testes2/template.txt"), true);*/
        LogMessageCenter.getLogger().setLogLevel(MTULogLevel.INFO);
        //GSEATemplates.writeGSEAWithTopGoTemplate("/home/orocha/discodados/ApenasTrabalho/testes_ecoli", true);
		RunEnrichmentAnalysisWithMethodResults ex=new RunEnrichmentAnalysisWithMethodResults("/home/orocha/discodados/ApenasTrabalho/testes_ecoli/Biclustering_GSEA_topGO_Profile.conf");
		ex.execute();
		
	}


	/* (non-Javadoc)
	 * @see jbiclustgecli.cli.executemodules.CommandLineExecuter#needsToCloseRsession()
	 */
	@Override
	public boolean needsToCloseRsession() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public String getBiclusteringResultsFolder() {
		// TODO Auto-generated method stub
		return null;
	}

}
