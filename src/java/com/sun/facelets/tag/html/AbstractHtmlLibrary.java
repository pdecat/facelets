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

package com.sun.facelets.tag.html;

import com.sun.facelets.tag.AbstractTagLibrary;

/**
 * @author Jacob Hookom
 * @version $Id: AbstractHtmlLibrary.java,v 1.1 2005-05-21 17:54:41 jhook Exp $
 */
public abstract class AbstractHtmlLibrary extends AbstractTagLibrary {

    /**
     * @param namespace
     */
    public AbstractHtmlLibrary(String namespace) {
        super(namespace);
    }

    public void addHtmlComponent(String name, String componentType,
            String rendererType) {
        super.addComponent(name, componentType, rendererType,
                HtmlComponentHandler.class);
    }

}
