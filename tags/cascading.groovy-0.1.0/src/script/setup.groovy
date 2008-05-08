#!/usr/bin/env groovy

/*
 * Copyright (c) 2007-2008 Chris K Wensel. All Rights Reserved.
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

println("Installing Cascading Groovy Shell Extensions")

def installRoot = "${System.properties[ "user.home" ]}/.groovy"

File groovyHome = new File(installRoot);
File groovyLib = new File(groovyHome, "lib");

println "  installing to: ${groovyHome}"

File groovyProfile = new File(groovyHome, 'groovysh.profile')

def ant = new AntBuilder()

groovyLib.mkdirs();

println "  copying files to: ${groovyLib}"

// remove older jars, catch all cascading.jars
// need to track a manifest instead
ant.delete()
  {
    fileset(dir: groovyLib, includes: "cascading*.jar")
  }

ant.copy(todir: groovyLib)
  {
    fileset(dir: "./lib")
      {
        include(name: "*.jar")
      }
  }

println "Done"