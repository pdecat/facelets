/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sun.facelets.tag;

import java.io.IOException;

import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;

import com.sun.facelets.FaceletContext;
import com.sun.facelets.FaceletException;
import com.sun.facelets.FaceletHandler;

/**
 * A FaceletHandler that is derived of 1 or more, inner FaceletHandlers. This
 * class would be found if the next FaceletHandler is structually, a body
 * with multiple child elements as defined in XML.
 * 
 * @author Jacob Hookom
 * @version $Id: CompositeFaceletHandler.java,v 1.5 2008-07-13 19:01:36 rlubke Exp $
 */
public final class CompositeFaceletHandler implements FaceletHandler {

    private final FaceletHandler[] children;
    private final int len;
    
    public CompositeFaceletHandler(FaceletHandler[] children) {
        this.children = children;
        this.len = children.length;
    }
    
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException, FacesException, FaceletException, ELException {
        for (int i = 0; i < len; i++) {
            this.children[i].apply(ctx, parent);
        }
    }
    
    public FaceletHandler[] getHandlers() {
        return this.children;
    }
}
