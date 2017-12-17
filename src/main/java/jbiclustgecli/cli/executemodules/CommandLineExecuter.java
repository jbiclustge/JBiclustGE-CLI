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
package jbiclustgecli.cli.executemodules;

// TODO: Auto-generated Javadoc
/**
 * The Class CommandLineExecuter.
 */
public abstract class CommandLineExecuter{
	
	/** The validexecution. */
	private boolean validexecution=true;

	
	/**
	 * Needs configuration.
	 *
	 * @return true, if successful
	 */
	protected abstract boolean needsConfiguration();
	
	/**
	 * Configure.
	 *
	 * @return true, if successful
	 * @throws Exception the exception
	 */
	protected abstract boolean configure() throws Exception;
	
	/**
	 * Run executer.
	 *
	 * @throws Exception the exception
	 */
	protected abstract void runExecuter() throws Exception;
	
	
	/**
	 * Execute.
	 *
	 * @throws Exception the exception
	 */
	public void execute() throws Exception{
		if(needsConfiguration())
			validexecution=configure();
		
		if(validexecution)
			runExecuter();
	}
	
	
	/**
	 * Needs to close rsession.
	 *
	 * @return true, if successful
	 */
	public abstract boolean needsToCloseRsession();


}
