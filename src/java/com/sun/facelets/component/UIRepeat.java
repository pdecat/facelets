package com.sun.facelets.component;

import java.io.IOException;
import java.io.Serializable;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.FacesException;
import javax.faces.component.ContextCallback;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.FacesEvent;
import javax.faces.event.FacesListener;
import javax.faces.event.PhaseId;
import javax.faces.model.ArrayDataModel;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.ResultSetDataModel;
import javax.faces.model.ScalarDataModel;
import javax.faces.render.Renderer;

import com.sun.facelets.tag.jsf.ComponentSupport;
import com.sun.facelets.util.FacesAPI;

public class UIRepeat extends UIComponentBase implements NamingContainer {

    public static final String COMPONENT_TYPE = "facelets.ui.Repeat";

    public static final String COMPONENT_FAMILY = "facelets";

    private final static DataModel EMPTY_MODEL = new ListDataModel(
            Collections.EMPTY_LIST);

    // our data
    private Object value;

    private transient DataModel model;

    // variables
    private String var;

    private String varStatus;

    private int index = -1;

    // scoping
    private int offset = -1;

    private int size = -1;

    public UIRepeat() {
        this.setRendererType("facelets.ui.Repeat");
    }

    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    public int getOffset() {
        if (this.offset != -1) {
            return this.offset;
        }
        ValueBinding vb = this.getValueBinding("offset");
        if (vb != null) {
            return ((Integer) vb.getValue(FacesContext.getCurrentInstance()))
                    .intValue();
        }
        return 0;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getSize() {
        if (this.size != -1) {
            return this.size;
        }
        ValueBinding vb = this.getValueBinding("size");
        if (vb != null) {
            return ((Integer) vb.getValue(FacesContext.getCurrentInstance()))
                    .intValue();
        }
        return -1;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getVar() {
        return this.var;
    }

    public void setVar(String var) {
        this.var = var;
    }

    private void resetDataModel() {
        UIComponent parent = this.getParent();
        while (parent != null) {
            if (parent instanceof UIData || parent instanceof UIRepeat) {
                this.setDataModel(null);
                return;
            }
            parent = parent.getParent();
        }
    }

    private void setDataModel(DataModel model) {
        this.model = model;
    }

    private DataModel getDataModel() {
        if (this.model == null) {
            Object val = this.getValue();
            if (val == null) {
                this.model = EMPTY_MODEL;
            } else if (val instanceof DataModel) {
                this.model = (DataModel) val;
            } else if (val instanceof List) {
                this.model = new ListDataModel((List) val);
            } else if (Object[].class.isAssignableFrom(val.getClass())) {
                this.model = new ArrayDataModel((Object[]) val);
            } else if (val instanceof ResultSet) {
                this.model = new ResultSetDataModel((ResultSet) val);
            } else {
                this.model = new ScalarDataModel(val);
            }
        }
        return this.model;
    }

    public Object getValue() {
        if (this.value == null) {
            ValueBinding vb = this.getValueBinding("value");
            if (vb != null) {
                return vb.getValue(FacesContext.getCurrentInstance());
            }
        }
        return this.value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    private transient StringBuffer buffer;

    private StringBuffer getBuffer() {
        if (this.buffer == null) {
            this.buffer = new StringBuffer();
        }
        this.buffer.setLength(0);
        return this.buffer;
    }

    public String getClientId(FacesContext faces) {
        String id = super.getClientId(faces);
        if (this.index >= 0) {
            id = this.getBuffer().append(id).append(
                    NamingContainer.SEPARATOR_CHAR).append(this.index)
                    .toString();
        }
        return id;
    }

    private transient Object origValue;

    private void captureOrigValue() {
        if (this.var != null) {
            FacesContext faces = FacesContext.getCurrentInstance();
            Map attrs = faces.getExternalContext().getRequestMap();
            this.origValue = attrs.get(this.var);
        }
    }

    private void restoreOrigValue() {
        if (this.var != null) {
            FacesContext faces = FacesContext.getCurrentInstance();
            Map attrs = faces.getExternalContext().getRequestMap();
            if (this.origValue != null) {
                attrs.put(this.var, this.origValue);
            } else {
                attrs.remove(this.var);
            }
        }
    }

    private Map childState;

    private Map getChildState() {
        if (this.childState == null) {
            this.childState = new HashMap();
        }
        return this.childState;
    }

    private void saveChildState() {
        if (this.getChildCount() > 0) {

            FacesContext faces = FacesContext.getCurrentInstance();

            Iterator itr = this.getChildren().iterator();
            while (itr.hasNext()) {
                this.saveChildState(faces, (UIComponent) itr.next());
            }
        }
    }

    private void saveChildState(FacesContext faces, UIComponent c) {

        if (c instanceof EditableValueHolder && !c.isTransient()) {
            String clientId = c.getClientId(faces);
            SavedState ss = (SavedState) this.getChildState().get(clientId);
            if (ss == null) {
                ss = new SavedState();
                this.getChildState().put(clientId, ss);
            }
            ss.populate((EditableValueHolder) c);
        }

        // continue hack
        Iterator itr = c.getFacetsAndChildren();
        while (itr.hasNext()) {
            saveChildState(faces, (UIComponent) itr.next());
        }
    }

    private void restoreChildState() {
        if (this.getChildCount() > 0) {

            FacesContext faces = FacesContext.getCurrentInstance();

            Iterator itr = this.getChildren().iterator();
            while (itr.hasNext()) {
                this.restoreChildState(faces, (UIComponent) itr.next());
            }
        }
    }

    private void restoreChildState(FacesContext faces, UIComponent c) {
        // reset id
        c.setId(c.getId());

        // hack
        if (c instanceof EditableValueHolder && !c.isTransient()) {
            EditableValueHolder evh = (EditableValueHolder) c;
            String clientId = c.getClientId(faces);
            SavedState ss = (SavedState) this.getChildState().get(clientId);
            if (ss != null) {
                ss.apply(evh);
            } else {
                NullState.apply(evh);
            }
        }

        // continue hack
        Iterator itr = c.getFacetsAndChildren();
        while (itr.hasNext()) {
            restoreChildState(faces, (UIComponent) itr.next());
        }
    }

    private void setIndex(int index) {

        // save child state
        this.saveChildState();

        this.index = index;
        DataModel localModel = getDataModel();
        localModel.setRowIndex(index);

        if (this.index != -1 && this.var != null && localModel.isRowAvailable()) {
            FacesContext faces = FacesContext.getCurrentInstance();
            Map attrs = faces.getExternalContext().getRequestMap();
            attrs.put(var, localModel.getRowData());
        }

        // restore child state
        this.restoreChildState();
    }

    private boolean isIndexAvailable() {
        return this.getDataModel().isRowAvailable();
    }

    public void process(FacesContext faces, PhaseId phase) {

        // stop if not rendered
        if (!this.isRendered())
            return;

        // clear datamodel
        this.resetDataModel();

        // reset index
        this.captureOrigValue();
        this.setIndex(-1);

        try {
            // has children
            if (this.getChildCount() > 0) {
                Iterator itr;
                UIComponent c;

                int i = this.getOffset();
                int end = this.getSize();
                end = (end >= 0) ? i + end : Integer.MAX_VALUE;

                // grab renderer
                String rendererType = getRendererType();
                Renderer renderer = null;
                if (rendererType != null) {
                    renderer = getRenderer(faces);
                }

                this.setIndex(i);
                while (i <= end && this.isIndexAvailable()) {

                    if (PhaseId.RENDER_RESPONSE.equals(phase)
                            && renderer != null) {
                        renderer.encodeChildren(faces, this);
                    } else {
                        itr = this.getChildren().iterator();
                        while (itr.hasNext()) {
                            c = (UIComponent) itr.next();
                            if (PhaseId.APPLY_REQUEST_VALUES.equals(phase)) {
                                c.processDecodes(faces);
                            } else if (PhaseId.PROCESS_VALIDATIONS
                                    .equals(phase)) {
                                c.processValidators(faces);
                            } else if (PhaseId.UPDATE_MODEL_VALUES
                                    .equals(phase)) {
                                c.processUpdates(faces);
                            } else if (PhaseId.RENDER_RESPONSE.equals(phase)) {
                                if (FacesAPI.getVersion() >= 12) {
                                    c.encodeAll(faces);
                                } else {
                                    ComponentSupport.encodeRecursive(faces, c);
                                }
                            }
                        }
                    }
                    i++;
                    this.setIndex(i);
                }
            }
        } catch (IOException e) {
            throw new FacesException(e);
        } finally {
            this.setIndex(-1);
            this.restoreOrigValue();
        }
    }

    public boolean invokeOnComponent(FacesContext faces, String clientId,
            ContextCallback callback) throws FacesException {
        String id = super.getClientId(faces);
        if (clientId.equals(id)) {
            callback.invokeContextCallback(faces, this);
            return true;
        } else if (clientId.startsWith(id)) {
            int prevIndex = this.index;
            int idxStart = clientId.indexOf(NamingContainer.SEPARATOR_CHAR, id
                    .length());
            if (idxStart != -1
                    && Character.isDigit(clientId.charAt(idxStart + 1))) {
                int idxEnd = clientId.indexOf(NamingContainer.SEPARATOR_CHAR,
                        idxStart);
                if (idxEnd != -1) {
                    int newIndex = Integer.parseInt(clientId.substring(
                            idxStart, idxEnd));
                    boolean found = false;
                    try {
                        this.captureOrigValue();
                        this.setIndex(newIndex);
                        if (this.isIndexAvailable()) {
                            found = super.invokeOnComponent(faces, clientId,
                                    callback);
                        }
                    } finally {
                        this.setIndex(prevIndex);
                        this.restoreOrigValue();
                    }
                    return found;
                }
            } else {
                return super.invokeOnComponent(faces, clientId, callback);
            }
        }
        return false;
    }

    public void processDecodes(FacesContext faces) {
        this.setDataModel(null);
        this.process(faces, PhaseId.APPLY_REQUEST_VALUES);
        super.decode(faces);
    }

    public void processUpdates(FacesContext faces) {
        this.process(faces, PhaseId.UPDATE_MODEL_VALUES);
    }

    public void processValidators(FacesContext faces) {
        this.process(faces, PhaseId.PROCESS_VALIDATIONS);
    }
    
    private final static SavedState NullState = new SavedState();

    // from RI
    private final static class SavedState implements Serializable {

        private Object submittedValue;

        private static final long serialVersionUID = 2920252657338389849L;

        Object getSubmittedValue() {
            return (this.submittedValue);
        }

        void setSubmittedValue(Object submittedValue) {
            this.submittedValue = submittedValue;
        }

        private boolean valid = true;

        boolean isValid() {
            return (this.valid);
        }

        void setValid(boolean valid) {
            this.valid = valid;
        }

        private Object value;

        Object getValue() {
            return (this.value);
        }

        public void setValue(Object value) {
            this.value = value;
        }

        private boolean localValueSet;

        boolean isLocalValueSet() {
            return (this.localValueSet);
        }

        public void setLocalValueSet(boolean localValueSet) {
            this.localValueSet = localValueSet;
        }

        public String toString() {
            return ("submittedValue: " + submittedValue + " value: " + value
                    + " localValueSet: " + localValueSet);
        }

        public void populate(EditableValueHolder evh) {
            this.value = evh.getValue();
            this.valid = evh.isValid();
            this.submittedValue = evh.getSubmittedValue();
            this.localValueSet = evh.isLocalValueSet();
        }

        public void apply(EditableValueHolder evh) {
            evh.setValue(this.value);
            evh.setValid(this.valid);
            evh.setSubmittedValue(this.submittedValue);
            evh.setLocalValueSet(this.localValueSet);
        }

    }

    private final class IndexedEvent extends FacesEvent {

        private final FacesEvent target;

        private final int index;

        public IndexedEvent(UIRepeat owner, FacesEvent target, int index) {
            super(owner);
            this.target = target;
            this.index = index;
        }

        public boolean isAppropriateListener(FacesListener listener) {
            return this.target.isAppropriateListener(listener);
        }

        public void processListener(FacesListener listener) {
            UIRepeat owner = (UIRepeat) this.getComponent();
            int prevIndex = owner.index;
            try {
                owner.setIndex(this.index);
                if (owner.isIndexAvailable()) {
                    this.target.processListener(listener);
                }
            } finally {
                owner.setIndex(prevIndex);
            }
        }

        public int getIndex() {
            return index;
        }

        public FacesEvent getTarget() {
            return target;
        }

    }

    public void broadcast(FacesEvent event) throws AbortProcessingException {
        if (event instanceof IndexedEvent) {
            IndexedEvent idxEvent = (IndexedEvent) event;
            int prevIndex = this.index;
            try {
                this.setIndex(idxEvent.getIndex());
                if (this.isIndexAvailable()) {
                    FacesEvent target = idxEvent.getTarget();
                    target.getComponent().broadcast(target);
                }
            } finally {
                this.setIndex(prevIndex);
            }
        }
    }

    public void queueEvent(FacesEvent event) {
        super.queueEvent(new IndexedEvent(this, event, this.index));
    }

    public void restoreState(FacesContext faces, Object object) {
        Object[] state = (Object[]) object;
        super.restoreState(faces, state[0]);
        this.childState = (Map) state[1];
        this.offset = ((Integer) state[2]).intValue();
        this.size = ((Integer) state[3]).intValue();
        this.var = (String) state[4];
        this.value = state[5];
    }

    public Object saveState(FacesContext faces) {
        Object[] state = new Object[6];
        state[0] = super.saveState(faces);
        state[1] = this.childState;
        state[2] = Integer.valueOf(this.offset);
        state[3] = Integer.valueOf(this.size);
        state[4] = this.var;
        state[5] = this.value;
        return state;
    }

    public void encodeChildren(FacesContext faces) throws IOException {
        if (!isRendered()) {
            return;
        }
        this.setDataModel(null);
        this.process(faces, PhaseId.RENDER_RESPONSE);
    }

    public boolean getRendersChildren() {
        Renderer renderer = null;
        if (getRendererType() != null) {
            if (null != (renderer = getRenderer(getFacesContext()))) {
                return renderer.getRendersChildren();
            }
        }
        return true;
    }
}
