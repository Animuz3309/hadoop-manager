import {Terminal} from 'xterm';
import * as attach from 'xterm/lib/addons/attach/attach';
import * as fit from 'xterm/lib/addons/fit/fit';

function termContainer() {
  var element = document.createElement('div');
  element.id = "xterm-container";
  return element;
}
document.body.appendChild(termContainer());

Terminal.applyAddon(attach); // Apply the `attach` addon
Terminal.applyAddon(fit);     // Apply the `fit` addon

var term = new Terminal(),
  protocol = 'ws://',
  host = 'localhost:2375/',
  path = 'containers/df5b86a719b2/attach/ws?',
  params = 'logs=0&stream=1&stdin=1&stdout=1&stderr=1',
  socketURL = protocol + host + path + params;

var ws = new WebSocket(socketURL);
ws.addEventListener('open', () => {
  console.log('ws connected');
  term.open(document.getElementById('xterm-container'));
  term.fit();                       // Make the terminal's size and geometry fit the size of #terminal-container
  term.attach(ws, true, true);      // Attach the above socket to `term`
});

ws.addEventListener('close', () => {
  console.log('ws disconnected');
  term.detach(ws);
});
