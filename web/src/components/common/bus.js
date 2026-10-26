// Vue 3 移除了 new Vue() 作为 Event Bus 的用法。
// 这里用一个极简的自定义事件总线实现，保持 $on / $emit / $off 接口不变，
// 避免引入额外依赖（mitt）。
class Bus {
    constructor() {
        this.events = {};
    }
    $on(event, handler) {
        if (!this.events[event]) {
            this.events[event] = [];
        }
        this.events[event].push(handler);
    }
    $off(event, handler) {
        if (!this.events[event]) {
            return;
        }
        if (!handler) {
            delete this.events[event];
            return;
        }
        this.events[event] = this.events[event].filter(h => h !== handler);
    }
    $emit(event, ...args) {
        if (this.events[event]) {
            this.events[event].forEach(handler => handler(...args));
        }
    }
}

const bus = new Bus();

export default bus;
