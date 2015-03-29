/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

This program is free software; you can redistribute it and/or 
modify it under the terms of the GNU General Public License 
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful, 
but WITHOUT ANY WARRANTY; without even the implied warranty of 
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
General Public License for more details.

You should have received a copy of the GNU General Public License 
along with this program; if not, write to the Free Software 
Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
MA 02111-1307 USA

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
package ca.ubc.tyruba.tod;

import java.lang.reflect.Constructor;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.structure.IMutableStructureDatabase;
import tod.impl.database.structure.standard.HostInfo;
import tod.impl.database.structure.standard.ThreadInfo;
import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.EventGenerator;
import tod.impl.dbgrid.Fixtures;
import tod.impl.dbgrid.GridLogBrowser;
import tod.impl.dbgrid.GridMaster;
import tyRuBa.applications.CommandLine;
import tyRuBa.engine.FrontEnd;
import tyRuBa.engine.Implementation;
import tyRuBa.engine.ModedRuleBaseIndex;
import tyRuBa.engine.NativePredicate;
import tyRuBa.engine.RBTerm;
import tyRuBa.engine.TyRuBaConf;
import tyRuBa.modes.Type;
import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;
import tyRuBa.tdbc.Connection;

public class Main {
	

	public static ILogBrowser setupTOD() {
		GridMaster theMaster;
		IMutableStructureDatabase theStructureDatabase;
		GridLogBrowser theLogBrowser;
		theMaster = Fixtures.setupLocalMaster();
		theStructureDatabase = theMaster.getStructureDatabase();
//		theStructureDatabase.clear();
		
		for (int i=1;i<=100;i++) 
		{
			HostInfo theHostInfo = new HostInfo(i, ""+i);
			theMaster.registerHost(theHostInfo);
			
			for (int j=1;j<=100;j++)
			{
				theMaster.registerThread(new ThreadInfo(theHostInfo, j, j, ""+j));
			}
			
//			IMutableClassInfo theClass = theStructureDatabase.getNewClass("C"+i);
//			theClass.getNewBehavior("m"+i, "()V", false);
//			theClass.getNewField("f"+i, PrimitiveTypeInfo.BOOLEAN, false);
		}
		theLogBrowser = DebuggerGridConfig.createLocalLogBrowser(null, theMaster);

		EventGenerator theEventGenerator = createEventGenerator(theStructureDatabase);
		theEventGenerator.fillStructureDatabase(theStructureDatabase);
		
		System.out.println("filling...");
		Fixtures.fillDatabase(theMaster, theEventGenerator, 100);
		
		return theLogBrowser;
	}
	
	public static void main(String[] args) throws TypeModeError, ParseException {
		FrontEnd fe = new FrontEnd(new TyRuBaConf());
		Connection conn = new Connection(fe);
		TodPredicate.setTodDatabase(setupTOD());
		TodPredicate.defineTodPredicates(fe);
		
		fe.parse(":- behaviorCall(?ts, ?id, ?E).");
		fe.parse(":- behaviorCall(?ts, 55, ?E).");
		fe.parse(":- behaviorCall(?ts, ?id, ?E), greater(?ts, 1500).");
		
		System.exit(0);
		
//		CommandLine repl = new CommandLine();
//		repl.frontend = fe;		
//		repl.realmain(new String[] {"-i"});
	}
	
	static EventGenerator createEventGenerator(IMutableStructureDatabase aStructureDatabase)
	{
		try
		{
			Class theClass = DebuggerGridConfig.getDbImpl().getClass("EventGenerator");
			Constructor theConstructor = theClass.getConstructor(
					IMutableStructureDatabase.class,
					long.class, int.class, int.class,
					int.class, int.class, int.class,
					int.class, int.class, int.class,
					int.class);
			
			return (EventGenerator) theConstructor.newInstance(aStructureDatabase, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	
}
