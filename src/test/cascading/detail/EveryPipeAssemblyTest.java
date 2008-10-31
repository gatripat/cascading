/*
 * Copyright (c) 2007-2008 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.cascading.org/
 *
 * This file is part of the Cascading project.
 *
 * Cascading is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cascading is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cascading.  If not, see <http://www.gnu.org/licenses/>.
 */

package cascading.detail;

import java.util.Properties;

import cascading.pipe.Pipe;
import junit.framework.Test;
import junit.framework.TestSuite;

/** @version : IntelliJGuide,v 1.13 2001/03/22 22:35:22 SYSTEM Exp $ */
public class EveryPipeAssemblyTest extends PipeAssemblyTestBase
  {

  public static Test suite() throws Exception
    {
    TestSuite suite = new TestSuite();

    Properties properties = loadProperties( "op.properties" );
    makeSuites( properties, buildOpPipes( properties, null, new Pipe( "every" ), new EveryAssemblyFactory(), OP_ARGS_FIELDS, OP_DECL_FIELDS, OP_SELECT_FIELDS, OP_VALUE ), suite, EveryPipeAssemblyTest.class );

    return suite;
    }

  public EveryPipeAssemblyTest( Properties properties, String name, Pipe pipe )
    {
    super( properties, name, pipe );
    }
  }