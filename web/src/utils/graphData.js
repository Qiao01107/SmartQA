/**
 * 图谱数据辅助：只使用现有后端实体/关系查询接口，不在前端保存任何 Neo4j 凭据。
 */

const DEFAULT_LIMIT = 1000;

export function loadGraph($axios, limit = DEFAULT_LIMIT) {
    return Promise.all([
        $axios.post('/api/get_entity', { start: 1, number: limit }),
        $axios.post('/api/get_relation', { start: 1, number: limit })
    ]).then(([entityResponse, relationResponse]) => {
        return buildGraph(entityResponse.data || [], relationResponse.data || []);
    });
}

export function buildGraph(entities, relations) {
    const nodeMap = new Map();
    const edges = [];

    const addNode = (id, label, name) => {
        if (id === null || id === undefined || Number.isNaN(Number(id))) {
            return;
        }
        const numericId = Number(id);
        if (!nodeMap.has(numericId)) {
            nodeMap.set(numericId, {
                id: numericId,
                label: name || String(numericId),
                title: label ? `${label}: ${name || ''}` : (name || ''),
                group: label || 'Entity'
            });
        }
    };

    (entities || []).forEach(entity => addNode(entity.id, entity.label, entity.name));

    (relations || []).forEach((relation, index) => {
        const from = Number(relation.startEntityId);
        const to = Number(relation.endEntityId);
        if (Number.isNaN(from) || Number.isNaN(to)) {
            return;
        }
        addNode(from, relation.startEntityLabel, relation.startEntityName);
        addNode(to, relation.endEntityLabel, relation.endEntityName);
        edges.push({
            id: `relation-${index}-${from}-${to}`,
            from,
            to,
            label: relation.relationName || relation.relationLabel || '',
            title: relation.relationLabel
                ? `${relation.relationLabel}${relation.relationName ? ': ' + relation.relationName : ''}`
                : (relation.relationName || ''),
            relationLabel: relation.relationLabel || '',
            relationName: relation.relationName || ''
        });
    });

    return {
        nodes: Array.from(nodeMap.values()),
        edges
    };
}

function uniqueNodes(graph, ids, fallbackNodes) {
    const selected = new Set(ids);
    const nodes = graph.nodes.filter(node => selected.has(node.id));
    if (nodes.length === 0 && fallbackNodes && fallbackNodes.length) {
        return fallbackNodes;
    }
    return nodes;
}

export function entityNeighborhood(graph, entityId, limit = 200) {
    const id = Number(entityId);
    const relatedEdges = graph.edges
        .filter(edge => edge.from === id || edge.to === id)
        .slice(0, limit);
    const ids = [id];
    relatedEdges.forEach(edge => {
        ids.push(edge.from, edge.to);
    });
    return {
        nodes: uniqueNodes(graph, ids, graph.nodes.filter(node => node.id === id)),
        edges: relatedEdges
    };
}

export function relationSubgraph(graph, relation, limit = 20) {
    const from = Number(relation.startEntityId);
    const to = Number(relation.endEntityId);
    const relationLabel = relation.relationLabel || '';
    const relationName = relation.relationName || '';
    const edges = graph.edges
        .filter(edge => {
            if (edge.from !== from || edge.to !== to) {
                return false;
            }
            if (relationLabel && edge.relationLabel !== relationLabel) {
                return false;
            }
            if (relationName && edge.relationName !== relationName) {
                return false;
            }
            return true;
        })
        .slice(0, limit);
    const ids = [from, to];
    edges.forEach(edge => {
        ids.push(edge.from, edge.to);
    });
    return {
        nodes: uniqueNodes(graph, ids, graph.nodes.filter(node => node.id === from || node.id === to)),
        edges
    };
}

export function filterGraph(graph, filters = {}) {
    const limit = Number(filters.limit) > 0 ? Number(filters.limit) : 200;
    const entityLabel = (filters.entityLabel || '').trim();
    const entityName = (filters.entityName || '').trim();
    const relationLabel = (filters.relationLabel || '').trim();
    const relationName = (filters.relationName || '').trim();

    const hasEntityFilter = Boolean(entityLabel || entityName);
    const hasRelationFilter = Boolean(relationLabel || relationName);

    let startIds = [];
    if (hasEntityFilter) {
        startIds = graph.nodes
            .filter(node => {
                if (entityLabel && node.group !== entityLabel) {
                    return false;
                }
                if (entityName && node.label !== entityName) {
                    return false;
                }
                return true;
            })
            .map(node => node.id);
    }

    let candidateEdges = graph.edges;
    if (hasEntityFilter) {
        const startSet = new Set(startIds);
        candidateEdges = candidateEdges.filter(edge => startSet.has(edge.from));
    }
    if (hasRelationFilter) {
        candidateEdges = candidateEdges.filter(edge => {
            if (relationLabel && edge.relationLabel !== relationLabel) {
                return false;
            }
            if (relationName && edge.relationName !== relationName) {
                return false;
            }
            return true;
        });
    }

    const limitedEdges = candidateEdges.slice(0, limit);
    const ids = new Set(startIds);
    limitedEdges.forEach(edge => {
        ids.add(edge.from);
        ids.add(edge.to);
    });

    if (!hasEntityFilter && !hasRelationFilter && limitedEdges.length === 0) {
        return { nodes: graph.nodes.slice(0, limit), edges: [] };
    }

    return {
        nodes: uniqueNodes(graph, Array.from(ids), graph.nodes.slice(0, limit)),
        edges: limitedEdges
    };
}
