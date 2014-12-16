/// <reference path="jquery.d.ts" />
/// <reference path="bacon.d.ts" />

function Vector2(x,y) {
    var self = this;
    self.x = x;
    self.y = y;
}

function rotateLeft(pos) {
    return new Vector2(pos.y, -pos.x);
}

function rotateRight(pos) {
    return new Vector2(-pos.y, pos.x);
}

$(document).ready(function() {
    var tick   = $('#tick').asEventStream('click');
    var lefts  = $('button.left').asEventStream('click');
    var rights = $('button.right').asEventStream('click');

    var actions =
        lefts.map(() => rotateLeft).merge(
        rights.map(() =>  rotateRight));

    var startDirection = new Vector2(0,1);
    var direction = actions.scan(startDirection, (x, f) => f(x));

});
