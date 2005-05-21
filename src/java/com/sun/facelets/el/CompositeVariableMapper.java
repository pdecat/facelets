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

package com.sun.facelets.el;

import javax.el.ValueExpression;
import javax.el.VariableMapper;

/**
 * Composite VariableMapper that attempts to load the ValueExpression from the first
 * VariableMapper, then the second if <code>null</code>.
 * 
 * @see javax.el.VariableMapper
 * @see javax.el.ValueExpression
 * 
 * @author Jacob Hookom
 * @version $Id: CompositeVariableMapper.java,v 1.1 2005-05-21 17:54:52 jhook Exp $
 */
public final class CompositeVariableMapper extends VariableMapper {

    private final VariableMapper var0;

    private final VariableMapper var1;

    public CompositeVariableMapper(VariableMapper var0, VariableMapper var1) {
        this.var0 = var0;
        this.var1 = var1;
    }

    /**
     * @see javax.el.VariableMapper#resolveVariable(java.lang.String)
     */
    public ValueExpression resolveVariable(String name) {
        ValueExpression ve = this.var0.resolveVariable(name);
        if (ve == null) {
            return this.var1.resolveVariable(name);
        }
        return ve;
    }

    /**
     * @see javax.el.VariableMapper#setVariable(java.lang.String, javax.el.ValueExpression)
     */
    public ValueExpression setVariable(String name, ValueExpression expression) {
        return this.var0.setVariable(name, expression);
    }

}
