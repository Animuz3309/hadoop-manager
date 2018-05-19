import * as _ from 'lodash';
import {CommonModel} from '../core/CommonModel';

export class Cluster extends CommonModel {
  name;
  constructor({init: init = null}) {
    if (init) {
      super({init});
    }
  }

  getId() {
    return this.name;
  }
}
