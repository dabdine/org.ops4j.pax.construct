package org.ops4j.pax.construct;

/*
 * Copyright 2007 Stuart McCulloch
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static org.ops4j.pax.construct.PomUtils.readPom;
import static org.ops4j.pax.construct.PomUtils.writePom;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;

/**
 * Foundation for all OSGi sub-project goals that use archetypes.
 */
public abstract class AbstractChildArchetypeMojo extends AbstractArchetypeMojo
{
    private static boolean seenRootProject = false;

    /**
     * Should we attempt to overwrite entries.
     * 
     * @parameter expression="${overwrite}" default-value="false"
     */
    private boolean overwrite;

    /**
     * The newly generated POM file - this is set in the _root_ project execution
     */
    protected static File childPomFile;

    protected boolean checkEnvironment()
        throws MojoExecutionException
    {
        // only create files under primary root project
        if ( seenRootProject || project.getParent() != null )
        {
            return false;
        }

        seenRootProject = true;
        return true;
    }

    protected void setChildProjectName( final String childProjectName )
        throws MojoExecutionException
    {
        // This somehow forces Maven to keep POM formatting & XSD
        File dir = new File( targetDirectory, childProjectName );
        dir.mkdir();

        childPomFile = new File( dir, "pom.xml" );

        // update parent modules
        linkParentToChild();
    }

    protected void linkParentToChild()
        throws MojoExecutionException
    {
        try
        {
            final String childName = childPomFile.getParentFile().getName();

            Document parentPom = readPom( project.getFile() );

            Element projectElem = parentPom.getElement( null, "project" );
            PomUtils.addModule( projectElem, childName, overwrite );

            writePom( project.getFile(), parentPom );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Unable to link parent pom to child", e );
        }
    }

    protected void linkChildToParent()
        throws MojoExecutionException
    {
        try
        {
            Document childPom = readPom( childPomFile );

            Element projectElem = childPom.getElement( null, "project" );
            PomUtils.setParent( projectElem, project, overwrite );

            writePom( childPomFile, childPom );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Unable to link child pom to parent", e );
        }
    }
}
