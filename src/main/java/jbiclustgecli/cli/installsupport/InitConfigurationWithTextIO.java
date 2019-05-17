package jbiclustgecli.cli.installsupport;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.swing.JFileChooser;

import org.apache.commons.io.FilenameUtils;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextTerminal;
import org.beryx.textio.console.ConsoleTextTerminal;
import org.beryx.textio.swing.SwingTextTerminal;
import org.javatuples.Pair;

import jbiclustge.utils.osystem.CustomRInstallationManager;
import jbiclustge.utils.osystem.JBiclustGESetupManager;
import jbiclustge.utils.osystem.SystemFolderTools;
import jbiclustge.utils.osystem.CustomRInstallationManager.RenvVersion;
import jbiclustge.utils.osystem.progcheck.RInstallerProgressionChecker;
import jbiclustge.utils.osystem.progcheck.RInstallerProgressionCheckerTextIO;
import jbiclustge.utils.props.JBiGePropertiesManager;
import jbiclustgecli.cli.CommandLineInterfaceTextIO;
import pt.ornrocha.fileutils.MTUDirUtils;
import pt.ornrocha.fileutils.MTUFileUtils;
import pt.ornrocha.logutils.MTULogLevel;
import pt.ornrocha.logutils.messagecomponents.LogMessageCenter;
import pt.ornrocha.rtools.installutils.RInstallTools;
import pt.ornrocha.systemutils.OSystemUtils;
import pt.ornrocha.systemutils.linuxshell.MTUShellLinux;
import pt.ornrocha.systemutils.linuxshell.listeners.TextIOProgressShellListener;

public class InitConfigurationWithTextIO {
	
	
	private static String INSTALLNEW="Start installation";
	private static String CHECKREQUIREMENTS="Verify if that all requirements for installation are set";
	private static String QUIT="Quit";
	
	private static String INSTALLLOCALVERSION="Install a new local R environment";
	private static String USELOCALVERSION="Use a local R environment installed previously";
	
