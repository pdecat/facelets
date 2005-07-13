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

package com.sun.facelets;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.FacesException;
import javax.faces.application.StateManager;
import javax.faces.application.ViewHandler;
import javax.faces.application.ViewHandlerWrapper;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.RenderKit;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.sun.facelets.compiler.Compiler;
import com.sun.facelets.compiler.SAXCompiler;
import com.sun.facelets.compiler.TagLibraryConfig;
import com.sun.facelets.spi.RefreshableFaceletFactory;
import com.sun.facelets.tag.TagLibrary;
import com.sun.facelets.util.FacesAPI;

/**
 * ViewHandler implementation for Facelets
 * 
 * @author Jacob Hookom
 * @version $Id: FaceletViewHandler.java,v 1.8 2005-07-13 02:18:56 adamwiner Exp $
 */
public class FaceletViewHandler extends  ViewHandler {

    protected final static Logger log = Logger
            .getLogger("facelets.viewHandler");

    public final static long DEFAULT_REFRESH_PERIOD = 2;

    public final static String REFRESH_PERIOD_PARAM_NAME = "facelet.REFRESH_PERIOD";

    public final static String LIBRARIES_PARAM_NAME = "facelet.LIBRARIES";

    protected final static String STATE_KEY = "com.sun.facelets.VIEW_STATE";

    private final ViewHandler parent;

    private boolean initialized = false;

    private String defaultSuffix;

    protected static void removeTransient(UIComponent c) {
        UIComponent d, e;
        if (c.getChildCount() > 0) {
            for (Iterator itr = c.getChildren().iterator(); itr.hasNext();) {
                d = (UIComponent) itr.next();
                if (d.getFacets().size() > 0) {                  
                    for (Iterator jtr = d.getFacets().values().iterator(); jtr
                            .hasNext();) {
                        e = (UIComponent) jtr.next();
                        if (e.isTransient()) {
                            jtr.remove();
                        } else {
                            removeTransient(e);
                        }
                    }
                }
                if (d.isTransient()) {
                    itr.remove();
                } else {
                    removeTransient(d);
                }
            }
        }
        if (c.getFacets().size() > 0) {
            for (Iterator itr = c.getFacets().values().iterator(); itr
                    .hasNext();) {
                d = (UIComponent) itr.next();
                if (d.isTransient()) {
                    itr.remove();
                } else {
                    removeTransient(d);
                }
            }
        }
    }

    /**
     * 
     */
    public FaceletViewHandler(ViewHandler parent) {
        this.parent = parent;
    }

    protected void initialize() {
        synchronized (this) {
            if (!this.initialized) {
                log.fine("Initializing");
                Compiler c = this.createCompiler();
                this.initializeCompiler(c);
                FaceletFactory f = this.createFaceletFactory(c);
                FaceletFactory.setInstance(f);
                this.initialized = true;
                log.fine("Initialization Successful");
            }
        }
    }

    protected FaceletFactory createFaceletFactory(Compiler c) {
        long refreshPeriod = DEFAULT_REFRESH_PERIOD;
        FacesContext ctx = FacesContext.getCurrentInstance();
        String userPeriod = ctx.getExternalContext().getInitParameter(
                REFRESH_PERIOD_PARAM_NAME);
        if (userPeriod != null && userPeriod.length() > 0) {
            refreshPeriod = Long.parseLong(userPeriod);
        }
        log.fine("Using Refresh Period: " + refreshPeriod + " sec");
        try {
            return new RefreshableFaceletFactory(c, ctx.getExternalContext()
                    .getResource("/"), refreshPeriod);
        } catch (MalformedURLException e) {
            throw new FaceletException("Error Creating FaceletFactory", e);
        }
    }

    protected Compiler createCompiler() {
        return new SAXCompiler();
    }

