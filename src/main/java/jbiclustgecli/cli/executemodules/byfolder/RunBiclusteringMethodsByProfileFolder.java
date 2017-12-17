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
package jbiclustgecli.cli.executemodules.byfolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;

import jbiclustge.methods.algorithms.AbstractBiclusteringAlgorithmCaller;
import jbiclustge.methods.algorithms.r.RBiclustAlgorithmCaller;
import jbiclustge.propertiesmodules.PropertiesModules;
import jbiclustge.propertiesmodules.components.GSEAInfoContainer;
import jbiclustge.propertiesmodules.readers.BiclusteringMethodsModuleLoader;
import jbiclustge.propertiesmodules.readers.ConcurrentProcessesModuleloader;
import jbiclustge.propertiesmodules.readers.ExpressionDatasetModuleLoader;
import jbiclustge.propertiesmodules.readers.GSEAModuleLoader;
import jbiclustge.propertiesmodules.readers.PlotsOptionsModuleLoader;
import jbiclustge.utils.osystem.JBiclustGESetupManager;
import jbiclustgecli.cli.executemodules.CommandLineExecuter;
import jbiclustgecli.processcontrolcenters.gsea.BiclusteringMethodsExecutionWithGSEAControlCenter;
import pt.ornrocha.fileutils.MTUDirUtils;
import pt.ornrocha.logutils.MTULogLevel;
import pt.ornrocha.logutils.messagecomponents.LogMessageCenter;
import pt.ornrocha.propertyutils.PropertiesUtilities;
import pt.ornrocha.systemutils.OSystemUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class RunBiclusteringMethodsByFolderConfigurations.
 */
public class RunBiclusteringMethodsByProfileFolder extends CommandLineExecuter{

	/** The dirconfs. */
	protected String profiledir;

	/** The props. */
	protected Properties props;

	/** The controlcenter. */
	//protected AbstractBiclusteringProcessCLIControlCenter controlcenter;


	protected ExpressionDatasetModuleLoader expressiondataloader=null;

	/** The methodsloader. */
	protected BiclusteringMethodsModuleLoader methodsloader=null;

	/** The gsealoader. */
	protected GSEAModuleLoader gsealoader=null;

	/** The concurrencyloader. */
	protected ConcurrentProcessesModuleloader concurrencyloader=null;

	/** The plotoptionloader. */
	protected PlotsOptionsModuleLoader plotoptionloader=null;

	/** The shutdownrenev. */
	protected boolean shutdownrenev=false;

	/** The dataset. */
	protected static String DATASET="dataset";

	/** The filesindir. */
	protected ArrayList<String> filesindir;

	/**
	 * Instantiates a new run biclustering methods by folder configurations.
	 *
	 * @param profiledir the dirconfigs
	 */
	public RunBiclusteringMethodsByProfileFolder(String profiledir){
		this.profiledir=profiledir;
		filesindir=MTUDirUtils.getFilePathsInsideDirectory(profiledir, true, false);
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


		String datasetfilepath=getDatatsetFilePath();
		if(datasetfilepath==null)
			throw new IOException("Dataset file was not found");


		String profileconfig=FilenameUtils.concat(profiledir, PropertiesModules.PROFILEFILENAME);

		if(profileconfig==null)
			throw new IOException("Profile configuration file was not found");


		String algorithmsfolder=FilenameUtils.concat(profiledir, PropertiesModules.ALGORITHMSCONFFOLDERNAME);
		if(algorithmsfolder==null)
			throw new IOException("Algorithms configuration folder was not found");




		props=PropertiesUtilities.loadFileProperties(profileconfig);
		props.setProperty(PropertiesModules.INPUTDATASETFILEPATH, OSystemUtils.validatePath(datasetfilepath));
		props.setProperty(PropertiesModules.ALGORITHMCONFIGURATIONSFOLDER, FilenameUtils.concat(profiledir,PropertiesModules.ALGORITHMSCONFFOLDERNAME));

		expressiondataloader=ExpressionDatasetModuleLoader.load(props);

		if(expressiondataloader.getExpressionDataset()!=null){

			methodsloader=BiclusteringMethodsModuleLoader.load(props);

			if(methodsloader.getMethodsToRun().size()>0){
				String gseafile=getGSEAConfigurationFile();
				if(gseafile!=null) {
					props.setProperty(PropertiesModules.GSEACONFIGURATIONFILE, OSystemUtils.validatePath(gseafile));
					gsealoader=GSEAModuleLoader.load(props);
				}
				concurrencyloader=ConcurrentProcessesModuleloader.load(props);
				plotoptionloader=PlotsOptionsModuleLoader.load(props);

				return true;
			}
			else
				return false;

		}

		return true;
	}

