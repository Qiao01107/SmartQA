<template>
    <div class="graph-view">
        <div v-if="loading" class="graph-status">图谱加载中...</div>
        <div v-else-if="error" class="graph-status error">{{ error }}</div>
        <div v-else-if="!nodes || nodes.length === 0" class="graph-status">{{ emptyText }}</div>
        <div ref="canvas" class="graph-canvas" v-show="!loading && !error && nodes && nodes.length" :style="{ height: height }"></div>
    </div>
</template>

<script>
import { Network } from 'vis-network/standalone';

export default {
    name: 'GraphView',
    props: {
        nodes: {
            type: Array,
            default: () => []
        },
        edges: {
            type: Array,
            default: () => []
        },
        height: {
            type: String,
            default: '500px'
        },
        loading: {
            type: Boolean,
            default: false
        },
        error: {
            type: String,
            default: ''
        },
        emptyText: {
            type: String,
            default: '暂无可展示的图谱数据'
        }
    },
    data() {
        return {
            network: null
        };
    },
    watch: {
        nodes: {
            handler() {
                this.renderGraph();
            },
            deep: true
        },
        edges: {
            handler() {
                this.renderGraph();
            },
            deep: true
        }
    },
    mounted() {
        this.renderGraph();
    },
    beforeUnmount() {
        this.destroyGraph();
    },
    methods: {
        destroyGraph() {
            if (this.network) {
                this.network.destroy();
                this.network = null;
            }
        },
        renderGraph() {
            this.$nextTick(() => {
                const container = this.$refs.canvas;
                if (!container) {
                    return;
                }
                this.destroyGraph();
                if (!this.nodes || this.nodes.length === 0) {
                    return;
                }
                const options = {
                    autoResize: true,
                    nodes: {
                        shape: 'dot',
                        size: 18,
                        borderWidth: 1,
                        font: { size: 14, color: '#303133' }
                    },
                    edges: {
                        arrows: { to: { enabled: true, scaleFactor: 0.8 } },
                        font: { size: 11, align: 'middle' },
                        smooth: { type: 'dynamic' }
                    },
                    physics: {
                        stabilization: { iterations: 150 },
                        barnesHut: { gravitationalConstant: -8000, springLength: 120 }
                    },
                    interaction: { hover: true, tooltipDelay: 200 }
                };
                this.network = new Network(container, {
                    nodes: this.nodes,
                    edges: this.edges
                }, options);
                this.network.once('stabilizationIterationsDone', () => {
                    if (this.network) {
                        this.network.fit({ animation: false });
                    }
                });
                // 弹窗内的容器首次显示时尺寸可能为 0，延迟重绘一次。
                window.setTimeout(() => {
                    if (this.network) {
                        this.network.redraw();
                        this.network.fit({ animation: false });
                    }
                }, 150);
            });
        }
    }
};
</script>

<style scoped>
.graph-view {
    width: 100%;
    min-height: 120px;
    border: 1px solid #ebeef5;
    background: #fff;
    box-sizing: border-box;
}
.graph-canvas {
    width: 100%;
}
.graph-status {
    height: 120px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #909399;
    font-size: 14px;
    padding: 0 20px;
    text-align: center;
}
.graph-status.error {
    color: #f56c6c;
}
</style>
