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

package com.sun.facelets.tag.jsf;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.el.ELException;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.component.ActionSource;
import javax.faces.component.ActionSource2;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.el.ValueBinding;
import javax.faces.event.ActionEvent;
import javax.faces.event.MethodExpressionActionListener;
import javax.faces.event.MethodExpressionValueChangeListener;
import javax.faces.event.ValueChangeEvent;
import javax.faces.validator.MethodExpressionValidator;

import com.sun.facelets.FaceletContext;
import com.sun.facelets.el.ELAdaptor;
import com.sun.facelets.el.LegacyMethodBinding;
import com.sun.facelets.el.LegacyValueBinding;
import com.sun.facelets.tag.MetaTagHandler;
import com.sun.facelets.tag.TagAttribute;
import com.sun.facelets.tag.Metadata;
import com.sun.facelets.tag.TagException;
import com.sun.facelets.tag.TagHandler;
import com.sun.facelets.tag.MetaRuleset;
import com.sun.facelets.util.FacesAPI;

/**
 * Implementation of the tag logic used in the JSF specification. This is your
 * golden hammer for wiring UIComponents to Facelets.
 * 
 * @author Jacob Hookom
 * @version $Id: ComponentHandler.java,v 1.12.4.2 2006-03-28 07:31:12 jhook Exp $
 */
public class ComponentHandler extends MetaTagHandler {

    private final static Logger log = Logger
            .getLogger("facelets.tag.component");
    
    private final TagAttribute binding;

    private final String componentType;

    private final TagAttribute id;

    private final String rendererType;

    public ComponentHandler(ComponentConfig config) {
        super(config);
        this.componentType = config.getComponentType();
        this.rendererType = config.getRendererType();
        this.id = this.getAttribute("id");
        this.binding = this.getAttribute("binding");
    }

    /**
     * Method handles UIComponent tree creation in accordance with the JSF 1.2
     * spec.
     * <ol>
     * <li>First determines this UIComponent's id by calling
     * {@link #getId(FaceletContext) getId(FaceletContext)}.</li>
     * <li>Search the parent for an existing UIComponent of the id we just
     * grabbed</li>
     * <li>If found, {@link #markForDeletion(UIComponent) mark} its children
     * for deletion.</li>
     * <li>If <i>not</i> found, call
     * {@link #createComponent(FaceletContext) createComponent}.
     * <ol>
     * <li>Only here do we apply
     * {@link ObjectHandler#setAttributes(FaceletContext, Object) attributes}</li>
     * <li>Set the UIComponent's id</li>
     * <li>Set the RendererType of this instance</li>
     * </ol>
     * </li>
     * <li>Now apply the nextHandler, passing the UIComponent we've
     * created/found.</li>
     * <li>Now add the UIComponent to the passed parent</li>
     * <li>Lastly, if the UIComponent already existed (found), then
     * {@link #finalizeForDeletion(UIComponent) finalize} for deletion.</li>
     * </ol>
     * 
     * @see com.sun.facelets.FaceletHandler#apply(com.sun.facelets.FaceletContext,
     *      javax.faces.component.UIComponent)
     * 
     * @throws TagException
     *             if the UIComponent parent is null
     */
    public final void apply(FaceletContext ctx, UIComponent parent)
            throws IOException, FacesException, ELException {
        // make sure our parent is not null
        if (parent == null) {
            throw new TagException(this.tag, "Parent UIComponent was null");
        }

        // our id
        String id = ctx.generateUniqueId(this.tagId);

        // grab our component
        UIComponent c = ComponentSupport.findChildByTagId(parent, id);
        boolean componentFound = false;
        if (c != null) {
            componentFound = true;
            // mark all children for cleaning
            if (log.isLoggable(Level.FINE)) {
                log.fine(this.tag
                        + " Component["+id+"] Found, marking children for cleanup");
            }
            ComponentSupport.markForDeletion(c);
        } else {
            c = this.createComponent(ctx);
            if (log.isLoggable(Level.FINE)) {
                log.fine(this.tag + " Component["+id+"] Created: "
                        + c.getClass().getName());
            }
            this.setAttributes(ctx, c);
            
            // mark it owned by a facelet instance
            c.getAttributes().put(ComponentSupport.MARK_CREATED, id);
            
            // assign our unique id
            if (this.id != null) {
                c.setId(ctx.generateUniqueId(this.id.getValue(ctx)));
            } else {
                UIViewRoot root = ComponentSupport.getViewRoot(ctx, parent);
                if (root != null) {
                    String uid = root.createUniqueId();
                    c.setId(uid);
                }
            }
            
            if (this.rendererType != null) {
                c.setRendererType(this.rendererType);
            }
            
            // hook method
            this.onComponentCreated(ctx, c, parent);
        }

        // first allow c to get populated
        this.nextHandler.apply(ctx, c);

        // finish cleaning up orphaned children
        if (componentFound) {
            ComponentSupport.finalizeForDeletion(c);
            parent.getChildren().remove(c);
        }
        

        this.onComponentPopulated(ctx, c, parent);

        // add to the tree afterwards
        // this allows children to determine if it's
        // been part of the tree or not yet
        parent.getChildren().add(c);
    }

