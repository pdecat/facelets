/**
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

package com.sun.facelets.el;

import java.lang.reflect.Method;

import javax.el.FunctionMapper;

/**
 * Composite FunctionMapper that attempts to load the Method from the first
 * FunctionMapper, then the second if <code>null</code>.
 * 
 * @see javax.el.FunctionMapper
 * @see java.lang.reflect.Method
 * 
 * @author Jacob Hookom
 * @version $Id: CompositeFunctionMapper.java,v 1.2 2005-08-24 04:38:56 jhook Exp $
 */
public final class CompositeFunctionMapper extends FunctionMapper {

    private final FunctionMapper fn0;

    private final FunctionMapper fn1;

    public CompositeFunctionMapper(FunctionMapper fn0, FunctionMapper fn1) {
        this.fn0 = fn0;
        this.fn1 = fn1;
    }

    /**
     * @see javax.el.FunctionMapper#resolveFunction(java.lang.String, java.lang.String)
     */
    public Method resolveFunction(String prefix, String name) {
        Method m = this.fn0.resolveFunction(prefix, name);
        if (m == null) {
            return this.fn1.resolveFunction(prefix, name);
        }
        return m;
    }

}
