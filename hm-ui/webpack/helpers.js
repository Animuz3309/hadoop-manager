let path = require('path');


// Helper functions
let _root = path.resolve(__dirname, '..');

console.log('root directory:', root());

function hasProcessFlag(flag) {
  return process.argv.join('').indexOf(flag) > -1;
}

function root(args) {
  let aargs = Array.prototype.slice.call(arguments, 0);
  return path.join.apply(path, [_root].concat(aargs));
}

function rootNode(args) {
  let aargs = Array.prototype.slice.call(arguments, 0);
  return root.apply(path, ['node_modules'].concat(aargs));
}

function prependExt(extensions, args) {
  let aargs = args || [];
  if (!Array.isArray(aargs)) {
    aargs = [aargs];
  }
  return extensions.reduce(function(memo, val) {
    return memo.concat(val, aargs.map(function(prefix) {
      return prefix + val;
    }));
  }, ['']);
}

function packageSort(packages) {
  let len = packages.length - 1;
  let first = packages[0];
  let last = packages[len];
  return function sort(a, b) {
    let i = packages.indexOf(a.names[0]);
    let j = packages.indexOf(b.names[0]);
    if (i > j) {
      return 1;
    } else if (i === j) {
      return 0;
    }
    return -1;
  };
}

function reverse(arr) {
  return arr.reverse();
}

exports.reverse = reverse;
exports.hasProcessFlag = hasProcessFlag;
exports.root = root;
exports.rootNode = rootNode;
exports.prependExt = prependExt;
exports.prepend = prependExt;
exports.packageSort = packageSort;
