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

package com.sun.facelets.tag.jsf;

import java.io.IOException;

import javax.el.ELException;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.validator.Validator;

import com.sun.facelets.FaceletContext;
import com.sun.facelets.FaceletException;
import com.sun.facelets.tag.MetaTagHandler;
import com.sun.facelets.tag.TagAttribute;
import com.sun.facelets.tag.TagConfig;
import com.sun.facelets.tag.TagException;
import com.sun.facelets.tag.MetaRuleset;

/**
 * Handles setting a Validator instance on a EditableValueHolder. Will wire all
 * attributes set to the Validator instance created/fetched. Uses the "binding"
 * attribute for grabbing instances to apply attributes to. <p/> Will only
 * set/create Validator is the passed UIComponent's parent is null, signifying
 * that it wasn't restored from an existing tree.
 * 
 * @author Jacob Hookom
 * @version $Id: ValidateHandler.java,v 1.4 2008-07-13 19:01:46 rlubke Exp $
 */
public class ValidateHandler extends MetaTagHandler {

    private final TagAttribute binding;
    
    private String validatorId;

    /**
     * 
     * @param config
     * @deprecated
     */
    public ValidateHandler(TagConfig config) {
        super(config);
        this.binding = this.getAttribute("binding");
    }
    
    public ValidateHandler(ValidatorConfig config) {
        this((TagConfig) config);
        this.validatorId = config.getValidatorId();
    }

    /**
     * TODO
     * 
     * @see com.sun.facelets.FaceletHandler#apply(com.sun.facelets.FaceletContext,
     *      javax.faces.component.UIComponent)
     */
    public final void apply(FaceletContext ctx, UIComponent parent)
            throws IOException, FacesException, FaceletException, ELException {

        if (parent == null || !(parent instanceof EditableValueHolder)) {
            throw new TagException(this.tag,
                    "Parent not an instance of EditableValueHolder: " + parent);
        }

        // only process if it's been created
        if (parent.getParent() == null) {
            // cast to a ValueHolder
            EditableValueHolder evh = (EditableValueHolder) parent;
            ValueExpression ve = null;
            Validator v = null;
            if (this.binding != null) {
                ve = this.binding.getValueExpression(ctx, Validator.class);
                v = (Validator) ve.getValue(ctx);
            }
            if (v == null) {
                v = this.createValidator(ctx);
                if (ve != null) {
                    ve.setValue(ctx, v);
                }
            }
            if (v == null) {
                throw new TagException(this.tag, "No Validator was created");
            }
            this.setAttributes(ctx, v);
            evh.addValidator(v);
        }
    }

    /**
     * Template method for creating a Validator instance
     * 
     * @param ctx
     *            FaceletContext to use
     * @return a new Validator instance
     */
    protected Validator createValidator(FaceletContext ctx) {
        if (this.validatorId == null) {
            throw new TagException(
                    this.tag,
                    "Default behavior invoked of requiring a validator-id passed in the constructor, must override ValidateHandler(ValidatorConfig)");
        }
        return ctx.getFacesContext().getApplication().createValidator(
                this.validatorId);
    }

    protected MetaRuleset createMetaRuleset(Class type) {
        return super.createMetaRuleset(type).ignore("binding");
    }

}
