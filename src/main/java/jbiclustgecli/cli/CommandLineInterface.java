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
package jbiclustgecli.cli;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Scanner;

import javax.swing.JFileChooser;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;

import jbiclustge.enrichmentanalysistools.ontologizer.components.OntologizerPropertiesContainer;
import jbiclustge.enrichmentanalysistools.topgo.components.TopGoPropertiesContainer;
import jbiclustge.methods.algorithms.BiclusteringMethod;
import jbiclustge.propertiesmodules.PropertiesModules;
import jbiclustge.propertiesmodules.templates.GSEATemplates;
import jbiclustge.results.biclusters.BiclusteringUtils;
import jbiclustge.utils.osystem.JBiclustGESetupManager;
import jbiclustge.utils.osystem.SystemFolderTools;
import jbiclustge.utils.properties.AlgorithmProperties;
import jbiclustgecli.cli.executemodules.byconfig.RunEnrichmentAnalysisWithMethodResults;
import jbiclustgecli.cli.executemodules.byfolder.RunBiclusteringMethodsByProfileFolder;
import jbiclustgecli.syntheticdatasets.SyntheticEvaluationControlCenter;
import pt.ornrocha.fileutils.MTUDirUtils;
import pt.ornrocha.fileutils.MTUFileUtils;
import pt.ornrocha.logutils.MTULogLevel;
import pt.ornrocha.logutils.messagecomponents.LogMessageCenter;
import pt.ornrocha.propertyutils.EnhancedPropertiesWithSubGroups;
import pt.ornrocha.rtools.connectors.RConnector;
import pt.ornrocha.systemutils.OSystemUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class CommandLineInterface.
 */
public class CommandLineInterface {
	
	
	/** The options. */
	private Options options;
	
	/** The inputargs. */
	private String[] inputargs;
	
	
	
	/**
	 * Instantiates a new command line interface.
	 *
	 * @param args the args
	 */
	public CommandLineInterface(String[] args){
		this.inputargs=args;
		setupInputOptions();
	}
	
	
	/**
	 * Setup input options.
	 */
	private void setupInputOptions(){
		options=new Options();
		
		Option help= Option.builder("h").hasArg(false).longOpt("help").build();
		options.addOption(help);
		
		Option initconf=Option.builder("conf")
				.numberOfArgs(1)
				.optionalArg(true)
				.argName("folder path (optional)")
				.desc("Makes initial configuration of biclustering methods. All necessary dependencies of R will can be installed in a folder defined by the user [set folder path after \"conf\" argument, or set \"choosefolder\" to open a help dialog]. If the folder is not set, all dependencies of R will be installed in a folder defined by JBiclustGE.\n")
				.longOpt("configure")
				.build();
		options.addOption(initconf);
		
		Option newprofile=Option.builder("newprofile")
				.desc("Interactive interface to create files to perform biclustering analysis.")
				.longOpt("new_profile")
				.build();
		options.addOption(newprofile);
		
		Option runbicprofile=Option.builder("run")
				.numberOfArgs(1)
				.hasArg(true)
				.argName("configuration filepath")
				.desc("Execute biclustering methods defined in a profile. Input arguments: path of the folder of a profile or if \"choosefolder\" it is defined after \"run\" opens a help dialog to choose that folder.")
				.longOpt("run_profile")
				.build();
		options.addOption(runbicprofile);
	
		Option newgseaprofile=Option.builder("newgsea")
				.desc("Create template files to perform the Gene Set Enrichment Analysis.")
				.longOpt("new_gsea_analysis")
				.build();
		options.addOption(newgseaprofile);
		
		Option rungseaprofile=Option.builder("rungsea")
				.numberOfArgs(1)
				.hasArg(true)
				.argName("configuration filepath")
				.desc("Execute the Gene Set Enrichment Analysis using one of the two configuration files presented below. Input arguments: path of gsea configuration file (Biclustering_GSEA_Ontologizer_Profile.conf or Biclustering_GSEA_topGO_Profile.conf ), or if \"choosefile\" it is  defined after \"rungsea\" a help dialog its opened to choose the required file.")
				.longOpt("run_gsea")
				.build();
		options.addOption(rungseaprofile);
		
		Option newsyntheticprofile=Option.builder("configsynthetic")
				.desc("Create a configuration to perform the analysis of synthetic datasets.")
				.longOpt("configure_synthetic_dataset_analysis")
				.build();
		options.addOption(newsyntheticprofile);
		
		Option runsyntheticexperiments=Option.builder("runsynthetic")
				.numberOfArgs(1)
				.hasArg(true)
				.argName("folder path of synthetic experiments")
				.desc("Execute the analyis of the synthetic experiments")
				.longOpt("run_synthetic_experiments")
				.build();
		options.addOption(runsyntheticexperiments);
		
		
		
		Option verbosity=Option.builder("v")
				.argName("option")
				.numberOfArgs(1)
				.hasArg(true)
				.desc("Verbosity level: off, warn, info, debug, trace ")
				.longOpt("verbosity")
				.build();
		options.addOption(verbosity);
		
	/*	Option logfile   = OptionBuilder.withArgName( "file" )
                .hasArg()
                .withDescription(  "use given file for log" )
                .create( "logfile" );
		

		
		Option compressresults=OptionBuilder
				.withDescription("Compress results to a zip file")
				.withLongOpt("compress")
				.create("z");
		options.addOption(compressresults);*/
		

		
		
	}
	