    /**
     * If the binding attribute was specified, use that in conjuction with our
     * componentType String variable to call createComponent on the Application,
     * otherwise just pass the componentType String.
     * <p />
     * If the binding was used, then set the ValueExpression "binding" on the
     * created UIComponent.
     * 
     * @see Application#createComponent(javax.faces.el.ValueBinding,
     *      javax.faces.context.FacesContext, java.lang.String)
     * @see Application#createComponent(java.lang.String)
     * @param ctx
     *            FaceletContext to use in creating a component
     * @return
     */
    protected UIComponent createComponent(FaceletContext ctx) {
        UIComponent c = null;
        FacesContext faces = ctx.getFacesContext();
        Application app = faces.getApplication();
        if (this.binding != null) {
            ValueExpression ve = this.binding.getValueExpression(ctx,
                    Object.class);
            if (FacesAPI.getVersion() >= 12) {
                c = app.createComponent(ve, faces, this.componentType);
                if (c != null) {
                    // Make sure the component supports 1.2
                    if (FacesAPI.getComponentVersion(c) >= 12) {
                        c.setValueExpression("binding", ve);
                    } else {
                        ValueBinding vb = new LegacyValueBinding(ve);
                        c.setValueBinding("binding", vb);
                    }

                }
            } else {
                ValueBinding vb = new LegacyValueBinding(ve);
                c = app.createComponent(vb, faces, this.componentType);
                if (c != null) {
                    c.setValueBinding("binding", vb);
                }
            }
        } else {
            c = app.createComponent(this.componentType);
        }
        return c;
    }

    /**
     * If the id TagAttribute was specified, get it's value, otherwise generate
     * a unique id from our tagId.
     * 
     * @see TagAttribute#getValue(FaceletContext)
     * @param ctx
     *            FaceletContext to use
     * @return what should be a unique Id
     */
    protected String getId(FaceletContext ctx) {
        if (this.id != null) {
            return this.id.getValue(ctx);
        }
        return ctx.generateUniqueId(this.tagId);
    }

    protected MetaRuleset createMetaRuleset(Class type) {
        MetaRuleset m = super.createMetaRuleset(type);
        
        // ignore standard component attributes
        m.ignore("binding").ignore("id");
        
        // add auto wiring for attributes
        m.addRule(ComponentRule.Instance);
        
        // if it's an ActionSource
        if (ActionSource.class.isAssignableFrom(type)) {
            m.addRule(ActionSourceRule.Instance);
        }
        
        // if it's a ValueHolder
        if (ValueHolder.class.isAssignableFrom(type)) {
            m.addRule(ValueHolderRule.Instance);
            
            // if it's an EditableValueHolder
            if (EditableValueHolder.class.isAssignableFrom(type)) {
                m.ignore("submittedValue");
                m.ignore("valid");
                m.addRule(EditableValueHolderRule.Instance);
            }
        }
        
        return m;
    }
    
    /**
     * A hook method for allowing developers to do additional processing once Facelets
     * creates the component.  The 'setAttributes' method is still perferred, but this
     * method will provide the parent UIComponent before it's been added to the tree and
     * before any children have been added to the newly created UIComponent.
     * 
     * @param ctx
     * @param c
     * @param parent
     */
    protected void onComponentCreated(FaceletContext ctx, UIComponent c, UIComponent parent) {
        // do nothing
    }

    protected void onComponentPopulated(FaceletContext ctx, UIComponent c, UIComponent parent) {
        // do nothing
    }
}
