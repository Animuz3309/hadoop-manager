import React, {Component, PropTypes} from 'react';
import Helmet from 'react-helmet';
import {connect} from 'react-redux';
import _ from 'lodash';
import {Row, Col} from 'react-bootstrap';
import {StatisticsPanel, DashboardNodesList, DashboardClustersList} from '../../components';

//todo dashboard load clusters, nodes, events from server
@connect(
  state => ({
    clusters: state.clusters,
    nodes: state.nodes
  })
)
export default class Dashboard extends Component {
  static propTypes = {
    clusters: PropTypes.object,
    nodes: PropTypes.object
  };

  statisticsMetrics = [
    {
      type: 'number',
      title: 'Cluster Running',
      titles: 'Clusters Running'
    },
    {
      type: 'number',
      title: 'Running Node',
      titles: 'Running Nodes'
    },
    {
      type: 'number',
      title: 'Running Container',
      titles: 'Running Containers'
    },
    {
      type: 'number',
      title: 'Errors in last 24 hour',
      titles: 'Error in last 24 hours'
    }
  ];

  componentDidMount() {
    // todo load clusters, nodes, events from api server
  }

  render() {
    const styles = require('./Dashboard.scss');

    let activeClusters = 0;
    let runningNodes = 0;
    let runningContainers = 0;
    const errorCount = 0;

    let top5Memory = [];
    let top5CPU = [];
    let top5Network = [];
    if (this.props.clusters) {
      let clustersList = Object.values(this.props.clusters).filter((cluster) => (cluster.features && !_.isEmpty(cluster.features)));
      let clustersAll = Object.values(this.props.clusters).filter((cluster) => cluster.name === 'all');

      // 处理cluster与activeCluster的数量
      if (this.props.clusters) {
        const clusters = clustersList;
        activeClusters = clusters.length;
      }

      // 处理运行容器的数量
      clustersAll.forEach((cluster) => {
        runningContainers += cluster.containers.on || 0;
      });
    }

    // 处理nodes数量以及nodes的mem, cpu, network负载
    if (this.props.nodes) {
      const nodes = Object.values(this.props.nodes);

      top5Memory = nodes.filter((node) => {
        if (typeof node.health !== "undefined" && node.on === true) {
          runningNodes += 1;
          return true;
        }
      }).sort((a, b) => {
        if (a.health.sysMemUsed > b.health.sysMemUsed) {
          return -1;
        } else if (a.health.sysMemUsed < b.health.sysMemUsed) {
          return 1;
        }
        return 0;
      });

      top5CPU = nodes.filter((node) => {
        if (typeof node.health !== "undefined" && node.on === true) {
          return true;
        }
      }).map((node)=> {
        if (_.isEmpty(node.health.sysCpuLoad)) {
          node.health.sysCpuLoad = 0;
        }
        return node;
      }).sort((a, b) => {
        if (a.health.sysCpuLoad > b.health.sysCpuLoad) {
          return -1;
        } else if (a.health.sysCpuLoad < b.health.sysCpuLoad) {
          return 1;
        }
        return 0;
      });

      top5Network = nodes.filter((node) => {
        if (typeof node.health !== "undefined" && node.on === true) {
          return true;
        }
      }).sort((a, b) => {
        if (a.health.netTotal > b.health.netTotal) {
          return -1;
        } else if (a.health.netTotal < b.health.netTotal) {
          return 1;
        }

        return 0;
      });
    }

    let clusters = [];

    return (
      <div className={styles.home}>
        <Helmet title="Home"/>
        <StatisticsPanel metrics={this.statisticsMetrics}
                         values={[activeClusters, runningNodes, runningContainers, errorCount]}
        />
        <DashboardClustersList loading={typeof this.props.clusters === "undefined"}
                               data={clusters}
        />

        <Row>
          <Col md={4}>
            <DashboardNodesList title="Top Memory Usage"
                                count={5}
                                metric="sysMemUsed"
                                metricTitle="Memory Usage"
                                data={top5Memory}
            />
          </Col>

          <Col md={4}>
            <DashboardNodesList title="Top CPU Usage"
                                count={5}
                                metric="sysCpuLoad"
                                metricTitle="CPU Usage"
                                data={top5CPU}
            />
          </Col>

          <Col md={4}>
            <DashboardNodesList title="Top Network Usage"
                                count={5}
                                metric="networkIO"
                                metricTitle="Network Usage (I/O)"
                                data={top5Network}
            />
          </Col>
        </Row>
      </div>
    );
  }
}