	private static String COMPILELINUXLIBS="Compile all libs necessary to build R environment";
	
	
	public static boolean configureJbiclustGeEnvironment(TextIO terminal) throws Exception {
		LogMessageCenter.getLogger().setLogLevel(MTULogLevel.OFF);

		if(terminal==null)
			terminal = CommandLineInterfaceTextIO.getNewTerminal();


		terminal.getTextTerminal().println("#########################################");
		terminal.getTextTerminal().println("          Welcome to JBiclustGe          ");
		terminal.getTextTerminal().println("                                         ");
		terminal.getTextTerminal().println("            Lets configure it            ");
		terminal.getTextTerminal().println("#########################################");

		terminal.getTextTerminal().println();

		boolean install=true;
		boolean installR=true;
		boolean statusR=true;

		String R_PATH=null;
		String R_Libs_Path=null;

		String beginopt =null;

		if(OSystemUtils.isLinux())
			beginopt = terminal.newStringInputReader()
			.withNumberedPossibleValues(INSTALLNEW, CHECKREQUIREMENTS,QUIT)
			.read("Choose one of the options:");
		else
			beginopt = terminal.newStringInputReader()
			.withNumberedPossibleValues(INSTALLNEW, QUIT)
			.read("Choose one of the options:");

		terminal.getTextTerminal().println("\n");
		if(beginopt.equals(CHECKREQUIREMENTS)) {
			verifyRequirements(terminal.getTextTerminal());

			install=terminal.newBooleanInputReader().read("Proceed installation?");
		}
		else if(beginopt.equals(QUIT))
			System.exit(0);
		else
			terminal.getTextTerminal().println("Starting installation process.\n");


		if(install) {
			//System.out.println("Checking if R environment is installed.");
			String rpath=null;

			try {
				rpath=RInstallTools.getSystemR_PATH();
			} catch (Exception e) {
				LogMessageCenter.getLogger().toClass(InitConfigurationWithTextIO.class).addCriticalErrorMessage(e);
			}

			if(OSystemUtils.isWindows()) {

				if(rpath==null) {
					terminal.getTextTerminal().println("****** The R environment and the Rtools were not detected in this computer *****\n"
							+ "****** Several features will not be available without these packages of R  *****\n"
							+ "****** Get R from  https://www.r-project.org/                              *****\n"
							+ "****** Get Rtools from  https://cran.r-project.org/bin/windows/Rtools/     *****\n"
							+ "********************************************************************************\n\n");

					install=terminal.newBooleanInputReader().read("Proceed with the installation?");
					if(install) {
						statusR=false;
					}
					else
						System.exit(0);
				}
				



			}
			else if(OSystemUtils.isLinux()) {

				String rinstallopt=null;
				String installsysR="Use R environment installed in the system: "+rpath;

				if(rpath!=null)
					rinstallopt = terminal.newStringInputReader()
					.withNumberedPossibleValues(installsysR,INSTALLLOCALVERSION, USELOCALVERSION)
					.read("Choose one of the options:");

				else
					rinstallopt = terminal.newStringInputReader()
					.withNumberedPossibleValues(INSTALLLOCALVERSION, USELOCALVERSION)
					.read("Choose one of the options:");

				terminal.getTextTerminal().println("\n");

				if(rinstallopt.equals(INSTALLLOCALVERSION)) {

					boolean installonlyR=true;

					ArrayList<String> libsneeded=checkMandatoryLinuxLibs();
					if(libsneeded.size()>0) {
						libsneeded.addAll(JBiclustGESetupManager.checkMissingDevLinuxLibs());

						terminal.getTextTerminal().println("The following libraries must be installed to compile R and their packages and have all the features working properly:\n"+libsneeded+"\n\n");
						install=terminal.newBooleanInputReader().read("You will need sudo privileges, do you want to continue installation?");
						if(!install)
							System.exit(0);
						else {		

							String[] installlibscmd=getLinuxInstallLibsCmd(libsneeded);
							TextIOProgressShellListener proclist=(TextIOProgressShellListener) new TextIOProgressShellListener(terminal);//.checkForErrors("No rule to make target 'install'.  Stop.");

							terminal.getTextTerminal().println("\nInstalling libs required to compile R, please wait...\n");
							if(terminal.getTextTerminal() instanceof ConsoleTextTerminal) {
								boolean showout=terminal.newBooleanInputReader().read("These libraries will be installed, do you want to see the installation progress?");
								if(showout)
									proclist.showInfoOutput(true);
							}

							try {
								installR = MTUShellLinux.executeCMDsAsSudoTextIOTerminal(terminal, proclist, installlibscmd);
								
								boolean xorgdevinstalled=JBiclustGESetupManager.checkIfX11HeadersInstalled();
								if(!xorgdevinstalled) {
									boolean installxorgdev=terminal.newBooleanInputReader().read("Is advisable to install xorg-dev library, do you want to instal?");
									if(installxorgdev) {
										ArrayList<String> xorgdev=new ArrayList<>();
										xorgdev.add("xorg-dev");
										TextIOProgressShellListener installlistener=(TextIOProgressShellListener) new TextIOProgressShellListener(terminal);
										boolean okinstall=MTUShellLinux.executeCMDsAsSudoTextIOTerminal(terminal, installlistener, getLinuxInstallLibsCmd(xorgdev));
										if(!okinstall)
											terminal.getTextTerminal().println("An error occurred during the installation of xorg-dev");
									}
								}
								
							} catch (Exception e) {
								LogMessageCenter.getLogger().addCriticalErrorMessage(e);
								statusR=false;
								installR=false;
							}

							if(!installR) {
								install=terminal.newBooleanInputReader().read("An error occurred during the installation of the required linux libraries to compile R! Do you want to proceed?");
								if(!install)
									System.exit(0);
							}

						}
					}
					else {

						ArrayList<String> linuxdevlibs=JBiclustGESetupManager.checkMissingDevLinuxLibs();

						if(linuxdevlibs.size()>0) {

							String INSTALLMISSDEVLIBS="Install all "+linuxdevlibs.size()+" development libs that are needed (You will need sudo privileges)";

							rinstallopt = terminal.newStringInputReader()
									.withNumberedPossibleValues(COMPILELINUXLIBS, INSTALLMISSDEVLIBS)
									.read("Installation of the required libraries to compile R, choose one of the options:");

							if(rinstallopt.equals(COMPILELINUXLIBS))
								installonlyR=false;
							else {

								TextIOProgressShellListener proclist=(TextIOProgressShellListener) new TextIOProgressShellListener(terminal);

								terminal.getTextTerminal().println("\nInstalling libs required to compile R, please wait...\n");
								if(terminal.getTextTerminal() instanceof ConsoleTextTerminal) {
									boolean showout=terminal.newBooleanInputReader().read("Required libs will be installed, do you want to see the installation progress?");
									if(showout)
										proclist.showInfoOutput(true);
								}
								try {
									installR = MTUShellLinux.executeCMDsAsSudoTextIOTerminal(terminal, proclist, getLinuxInstallLibsCmd(linuxdevlibs));
									
									boolean xorgdevinstalled=JBiclustGESetupManager.checkIfX11HeadersInstalled();
									if(!xorgdevinstalled) {
										boolean installxorgdev=terminal.newBooleanInputReader().read("Is advisable to install xorg-dev library, do you want to instal?");
										if(installxorgdev) {
											ArrayList<String> xorgdev=new ArrayList<>();
											xorgdev.add("xorg-dev");		
											TextIOProgressShellListener installlistener=(TextIOProgressShellListener) new TextIOProgressShellListener(terminal);
											boolean okinstall=MTUShellLinux.executeCMDsAsSudoTextIOTerminal(terminal, installlistener, getLinuxInstallLibsCmd(xorgdev));
											if(!okinstall)
												terminal.getTextTerminal().println("An error occurred during the installation of xorg-dev");
										}
									}
								} catch (Exception e) {
									LogMessageCenter.getLogger().addCriticalErrorMessage(e);
									statusR=false;
									installR=false;
								}

								if(!installR) {
									install=terminal.newBooleanInputReader().read("An error occurred during the installation of the required libraries! Do you want to proceed? (Y) Try to compile them, (N) Quit");
									if(!install)
										System.exit(0);
									else {
										installonlyR=false;
										installR=true;
									}
								}

							}
						}
						else
							installonlyR=true;

					}

					if(installR) {

						TextIOProgressShellListener rinstalllistener=(TextIOProgressShellListener) new TextIOProgressShellListener(terminal)
								.checkForErrors("No rule to make target 'install'.")
								.checkForErrors("recipe for target 'install' failed");


						RenvVersion rversion =terminal.newEnumInputReader(RenvVersion.class).withAllValuesNumbered().withDefaultValue(RenvVersion.R_3_4_3).read("Which R version do you want to install?");
						
						int nproc=terminal.newIntInputReader().withDefaultValue(1).read("How many concurrent compiling threads do you want to run in parallel?");

						CustomRInstallationManager installer=new CustomRInstallationManager(rversion,nproc).addProgressListener(rinstalllistener);
						ArrayList<Pair<RenvVersion, String>> currentcustomversion=CustomRInstallationManager.checkIfAnyCustomVersionItsInstalled();

						if(currentcustomversion!=null) {

							for (Pair<RenvVersion, String> pair : currentcustomversion) {
								RenvVersion installedversion=pair.getValue0();
								if(installedversion.equals(rversion)) {

									boolean newinstall=terminal.newBooleanInputReader().read("The same R version is already installed, do you want to install a new one?");
									if(!newinstall) {
										statusR=true;
										installR=false;
										R_PATH=pair.getValue1();
										R_Libs_Path=installer.getFolderToRpackages();
									}
									else {
										statusR=true;
										installR=true;
										MTUDirUtils.deleteDirectory(installer.getRootDir());
									}
									break;
								}
							}
						}


						if(installR) {

							terminal.getTextTerminal().println("Installing R environment "+rversion.getVersion()+" and it will take a while, please wait...\n");

							if(terminal.getTextTerminal() instanceof ConsoleTextTerminal) {
								boolean showout=terminal.newBooleanInputReader().read("Do you want to see the installation progress?");
								if(showout)
									rinstalllistener.showInfoOutput(true);
							}

							boolean xorgnotsupport=!JBiclustGESetupManager.checkIfX11HeadersInstalled();

							if(installonlyR) {
								JBiclustGESetupManager.removeLibsExportFeaturesConfig();
								statusR=installer.installOnlyR().X11NotSupported(xorgnotsupport).install();
								if(statusR) {
									R_PATH=installer.getR_Path();
									R_Libs_Path=installer.getFolderToRpackages();
								}
							}
							else {

								statusR=installer.X11NotSupported(xorgnotsupport).install();
								if(statusR) {
									R_PATH=installer.getR_Path();
									R_Libs_Path=installer.getFolderToRpackages();
									JBiclustGESetupManager.addLibsExportFeaturesConfig(installer.getRootDir());
									try {
										JBiclustGESetupManager.addLocalLinuxLibsTag(installer.getRootDir());
									} catch (IOException e) {
										LogMessageCenter.getLogger().addCriticalErrorMessage(e);
									}
								}	
							}

							if(!rinstalllistener.isValidProcessExecution()) {
								terminal.getTextTerminal().println("\nThe installation of R was not successful");
							}
							else
								terminal.getTextTerminal().println("\nR has been installed successfully.");
						}
					}

				}
				else if(rinstallopt.equals(USELOCALVERSION)) {

					String OPENLOCATIONWITHCHOOSER="Open location using a file chooser";
					String WRITELOCALPATHLOCATION="I will give the path to the folder containing a local R environment, installed previously";

					String installlocalr = terminal.newStringInputReader()
							.withNumberedPossibleValues(OPENLOCATIONWITHCHOOSER,WRITELOCALPATHLOCATION)
							.read("Choose one of the options:");


					String localRenvDir=null;

					if(installlocalr.equals(OPENLOCATIONWITHCHOOSER)) {

						try {
							JFileChooser chooser=new JFileChooser();
							chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
							int tag=chooser.showOpenDialog(null);
							if(tag==JFileChooser.APPROVE_OPTION) {
								localRenvDir=chooser.getSelectedFile().getAbsolutePath();			
							}
						}catch (Exception e) {
							terminal.getTextTerminal().println("*** Is not possible to open a FileChooser ***");
							localRenvDir=terminal.newStringInputReader().withMinLength(1).read("\nPlease, write the absolute path to the folder containing a local R environment");
						}
					}
					else if(installlocalr.equals(WRITELOCALPATHLOCATION)) {
						localRenvDir=terminal.newStringInputReader().withMinLength(1).read("\nWrite the absolute path to the folder containing a local R environment");
					}

					if(localRenvDir!=null) {

						R_PATH=JBiclustGESetupManager.getMostLikelyREnvDir(localRenvDir);
						R_Libs_Path=JBiclustGESetupManager.getMostLikelyREnvFolderToRpackages(localRenvDir);
						if(R_Libs_Path!=null && !new File(R_Libs_Path).exists())
							new File(R_Libs_Path).mkdirs();

						if(R_PATH!=null) {
							String rootfolder=JBiclustGESetupManager.getRLocalRootFolderFromPath(R_PATH);
							if(JBiclustGESetupManager.usesLocalCompiledLinuxLibs(rootfolder))
								JBiclustGESetupManager.addLibsExportFeaturesConfig(JBiclustGESetupManager.getRLocalRootFolderFromPath(R_PATH));
						}
					}

				}
				else {

					ArrayList<String> libsneeded=JBiclustGESetupManager.checkMissingDevLinuxLibs();

					if(libsneeded.size()>0) {
						terminal.getTextTerminal().println("\nIt is advisable to install the following libs: "+libsneeded+"\n");

						boolean installlibs=terminal.newBooleanInputReader().read("If you not install them, errors could be raised during compiling process of some R packages.\n"
								+ "However, You will need sudo privileges! Do you want to install them?");

						if(installlibs) {

							TextIOProgressShellListener proclist=(TextIOProgressShellListener) new TextIOProgressShellListener(terminal);
							if(terminal.getTextTerminal() instanceof ConsoleTextTerminal) {
								boolean showout=terminal.newBooleanInputReader().read("Required libs will be installed, do you want to see the installation progress?");
								if(showout)
									proclist.showInfoOutput(true);
							}
							try {
								statusR = MTUShellLinux.executeCMDsAsSudoTextIOTerminal(terminal, proclist, getLinuxInstallLibsCmd(libsneeded));
							} catch (Exception e) {
								LogMessageCenter.getLogger().addCriticalErrorMessage(e);
								statusR=true;
							}
						}
						else {
							statusR=true;
						}
					}

					JBiclustGESetupManager.removeLibsExportFeaturesConfig();
					R_PATH=RInstallTools.getSystemR_PATH();
					String libsfolder=JBiclustGESetupManager.checkRLibsInstallPath(R_PATH);
					boolean opt=terminal.newBooleanInputReader().read("The R packages will be placed to:\n"+libsfolder+
							"\n Do you want to change folder?");

					if(opt) {
						String newlocalpackages=terminal.newStringInputReader().withMinLength(1).read("\nWrite the absolute path to a folder where you want to install the R packages");
						R_Libs_Path=newlocalpackages;
					}
					else
						R_Libs_Path=libsfolder;
				}	
			}





			try {
				if(R_PATH==null) 
					R_PATH=RInstallTools.getSystemR_PATH();
			} catch (Exception e) {
				LogMessageCenter.getLogger().addCriticalErrorMessage(e);
			}

			if(R_PATH==null && install && statusR==false) {
				install=terminal.newBooleanInputReader().read("An error occurred during the installation of R! Do you want to proceed?");
				if(!install)
					System.exit(0);
			}

			if(R_PATH==null)
				statusR=false;


			if(JBiclustGESetupManager.isJbiclustGEConfigured()) 
				JBiclustGESetupManager.resetPreviousConfiguration();
			//System.out.println(rinstallopt);


			if(install) {
				//try {

				boolean executablesstate=true;


				if(!JBiclustGESetupManager.checkIfAllExecutablesInstalled()) {

					String algorithmszipfile=null;

					String DOWNLOADEXECUTABLES="Download biclustering algorithms from JBiclustGE website";
					String OPENZIPFILELOCATION="Open a file chooser (so I can choose the zip file)";
					String WRITEPATHLOCATION="I will give the path to zip file";

					String zipinstallopt = terminal.newStringInputReader()
							.withNumberedPossibleValues(DOWNLOADEXECUTABLES, OPENZIPFILELOCATION,WRITEPATHLOCATION)
							.read("File with biclustering algorithms, choose one of the options:");


					if(zipinstallopt.equals(OPENZIPFILELOCATION)) {

						try {
							JFileChooser chooser=new JFileChooser();
							chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
							int tag=chooser.showOpenDialog(null);
							if(tag==JFileChooser.APPROVE_OPTION) {
								algorithmszipfile=chooser.getSelectedFile().getAbsolutePath();			
							}
						}catch (Exception e) {
							terminal.getTextTerminal().println("*** Is not possible to open a FileChooser ***");
							algorithmszipfile=terminal.newStringInputReader().withMinLength(1).read("\nPlease, write absolute path to the zip file");
						}
					}
					else if(zipinstallopt.equals(WRITEPATHLOCATION)) {
						algorithmszipfile=terminal.newStringInputReader().withMinLength(1).read("Write absolute path to the zip file");
					}

					try {

						if(algorithmszipfile!=null) {
							String currentfolder=SystemFolderTools.getCurrentDir();
							String zipfilename;

							zipfilename = JBiclustGESetupManager.getRequiredZipFileName();

							String zipfilepath=FilenameUtils.concat(currentfolder, zipfilename+".zip");
							MTUFileUtils.copyFile(algorithmszipfile, zipfilepath);
						}

						JBiclustGESetupManager.createJBiclustGEStatus();


						JBiclustGESetupManager.setupExecutableBiclusteringMethods();

						terminal.getTextTerminal().println("\nBiclustering algorithms were successfully installed\n");
					} catch (Exception e) {
						executablesstate=terminal.newBooleanInputReader().read("An error occurred during installation of the executables of the necessary biclustering algorithms! Do you want to proceed?");
						if(!executablesstate)
							System.exit(0);
						else
							executablesstate=true;
					}
				}
				else {
					JBiclustGESetupManager.createJBiclustGEStatus();
					
					ArrayList<String> msg=new ArrayList<>();
					ArrayList<String> execnames=JBiclustGESetupManager.getAlgorithmExecutableNames();
					for (int i = 0; i < execnames.size(); i++) {
						String name=execnames.get(i);
						msg.add(name+" = installed");
						LogMessageCenter.getLogger().toClass(InitConfigurationWithTextIO.class).addInfoMessage(name+" = installed");
					}
					
					try {
						MTUFileUtils.SaveorAppendArrayLisToFile(JBiclustGESetupManager.getStatusFile().getAbsolutePath(), msg, true);
					} catch (IOException e) {
		                LogMessageCenter.getLogger().toClass(InitConfigurationWithTextIO.class).addCriticalErrorMessage(e);
					}
				}

  

				String rpackageslib=null;

				if(statusR && executablesstate && !R_PATH.equals(RInstallTools.NONE_R_HOME)) {

					if(R_PATH!=null)
						OSystemUtils.setEnvVariable("R_HOME", R_PATH);

					try {

						RInstallerProgressionCheckerTextIO infolistn=new RInstallerProgressionCheckerTextIO(terminal);

						terminal.getTextTerminal().println("\nInstalling the required R packages, please wait...\n");
						if(terminal.getTextTerminal() instanceof ConsoleTextTerminal) {
							boolean showout=terminal.newBooleanInputReader().read("Do you want to see the progress of installing the R packages?");
							infolistn.showInformation(showout);
						}

						JBiclustGESetupManager.settingscontainer.addSetting(RInstallerProgressionChecker.RINSTALLERPROGRESSCHECK, infolistn);

						rpackageslib=JBiclustGESetupManager.setupREnvironmentPackages(R_PATH,R_Libs_Path,false);

						if(JBiclustGESetupManager.settingscontainer.containsSetting(RInstallerProgressionChecker.RPACKAGESFAILED)) {
							LinkedHashMap<String, ArrayList<String>> errorpackages=(LinkedHashMap<String, ArrayList<String>>) JBiclustGESetupManager.settingscontainer.getSetting(RInstallerProgressionChecker.RPACKAGESFAILED);

							if(errorpackages.size()>0) {

								terminal.getTextTerminal().println("\nThe installation of the following R packages were not successful:\n");
								for (String packname : errorpackages.keySet()) {
									terminal.getTextTerminal().println("Package "+packname+" and dependencies "+errorpackages.get(packname)+"\n");
								}

								//terminal.newBooleanInputReader().read("\nDo you want to save these ");

							}

							//Workbench.getInstance().error("The following packages were not installed: "+failedpackages);
							JBiclustGESetupManager.settingscontainer.removeSetting(RInstallerProgressionChecker.RPACKAGESFAILED);
						}
					} catch (Exception e) {
						LogMessageCenter.getLogger().addCriticalErrorMessage(e);
					}

				}


				try {
					if(statusR || executablesstate) {
						JBiclustGESetupManager.setupJBiclustGEProperties(rpackageslib,R_PATH);
						JBiclustGESetupManager.addConfiguredTag();
						JBiclustGESetupManager.addInstallationDoneTag();
						JBiGePropertiesManager.getManager().reload();

						terminal.getTextTerminal().println("\nJBiclustGE was successfully configured. Now you can execute biclustering algorithms.");
						if(terminal.getTextTerminal() instanceof SwingTextTerminal) {
							boolean closeterm=terminal.newBooleanInputReader().read("Do you want to close terminal?");
							if(closeterm)
								terminal.dispose();
						}
						
						return true;
					}
					else {
						terminal.getTextTerminal().println("\n\nJBiclustGE could not be configured due to the lack of the necessary algorithms and R environment\n");

					}
				} catch (Exception e) {
					LogMessageCenter.getLogger().addCriticalErrorMessage(e);
				}



			}
			else
				System.exit(0);

		}
		else
			terminal.getTextTerminal().println("\nInstallation process aborted.");

		return false;
	}
	
	

