package org.intermine.web;

/*
 * Copyright (C) 2002-2004 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.intermine.metadata.Model;
import org.intermine.objectstore.query.ResultsInfo;
import org.intermine.objectstore.query.BagConstraint;

/**
 * Class to represent a path-based query
 * @author Mark Woodbridge
 */
public class PathQuery
{
    protected Model model;
    protected Map nodes = new LinkedHashMap();
    protected List view = new ArrayList();
    protected ResultsInfo info;
   
    /**
     * Constructor
     * @param model the Model on which to base this query
     */
    public PathQuery(Model model) {
        this.model = model;
    }

    /**
     * Gets the value of model
     * @return the value of model
     */
    public Model getModel() {
        return model;
    }

    /**
     * Sets the value of nodes
     * @param nodes the value of nodes
     */
    public void setNodes(Map nodes) {
        this.nodes = nodes;
    }

    /**
     * Gets the value of nodes
     * @return the value of nodes
     */
    public Map getNodes() {
        return nodes;
    }

    /**
     * Sets the value of view
     * @param view the value of view
     */
    public void setView(List view) {
        this.view = view;
    }

    /**
     * Gets the value of view
     * @return the value of view
     */
    public List getView() {
        return view;
    }

    /**
     * Get info regarding this query
     * @return the info
     */
    public ResultsInfo getInfo() {
        return info;
    }

    /**
     * Set info about this query
     * @param info the info
     */
    public void setInfo(ResultsInfo info) {
        this.info = info;
    }

    /**
     * Provide a list of the names of bags mentioned in the query
     * @return the list of bag names
     */
    public List getBagNames() {
        List bagNames = new ArrayList();
        for (Iterator i = nodes.values().iterator(); i.hasNext();) {
            PathNode node = (PathNode) i.next();
            for (Iterator j = node.getConstraints().iterator(); j.hasNext();) {
                Constraint c = (Constraint) j.next();
                if (BagConstraint.VALID_OPS.contains(c.getOp())) {
                    bagNames.add(c.getValue());
                }
            }
        }
        return bagNames;
    }

    /**
     * Add a node to the query using a path, adding parent nodes if necessary
     * @param path the path for the new Node
     * @return the PathNode that was added to the nodes Map
     */
    public PathNode addNode(String path) {
        PathNode node;
        if (path.indexOf(".") == -1) {
            node = new PathNode(path);
        } else {
            String prefix = path.substring(0, path.lastIndexOf("."));
            if (nodes.containsKey(prefix)) {
                PathNode parent = (PathNode) nodes.get(prefix);
                String fieldName = path.substring(path.lastIndexOf(".") + 1);
                node = new PathNode(parent, fieldName, model);
            } else {
                addNode(prefix);
                return addNode(path);
            }
        }
        nodes.put(path, node);
        return node;
    }

    /**
     * Clone this PathQuery
     * @return a PathQuery
     */
    public Object clone() {
        PathQuery query = new PathQuery(model);
        for (Iterator i = nodes.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            query.getNodes().put(entry.getKey(), clone((PathNode) entry.getValue()));
        }
        query.getView().addAll(view);
        return query;
    }

    /**
     * Clone a PathNode
     * @param node a PathNode
     * @return a copy of the PathNode
     */
    protected PathNode clone(PathNode node) {
        PathNode newNode;
        PathNode parent = (PathNode) nodes.get(node.getPrefix());
        if (parent == null) {
            newNode = new PathNode(node.getType());
        } else {
            newNode = new PathNode(parent, node.getFieldName(), model);
            newNode.setType(node.getType());
        }
        for (Iterator i = node.getConstraints().iterator(); i.hasNext();) {
            Constraint constraint = (Constraint) i.next();
            newNode.getConstraints().add(new Constraint(constraint.getOp(), constraint.getValue()));
        }
        return newNode;
    }

    /**
     * @see Object#equals
     */
    public boolean equals(Object o) {
        return (o instanceof PathQuery)
            && model.equals(((PathQuery) o).model)
            && nodes.equals(((PathQuery) o).nodes)
            && view.equals(((PathQuery) o).view);
    }

    /**
     * @see Object#hashCode
     */
    public int hashCode() {
        return 2 * model.hashCode()
            + 3 * nodes.hashCode()
            + 5 * view.hashCode();
    }
}
