export class InitiableModel {
  constructor({init = {}}) {
    this.initializeByConfig(init);
  }

  initializeByConfig(init) {
    Object.keys(init).forEach((property) => {
      this[property] = init[property];
    });
  }
}