	/**
	 * Parses the.
	 *
	 * @throws Exception the exception
	 */
	public void parse() throws Exception{
		try {
			CommandLineParser parser = new DefaultParser();
			CommandLine cmd= parser.parse(options,inputargs);
			
			LogMessageCenter.getLogger().enableStackTrace();
			
			if(cmd.getOptions().length==0)
				help();
			else if (cmd.hasOption("h"))
				help();
			
			else{
				
				if(cmd.hasOption("v")){
					String param=cmd.getOptionValue("v");
					MTULogLevel level=MTULogLevel.getLevelFromStringName(param);
					LogMessageCenter.getLogger().setLogLevel(level);
				}
				else
					LogMessageCenter.getLogger().setLogLevel(MTULogLevel.INFO);
				
				
				if(cmd.hasOption("conf")){
					String[] args=cmd.getOptionValues("conf");
					
					if(JBiclustGESetupManager.isJbiclustGEConfigured())
							JBiclustGESetupManager.resetPreviousConfiguration();

					if(args!=null && args.length==1) {
						String folder=null;
						if(args[0].equals("choosefolder")) {
							JFileChooser chooser=new JFileChooser();
							chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
							int tag=chooser.showSaveDialog(null);
							if(tag==JFileChooser.APPROVE_OPTION) {
								folder=chooser.getSelectedFile().getAbsolutePath();			
							}
							else
								System.exit(0);
						}
						else
							folder=args[0];
						
						JBiclustGESetupManager.setupJBiclustGEMethodsEnvironment(folder);
						
					}
					else
						JBiclustGESetupManager.setupJBiclustGEMethodsEnvironment(null);
					
				}
				
				else if(cmd.hasOption("newprofile")){
				
					Scanner scanner = new Scanner(System.in);
					
				
                    String dirpath=folderSelectionWithOptionalJFileChooser("Please, set the path of directory where the Profile will be saved: ", scanner);
                    File f=new File(dirpath);
                    if(!f.exists())
                    	f.mkdirs();
				
					
			
					EnhancedPropertiesWithSubGroups allprops=new EnhancedPropertiesWithSubGroups();
					allprops.appendProperties(getGeneExpressionFileProperties(scanner,dirpath));
					allprops.appendProperties(getBiclusteringMethodsProperties(scanner,dirpath,true));
					
					System.out.println("Do you want to perform the Gene Enrichment Analysis in Runtime [yes/y]; No I will perform later [no/n]:");
					if(checkYesNoParameter(scanner)){
						allprops.appendProperties(checkGSEATemplateProperties(scanner, dirpath,false));
						
						System.out.println("You want to define a folder to store Enrichment results? (otherwise will be saved in the same folder of the bicluster files) [yes/no (y/n)]:");
						if(checkYesNoParameter(scanner)){
					
							String gseadirpath=folderSelectionWithOptionalJFileChooser("Please set the folder path, where the Enrichment results will be stored: ", scanner);
							allprops.addPropertyToGroupCategory("Report Results",PropertiesModules.OUTPUTDIRECTORY, gseadirpath, "Directory where results will be stored");
						}

					}
					
					EnhancedPropertiesWithSubGroups extraoptions=getExtraProperties(scanner);
					if(extraoptions!=null)
						allprops.appendProperties(extraoptions);
					
					String filepath=FilenameUtils.concat(dirpath, PropertiesModules.PROFILEFILENAME);
					allprops.store(new FileWriter(filepath), true);
				
					StringBuilder strmsg=new StringBuilder();
					strmsg.append("Your configuration files are ready to execute, to run this configuration in shell, do the following: ");
					if(OSystemUtils.isLinux()) {
						File check1=new File("/usr/local/bin/jbiclustge-cli");
						if(check1.exists())
							strmsg.append("jbiclustge-cli -run "+dirpath);
						else if(new File(FilenameUtils.concat(SystemFolderTools.getCurrentDir(), "jbiclustge-cli.sh")).exists()) {
							strmsg.append("./jbiclustge-cli.sh -run "+dirpath+"\n");
							strmsg.append("or: java -jar jbiclustge-cli.jar "+dirpath);
						}
						else
							strmsg.append("java -jar jbiclustge-cli.jar "+dirpath);
					}
					else
						strmsg.append("java -jar jbiclustge-cli.jar "+dirpath);
	
				   System.out.println(strmsg.toString());
				}

				else if(cmd.hasOption("run")){
					String filepath=cmd.getOptionValue("run");
					
					if(filepath.toLowerCase().equals("choosefolder")) {
						JFileChooser chooser=new JFileChooser();
						chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						int tag=chooser.showOpenDialog(null);
						if(tag==JFileChooser.APPROVE_OPTION) {
							filepath=chooser.getSelectedFile().getAbsolutePath();			
						}
						else
							System.exit(0);
					}
					
					if(filepath!=null && !filepath.isEmpty()){
						RunBiclusteringMethodsByProfileFolder exe=new RunBiclusteringMethodsByProfileFolder(OSystemUtils.validatePath(filepath));
						exe.execute();
						if(exe.needsToCloseRsession()) {
							RConnector.closeSession();
						}
					}
					else
						System.out.println("Incorrect number of input parameters");
						
				}

				else if(cmd.hasOption("newgsea")){
					
					Scanner scannergsea = new Scanner(System.in);
					

					String input="";
					String msg="Please, set the path of directory where the configuration will be saved.";
					String dirpath=folderSelectionWithOptionalJFileChooser(msg, scannergsea);
					
					EnhancedPropertiesWithSubGroups allprops=new EnhancedPropertiesWithSubGroups();
					
					System.out.println("You want to choose input parameters now [yes/y]; No, I will edit configuration file later [no/n]");
					if(checkYesNoParameter(scannergsea)){
						
						System.out.println("Where the results are stored? In a folder of a profile that was executed [yes/y]. In other folder [no/n]");
						String dirres=null;
						if(checkYesNoParameter(scannergsea)) {
							dirres=folderSelectionWithOptionalJFileChooser("Please set the path of the folder of executed profile: ", scannergsea);
							dirres=OSystemUtils.validatePath(FilenameUtils.concat(dirres, "Results_biclustering"));
						}
						else {
							dirres=folderSelectionWithOptionalJFileChooser("Please set the path of the folder where the results are stored: ", scannergsea);
						}
			
						allprops.addPropertyToGroupCategory("Folder of results",PropertiesModules.ANALYSERESULTINFOLDER ,  OSystemUtils.validatePath(dirres), "Directory where results are stored");
						
						String filegeneexp=fileSelectionWithOptionalOpenJFileChooser("Please set the file path of the gene expression dataset, used in the biclustering analysis: ", scannergsea);
						
						allprops.addPropertyToGroupCategory("Gene Expression Dataset to Analyse", PropertiesModules.INPUTDATASETFILEPATH,  OSystemUtils.validatePath(filegeneexp), "");
					}
					else{
						allprops.appendProperties(PropertiesModules.getFilesFolderToGSEAAnalysis());
						allprops.appendProperties(PropertiesModules.getInputExpressionDatasetFileModule());
					}
					
					EnhancedPropertiesWithSubGroups gseainitprops=checkGSEATemplateProperties(scannergsea, dirpath,true);
					allprops.appendProperties(gseainitprops);
					System.out.println("You want to define a folder to store Enrichment results? (otherwise will be saved in the same folder of the bicluster files) [yes/no]:");
					if(checkYesNoParameter(scannergsea)){
						System.out.println("Define folder now [yes/y]; I will define later in configuration file [no/n] ");
						if(checkYesNoParameter(scannergsea)) {
							String folderpath=folderSelectionWithOptionalJFileChooser("Set the folder to store Enrichment results.", scannergsea);
							allprops.addPropertyToGroupCategory("Report Results",PropertiesModules.OUTPUTDIRECTORY, folderpath, "Directory where results will be stored");
						}
						else
							allprops.appendProperties(PropertiesModules.getResultsReporterModule());
					}

					
					
					String name="Biclustering_GSEA_Ontologizer_Profile.conf";
					if(gseainitprops.getProperty(PropertiesModules.GSEAPROCESSOR).equals("topgo"))
						name="Biclustering_GSEA_topGO_Profile.conf";
					
					String filepath=FilenameUtils.concat(dirpath, name);
					allprops.store(new FileWriter(filepath), true);
					
			        LogMessageCenter.getLogger().addInfoMessage("Gene set Enrichment Analysis was stored at: "+filepath);
				}
				
				
				else if(cmd.hasOption("rungsea")){
				
					String filepath=cmd.getOptionValue("rungsea");

					System.out.println(filepath);
					if(filepath.toLowerCase().equals("choosefile")) {
						JFileChooser chooser=new JFileChooser();
						int tag=chooser.showOpenDialog(null);
						if(tag==JFileChooser.APPROVE_OPTION) {
							filepath=chooser.getSelectedFile().getAbsolutePath();			
						}
						else
							System.exit(0);
					}
					
					if(filepath!=null && !filepath.isEmpty()){
					RunEnrichmentAnalysisWithMethodResults exe=new RunEnrichmentAnalysisWithMethodResults(OSystemUtils.validatePath(filepath));	
						exe.execute();
						System.out.println("Gene Enrichment Analysis was finished");
					}
						
				}
				else if(cmd.hasOption("configsynthetic")) {
					
					Scanner scanner = new Scanner(System.in);
					
					String folderdatasets=folderSelectionWithOptionalJFileChooser("Please, set the path where the synthetic datasets are stored", scanner);
					
					EnhancedPropertiesWithSubGroups allprops=new EnhancedPropertiesWithSubGroups();
					
					allprops.appendProperties(getBiclusteringMethodsProperties(scanner,folderdatasets,false));
					
					
					System.out.println("Execute synthetic analysis by each Biclustering method [yes/y] or execute randomly [no/n]:");
					
					boolean option=checkYesNoParameter(scanner);
					
					allprops.addPropertyToGroupCategory("Execute analysis by each Biclustering method", "analysis_by_method", String.valueOf(option), "Allowed options: false or true");
					
					
					
					
					
					String configpath=FilenameUtils.concat(folderdatasets, "synthetic_run_config.conf");
					allprops.store(new FileWriter(configpath), true);
					
					
				}
				else if(cmd.hasOption("runsynthetic")) {
					
					String folderdatasets=cmd.getOptionValue("runsynthetic");
					
					if(folderdatasets.toLowerCase().equals("choosefolder")) {
						JFileChooser chooser=new JFileChooser();
						chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						int tag=chooser.showOpenDialog(null);
						if(tag==JFileChooser.APPROVE_OPTION) {
							folderdatasets=chooser.getSelectedFile().getAbsolutePath();			
						}
						else
							System.exit(0);
					}
					
					if(folderdatasets!=null && !folderdatasets.isEmpty()) {
						
						
						boolean isvalid=isValidToExecuteSyntheticDatasets(folderdatasets);
						if(isvalid) {
							SyntheticEvaluationControlCenter cc=new SyntheticEvaluationControlCenter(folderdatasets);
							cc.run();
								
						}	
					}
					
				}
	
			}

	
			
		} catch (ParseException e) {
			System.out.println(e);
			help();
		}
		
	}
	
	
	private LinkedHashMap<String, String> filterDatasetFolders(String folderdatasets){
		
		LinkedHashMap<String, String> typeexperiments=new LinkedHashMap<>();
		ArrayList<String> folders=MTUDirUtils.getFilePathsInsideDirectory(folderdatasets, false, true);
		
		for (int i = 0; i < folders.size(); i++) {
			String namefolder=FilenameUtils.getBaseName(folders.get(i));
			
			if(namefolder.toLowerCase().contains("noise"))
				typeexperiments.put(folders.get(i), "noise");
			else if(namefolder.toLowerCase().contains("overlap")) {
				
				if(namefolder.toLowerCase().contains("symmetric"))
					typeexperiments.put(folders.get(i), "symmetric_overlap");
				else if(namefolder.toLowerCase().contains("asymmetric"))
					typeexperiments.put(folders.get(i), "asymmetric_overlap");
				else
					typeexperiments.put(folders.get(i), "without_perturbations");
			}
			else
				typeexperiments.put(folders.get(i), "without_perturbations");
		}
		
		return typeexperiments;
	}
	
