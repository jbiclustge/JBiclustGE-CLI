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

import java.util.ArrayList;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;

import jbiclustge.methods.algorithms.AbstractBiclusteringAlgorithmCaller;
import jbiclustge.methods.algorithms.r.RBiclustAlgorithmCaller;
import jbiclustge.propertiesmodules.readers.ConcurrentProcessesModuleloader;
import jbiclustge.propertiesmodules.readers.ExpressionDatasetModuleLoader;
import jbiclustge.propertiesmodules.templates.RunMethodsTemplates;
import jbiclustge.utils.osystem.JBiclustGESetupManager;
import jbiclustge.utils.props.AlgorithmProperties;
import jbiclustgecli.cli.executemodules.CommandLineExecuter;
import jbiclustgecli.processcontrolcenters.AbstractBiclusteringProcessCLIControlCenter;
import jbiclustgecli.processcontrolcenters.biclustermethods.BiclusteringMethodsExecutionControlCenter;
import pt.ornrocha.fileutils.MTUDirUtils;
import pt.ornrocha.logutils.MTULogLevel;
import pt.ornrocha.logutils.messagecomponents.LogMessageCenter;
import pt.ornrocha.rtools.connectors.RConnector;
import pt.ornrocha.systemutils.OSystemUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class RunBiclusteringMethodsByFolderConfigurations.
 */
public class RunBiclusteringMethodsByFolderConfigurations extends CommandLineExecuter{
	
	/** The dirconfs. */
	protected String dirconfs;
	
	/** The controlcenter. */
	protected AbstractBiclusteringProcessCLIControlCenter controlcenter;
	
	/** The shutdownrenev. */
	protected boolean shutdownrenev=false;
	
	/**
	 * Instantiates a new run biclustering methods by folder configurations.
	 *
	 * @param dirconfigs the dirconfigs
	 */
	public RunBiclusteringMethodsByFolderConfigurations(String dirconfigs){
		this.dirconfs=dirconfigs;
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
		
		
			try {
				
				String expressionsetconfig=FilenameUtils.concat(dirconfs, RunMethodsTemplates.CONFIGFILENAME);
				Properties dataprops=AlgorithmProperties.loadProperties(expressionsetconfig);
				ExpressionDatasetModuleLoader datasetloader=new ExpressionDatasetModuleLoader(dataprops);
				//ConcurrentProcessesPropertyModuleloader ccploader=new ConcurrentProcessesPropertyModuleloader(dataprops);
				
				String methodresultsdir=MTUDirUtils.checkEnumeratedDirAndIncrementIfExists(dirconfs, "Results_methods");
				controlcenter=new BiclusteringMethodsExecutionControlCenter(datasetloader.getExpressionDataset(), methodresultsdir);
				controlcenter.setNumberConcurrentProcesses(ConcurrentProcessesModuleloader.getNumberConcurrentProcessesFromLoader(dataprops));
                   
			    String algorithmsdir=FilenameUtils.concat(dirconfs, "algorithms");	
				
				ArrayList<AbstractBiclusteringAlgorithmCaller> methods=AlgorithmProperties.loadBiclusterMethodsFromConfigurationsInDirectory(algorithmsdir);
				System.out.println("Methods: "+methods);
				for (int i = 0; i < methods.size(); i++) {
					if(methods.get(i) instanceof RBiclustAlgorithmCaller)
						shutdownrenev=true;
					controlcenter.addBiclusteringMethod(methods.get(i));
				}

			} catch (Exception e) {
				LogMessageCenter.getLogger().addCriticalErrorMessage("Error loading methods to control center: ", e);
				return false;
			}
		return true;
	}

	/* (non-Javadoc)
	 * @see cli.executemodules.CommandLineExecuter#runExecuter()
	 */
	@Override
	protected void runExecuter() throws Exception {
		
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
		
		//RunBiclusteringMethodsByFolderConfigurations.writeConfigurationsToFolder("/home/orocha/discodados/ApenasTrabalho/testes_ecoli");
		
		LogMessageCenter.getLogger().setLogLevel(MTULogLevel.TRACE);
		//RunBiclusteringMethodsByFolderConfigurations.writeConfigurationsToFolder("/home/orocha/discodados/ApenasTrabalho/Testes/configs");
		if(!JBiclustGESetupManager.isJbiclustGEConfigured()){
			if(OSystemUtils.isLinux())
			  //JBiclustGESetupManager.setupJBiclustGEMethodsEnvironment("/home/orocha/discodados/ApenasTrabalho/Testes/TestRlibs");
				JBiclustGESetupManager.setupJBiclustGEMethodsEnvironment(null,"/home/orocha/JBiclustGE-CLI/Rlocal_3.4.3");
			else
				JBiclustGESetupManager.setupJBiclustGEMethodsEnvironment("E:\\libsR",null);
			
		}
		RunBiclusteringMethodsByFolderConfigurations runner=null;
		if(OSystemUtils.isLinux())
			runner=new RunBiclusteringMethodsByFolderConfigurations("/home/orocha/discodados/ApenasTrabalho/testes_ecoli");
		if(OSystemUtils.isWindows())
			runner=new RunBiclusteringMethodsByFolderConfigurations("E:\\configs");
		runner.execute();

	}



	/* (non-Javadoc)
	 * @see jbiclustgecli.cli.executemodules.CommandLineExecuter#needsToCloseRsession()
	 */
	@Override
	public boolean needsToCloseRsession() {
		return shutdownrenev;
	}



	@Override
	public String getBiclusteringResultsFolder() {
		// TODO Auto-generated method stub
		return null;
	}

}
