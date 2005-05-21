/**
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * Licensed under the Common Development and Distribution License,
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.sun.com/cddl/
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.sun.facelets;

import java.io.IOException;

import javax.el.ELException;
import javax.faces.FacesException;

/**
 * FaceletFactory for producing Facelets relative to the context of the
 * underlying implementation.
 * 
 * @author Jacob Hookom
 * @version $Id: FaceletFactory.java,v 1.1 2005-05-21 17:55:00 jhook Exp $
 */
public abstract class FaceletFactory {

    private static FaceletFactory Instance = null;

    /**
     * Return a Facelet instance as specified by the file at the passed URI.
     * 
     * @param uri
     * @return
     * @throws IOException
     * @throws FaceletException
     * @throws FacesException
     * @throws ELException
     */
    public abstract Facelet getFacelet(String uri) throws IOException,
            FaceletException, FacesException, ELException;

    /**
     * Set the static instance
     * 
     * @param factory
     */
    public static final void setInstance(FaceletFactory factory) {
        Instance = factory;
    }

    /**
     * Get the static instance
     * 
     * @return
     */
    public static final FaceletFactory getInstance() {
        return Instance;
    }
}
