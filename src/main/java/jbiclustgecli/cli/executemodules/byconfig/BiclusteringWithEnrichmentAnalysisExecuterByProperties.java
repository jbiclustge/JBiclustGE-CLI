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
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.javatuples.Triplet;

import jbiclustge.datatools.expressiondata.dataset.ExpressionData;
import jbiclustge.enrichmentanalysistools.common.EnrichmentAnalyserProcessor;
import jbiclustge.enrichmentanalysistools.ontologizer.OntologizerEnrichmentAnalyser;
import jbiclustge.enrichmentanalysistools.ontologizer.components.OntologizerPropertiesContainer;
import jbiclustge.enrichmentanalysistools.topgo.TopGOEnrichmentAnalyser;
import jbiclustge.enrichmentanalysistools.topgo.components.TopGoPropertiesContainer;
import jbiclustge.execution.controlcenters.BiclusteringWithGeneEnrichmentAnalysisControlCenter;
import jbiclustge.methods.algorithms.BiclusteringMethod;
import jbiclustge.propertiesmodules.PropertiesModules;
import jbiclustge.propertiesmodules.readers.ExpressionDatasetModuleLoader;
import jbiclustge.reporters.BiclusteringGSEAReporterType;
import jbiclustge.results.biclusters.BiclusteringUtils;
import jbiclustgecli.cli.executemodules.CommandLineExecuter;
import pt.ornrocha.collections.MTUMapUtils;
import pt.ornrocha.logutils.messagecomponents.LogMessageCenter;
import pt.ornrocha.propertyutils.EnhancedPropertiesWithSubGroups;
import pt.ornrocha.propertyutils.PropertiesUtilities;
import pt.ornrocha.rtools.connectors.RConnector;

// TODO: Auto-generated Javadoc
/**
 * The Class BiclusteringWithEnrichmentAnalysisExecuterByProperties.
 */
public class BiclusteringWithEnrichmentAnalysisExecuterByProperties extends CommandLineExecuter{
	
	
	/** The props. */
	private Properties props;
	
	/** The controlcenter. */
	private BiclusteringWithGeneEnrichmentAnalysisControlCenter controlcenter;
	
	/** The enrichmentanalyser. */
	private EnrichmentAnalyserProcessor enrichmentanalyser;
	
	/** The pvalues. */
	private ArrayList<Double> pvalues;
	
	/** The useadjustedpvalues. */
	private boolean useadjustedpvalues=false;
	
	/** The outputdir. */
	private String outputdir;
	
	/** The typereport. */
	private BiclusteringGSEAReporterType typereport;
	
	/** The numberprocesses. */
	private int numberprocesses=1;
	
	/** The inputprioritymethods. */
	private LinkedHashMap<Integer, ArrayList<Triplet<BiclusteringMethod, String, Integer>>>inputprioritymethods=new LinkedHashMap<>();
	
	/** The compressresults. */
	private boolean compressresults=false;

	
	/**
	 * Instantiates a new biclustering with enrichment analysis executer by properties.
	 *
	 * @param propsfilepath the propsfilepath
	 * @throws FileNotFoundException the file not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public BiclusteringWithEnrichmentAnalysisExecuterByProperties(String propsfilepath) throws FileNotFoundException, IOException{
		this.props=PropertiesUtilities.loadFileProperties(propsfilepath);
	}
	
	/**
	 * Instantiates a new biclustering with enrichment analysis executer by properties.
	 *
	 * @param file the file
	 */
	public BiclusteringWithEnrichmentAnalysisExecuterByProperties(Properties file){
		this.props=file;
	}
	
	

	

	  
	  

	/**
	 * Compress results.
	 *
	 * @param compressresults the compressresults
	 */
	public void compressResults(boolean compressresults) {
		this.compressresults = compressresults;
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
		boolean run=false;
		try {
			run=loadControlCenter();
		} catch (Exception e) {
			LogMessageCenter.getLogger().addCriticalErrorMessage("Error configuring control center: ", e);
		}
		if(run){
			loadBiclusteringMethods();
			
			try {
				configureEnrichmentAnalyser();
			} catch (Exception e) {
				LogMessageCenter.getLogger().addCriticalErrorMessage("Error configuring Enrichment Analyser", e);
				run=false;
			}
			if(run){
				setPvalues();
				setupReporter();	
				getConcurrentProcesses();
			}
			
		}
		return run;	
	}
	
