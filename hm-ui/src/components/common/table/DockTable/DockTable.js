import React, {Component, PropTypes} from 'react';
import _ from 'lodash';
import TimeUtils from 'utils/TimeUtils';

export default class DockTable extends Component {
  static propTypes = {
    title: PropTypes.string,
    columns: PropTypes.array.isRequired,
    rows: PropTypes.array.isRequired,
    // String to dispaly if the value is 'null'
    nullDisplayName: PropTypes.string,
    // String to display if the value is empty string
    emptyDisplayName: PropTypes.string,
    groupBy: PropTypes.string,
    hideGroupColumn: PropTypes.bool,
    groupBySelect: PropTypes.array,
    searchable: PropTypes.bool,
    striped: PropTypes.bool,
    size: PropTypes.string
  };

  static defaultProps = {
    searchable: true,
    striped: true,
    hideGroupColumn: false,
    nullDisplayName: '',
    emptyDisplayName: ''
  };

  static SIZES = {SM: 'SM'};

  static FORMATTERS = {
    datetime: TimeUtils.format
  };

  // general
  total = 0;      // before filtering
  pagesTotal = 0; // before filtering
  pageSize = 20;
  columnsMap = {};

  // current show
  groupBy = "";
  groupByColumn;
  groups;
  columnsWithoutGroup;
  allColumns;
  sortBy = "";

  // after filtering and sorting
  query = "";
  sorting = "";
  filteredTotal = 0;
  sortedRows ;
  pagesNumber = 1;

  // for current page
  currentGroups = [];
  currentRows = [];
  currentPage = 1;

  constructor(...params) {
    super(...params);
    const {groupBy, columns, searchable} = this.props;
    this.columnsMap = _.keyBy(columns, 'name');
    this.state = {
      currentPage: 1,
      groupBy: groupBy,
      closedGroups: {},
      sortingColumn: "",
      sortingOrder: 'asc',
      query: ""
    };

    this.allColumns = columns;
    this.initInputData(this.props);
  }

  componentWillReceiveProps(nextProps) {
    this.initInputData(nextProps);
  }

  initInputData(props) {
    this.total = props.rows.length;
    this.pagesTotal = Math.max(1, Math.ceil(this.total / this.pageSize));
    this.applyGroup(props);
  }

  render() {
    if (this.groupBy !== this.state.groupBy) {
      this.applyGroup(this.props);
    }
    if ((this.query !== this.state.query) ||
      (this.sorting !== `${this.state.sortingColumn} ${this.state.sortingOrder}`)) {
      this.applyFilteringAndSorting(this.props);
    }
    if (this.currentPage !== this.state.currentPage) {
      this.generateCurrentPageData();
    }
    const s = require('./DockTable.scss');
    const {title, groupBy, groupBySelect, size, searchable} = this.props;
    let formControlSm = ' ';
    if (size && size === DockTable.SIZES.SM) {
      formControlSm += 'table-sm';
    }
    return (
      <div className={s.dockTable}>
        <div className="docktable-header clearfix">
          {title && (
            <h2>{title}</h2>
          )}

          {groupBySelect && (
            <div className="select-container">
              <form className="form-inline">
                <div className="form-group">
                  <label>Group by: </label>
                  <select ref="groupBy"
                          className="form-control"
                          defaultValue={groupBy}
                          onChange={this.groupByChange.bind(this)}>
                    <option value=""/>
                    {groupBySelect.map(groupBy => (<option key={groupBy} value={groupBy}>{groupBy}</option>))}
                  </select>
                </div>
              </form>
            </div>
          )}

          {searchable && (
            <input className={"form-control input-search" + formControlSm}
                   onChange={this.queryChange.bind(this)}
                   placeholder="Search"
            />
          )}
        </div>
          <div className="table-responsive">

          {this.groups && this.renderGroups()}

          {this.sortedRows && this.renderNoGroups()}

          {this.renderPagination()}

          </div>
      </div>
    );
  }

  renderGroups() {
    const {size} = this.props;
    let classes = ' ';
    if (size && size === DockTable.SIZES.SM) {
      classes += 'table-sm';
    }
    return (
      <table className={"table table-bordered" + classes}>
        {this.renderHeaderGroups()}
        {this.renderGroupsBodies()}
      </table>
    );
  }