	private static String[] getLinuxInstallLibsCmd(ArrayList<String> installlibs) {
		
		String[] cmds=new String[installlibs.size()+4];
		cmds[0]="apt-get";
		cmds[1]="install";
		cmds[2]="-f";
		cmds[3]="-y";
		for (int i = 0; i < installlibs.size(); i++) {
			cmds[i+4]=installlibs.get(i);
		}
		
		return cmds;
	}
	
	
	private static void verifyRequirements(TextTerminal<?> terminal) {

		StringBuilder str=new StringBuilder();

		try {
			String rinstalled= RInstallTools.getSystemR_HOME();
			if(rinstalled==null) {
				str.append("R environment needs to be installed\n\n");
			}


			if(OSystemUtils.isLinux()) {
				ArrayList<String> packageneeds=checkMissingRequirements();
				if(packageneeds.size()>0) {
					str.append("Linux Libraries that must be installed: \n");
					for (String string : packageneeds) {
						str.append(string+"\n");
					}
				}

			}

		}catch (Exception e) {

		}
		
		if(str.length()==0)
			terminal.println("All requirements are fulfilled\n");
		else
			terminal.print(str.toString()+"\n");

	}
	
	
	private static ArrayList<String> checkMandatoryLinuxLibs() throws Exception{

		ArrayList<String> mandatorylibs=new ArrayList<>();

		LinkedHashMap<String, Boolean> cp= JBiclustGESetupManager.checkIfCompilingToolsInstalled();

		for (String compt : cp.keySet()) {
			boolean state=cp.get(compt);
			if(!state)
				mandatorylibs.add(compt);
		}

		LinkedHashMap<String, Boolean> mp= JBiclustGESetupManager.checkIfMandatoryLibrariesInstalled();

		for (String pack : mp.keySet()) {
			boolean state=mp.get(pack);
			if(!state)
				mandatorylibs.add(JBiclustGESetupManager.getDebSystemRelatedPackage(pack));
		}


		if(!JBiclustGESetupManager.checkIfFortranInstalled())
			mandatorylibs.add("gfortran");

		return mandatorylibs;
	}
	

	private static ArrayList<String> checkMissingRequirements() throws Exception {
		
		ArrayList<String> packagesmissing=checkMandatoryLinuxLibs();
	    packagesmissing.addAll(JBiclustGESetupManager.checkMissingDevLinuxLibs());
		
		return packagesmissing;
	}
	
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
