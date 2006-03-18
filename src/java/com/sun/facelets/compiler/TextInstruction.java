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

package com.sun.facelets.compiler;


import java.io.IOException;
import java.util.List;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ExpressionFactory;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.sun.facelets.el.ELAdaptor;
import com.sun.facelets.el.ELText;

class TextInstruction implements Instruction {
    private final ELText txt;
    
    private final String alias;

    public TextInstruction(String alias, ELText txt) {
        this.alias = alias;
        this.txt = txt;
    }

    public void write(FacesContext context) throws IOException {
        ResponseWriter out = context.getResponseWriter();
        try {
            ELContext elContext = ELAdaptor.getELContext(context);
            txt.write(out, elContext);
        } catch (ELException e) {
            throw new ELException(this.alias + ": " + e.getMessage(), e.getCause());
        } catch (Exception e) {
            throw new ELException(this.alias + ": " + e.getMessage(), e);
        }
    }


    public Instruction apply(ExpressionFactory factory, ELContext ctx) {
        ELText nt = this.txt.apply(factory, ctx);
        if (nt == this.txt) {
            return this;
        }

        return new TextInstruction(alias, nt);
    }

    public boolean isLiteral() {
        return txt.isLiteral();
    }
}
