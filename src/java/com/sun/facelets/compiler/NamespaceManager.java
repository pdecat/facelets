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

package com.sun.facelets.compiler;

import java.util.ArrayList;
import java.util.List;

import com.sun.facelets.tag.TagLibrary;

/**
 * @author Jacob Hookom
 * @version $Id: NamespaceManager.java,v 1.2 2005-07-18 08:25:29 jhook Exp $
 */
public final class NamespaceManager {

    private final static class NS {
        public final String prefix;

        public final String namespace;

        public NS(String prefix, String ns) {
            this.prefix = prefix;
            this.namespace = ns;
        }
    }

    protected final List namespaces;

    /**
     * 
     */
    public NamespaceManager() {
        this.namespaces = new ArrayList();
    }

    public void reset() {
        this.namespaces.clear();
    }

    public void pushNamespace(String prefix, String namespace) {
        NS ns = new NS(prefix, namespace);
        this.namespaces.add(0, ns);
    }

    public String getNamespace(String prefix) {
        NS ns = null;
        for (int i = 0; i < this.namespaces.size(); i++) {
            ns = (NS) this.namespaces.get(i);
            if (ns.prefix.equals(prefix)) {
                return ns.namespace;
            }
        }
        return null;
    }

    public void popNamespace(String prefix) {
        NS ns = null;
        for (int i = 0; i < this.namespaces.size(); i++) {
            ns = (NS) this.namespaces.get(i);
            if (ns.prefix.equals(prefix)) {
                this.namespaces.remove(i);
                return;
            }
        }
    }
    
    public final NamespaceUnit toNamespaceUnit(TagLibrary library) {
        NamespaceUnit unit = new NamespaceUnit(library);
        if (this.namespaces.size() > 0) {
            NS ns = null;
            for (int i = this.namespaces.size() - 1; i >= 0; i--) {
                ns = (NS) this.namespaces.get(i);
                unit.setNamespace(ns.prefix, ns.namespace);
            }
        }
        return unit;
    }

}
