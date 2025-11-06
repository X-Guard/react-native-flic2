type EventMap = Record<string, (...args: any[]) => void>;

export class TypedEmitter<E extends EventMap> {

  private listeners: { [K in keyof E]?: Set<E[K]> } = {};

  on<K extends keyof E>(event: K, fn: E[K]): this {

    (this.listeners[event] ??= new Set()).add(fn);

    return this;

  }

  off<K extends keyof E>(event: K, fn: E[K]): this {

    this.listeners[event]?.delete(fn);
    return this;

  }

  once<K extends keyof E>(event: K, fn: E[K]): this {

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