	/**
	 * Load control center.
	 *
	 * @return true, if successful
	 * @throws Exception the exception
	 */
	private boolean loadControlCenter() throws Exception{

		ExpressionDatasetModuleLoader datasetloader=new ExpressionDatasetModuleLoader(props);
		ExpressionData dataset=datasetloader.getExpressionDataset();
		
		if(dataset!=null){
			controlcenter=new BiclusteringWithGeneEnrichmentAnalysisControlCenter(dataset);
			controlcenter.setCompressResults(compressresults);
			return true;
		}
		
		return false;
	}
	
	
	/**
	 * Load biclustering methods.
	 */
	private void loadBiclusteringMethods(){
		
		for (BiclusteringMethod method : BiclusteringMethod.values()) {
			String methodname=method.getName();
			String key=methodname+PropertiesModules.EXECUTETAG;
			if(props.containsKey(key)){
				boolean use=PropertiesUtilities.getBooleanPropertyValue(props, key, false, getClass());
				if(use){
					int priorityvalue=PropertiesUtilities.getIntegerPropertyValue(props, (methodname+PropertiesModules.PRIORITYTAG), 1, getClass());
					String configurationfile=PropertiesUtilities.getStringPropertyValue(props, (methodname+PropertiesModules.CONFIGURATIONFILETAG), null, getClass());
					int numberruns=PropertiesUtilities.getIntegerPropertyValue(props, (methodname+PropertiesModules.NUMBERRUNSTAG), 1, getClass());
					appendMethodToPriorityFile(method, configurationfile, priorityvalue, numberruns);
					
				}	
			}
		}
	}
	
	/**
	 * Append method to priority file.
	 *
	 * @param method the method
	 * @param configuration the configuration
	 * @param priority the priority
	 * @param numberruns the numberruns
	 */
	private void appendMethodToPriorityFile(BiclusteringMethod method, String configuration,int priority, int numberruns){
		
		Triplet<BiclusteringMethod, String, Integer> methodrun=new Triplet<BiclusteringMethod, String, Integer>(method, configuration, numberruns);
		if(inputprioritymethods.containsKey(priority)){
			inputprioritymethods.get(priority).add(methodrun);
		}
		else{
			ArrayList<Triplet<BiclusteringMethod, String, Integer>> listmethods=new ArrayList<>();
			listmethods.add(methodrun);
			inputprioritymethods.put(priority, listmethods);
		}
		
	}
	

	/**
	 * Configure enrichment analyser.
	 *
	 * @throws FileNotFoundException the file not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void configureEnrichmentAnalyser() throws FileNotFoundException, IOException{
		
		if(props.containsKey(PropertiesModules.GSEAPROCESSOR) && !props.getProperty(PropertiesModules.GSEAPROCESSOR).isEmpty()){
		
			String processorname=PropertiesUtilities.getStringPropertyValue(props, PropertiesModules.GSEAPROCESSOR, "ontologizer", getClass());
		
			if(processorname.toLowerCase().equals("ontologizer") || processorname.toLowerCase().equals("topgo")){
			
				if(processorname.toLowerCase().equals("topgo"))
					enrichmentanalyser=new TopGOEnrichmentAnalyser();
				else
					enrichmentanalyser=new OntologizerEnrichmentAnalyser();
			
			
				String gseaconfigurationfile=PropertiesUtilities.getStringPropertyValue(props, PropertiesModules.GSEACONFIGURATIONFILE, null, getClass());
				if(gseaconfigurationfile!=null)
					enrichmentanalyser.setProperties(PropertiesUtilities.loadFileProperties(gseaconfigurationfile));
				else
					throw new IOException("Invalid properties file for gene enrichment analyser ");

			}
			else
				throw new IOException("Invalid Gene Enrichment analyser, only ontologizer or topgo can be used");
		}


	}
	
	
	/**
	 * Sets the pvalues.
	 */
	private void setPvalues(){
		
		String usepvalues=PropertiesUtilities.getStringPropertyValue(props, PropertiesModules.GSEAOUTPVALUES, null, getClass());
		
		if(usepvalues!=null){
			pvalues=new ArrayList<>();
			if(usepvalues.contains(";")){
				String[] values=usepvalues.trim().split(";");
				
				for (String pv : values) {
					try {
						pvalues.add(Double.valueOf(pv.trim()));
					} catch (Exception e) {
						LogMessageCenter.getLogger().addErrorMessage("invalid input value["+pv+"], must be a double value");
					}
				}
			}
			
			else{
				if(usepvalues.matches("\\d+.\\d+")){
					pvalues.add(Double.valueOf(usepvalues));
				}
			}
			
			useadjustedpvalues=PropertiesUtilities.getBooleanPropertyValue(props, PropertiesModules.GSEAUSEADJUSTEDPVALUES, false, getClass());
			
	
		}
	}
	