  renderNoGroups() {
    const columns = this.allColumns;
    const {size, striped} = this.props;
    let emptyTrsNumber = 0;
    if (this.currentPage !== 1) {
      emptyTrsNumber = this.pageSize - this.currentRows.length;
    }
    let emptyTrs = new Array(emptyTrsNumber);
    emptyTrs.fill(1);

    let classes = '';
    if (striped) {
      classes += ' table-striped';
    }
    if (size && size === DockTable.SIZES.SM) {
      classes += ' table-sm';
    }

    return (
      <table className={"table table-bordered" + classes}>
        {this.renderHeaderNoGroups()}
        <tbody>
        {this.currentRows.map((model, i) => (
          <tr className={"tr-value " + (model.trColor ? model.trColor : "")} key={i} {...model.__attributes}>
            {columns.map(column => this.tdRender(column.name, model))}
          </tr>
        ))}
        {emptyTrs.map((value, i) => (
          <tr key={i}>{columns.map(column => <td key={column.name}>&nbsp;</td>)}</tr>
        ))}
        </tbody>
      </table>
    );
  }

  applyGroup(props) {
    const {columns} = props;
    let groupBy = this.state.groupBy;
    this.groupBy = groupBy;
    if (groupBy) {
      this.columnsWithoutGroup = columns.filter(column => column.name !== groupBy);
      this.groupByColumn = columns.find(column => column.name === groupBy);
      this.allColumns = [this.groupByColumn].concat(this.columnsWithoutGroup);
    } else {
      this.groupByColumn = null;
      this.columnsWithoutGroup = null;
    }
    this.applyFilteringAndSorting(props);
  }

  applyFilteringAndSorting(props) {
    this.query = this.state.query;
    const {sortingColumn, sortingOrder} = this.state;
    this.sorting = `${sortingColumn} ${sortingOrder}`;
    const {rows, columns} = props;
    let columnNames = columns.map(column => column.name);
    let filteredRows = this.filterRows(this.query, rows, columnNames);
    this.filteredTotal = filteredRows.length;
    this.pagesNumber = Math.max(1, Math.ceil(this.filteredTotal / this.pageSize));
    if (this.state.currentPage > this.pagesNumber) {
      this.setState({
        ...this.state,
        currentPage: 1
      });
    }

    let groups;
    let sortedRows;
    if (this.groupBy && !sortingColumn) {
      groups = _.groupBy(filteredRows, this.groupBy);
      groups = _.mapValues(groups, (rows, key) => ({rows: rows, opened: true, key: key}));
    } else {
      sortedRows = sortingColumn ? _.orderBy(filteredRows, sortingColumn, sortingOrder) : filteredRows;
    }
    this.groups = groups;
    this.sortedRows = sortedRows;

    this.generateCurrentPageData();
  }

  generateCurrentPageData() {
    this.currentPage = this.state.currentPage;
    this.generateCurrentGroups();
    this.generateCurrentRows();
  }

  generateCurrentGroups(page = null) {
    if (!this.groups) {
      //so it is simple rows mode
      this.currentGroups = null;
      return;
    }
    this.currentGroups = [];
    if (this.groups.length === 0) {
      return;
    }
    const currentPage = page ? page : this.state.currentPage;
    let groups = [];
    //to save order of props use _.forOwn instead _.values
    _.forOwn(this.groups, (group, key) => groups.push(group));
    let toSkip = this.pageSize * (currentPage - 1);
    let skipped = 0;
    let i = 0;
    //wholes group to skip
    while ((groups.length > i) && (skipped + groups[i].rows.length <= toSkip)) {
      skipped += groups[i].rows.length;
      i++;
    }

    let leftToSkip = toSkip - skipped;
    let leftToAdd = this.pageSize;
    //group that was split between two pages
    if ((groups.length > i) && (leftToSkip > 0)) {
      let group = {...groups[i]};
      group.currentRows = group.rows.slice(leftToSkip, leftToSkip + leftToAdd);
      leftToAdd -= group.currentRows.length;
      this.currentGroups.push(group);
      i++;
    }
    while ((groups.length > i) && leftToAdd && groups[i].rows.length <= leftToAdd) {
      let group = {...groups[i]};
      group.currentRows = group.rows;
      this.currentGroups.push(group);
      leftToAdd -= group.currentRows.length;
      i++;
    }

    //group that was split between two pages
    if ((groups.length > i) && leftToAdd) {
      let group = {...groups[i]};
      group.currentRows = group.rows.slice(-leftToAdd);
      this.currentGroups.push(group);
    }
  }

