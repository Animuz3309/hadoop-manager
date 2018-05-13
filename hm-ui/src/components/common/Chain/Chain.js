import React, {Component, PropTypes} from 'react';
import {Button, Popover, ProgressBar, OverlayTrigger} from 'react-bootstrap';
import {Link} from 'react-router';

export default class Chain extends Component {
  static propTypes = {
    maxCount: PropTypes.number,
    data: PropTypes.any.isRequired,
    render: PropTypes.func,
    popoverRender: PropTypes.func,
    popoverPlacement: PropTypes.string,
    link: PropTypes.string,
    Transform: PropTypes.bool
  };

  render() {
    const s = require('./Chain.scss');
    let maxCount = this.props.maxCount || 5;
    let src = this.props.data;
    let btnClass = this.props.Transform ? "spaced-items" : "spaced-items btnNotTransformed";

    if (src instanceof Function) {
      src = src();
    }

    if (src instanceof String) {
      src = src.split(" ");
    }

    src = src || [];
    let first = maxCount >= src.length ? maxCount : maxCount - 2;

    let itemRender = this.props.render || ((a) => String(a));
    let buttonRender = (item, i) => (
      <Button key={"item." + i} bsStyle="info" className={btnClass}>
        {itemRender(item)}
      </Button>);
    let simpleLinkRender = (item, i) => (
      <Button key={"item." + i} bsStyle="info" className={btnClass}>
        <Link className={s.chainLink} to={this.props.link}>{itemRender(item)}</Link>
      </Button>);

    let labelRender;
    if (this.props.popoverRender) {
      labelRender = (item, i) => (
        <OverlayTrigger key={"item." + i}
                        trigger="click"
                        rootClose
                        placement={this.props.popoverPlacement || "left"}
                        overlay={this.props.popoverRender(item)}
        >
          {buttonRender(item, i)}
        </OverlayTrigger>
      );
    } else if (this.props.link) {
      labelRender = simpleLinkRender;
    } else {
      labelRender = buttonRender;
    }

    let popover = (items) => (
      <Popover id="etc-items-popover" title="Other items">
        <span className={s.chain}>
          {items.map(labelRender)}
        </span>
      </Popover>
    );

    let items = [];
    return (
      <span className={s.chain}>
        {src.map((item, i) => {
          if (i <= first) {
            return labelRender(item, i);
          } else if (i > first && i < src.length - 1) {
            items.push(item);
          } else {
            //we show last element after '...', note that label always append to first style 'label-' prefix.
            return [
              <OverlayTrigger trigger="click" rootClose placement="bottom" overlay={popover(items)}>
                <Button key="item.etc" className="etc spaced-items" title={items.join(', ')}>...</Button>
              </OverlayTrigger>,
              labelRender(item, i)
            ];
          }
        })}
      </span>
    );
  }
}
