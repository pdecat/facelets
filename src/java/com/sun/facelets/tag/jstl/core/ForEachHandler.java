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

package com.sun.facelets.tag.jstl.core;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.el.ELException;
import javax.el.ValueExpression;
import javax.el.VariableMapper;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;

import com.sun.facelets.FaceletContext;
import com.sun.facelets.FaceletException;
import com.sun.facelets.tag.TagAttribute;
import com.sun.facelets.tag.TagAttributeException;
import com.sun.facelets.tag.TagConfig;
import com.sun.facelets.tag.TagHandler;

/**
 * @author Jacob Hookom
 * @version $Id: ForEachHandler.java,v 1.7 2005-10-19 06:39:29 jhook Exp $
 */
public final class ForEachHandler extends TagHandler {

    private static class ArrayIterator implements Iterator {

        protected final Object array;

        protected int i;

        protected final int len;

        public ArrayIterator(Object src) {
            this.i = 0;
            this.array = src;
            this.len = Array.getLength(src);
        }

        public boolean hasNext() {
            return this.i < this.len;
        }

        public Object next() {
            return Array.get(this.array, this.i++);
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private final TagAttribute begin;

    private final TagAttribute end;

    private final TagAttribute items;

    private final TagAttribute step;

    private final TagAttribute tranzient;

    private final TagAttribute var;

    private final TagAttribute varStatus;

    /**
     * @param config
     */
    public ForEachHandler(TagConfig config) {
        super(config);
        this.items = this.getAttribute("items");
        this.var = this.getAttribute("var");
        this.begin = this.getAttribute("begin");
        this.end = this.getAttribute("end");
        this.step = this.getAttribute("step");
        this.varStatus = this.getAttribute("varStatus");
        this.tranzient = this.getAttribute("transient");

        if (this.items == null && this.begin != null && this.end == null) {
            throw new TagAttributeException(
                    this.tag,
                    this.begin,
                    "If the 'items' attribute is not specified, but the 'begin' attribute is, then the 'end' attribute is required");
        }
    }

    public void apply(FaceletContext ctx, UIComponent parent)
            throws IOException, FacesException, FaceletException, ELException {
        int s = this.getBegin(ctx);
        int e = this.getEnd(ctx);
        int m = this.getStep(ctx);
        boolean t = this.getTransient(ctx);
        Object src = null;
        ValueExpression srcVE = null;
        if (this.items != null) {
            srcVE = this.items.getValueExpression(ctx, Object.class);
            src = srcVE.getValue(ctx);
        } else {
            byte[] b = new byte[e - s + 1];
            for (int i = 1; i < b.length + 1; i++) {
                b[i - 1] = (byte) i;
            }
            src = b;
        }
        if (src != null) {
            Iterator itr = this.toIterator(src);
            if (itr != null) {
                int i = 1;

                // move to start
                while (i < s && itr.hasNext()) {
                    itr.next();
                    i++;
                }

                String v = this.getVarName(ctx);
                String vs = this.getVarStatusName(ctx);
                VariableMapper vars = ctx.getVariableMapper();
                ValueExpression ve = null;
                ValueExpression vO = this.capture(v, vars);
                ValueExpression vsO = this.capture(vs, vars);
                int mi = 0;
                Object value = null;
                try {
                    while (i <= e && itr.hasNext()) {
                        value = itr.next();

                        // set the var
                        if (v != null) {
                            if (t || srcVE == null) {
                                ctx.setAttribute(v, value);
                            } else {
                                ve = this.getVarExpr(srcVE, src, value, i - 1);
                                vars.setVariable(v, ve);
                            }
                        }

                        // set the varStatus
                        if (vs != null) {
                            IterationStatus itrS = new IterationStatus(i, s, e,
                                    m);
                            if (t || srcVE == null) {
                                ctx.setAttribute(vs, itrS);
                            } else {
                                ve = new IterationStatusExpression(itrS);
                                vars.setVariable(vs, ve);
                            }
                        }

                        // execute body
                        this.nextHandler.apply(ctx, parent);

                        // increment steps
                        mi = 1;
                        while (mi < m && itr.hasNext()) {
                            itr.next();
                            mi++;
                            i++;
                        }
                        i++;
                    }
                } finally {
                    if (v != null) {
                        vars.setVariable(v, vO);
                    }
                    if (vs != null) {
                        vars.setVariable(vs, vsO);
                    }
                }
            }
        }
    }

    private final ValueExpression capture(String name, VariableMapper vars) {
        if (name != null) {
            return vars.setVariable(name, null);
        }
        return null;
    }

    private final int getBegin(FaceletContext ctx) {
        if (this.begin != null) {
            return this.begin.getInt(ctx);
        }
        return 0;
    }

    private final int getEnd(FaceletContext ctx) {
        if (this.end != null) {
            return this.end.getInt(ctx);
        }
        return Integer.MAX_VALUE;
    }

    private final int getStep(FaceletContext ctx) {
        if (this.step != null) {
            return this.step.getInt(ctx);
        }
        return 1;
    }

    private final boolean getTransient(FaceletContext ctx) {
        if (this.tranzient != null) {
            return this.tranzient.getBoolean(ctx);
        }
        return false;
    }

    private final ValueExpression getVarExpr(ValueExpression ve, Object src,
            Object value, int i) {
        if (src instanceof Collection || src.getClass().isArray()) {
            return new IndexedValueExpression(ve, i);
        } else if (src instanceof Map && value instanceof Map.Entry) {
            return new MappedValueExpression(ve, (Map.Entry) value);
        }
        throw new IllegalStateException("Cannot create VE for: " + src);
    }

    private final String getVarName(FaceletContext ctx) {
        if (this.var != null) {
            return this.var.getValue(ctx);
        }
        return null;
    }

    private final String getVarStatusName(FaceletContext ctx) {
        if (this.varStatus != null) {
            return this.varStatus.getValue(ctx);
        }
        return null;
    }

    private final Iterator toIterator(Object src) {
        if (src == null) {
            return null;
        } else if (src instanceof Collection) {
            return ((Collection) src).iterator();
        } else if (src instanceof Map) {
            return ((Map) src).entrySet().iterator();
        } else if (src.getClass().isArray()) {
            return new ArrayIterator(src);
        } else {
            throw new TagAttributeException(this.tag, this.items,
                    "Must evaluate to a Collection, Map, Array, or null.");
        }
    }

}
