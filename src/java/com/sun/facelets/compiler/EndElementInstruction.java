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
import javax.el.ExpressionFactory;
import javax.el.ELException;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.sun.facelets.el.ELAdaptor;
import com.sun.facelets.el.ELText;

final class EndElementInstruction implements Instruction {
    private final String element;

    public EndElementInstruction(String element) {
        this.element = element;
    }

    public void write(FacesContext context) throws IOException {
        context.getResponseWriter().endElement(this.element);
    }

    public Instruction apply(ExpressionFactory factory, ELContext ctx) {
        return this;
    }

    public boolean isLiteral() {
        return true;
    }
}
