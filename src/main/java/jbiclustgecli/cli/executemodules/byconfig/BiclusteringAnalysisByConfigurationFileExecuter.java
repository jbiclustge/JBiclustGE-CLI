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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;

import jbiclustge.methods.algorithms.AbstractBiclusteringAlgorithmCaller;
import jbiclustge.methods.algorithms.r.RBiclustAlgorithmCaller;
import jbiclustge.propertiesmodules.readers.BiclusteringMethodsModuleLoader;
import jbiclustge.propertiesmodules.readers.ConcurrentProcessesModuleloader;
import jbiclustge.propertiesmodules.readers.ExpressionDatasetModuleLoader;
import jbiclustge.propertiesmodules.readers.GSEAModuleLoader;
import jbiclustge.propertiesmodules.readers.PlotsOptionsModuleLoader;
import jbiclustgecli.cli.executemodules.CommandLineExecuter;
import jbiclustgecli.processcontrolcenters.gsea.BiclusteringMethodsExecutionWithGSEAControlCenter;
import pt.ornrocha.fileutils.MTUDirUtils;
import pt.ornrocha.propertyutils.PropertiesUtilities;
import pt.ornrocha.rtools.connectors.RConnector;

// TODO: Auto-generated Javadoc
/**
 * The Class BiclusteringAnalysisByConfigurationFileExecuter.
 */
public class BiclusteringAnalysisByConfigurationFileExecuter extends CommandLineExecuter{

	
	/** The props. */
	protected Properties props;
	
	/** The expressiondataloader. */
	protected ExpressionDatasetModuleLoader expressiondataloader=null;
	
	/** The methodsloader. */
	protected BiclusteringMethodsModuleLoader methodsloader=null;
	
	/** The gsealoader. */
	protected GSEAModuleLoader gsealoader=null;
	
	/** The concurrencyloader. */
	protected ConcurrentProcessesModuleloader concurrencyloader=null;
	
	/** The plotoptionloader. */
	protected PlotsOptionsModuleLoader plotoptionloader=null;
	
	/** The configdir. */
	protected String configdir=null;
	
	/** The shutdownrenev. */
	protected boolean shutdownrenev=false;
	
	/**
	 * Instantiates a new biclustering analysis by configuration file executer.
	 *
	 * @param configfilepath the configfilepath
	 * @throws FileNotFoundException the file not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public BiclusteringAnalysisByConfigurationFileExecuter(String configfilepath) throws FileNotFoundException, IOException{
		props=PropertiesUtilities.loadFileProperties(configfilepath);
		configdir=FilenameUtils.getFullPath(configfilepath);
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
	protected boolean configure() throws Exception {
		
		expressiondataloader=ExpressionDatasetModuleLoader.load(props);
		
		if(expressiondataloader.getExpressionDataset()!=null){
			
			methodsloader=BiclusteringMethodsModuleLoader.load(props);
			
			if(methodsloader.getMethodsToRun().size()>0){
				gsealoader=GSEAModuleLoader.load(props);
				concurrencyloader=ConcurrentProcessesModuleloader.load(props);
				plotoptionloader=PlotsOptionsModuleLoader.load(props);
				
				return true;
			}
			else
				return false;
			
		}
		else
			return false;
	}

	/* (non-Javadoc)
	 * @see cli.executemodules.CommandLineExecuter#runExecuter()
	 */
	@Override
	protected void runExecuter() throws Exception {
		
		String dirresults=FilenameUtils.concat(configdir, "Results_biclustering");
		MTUDirUtils.checkandsetDirectory(dirresults);
		BiclusteringMethodsExecutionWithGSEAControlCenter controlcenter=new BiclusteringMethodsExecutionWithGSEAControlCenter(expressiondataloader.getExpressionDataset(), gsealoader.getInfoContainer(), dirresults);
		if(expressiondataloader.isMissingValuesImputation()){
			expressiondataloader.getExpressionDataset().writeExpressionDatasetToFile(FilenameUtils.concat(configdir, "Used_gene_expression_dataset_with_imputated_values.csv"));
		}
		
		LinkedHashMap<AbstractBiclusteringAlgorithmCaller, Integer> methodstorun=methodsloader.getMethodsToRun();
		
		for (Map.Entry<AbstractBiclusteringAlgorithmCaller, Integer> methods : methodstorun.entrySet()) {
			
			for (int i = 0; i < methods.getValue(); i++) {
				AbstractBiclusteringAlgorithmCaller method=methods.getKey();
				if(method instanceof RBiclustAlgorithmCaller)
					shutdownrenev=true;
				controlcenter.addBiclusteringMethod(method);
			}
		}
		
		controlcenter.setNumberConcurrentProcesses(concurrencyloader.getNumberConcurrentProcesses());
		if(plotoptionloader.isCreateHeatMaps())
			controlcenter.saveBiclusterHeatmap();
		if(plotoptionloader.isCreateParallelCoordinates())
			controlcenter.saveBiclusterParallelCoord();
		
		controlcenter.execute();
		if(shutdownrenev)
			RConnector.closeSession();
		
	}
	
	
	
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] args) throws Exception {
		BiclusteringAnalysisByConfigurationFileExecuter exe=new BiclusteringAnalysisByConfigurationFileExecuter("/home/orocha/discodados/ApenasTrabalho/testes_ecoli/newTestes/Biclustering_Analysis_profile.conf");
		exe.execute();

	}





	/* (non-Javadoc)
	 * @see jbiclustgecli.cli.executemodules.CommandLineExecuter#needsToCloseRsession()
	 */
	@Override
	public boolean needsToCloseRsession() {
		return shutdownrenev;
	}

	

}
