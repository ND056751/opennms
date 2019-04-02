/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.api.topo.blablabla;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.CollapsibleRef;
import org.opennms.features.topology.api.topo.CollapsibleVertex;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeListener;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.Ref;
import org.opennms.features.topology.api.topo.RefComparator;
import org.opennms.features.topology.api.topo.SimpleConnector;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexListener;
import org.opennms.features.topology.api.topo.VertexRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XXXGraphImpl implements XXXGraph {

    protected static final String SIMPLE_VERTEX_ID_PREFIX = "v";
    protected static final String SIMPLE_EDGE_ID_PREFIX = "e";

    private static final Logger LOG = LoggerFactory.getLogger(XXXGraphImpl.class);

    private final String m_namespace;
    private final Map<String, Edge> m_edgeMap = new LinkedHashMap<>();
    private final Set<EdgeListener> m_edgeListeners = new CopyOnWriteArraySet<>();
    private final Map<String, Vertex> m_vertexMap = new LinkedHashMap<>();
    private final Set<VertexListener> m_vertexListeners = new CopyOnWriteArraySet<>();
    private final String m_contributesTo;

    private IdGenerator edgeIdGenerator = new IdGenerator(SIMPLE_EDGE_ID_PREFIX, this) {
        @Override
        public List<Ref> getContent() {
            return new ArrayList<>(getEdges());
        }
    };

    private IdGenerator vertexIdGenerator = new IdGenerator(SIMPLE_VERTEX_ID_PREFIX, this) {
        @Override
        public List<Ref> getContent() {
            return new ArrayList<>(getVertices());
        }
    };

    public String getNextVertexId() {
        return vertexIdGenerator.getNextId();
    }

    public String getNextEdgeId() {
        return edgeIdGenerator.getNextId();
    }

    public XXXGraphImpl(String namespace) {
        this(namespace, null);
    }

    public XXXGraphImpl(String namespace, String contributesTo) {
        m_namespace = Objects.requireNonNull(namespace);
        m_contributesTo = contributesTo;
    }

    @Override
    public String getNamespace() {
        return m_namespace;
    }

    @Override
    public boolean contributesTo(String namespace) {
        return m_contributesTo != null && m_contributesTo.equals(namespace);
    }

    @Override
    public Vertex getVertex(String namespace, String id) {
        return getVertex(new DefaultVertexRef(namespace, id));
    }

    @Override
    public Vertex getVertex(VertexRef reference, Criteria... criteria) {
        return getSimpleVertex(reference);
    }

    @Override
    // TODO MVR ??
    public List<Vertex> getVertices(CollapsibleRef collapsibleRef, Criteria... criteria) {
        return new ArrayList<>();
    }

    private Vertex getSimpleVertex(VertexRef reference) {
        if (reference != null && getNamespace().equals(reference.getNamespace())) {
            return m_vertexMap.get(reference.getId());
        }
        return null;
    }

    @Override
    public List<Vertex> getVertices(Collection<? extends VertexRef> references, Criteria... criteria) {
        List<Vertex> vertices = new ArrayList<>();
        for(VertexRef ref : references) {
            Vertex vertex = getSimpleVertex(ref);
            if (vertex != null) {
                vertices.add(vertex);
            }
        }
        return vertices;
    }

    @Override
    public List<Vertex> getVerticesWithoutCollapsibleVertices() {
        return getVertices().stream().filter(v -> v != null && !(v instanceof CollapsibleVertex)).collect(Collectors.toList());
    }

    private void fireVertexSetChanged() {
        for(VertexListener listener : m_vertexListeners) {
            listener.vertexSetChanged(this);
        }
    }

    private void fireVerticesAdded(Collection<Vertex> vertices) {
        for(VertexListener listener : m_vertexListeners) {
            listener.vertexSetChanged(this, vertices, null, null);
        }
    }

    private void fireVerticesRemoved(List<? extends VertexRef> all) {
        List<String> ids = new ArrayList<>(all.size());
        for(VertexRef vertex : all) {
            ids.add(vertex.getId());
        }
        for(VertexListener listener : m_vertexListeners) {
            listener.vertexSetChanged(this, null, null, ids);
        }
    }

    @Override
    public void addVertexListener(VertexListener vertexListener) {
        m_vertexListeners.add(vertexListener);
    }

    @Override
    public void removeVertexListener(VertexListener vertexListener) {
        m_vertexListeners.remove(vertexListener);
    }

    private void removeVertices(List<? extends VertexRef> vertices) {
        for(VertexRef vertex : vertices) {
            LoggerFactory.getLogger(this.getClass()).trace("Removing vertex: {}", vertex);
            // Remove the vertex from the main map
            m_vertexMap.remove(vertex.getId());
        }
        fireVerticesRemoved(vertices);
    }

    private void addVertices(Collection<Vertex> vertices) {
        for(Vertex vertex : vertices) {
            if (vertex.getNamespace() == null || vertex.getId() == null) {
                LoggerFactory.getLogger(this.getClass()).warn("Discarding invalid vertex: {}", vertex);
                continue;
            }
            LoggerFactory.getLogger(this.getClass()).trace("Adding vertex: {}", vertex);
            m_vertexMap.put(vertex.getId(), vertex);
        }
    }

    public void setVertices(List<Vertex> vertices) {
        clearVertices();
        addVertices(vertices);
        fireVertexSetChanged();
    }

    public void add(Vertex...vertices) {
        add(Arrays.asList(vertices));
    }

    public void add(Collection<Vertex> vertices) {
        addVertices(vertices);
        fireVerticesAdded(vertices);
    }

    public void remove(VertexRef... vertices) {
        removeVertices(Arrays.asList(vertices));
    }

    @Override
    public List<Vertex> getVertices(Criteria... criteria) {
        // TODO: Change code to properly filter on Criteria
        return Collections.unmodifiableList(new ArrayList<>(m_vertexMap.values()));
    }

    @Override
    public void clearVertices() {
        List<? extends Vertex> all = getVertices();
        removeVertices(all);
        vertexIdGenerator.reset();
    }

    @Override
    public int getVertexTotalCount() {
        return m_vertexMap.size();
    }

    /**
     * @deprecated You should search by the namespace and ID tuple instead
     */
    @Override
    public boolean containsVertexId(String id) {
        return containsVertexId(new DefaultVertexRef(getNamespace(), id));
    }

    @Override
    public boolean containsVertexId(VertexRef id, Criteria... criteria) {
        return getVertex(id, criteria) != null;
    }

    private Edge getEdge(String id) {
        return m_edgeMap.get(id);
    }

    @Override
    public Edge getEdge(String namespace, String id) {
        return getEdge(id);
    }

    @Override
    public Edge getEdge(EdgeRef reference) {
        return resolveEdge(reference);
    }

    private Edge resolveEdge(EdgeRef reference) {
        if (getNamespace().equals(reference.getNamespace())) {
            if (reference instanceof Edge) {
                return Edge.class.cast(reference);
            } else {
                return m_edgeMap.get(reference.getId());
            }
        }
        return null;
    }

    @Override
    public List<Edge> getEdges(Collection<? extends EdgeRef> references) {
        List<Edge> edges = new ArrayList<>();
        for(EdgeRef ref : references) {
            Edge edge = resolveEdge(ref);
            if (ref != null) {
                edges.add(edge);
            }
        }
        return Collections.unmodifiableList(edges);
    }

    private void fireEdgeSetChanged() {
        for(EdgeListener listener : m_edgeListeners) {
            listener.edgeSetChanged(this, null, null, null);
        }
    }

    private void fireEdgesAdded(List<Edge> edges) {
        for(EdgeListener listener : m_edgeListeners) {
            listener.edgeSetChanged(this, edges, null, null);
        }
    }

    private void fireEdgesRemoved(List<? extends EdgeRef> edges) {
        List<String> ids = new ArrayList<String>(edges.size());
        for(EdgeRef e : edges) {
            ids.add(e.getId());
        }
        for(EdgeListener listener : m_edgeListeners) {
            listener.edgeSetChanged(this, null, null, ids);
        }
    }

    @Override
    public void addEdgeListener(EdgeListener edgeListener) {
        m_edgeListeners.add(edgeListener);
    }

    @Override
    public void removeEdgeListener(EdgeListener edgeListener) {
        m_edgeListeners.remove(edgeListener);
    }

    @Override
    public List<Edge> getEdges(Criteria... criteria) {
        List<Edge> edges = new ArrayList<>();
        for (Edge edge : m_edgeMap.values()) {
            edges.add(edge.clone());
        }
        return Collections.unmodifiableList(edges);
    }

    @Override
    public void clearEdges() {
        List<Edge> all = getEdges();
        removeEdges(all);
        edgeIdGenerator.reset();
    }

    @Override
    public int getEdgeTotalCount() {
        return m_edgeMap.size();
    }

    public void setEdges(List<Edge> edges) {
        m_edgeMap.clear();
        addEdges(edges);
        fireEdgeSetChanged();
    }

    public void add(Edge...edges) {
        add(Arrays.asList(edges));
    }

    public void add(List<Edge> edges) {
        addEdges(edges);
        fireEdgesAdded(edges);
    }

    public void remove(EdgeRef... edges) {
        removeEdges(Arrays.asList(edges));
    }

    private void removeEdges(List<? extends EdgeRef> edges) {
        for(EdgeRef edge : edges) {
            m_edgeMap.remove(edge.getId());
        }
        fireEdgesRemoved(edges);
    }

    private void addEdges(List<Edge> edges) {
        for(Edge edge : edges) {
            if (edge.getNamespace() == null || edge.getId() == null) {
                LoggerFactory.getLogger(this.getClass()).warn("Discarding invalid edge: {}", edge);
                continue;
            }
            LoggerFactory.getLogger(this.getClass()).trace("Adding edge: {}", edge);
            m_edgeMap.put(edge.getId(), edge);
        }
    }

    @Override
    public void addVertices(Vertex... vertices) {
        add(vertices);
    }

    @Override
    public void removeVertex(VertexRef... vertexId) {
        remove(vertexId);
    }

    @Override
    public EdgeRef[] getEdgeIdsForVertex(VertexRef vertex) {
        if (vertex == null) return new EdgeRef[0];
        List<EdgeRef> retval = new ArrayList<EdgeRef>();
        for (Edge edge : getEdges()) {
            // If the vertex is connected to the edge then add it
            if (new RefComparator().compare(edge.getSource().getVertex(), vertex) == 0 || new RefComparator().compare(edge.getTarget().getVertex(), vertex) == 0) {
                retval.add(edge);
            }
        }
        return retval.toArray(new EdgeRef[0]);
    }

    @Override
    public final Map<VertexRef, Set<EdgeRef>> getEdgeIdsForVertices(VertexRef... vertices) {
        List<Edge> edges = getEdges();
        Map<VertexRef,Set<EdgeRef>> retval = new HashMap<VertexRef,Set<EdgeRef>>();
        for (VertexRef vertex : vertices) {
            if (vertex == null) continue;
            Set<EdgeRef> edgeSet = new HashSet<EdgeRef>();
            for (Edge edge : edges) {
                // If the vertex is connected to the edge then add it
                if (new RefComparator().compare(edge.getSource().getVertex(), vertex) == 0 || new RefComparator().compare(edge.getTarget().getVertex(), vertex) == 0) {
                    edgeSet.add(edge);
                }
            }
            retval.put(vertex, edgeSet);
        }
        return retval;
    }

    @Override
    public void addEdges(Edge... edges) {
        add(edges);
    }

    @Override
    public void removeEdges(EdgeRef... edges) {
        remove(edges);
    }

    @Override
    public Edge connectVertices(VertexRef sourceVertextId, VertexRef targetVertextId) {
        String nextEdgeId = "TODO MVR";// TODO MVR use getNextEdgeId() ??? getNextEdgeId();
        return connectVertices(nextEdgeId, sourceVertextId, targetVertextId, getNamespace());
    }

    @Override
    public void resetContainer() {
        clearEdges();
        clearVertices();
    }

    protected final AbstractEdge connectVertices(String id, VertexRef sourceId, VertexRef targetId, String namespace) {
        if (sourceId == null) {
            if (targetId == null) {
                LOG.warn("Source and target vertices are null");
                return null;
            } else {
                LOG.warn("Source vertex is null");
                return null;
            }
        } else if (targetId == null) {
            LOG.warn("Target vertex is null");
            return null;
        }
        SimpleConnector source = new SimpleConnector(sourceId.getNamespace(), sourceId.getId()+"-"+id+"-connector", sourceId);
        SimpleConnector target = new SimpleConnector(targetId.getNamespace(), targetId.getId()+"-"+id+"-connector", targetId);

        AbstractEdge edge = new AbstractEdge(namespace, id, source, target);

        addEdges(edge);

        return edge;
    }
}