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

package com.sun.facelets.tag.jsf.core;

import javax.faces.component.UIOutput;
import javax.faces.convert.DateTimeConverter;
import javax.faces.convert.NumberConverter;
import javax.faces.validator.DoubleRangeValidator;
import javax.faces.validator.LengthValidator;
import javax.faces.validator.LongRangeValidator;

import com.sun.facelets.tag.AbstractTagLibrary;

/**
 * For Tag details, see JSF Core <a target="_new"
 * href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/tlddocs/f/tld-summary.html">taglib
 * documentation</a>.
 * 
 * @author Jacob Hookom
 * @version $Id: CoreLibrary.java,v 1.4 2005-08-15 19:36:13 jhook Exp $
 */
public final class CoreLibrary extends AbstractTagLibrary {

    public final static String Namespace = "http://java.sun.com/jsf/core";

    public final static CoreLibrary Instance = new CoreLibrary();

    public CoreLibrary() {
        super(Namespace);

        this.addTagHandler("actionListener", ActionListenerHandler.class);

        this.addTagHandler("attribute", AttributeHandler.class);

        this.addConverter("convertDateTime", DateTimeConverter.CONVERTER_ID, ConvertDateTimeHandler.class);

        this.addConverter("convertNumber", NumberConverter.CONVERTER_ID, ConvertNumberHandler.class);

        this.addConverter("converter", null, ConvertDelegateHandler.class);

        this.addTagHandler("facet", FacetHandler.class);

        this.addTagHandler("loadBundle", LoadBundleHandler.class);

        this.addComponent("param", "javax.faces.Parameter", null);

        this.addComponent("selectItem", "javax.faces.SelectItem", null);

        this.addComponent("selectItems", "javax.faces.SelectItems", null);
        
        this.addValidator("validateLength", LengthValidator.VALIDATOR_ID);
        
        this.addValidator("validateLongRange", LongRangeValidator.VALIDATOR_ID);
        
        this.addValidator("validateDoubleRange", DoubleRangeValidator.VALIDATOR_ID);
        
        //this.addTagHandler("validateLength", ValidateLengthHandler.class);

        //this.addTagHandler("validateLongRange", ValidateLongRangeHandler.class);

        this.addValidator("validator", null, ValidateDelegateHandler.class);

        this.addTagHandler("valueChangeListener",
                ValueChangeListenerHandler.class);

        this.addTagHandler("view", ViewHandler.class);
        
        this.addComponent("verbatim", UIOutput.COMPONENT_TYPE, null);
    }
}
