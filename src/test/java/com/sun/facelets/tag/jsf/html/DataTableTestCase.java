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

package com.sun.facelets.tag.jsf.html;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.sun.facelets.Facelet;
import com.sun.facelets.FaceletFactory;
import com.sun.facelets.FaceletTestCase;
import com.sun.facelets.bean.Example;
import com.sun.facelets.util.FastWriter;

public class DataTableTestCase extends FaceletTestCase {

    public void testDataTable() throws Exception {
Facelet f = FaceletFactory.getInstance().getFacelet("dataTable.xml");
        
        FacesContext faces = FacesContext.getCurrentInstance();
        faces.getExternalContext().getRequestMap().put("company", Example.createCompany());
        
        
        UIViewRoot root = faces.getViewRoot();
        f.apply(faces, root);
        
        FastWriter fw = new FastWriter();
        ResponseWriter rw = faces.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        faces.setResponseWriter(rw);
        root.encodeAll(faces);
        System.out.println(fw);
    }

}