  generateCurrentRows() {
    if (!this.sortedRows) {
      this.currentRows = null;
      return;
    }
    let from = (this.state.currentPage - 1) * this.pageSize;
    let to = this.state.currentPage * this.pageSize;
    this.currentRows = this.sortedRows.slice(from, to);
  }

  toggleSorting(columnName) {
    let {sortingColumn, sortingOrder} = this.state;
    if (sortingColumn !== columnName) {
      sortingColumn = columnName;
      sortingOrder = 'asc';
    } else {
      sortingOrder = sortingOrder === 'asc' ? 'desc' : 'asc';
    }

    this.setState({...this.state, sortingColumn, sortingOrder, groupBy: ""});
    if (this.refs.groupBy) {
      this.refs.groupBy.value = "";
    }
  }

  groupByChange(event) {
    const groupBy = event.target.value;
    this.setState({
      ...this.state,
      groupBy: groupBy,
      currentPage: 1,
      query: "",
      closedGroups: {},
      sortingColumn: ''
    });
  }

  toggleGroup(groupName) {
    let closed = _.get(this.state.closedGroups, groupName, false);
    this.setState({
      ...this.state,
      closedGroups: {...this.state.closedGroups, [groupName]: !closed}
    });
  }

  static columnLabel(column) {
    if (column.label) {
      return column.label;
    }

    return column.name[0].toUpperCase() + column.name.slice(1);
  }

  tdRender(key, model) {
    let render = this.columnsMap[key].render;
    let formatter = this.columnsMap[key].formatter;
    let field = model[key];
    let td = null;
    if (typeof field === 'function') {
      td = field(model);
    } else if (render) {
      td = render(model);
    } else {
      let value = field;
      if (typeof formatter === 'string') {
        formatter = DockTable.FORMATTERS[formatter];
      }
      if (formatter) {
        value = formatter(field);
        // String() - prevent exception when formatter return invalid value
        if (value) {
          value = String(value);
        }
      } else if (typeof field === 'object') {
        value = JSON.stringify(field);
      }
      td = <td key={key} data-column={key}>{value === null || value === 'null' ? (<em>none</em>) : value }</td>;
    }
    return td;
  }

  renderGroupsBodies() {
    const {closedGroups} = this.state;
    const groupEls = [];
    let columns = this.columnsWithoutGroup;
    this.currentGroups.forEach(group => {
      let closed = _.get(closedGroups, group.key, false);
      // hiding group for single image is confused some users, therefore we remove this part
      // Figure out if we need to sub in empty or null string values for display
      let groupKeyDisplayName = group.key;
      if (group.key === '') {
        groupKeyDisplayName = this.props.emptyDisplayName;
      } else if (group.key === 'null') {
        groupKeyDisplayName = this.props.nullDisplayName;
      }
      groupEls.push(
        <tbody key={group.key}>
        <tr className="tr-group">
          <td colSpan={columns.length + (this.props.hideGroupColumn ? 0 : 1)}>
            <span className="group-title" onClick={this.toggleGroup.bind(this, group.key)}>
              {!closed && <i className="fa fa-minus"/>}
              {closed && <i className="fa fa-plus"/>}
              {groupKeyDisplayName}
            </span>
            <span className="text-muted">{' '}({group.rows.length})</span>
          </td>
        </tr>
        {!closed && group.currentRows.map((model, i) =>
          <tr key={i} className="tr-value" {...model.__attributes}>
            {!this.props.hideGroupColumn && <td/>}
            {columns.map(column => this.tdRender(column.name, model))}
          </tr>
        )}
        </tbody>
      );
    });
    return groupEls;
  }