    protected void initializeCompiler(Compiler c) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ExternalContext ext = ctx.getExternalContext();
        String libParam = ext.getInitParameter(LIBRARIES_PARAM_NAME);
        if (libParam != null) {
            libParam = libParam.trim();
            String[] libs = libParam.split(";");
            URL src;
            TagLibrary libObj;
            for (int i = 0; i < libs.length; i++) {
                try {
                    src = ext.getResource(libs[i]);
                    if (src == null) {
                        throw new FileNotFoundException(libs[i]);
                    }
                    libObj = TagLibraryConfig.create(src);
                    c.addTagLibrary(libObj);
                    log.fine("Successfully Loaded Library: " + libs[i]);
                } catch (IOException e) {
                    log.log(Level.SEVERE, "Error Loading Library: " + libs[i],
                            e);
                }
            }
        }
    }

    public UIViewRoot restoreView(FacesContext context, String viewId) {
        UIViewRoot root = null;
        try {
            root = this.parent.restoreView(context, viewId);
        } catch (Exception e) {
            log.log(Level.WARNING, "Error Restoring View: " + viewId, e);
        }
        if (root != null) {
            log.fine("View Restored: " + root.getViewId());
        } else {
            log.fine("Unable to restore View");
        }
        return root;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.faces.application.ViewHandlerWrapper#getWrapped()
     */
    protected ViewHandler getWrapped() {
        return this.parent;
    }

    public void renderView(FacesContext context, UIViewRoot viewToRender)
            throws IOException, FacesException {
        if (!this.initialized) {
            this.initialize();
        }

        // exit if the view is not to be rendered
        if (!viewToRender.isRendered()) {
            return;
        }

        if (log.isLoggable(Level.FINE)) {
            log.fine("Rendering View: " + viewToRender.getViewId());
        }

        // setup our viewId
        String renderedViewId = this.getRenderedViewId(context, viewToRender
                .getViewId());
        viewToRender.setViewId(renderedViewId);

        // grab our FaceletFactory and create a Facelet
        FaceletFactory factory = FaceletFactory.getInstance();
        Facelet f = factory.getFacelet(viewToRender.getViewId());

        // populate UIViewRoot
        f.apply(context, viewToRender);

        // setup writer
        ExternalContext extContext = context.getExternalContext();
        ResponseWriter writer = context.getResponseWriter();
        if (writer == null) {
            RenderKit renderKit = context.getRenderKit();
            ServletRequest request = (ServletRequest) extContext.getRequest();
            ServletResponse response = (ServletResponse) extContext
                    .getResponse();
            response.setBufferSize(8192);
            String encoding = request.getCharacterEncoding();
            // Create a dummy ResponseWriter with a bogus writer,
            // so we can figure out what content type the ReponseWriter
            // is really going to ask for
            writer = renderKit.createResponseWriter(new NullWriter(),
                    "text/html", encoding);
            response.setContentType(writer.getContentType() +
                                    "; charset = " + encoding);
            // Now, clone with the real writer
            writer = writer.cloneWithWriter(response.getWriter());
            context.setResponseWriter(writer);
        }

        // save the state
        StateManager stateMgr = context.getApplication().getStateManager();
        Object state = stateMgr.saveSerializedView(context);
        extContext.getRequestMap().put(STATE_KEY, state);

        // write the state
        writer.startDocument();
        
        if (FacesAPI.getVersion() >= 12) {
            viewToRender.encodeAll(context);
        } else {
            encodeRecursive(context, viewToRender);
        }
        
        writer.endDocument();
        writer.close();

        // finally clean up transients if viewState = true
        if (extContext.getRequestMap().containsKey(STATE_KEY)) {
            removeTransient(viewToRender);
        }
    }
    
    protected final static void encodeRecursive(FacesContext context, UIComponent viewToRender) throws IOException, FacesException {
        if (viewToRender.isRendered()) {
            viewToRender.encodeBegin(context);
            if (viewToRender.getRendersChildren()) {
                viewToRender.encodeChildren(context);
            }
            else if (viewToRender.getChildCount() > 0) {
                Iterator kids = viewToRender.getChildren().iterator();
                while (kids.hasNext()) {
                    UIComponent kid = (UIComponent) kids.next();
                    encodeRecursive(context, kid);
                }
            }
            viewToRender.encodeEnd(context);
        }
    }

    public String getDefaultSuffix(FacesContext context) throws FacesException {
        if (this.defaultSuffix == null) {
            ExternalContext extCtx = context.getExternalContext();
            String viewSuffix = extCtx
                    .getInitParameter(ViewHandler.DEFAULT_SUFFIX_PARAM_NAME);
            this.defaultSuffix = (viewSuffix != null) ? viewSuffix
                    : ViewHandler.DEFAULT_SUFFIX;
        }
        return this.defaultSuffix;
    }

    protected String getRenderedViewId(FacesContext context, String actionId) {
        ExternalContext extCtx = context.getExternalContext();
        String viewId = extCtx.getRequestPathInfo();
        if (extCtx.getRequestPathInfo() == null) {
            String facesSuffix = actionId.substring(actionId.lastIndexOf('.'));
            String viewSuffix = this.getDefaultSuffix(context);
            viewId = actionId.replaceFirst(facesSuffix, viewSuffix);
        }
        if (log.isLoggable(Level.FINE)) {
            log.fine("ActionId -> ViewId: " + actionId + " -> " + viewId);
        }
        return viewId;
    }

    public void writeState(FacesContext context) throws IOException {
        StateManager stateMgr = context.getApplication().getStateManager();
        Object state = context.getExternalContext().getRequestMap().get(
                STATE_KEY);
        if (state != null) {
            stateMgr.writeState(context, (StateManager.SerializedView) state);
        }
    }

    public Locale calculateLocale(FacesContext context) {
        return this.parent.calculateLocale(context);
    }

    public String calculateRenderKitId(FacesContext context) {
        return this.parent.calculateRenderKitId(context);
    }

    public UIViewRoot createView(FacesContext context, String viewId) {
        return this.parent.createView(context, viewId);
    }

    public String getActionURL(FacesContext context, String viewId) {
        return this.parent.getActionURL(context, viewId);
    }

    public String getResourceURL(FacesContext context, String path) {
        return this.parent.getResourceURL(context, path);
    }

    static private class NullWriter extends Writer
    {
        public void write(char[] buffer) {}
        public void write(char[] buffer, int off, int len) {}
        public void write(String str) {}
        public void write(int c) {}
        public void write(String str, int off, int len) {}
        public void close() {}
        public void flush() {}
    }
}
