type EventMap = Record<string, (...args: any[]) => void>;

export class TypedEmitter<E extends EventMap> {

  private initialized: boolean = false;

  private listeners: { [K in keyof E]?: Set<E[K]> } = {};

  initialize(): this {

    this.initialized = true;
    return this;

  }

  on<K extends keyof E>(event: K, fn: E[K]): this {

    if (!this.initialized) {

      throw new Error(
        'Emitter not initialized, make sure you call Flic2.start() first'
      );

    }

    (this.listeners[event] ??= new Set()).add(fn);

    return this;

  }

  off<K extends keyof E>(event: K, fn: E[K]): this {

    this.listeners[event]?.delete(fn);
    return this;

  }

  once<K extends keyof E>(event: K, fn: E[K]): this {

    if (!this.initialized) {

      throw new Error(
        'Emitter not initialized, make sure you call Flic2.start() first'
      );

    }

    const wrapped: E[K] = ((...args: Parameters<E[K]>) => {

      this.off(event, wrapped);
      fn(...args);

    }) as E[K];

    return this.on(event, wrapped);

  }

  emit<K extends keyof E>(event: K, ...args: Parameters<E[K]>): boolean {

    const ls = this.listeners[event];

    if (!ls || ls.size === 0) {

      return false;

    }

    for (const fn of [...ls]) {

      fn(...args);

    }

    return true;

  }

  clear(): this {

    this.listeners = {};
    return this;

  }

}
