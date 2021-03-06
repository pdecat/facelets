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

package com.sun.facelets.compiler;

import java.util.HashMap;
import java.util.Map;

import com.sun.facelets.FaceletHandler;
import com.sun.facelets.tag.TagLibrary;

/**
 * 
 * @author Jacob Hookom
 * @version $Id: NamespaceUnit.java,v 1.4 2008-07-13 19:01:34 rlubke Exp $
 */
final class NamespaceUnit extends CompilationUnit {

    private final Map ns = new HashMap();
    private final TagLibrary library;
    
    public NamespaceUnit(TagLibrary library) {
        this.library = library;
    }

    public FaceletHandler createFaceletHandler() {
        FaceletHandler next = this.getNextFaceletHandler();
        return new NamespaceHandler(next, this.library, this.ns);
    }
    
    public void setNamespace(String prefix, String uri) {
        this.ns.put(prefix, uri);
    }

}