	/**
	 * Setup reporter.
	 */
	private void setupReporter(){
		
		String reporttype=PropertiesUtilities.getStringPropertyValue(props, PropertiesModules.REPORTFORMAT, "csv", getClass());
		outputdir=PropertiesUtilities.getStringPropertyValue(props, PropertiesModules.OUTPUTDIRECTORY, null, getClass());
		if(outputdir!=null){
			typereport=BiclusteringGSEAReporterType.getReporterTypeFromString(reporttype);
		}
	}
	
	/**
	 * Gets the concurrent processes.
	 *
	 * @return the concurrent processes
	 */
	private void getConcurrentProcesses(){
		numberprocesses=PropertiesUtilities.getIntegerPropertyValue(props, PropertiesModules.SIMULTANEOUSPROCESSES, 1, getClass());
	}
	
	
	/**
	 * Load models.
	 *
	 * @throws FileNotFoundException the file not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void loadModels() throws FileNotFoundException, IOException{
		LinkedHashMap<Integer, ArrayList<Triplet<BiclusteringMethod, String, Integer>>> orderedmethods=(LinkedHashMap<Integer, ArrayList<Triplet<BiclusteringMethod, String, Integer>>>) MTUMapUtils.sortMapByKeys(inputprioritymethods, false);
		
		for (Integer order : orderedmethods.keySet()) {
			ArrayList<Triplet<BiclusteringMethod, String, Integer>> methodsorder=orderedmethods.get(order);
			
			// 
			Collections.shuffle(methodsorder);
			
			
			for (int i = 0; i < methodsorder.size(); i++) {
				Triplet<BiclusteringMethod, String, Integer> methodconfig=methodsorder.get(i);
				int runtimes=methodconfig.getValue2();
				String config=methodconfig.getValue1();
				BiclusteringMethod method=methodconfig.getValue0();
				
				for (int j = 0; j <runtimes; j++) {
					if(config!=null)
						controlcenter.addBiclusteringMethodUseFileProperties(method.getInstance(), config);
					else
						controlcenter.addBiclusteringMethod(method.getInstance());
					
				}
			}
		}
		//controlcenter.appendBiclusteringMethod(new RXMOTIFSMethod(), "/home/orocha/Dropbox/Bicluster_analysis/m3d_data/confs/BCXmotifs_configuration.conf");
	}
	
	/* (non-Javadoc)
	 * @see cli.executemodules.CommandLineExecuter#runExecuter()
	 */
	@SuppressWarnings("static-access")
	@Override
	protected void runExecuter() throws Exception {
		
		loadModels();
		controlcenter.setNumberSimultaneousProcesses(numberprocesses);
		controlcenter.setGeneEnrichmentAnalyserProcessor(enrichmentanalyser);
		controlcenter.setpValuesToAnalyseInEnrichmentAnalysis(pvalues);
		controlcenter.useAdjustedpvalues(useadjustedpvalues);
		controlcenter.setReporterType(typereport);
		controlcenter.setOutputResultsDirectory(outputdir);
		
		controlcenter.execute();
		//ArrayList<BiclusterMethodResultsContainer> results=controlcenter.getResults();
		
		System.out.println("############################ Finished ######################");

		RConnector.closeSession();
		
	}

	  
	/**
	 * Gets the template properties.
	 *
	 * @return the template properties
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static EnhancedPropertiesWithSubGroups getTemplateProperties() throws IOException{
		EnhancedPropertiesWithSubGroups props=new EnhancedPropertiesWithSubGroups();
		props.appendProperties(PropertiesModules.getInputExpressionDatasetFileModuleWithMissingvaluesImputation());
		props.appendProperties(PropertiesModules.getEnrichmentAnalysisModule());
		props.appendProperties(PropertiesModules.getResultsReporterModule());
		props.appendProperties(PropertiesModules.getConcorrentProcessesModule());
		props.appendProperties(PropertiesModules.getBiclusteringMethodsModule());
		return props;
		
	}
	
	
	
	/**
	 * Write biclustering run profile template.
	 *
	 * @param filepath the filepath
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void writeBiclusteringRunProfileTemplate(String filepath) throws IOException{
		EnhancedPropertiesWithSubGroups props=getTemplateProperties();
		props.store(new FileWriter(filepath), true);
	}
	
	
	/**
	 * Gets the template with algorithm method file paths.
	 *
	 * @param dirpath the dirpath
	 * @return the template with algorithm method file paths
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected static EnhancedPropertiesWithSubGroups getTemplateWithAlgorithmMethodFilePaths(String dirpath) throws IOException{
		EnhancedPropertiesWithSubGroups props=getTemplateProperties();
		LinkedHashMap<String, String> methods=BiclusteringUtils.writeBiclusteringMethodsConfigurationTemplateForCMD(dirpath);
		for (String keymethod : methods.keySet()) {
			if(props.containsKey(keymethod))
				props.setProperty(keymethod, methods.get(keymethod));
		}
		return props;
	}
	
	
	/**
	 * Write biclustering run profile template with algorithm methods.
	 *
	 * @param dirpath the dirpath
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void writeBiclusteringRunProfileTemplateWithAlgorithmMethods(String dirpath) throws IOException{
		EnhancedPropertiesWithSubGroups props=getTemplateWithAlgorithmMethodFilePaths(dirpath);
		String filename=FilenameUtils.concat(dirpath, "Biclustering_GSEA_Run_Profile.conf");
		props.store(new FileWriter(filename), true);
		
	}
	
	/**
	 * Write biclustering run profile template with algorithm methods.
	 *
	 * @param dirpath the dirpath
	 * @param gseaanalyser the gseaanalyser
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void writeBiclusteringRunProfileTemplateWithAlgorithmMethods(String dirpath,String gseaanalyser) throws IOException{
		EnhancedPropertiesWithSubGroups props=getTemplateWithAlgorithmMethodFilePaths(dirpath);
		String gseafilepath=null;
		if(gseaanalyser.toLowerCase().equals("topgo")){
			props.setProperty("gsea_processor", "topgo");
			gseafilepath=FilenameUtils.concat(dirpath, "topGO_configuration.conf");
			TopGoPropertiesContainer.writePropertiesFileToAnnotationDatabase(gseafilepath);
		}
		else{
			props.setProperty("gsea_processor", "ontologizer");
			gseafilepath=FilenameUtils.concat(dirpath, "Ontologizer_configuration.conf");
			OntologizerPropertiesContainer.writeCompletePropertiesFileTemplate(gseafilepath);
		}
		
		props.setProperty("gsea_configuration_file", gseafilepath);
		String filename=FilenameUtils.concat(dirpath, "Biclustering_GSEA_Run_Profile.conf");
		props.store(new FileWriter(filename), true);
	}
	
	
	
	  /**
  	 * The main method.
  	 *
  	 * @param args the arguments
  	 * @throws IOException Signals that an I/O exception has occurred.
  	 */
  	public static void main(String[] args) throws IOException{
		  BiclusteringWithEnrichmentAnalysisExecuterByProperties.writeBiclusteringRunProfileTemplateWithAlgorithmMethods("/home/orocha/discodados/Biclustering/Tests_framework");
	  }

	/* (non-Javadoc)
	 * @see jbiclustgecli.cli.executemodules.CommandLineExecuter#needsToCloseRsession()
	 */
	@Override
	public boolean needsToCloseRsession() {
		// TODO Auto-generated method stub
		return false;
	}
	

}