  renderPagination() {
    const {currentPage} = this.state;
    const MAX_PAGES_LINKS = 5;

    if (this.pagesTotal <= 1) {
      return <div></div>;
    }
    let from = (currentPage - 1) * this.pageSize + 1;
    let to = Math.min(currentPage * this.pageSize, this.filteredTotal);
    let pagesLinks = Math.min(this.pagesNumber, MAX_PAGES_LINKS);

    let pagesNumbers = new Array(pagesLinks); // 3, 4, 5, 6, 7
    //Math.floor((pagesLinks - 1) / 2) - left center eq to this.page.Current
    let startPage = Math.max(1, currentPage - Math.floor((pagesLinks - 1) / 2));
    for (let i = 0; i < pagesLinks; i++) {
      pagesNumbers[i] = startPage + i;
    }

    pagesNumbers = pagesNumbers.filter(pageNumber => pageNumber <= this.pagesNumber);
    while ((pagesNumbers.length < MAX_PAGES_LINKS) && (pagesNumbers[0] > 1)) {
      //if array is small like 5,6,7 - let's add few more links at start
      pagesNumbers.unshift(pagesNumbers[0] - 1);
    }

    return (
      <div className="pagination-wrapper">
        <div className="pagination-showing text-muted">
          {pagesNumbers.length > 1 && <span>Showing {from} to {to} of {this.filteredTotal}</span>}
          {pagesNumbers.length === 1 && <span>Showing {this.filteredTotal}</span>}
        </div>
        <nav>
          <ul className="pagination">
            <li className={"page-item " + (currentPage === 1 ? 'disabled' : '')}>
              <a className="page-link" onClick={this.changePage.bind(this, currentPage - 1)} aria-label="Previous">
                <span aria-hidden="true">&laquo;</span>
                <span className="sr-only">Previous</span>
              </a>
            </li>
            {pagesNumbers.map(pageNumber => {
              return (
                <li className={"page-item " + (currentPage === pageNumber ? 'active' : '')} key={pageNumber}>
                  <a className="page-link" onClick={this.changePage.bind(this, pageNumber)}>{pageNumber}</a>
                </li>
              );
            })}
            <li className={"page-item " + (currentPage === this.pagesNumber ? 'disabled' : '')}>
              <a className="page-link" onClick={this.changePage.bind(this, currentPage + 1)} aria-label="Next">
                <span aria-hidden="true">&raquo;</span>
                <span className="sr-only">Next</span>
              </a>
            </li>
          </ul>
        </nav>
      </div>
    );
  }

  changePage(pageNumber) {
    if ((pageNumber < 1) || (this.state.currentPage === pageNumber) || (pageNumber > this.pagesNumber)) {
      return;
    }

    this.setState({
      ...this.state,
      currentPage: pageNumber
    });
  }

  queryChange(e) {
    let query = e.target.value;
    this.setState({
      ...this.state,
      query
    });
  }

  filterRows(query, rows, columnsNames) {
    if (!query) {
      return rows;
    }
    let q = query.trim().toLowerCase();

    return rows.filter(row => {
      let data = _.pick(row, columnsNames);
      for (let fieldName in data) {
        if (data.hasOwnProperty(fieldName)) {
          let fieldValue = data[fieldName];
          if (fieldValue != null) {
            fieldValue = fieldValue.toString().toLowerCase();
            if (fieldValue.includes(q)) return true;
          }
        }
      }
      return false;
    });
  }

  renderHeaderGroups() {
    return (
      <thead>
      <tr>
        {this.groupByColumn && !this.props.hideGroupColumn && <th>{DockTable.columnLabel(this.groupByColumn)}</th>}
        {this.columnsWithoutGroup && this.columnsWithoutGroup.map(this.renderHeaderTh.bind(this))}
      </tr>
      </thead>
    );
  }

  renderHeaderNoGroups() {
    return (
      <thead>
      <tr>
        {this.allColumns && this.allColumns.map(this.renderHeaderTh.bind(this))}
      </tr>
      </thead>
    );
  }

  renderHeaderTh(column) {
    const {sortingColumn, sortingOrder} = this.state;

    let thAttr = {};
    if (column.width) {
      thAttr.width = column.width;
    }

    return (
      <th key={column.name}
          className={column.sortable ? 'sortable' : ''}
          onClick={column.sortable && this.toggleSorting.bind(this, column.name)}
          {...thAttr}>

        {DockTable.columnLabel(column)}

        {column.sortable && (
          <span className="sorting">
            {sortingColumn !== column.name && (
              <i className="fa fa-sort"/>
            )}

            {sortingColumn === column.name && (
              <span>
                {sortingOrder === 'asc' && <i className="fa fa-sort-asc"/>}
                {sortingOrder === 'desc' && <i className="fa fa-sort-desc"/>}
              </span>
            )}
          </span>
        )}
      </th>
    );
  }

}