	/* (non-Javadoc)
	 * @see cli.executemodules.CommandLineExecuter#runExecuter()
	 */
	@Override
	protected void runExecuter() throws Exception {

		String dirresults=FilenameUtils.concat(profiledir, "Results_biclustering");
		MTUDirUtils.checkandsetDirectory(dirresults);
		GSEAInfoContainer gseainfo=null;
		if(gsealoader!=null)
			gseainfo=gsealoader.getInfoContainer();
		BiclusteringMethodsExecutionWithGSEAControlCenter controlcenter=new BiclusteringMethodsExecutionWithGSEAControlCenter(expressiondataloader.getExpressionDataset(), gseainfo, dirresults);
		if(expressiondataloader.isMissingValuesImputation()){
			expressiondataloader.getExpressionDataset().writeExpressionDatasetToFile(FilenameUtils.concat(profiledir, "Used_gene_expression_dataset_with_imputated_values.csv"));
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
		/*if(shutdownrenev)
			RConnector.closeSession();*/
	}



	/**
	 * Gets the datatset file path.
	 *
	 * @return the datatset file path
	 */
	protected String getDatatsetFilePath() {

		for (String filepath : filesindir) {
			String name=FilenameUtils.getBaseName(filepath);
			if(name.toLowerCase().equals(DATASET))
				return filepath;
		}

		return null;
	}


	/**
	 * Gets the GSEA configuration file.
	 *
	 * @return the GSEA configuration file
	 */
	protected String getGSEAConfigurationFile() {

		for (String file : filesindir) {
			String name=FilenameUtils.getName(file);
			if(name.toLowerCase().equals(PropertiesModules.GSEAPROCESSORONTOLOGIZERCONFIGNAME.toLowerCase()) || 
					name.toLowerCase().equals(PropertiesModules.GSEAPROCESSORTOPGOCONFIGNAME.toLowerCase()))
				return file;
		}

		return null;

	}


	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] args) throws Exception {

		//RunBiclusteringMethodsByFolderConfigurations.writeConfigurationsToFolder("/home/orocha/discodados/ApenasTrabalho/testes_ecoli");

		LogMessageCenter.getLogger().setLogLevel(MTULogLevel.TRACE);
		//RunBiclusteringMethodsByFolderConfigurations.writeConfigurationsToFolder("/home/orocha/discodados/ApenasTrabalho/Testes/configs");
		if(!JBiclustGESetupManager.isJbiclustGEConfigured()){
			if(OSystemUtils.isLinux())
				//JBiclustGESetupManager.setupJBiclustGEMethodsEnvironment("/home/orocha/discodados/ApenasTrabalho/Testes/TestRlibs");
				JBiclustGESetupManager.setupJBiclustGEMethodsEnvironment(null);
			else
				JBiclustGESetupManager.setupJBiclustGEMethodsEnvironment("E:\\libsR");

		}
		RunBiclusteringMethodsByProfileFolder runner=null;
		if(OSystemUtils.isLinux())
			runner=new RunBiclusteringMethodsByProfileFolder("/home/orocha/discodados/ApenasTrabalho/testes_ecoli");
		if(OSystemUtils.isWindows())
			runner=new RunBiclusteringMethodsByProfileFolder("E:\\configs");
		runner.execute();

	}



	/* (non-Javadoc)
	 * @see jbiclustgecli.cli.executemodules.CommandLineExecuter#needsToCloseRsession()
	 */
	@Override
	public boolean needsToCloseRsession() {
		return shutdownrenev;
	}

}