	private boolean isValidToExecuteSyntheticDatasets(String folderdatasets) {
		boolean valid=true;
		ArrayList<String> folders=MTUDirUtils.getFilePathsInsideDirectory(folderdatasets, false, true);
		boolean algorithmsfound=false;
		for (int i = 0; i < folders.size(); i++) {
			String foldername=FilenameUtils.getBaseName(folders.get(i));
			if(foldername.toLowerCase().equals("algorithms")) {
				algorithmsfound=true;
				break;
			}
		}
		
		valid=algorithmsfound;
		
		if(!valid)
			System.out.println("The required \"algorithms\" folder with the configurations of bilcustering algorithms its missing. Please execute \"-configsynthetic\" first");
		
		
		return valid;
	}
	
	
	private String folderSelectionWithOptionalJFileChooser(String msg, Scanner scanner) {
		
		String dir=null;
		boolean valid=false;
		while (!valid) {
			System.out.println(msg+" [(optional) Press \"c\" to open file chooser dialog]");
			String input = scanner.next();
			String dirpath=null;
			if(input.toLowerCase().equals("c")) {
				JFileChooser chooser=new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int tag=chooser.showOpenDialog(null);
				if(tag==JFileChooser.APPROVE_OPTION) {
					dirpath=chooser.getSelectedFile().getAbsolutePath();			
				}
			}
			else
				dirpath=input;
			
			if(dirpath!=null && !dirpath.isEmpty()) {
				dir=OSystemUtils.validatePath(dirpath);
				valid=true;
			}	
		}
		return dir;
	}
	

