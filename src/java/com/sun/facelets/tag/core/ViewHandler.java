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
package com.sun.facelets.tag.core;

import java.io.IOException;

import javax.el.ELException;
import javax.el.MethodExpression;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.event.PhaseEvent;

import com.sun.facelets.FaceletContext;
import com.sun.facelets.FaceletException;
import com.sun.facelets.tag.ComponentSupport;
import com.sun.facelets.tag.TagAttribute;
import com.sun.facelets.tag.TagConfig;
import com.sun.facelets.tag.TagHandler;

/**
 * Container for all JavaServer Faces core and custom component actions used on
 * a page. <p/> See <a target="_new"
 * href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/tlddocs/f/view.html">tag
 * documentation</a>.
 * 
 * @author Jacob Hookom
 * @version $Id: ViewHandler.java,v 1.2 2005-07-20 06:37:09 jhook Exp $
 */
public final class ViewHandler extends TagHandler {

    private final static Class[] LISTENER_SIG = new Class[] { PhaseEvent.class };

    private final TagAttribute locale;

    private final TagAttribute renderKitId;

    private final TagAttribute beforePhaseListener;

    private final TagAttribute afterPhaseListener;

    /**
     * @param config
     */
    public ViewHandler(TagConfig config) {
        super(config);
        this.locale = this.getAttribute("locale");
        this.renderKitId = this.getAttribute("renderKitId");
        this.beforePhaseListener = this.getAttribute("beforePhaseListener");
        this.afterPhaseListener = this.getAttribute("afterPhaseListener");
    }

    /**
     * See taglib documentation.
     * 
     * @see com.sun.facelets.FaceletHandler#apply(com.sun.facelets.FaceletContext,
     *      javax.faces.component.UIComponent)
     */
    public void apply(FaceletContext ctx, UIComponent parent)
            throws IOException, FacesException, FaceletException, ELException {
        UIViewRoot root = ComponentSupport.getViewRoot(ctx, parent);
        if (root != null) {
            if (this.locale != null) {
                root.setLocale(ComponentSupport.getLocale(ctx,
                        this.locale));
            }
            if (this.renderKitId != null) {
                String v = this.renderKitId.getValue(ctx);
                root.setRenderKitId(v);
            }
            if (this.beforePhaseListener != null) {
                MethodExpression m = this.beforePhaseListener
                        .getMethodExpression(ctx, null, LISTENER_SIG);
                root.setBeforePhaseListener(m);
            }
            if (this.afterPhaseListener != null) {
                MethodExpression m = this.afterPhaseListener
                        .getMethodExpression(ctx, null, LISTENER_SIG);
                root.setAfterPhaseListener(m);
            }
        }
        this.nextHandler.apply(ctx, parent);
    }

}