	private String fileSelectionWithOptionalOpenJFileChooser(String msg, Scanner scanner) {
		
		String file=null;
		boolean valid=false;
		while (!valid) {
			System.out.println(msg+" [(optional) select \"c\" to open file chooser dialog]");
			String input = scanner.next();
			String filepath=null;
			if(input.toLowerCase().equals("c")) {
				JFileChooser chooser=new JFileChooser();
				int tag=chooser.showOpenDialog(null);
				if(tag==JFileChooser.APPROVE_OPTION) {
					filepath=chooser.getSelectedFile().getAbsolutePath();			
				}
			}
			else
				filepath=input;
			
			if(filepath!=null && !filepath.isEmpty()) {
				file=OSystemUtils.validatePath(filepath);
				valid=true;
			}	
		}
		return file;
	}
	
	
	/**
	 * Check yes no parameter.
	 *
	 * @param scanner the scanner
	 * @return true, if successful
	 */
	private boolean checkYesNoParameter(Scanner scanner){
		String input = scanner.next();
		if(input.toLowerCase().equals("y") || input.toLowerCase().equals("yes"))
			return true;
		return false;
	}
	
	
	/**
	 * Gets the gene expression file properties.
	 *
	 * @param scanner the scanner
	 * @return the gene expression file properties
	 * @throws IOException 
	 */
	private EnhancedPropertiesWithSubGroups getGeneExpressionFileProperties(Scanner scanner, String dir) throws IOException{
		
		EnhancedPropertiesWithSubGroups data=new EnhancedPropertiesWithSubGroups();
		
		String datapath=fileSelectionWithOptionalOpenJFileChooser("Set gene expression dataset file path: ", scanner);
		
		MTUFileUtils.copyFile(datapath, FilenameUtils.concat(dir, "dataset."+FilenameUtils.getExtension(datapath)));
		
		
		
		System.out.println("Dataset has missing values [yes/no (y/n)]");
		if(checkYesNoParameter(scanner)){
			
			System.out.println("Do you want to choose a missing imputation method now? [yes/y]; Let me choose later [no/n] ");
			if(checkYesNoParameter(scanner)){
				
				System.out.println("Which method (choose a number)?");
				System.out.println("1) AverageImputation");
				System.out.println("2) KMeansImputation");
				System.out.println("3) KNNImputation");
				System.out.println("4) LLSImputation");
				System.out.println("5) SVDImputation");
				System.out.println("6) ZeroValueImputation");
				System.out.println("7) I've changed my mind, I do not want any of these");
				
				int n=scanner.nextInt();
				
				String methodname=null;
				String methodtag=null;
				String methodsettings=null;
				
				switch (n) {
				case 1:
					methodname="AverageImputation";
					break;
				case 2:	
					methodname="KMeansImputation";
					methodtag=PropertiesModules.KMeansImputation;
					System.out.println("Number of clusters: ");
					int nc=scanner.nextInt();
					methodsettings=String.valueOf(nc+";");
					System.out.println("Number of runs: ");
					int nr=scanner.nextInt();
					methodsettings=methodsettings+String.valueOf(nr);
					break;
				case 3:	
					methodname="KNNImputation";
					methodtag=PropertiesModules.KNNImputation;
					System.out.println("number of neighbors used for imputation: ");
					int nn=scanner.nextInt();
					methodsettings=String.valueOf(nn);
					break;
				case 4:	
					methodname="LLSImputation";
					methodtag=PropertiesModules.LLSImputation;
					System.out.println("number of nearest neighbors used for imputation: ");
					int nei=scanner.nextInt();
					methodsettings=String.valueOf(nei);	
					break;
				case 5:	
					methodname="SVDImputation";
					methodtag=PropertiesModules.SVDImputation;
					System.out.println("number of of eigenvectors used for imputation: ");
					int nev=scanner.nextInt();
					methodsettings=String.valueOf(nev);	
					break;
				case 6:	
					methodname="ZeroValueImputation";
					break;
				default:
					break;
				}
				
				if(methodname!=null){
					data.addPropertyToSubGroupCategory("Data configuration", "Missing Value Imputation", PropertiesModules.MISSINGDATAIMPUTATIONMETHOD, methodname, "");
					if(methodtag!=null && methodsettings!=null)
						data.addPropertyToSubGroupCategory("Data configuration", "Imputation parameters", methodtag, methodsettings,"");
				}
				else
					data.addPropertyToSubGroupCategory("Data configuration", "Missing Value Imputation", PropertiesModules.MISSINGDATAIMPUTATIONMETHOD, "None", "");
			}
			else{
				data.appendProperties(PropertiesModules.getInputExpressionDatasetFileModuleWithMissingvaluesImputation());
				
			}
			
		}
			
		
		return data;
	}
	
	
	/**
	 * Gets the biclustering methods properties.
	 *
	 * @param scanner the scanner
	 * @param dir the dir
	 * @return the biclustering methods properties
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private EnhancedPropertiesWithSubGroups getBiclusteringMethodsProperties(Scanner scanner,String dir, boolean setnumbertimemethodsrun) throws IOException{
		EnhancedPropertiesWithSubGroups data=new EnhancedPropertiesWithSubGroups();

		System.out.println("Do you want to choose the biclustering methods? [yes/y]; Setup all, I will choose later [no/n] ");

		String dirmethods=FilenameUtils.concat(dir, PropertiesModules.ALGORITHMSCONFFOLDERNAME);
		MTUDirUtils.checkandsetDirectory(dirmethods);

		ArrayList<BiclusteringMethod> methodsselected=new ArrayList<>();

		if(checkYesNoParameter(scanner)){


			for (BiclusteringMethod method : BiclusteringMethod.values()) {
				if(method.isSupported()){
					System.out.println("Run "+method.getAlgorithmName()+" [yes/no (y/n)]:");
					if(checkYesNoParameter(scanner)){
						AlgorithmProperties.writeDefaultAlgorithmPropertiesToFile(dirmethods, method.getInstance(), true);
						methodsselected.add(method);
					}
				}
			}

		}
		else{

			BiclusteringUtils.writeBiclusteringMethodsConfigurationTemplate(dirmethods);
			methodsselected=BiclusteringMethod.getAllMethods();

			System.out.println("Algorithm configurations were stored in directory: "+dirmethods);
			System.out.println("\n");
			System.out.println("1) Delete the configuration templates that you don't want to execute");
			System.out.println("2) Change the parameters of the algorithm configurations (if necessary)\n\n");
		}

		data.addPropertyToGroupCategory("Biclustering Algorithms", PropertiesModules.ALGORITHMSCONFTYPE,  PropertiesModules.ALGORITHMSCONFTYPESINGLE, "Define if each algorithm have more than one configuration file");
		//data.addPropertyToGroupCategory("Biclustering Algorithms", PropertiesModules.ALGORITHMCONFIGURATIONSFOLDER,  OSystemUtils.validatePath(dirmethods), "Set the path of the folder that contains the configurations of the biclustering algorithms");


		if(setnumbertimemethodsrun) {
			System.out.println("Do you want to execute these methods more than one time? [yes/no (y/n)]: ");
			if(checkYesNoParameter(scanner)){

				for (BiclusteringMethod biclusteringMethod : methodsselected) {

					System.out.println("Number of runs for "+biclusteringMethod.getAlgorithmName()+": ");
					int n=1;
					try {
						n=scanner.nextInt();
					} catch (Exception e) {
						n=1;
					}
					data.addPropertyToSubGroupCategory("Biclustering Algorithms", "Number of runs for each algorithm", PropertiesModules.NUMBERRUNSPREFIX+biclusteringMethod.getName(), String.valueOf(n), "");

				}

			}
		}

		if(methodsselected.size()>1){
			System.out.println("Do you want to execute biclustering methods simultaneously [yes/no (y/n)]: ");
			if(checkYesNoParameter(scanner)){
				System.out.println("How many methods to execute simultaneously (please take care this could lead to instability of the system): ");
				int n=scanner.nextInt();
				data.addPropertyToGroupCategory("Concurrent Processes", PropertiesModules.SIMULTANEOUSPROCESSES, String.valueOf(n), "Number of simultaneous processes running in parallel");
			}
			else
				data.addPropertyToGroupCategory("Concurrent Processes", PropertiesModules.SIMULTANEOUSPROCESSES, "1", "Number of simultaneous processes running in parallel");

		}

		return data;
	}
	
	
	
	/**
	 * Check GSEA template properties.
	 *
	 * @param scannergsea the scannergsea
	 * @param dirpath the dirpath
	 * @return the enhanced properties with sub groups
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private EnhancedPropertiesWithSubGroups checkGSEATemplateProperties(Scanner scannergsea, String dirpath, boolean appendgseafilepath) throws IOException{
		
		System.out.print("Choose ontologizer(o) or topGO(t), [default=o]: ");
		String input = scannergsea.next();
		
		EnhancedPropertiesWithSubGroups initprops=GSEATemplates.getGSEABaseTemplateProperties();
		
		String gseafilepath=null;
		
		if(input.toLowerCase().equals("t") || input.toLowerCase().equals("topgo")){
			initprops.setProperty(PropertiesModules.GSEAPROCESSOR, "topgo");
			gseafilepath=FilenameUtils.concat(dirpath, PropertiesModules.GSEAPROCESSORTOPGOCONFIGNAME);
			if(!new File(gseafilepath).exists()){
				writeTopGoConfigurationFile(scannergsea,gseafilepath);
			}
			else{
				System.out.println("topGO configuration file already exists, do you want to override this configuration [yes/no (y/n)]:");
				if(checkYesNoParameter(scannergsea))
					writeTopGoConfigurationFile(scannergsea,gseafilepath);
				
			}
			
		}
		else{
			initprops.setProperty(PropertiesModules.GSEAPROCESSOR, "ontologizer");
			gseafilepath=FilenameUtils.concat(dirpath, PropertiesModules.GSEAPROCESSORONTOLOGIZERCONFIGNAME);
			if(!new File(gseafilepath).exists())
				OntologizerPropertiesContainer.writeCompletePropertiesFileTemplate(gseafilepath);
			else{
				System.out.println("Ontologizer configuration file already exists, do you want to override this configuration [yes/no (y/n)]:");
				if(checkYesNoParameter(scannergsea))
					OntologizerPropertiesContainer.writeCompletePropertiesFileTemplate(gseafilepath);
			}
			
			//LogMessageCenter.getLogger().addInfoMessage("Ontologizer configuration file was created in: "+gseafilepath);
		}
		
		
		System.out.println("Please, setup your settings to perform the Gene Enrichment Analysis in:  "+gseafilepath+"\n\n");
	
		if(appendgseafilepath)
			initprops.setProperty(PropertiesModules.GSEACONFIGURATIONFILE, OSystemUtils.validatePath(gseafilepath));
		
		return initprops;
	}
	
	
	/**
	 * Write top go configuration file.
	 *
	 * @param scannergsea the scannergsea
	 * @param topgoconfigfile the topgoconfigfile
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeTopGoConfigurationFile(Scanner scannergsea, String topgoconfigfile) throws IOException{
		
		System.out.println("Make template to use a custom annotation file in topgo [yes/no (y/n)]:");
		if(checkYesNoParameter(scannergsea))
			TopGoPropertiesContainer.writePropertiesFileToAnnotationFile(topgoconfigfile);
		else
			TopGoPropertiesContainer.writePropertiesFileToAnnotationDatabase(topgoconfigfile);
	
		LogMessageCenter.getLogger().addInfoMessage("topGO configuration file was created in: "+topgoconfigfile);
	}
	
	
	
	/**
	 * Gets the extra properties.
	 *
	 * @param scanner the scanner
	 * @return the extra properties
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private EnhancedPropertiesWithSubGroups getExtraProperties(Scanner scanner) throws IOException{
		EnhancedPropertiesWithSubGroups data=new EnhancedPropertiesWithSubGroups();
		boolean chosedoption=false;
		
		System.out.println("Do you want to create the parallel coordinates figures of the resulting biclusters [yes/no (y/n)]:");
		if(checkYesNoParameter(scanner)){
			data.addPropertyToSubGroupCategory("Plot bicluster results","Parallel Coordinates",PropertiesModules.MAKEPARALLELCOORD , "true", "Create the parallel coordinates figures of the resulting biclusters (true or false)");
			chosedoption=true;
		}
		
		System.out.println("Do you want to create the heat map figures of the resulting biclusters [yes/no (y/n)]:");
		if(checkYesNoParameter(scanner)){
			data.addPropertyToSubGroupCategory("Plot bicluster results","Heat Maps",PropertiesModules.MAKEHEATMAP , "true", "Create the heat map figures of the resulting biclusters (true or false)");
			chosedoption=true;
		}
		
		if(chosedoption)
			return data;
		else
		 return null;
	}
	
	
	
		/**
		 * Help.
		 */
		private void help() {
			HelpFormatter formater = new HelpFormatter();
			formater.setOptionComparator(null);
			formater.setWidth(100);
			
			if(OSystemUtils.isLinux()) {
				File check1=new File("/usr/local/bin/jbiclustge-cli");
				if(check1.exists())
					formater.printHelp("jbiclustge-cli -\"option\" [argument(s)]\n "
							+ "example: jbiclustge-cli -conf \"path of installation\"\n\n\n", options);
				else {
					formater.printHelp("jbiclustge-cli -\"option\" [argument(s)]\n "
							+ "example: jbiclustge-cli -conf \"path of installation\"\n"
							+ "or: java -jar jbiclustge-cli.jar -\"option\" [argument(s)]\n"
							+ "example: java -jar jbiclustge-cli.jar -conf \"path of installation\"\n\n\n"
							, options);
				}
			}
			else {
				formater.printHelp("jbiclustge-cli -\"option\" [argument(s)]\n "
						+ "example: jbiclustge-cli -conf \"path of installation\"\n"
						+ "or: java -jar jbiclustge-cli.jar -\"option\" [argument(s)]\n"
						+ "example: java -jar jbiclustge-cli.jar -conf \"path of installation\"\n\n\n"
						, options);
			}
		}
			
			/*formater.printHelp("java -jar jbiclustge-cli.jar -\"option\" [argument(s)]\n"
					+ "example: jbiclustge-cli.jar -conf \"path of installation\"\n\n", options);
			
			//System.exit(0);
			}*/
		


	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] args) throws Exception {
		new CommandLineInterface(args).parse();

	}

}
