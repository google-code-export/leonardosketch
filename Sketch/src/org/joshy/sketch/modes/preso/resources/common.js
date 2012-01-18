$(window).load(function(){
$(document).ready(function() {
var c = 1;
$(document).keydown(function(e) {
    var len = $("div").length;
//    alert("which = " + e.which);
    if(c > 1) {
        //left arrow, j
        if(e.which == 37 || e.which == 74) {
            $("#t"+(c-1)).removeClass("outgoing").addClass("current");
            $("#t"+(c+0)).removeClass("current").addClass("incoming");
            $("#t"+(c+1)).removeClass("incoming");//.addClass("current");
            c--;
        }
    }
    if(c < len) {
        //right arrow, 'k', space
        if(e.which == 39 || e.which == 75 || e.which == 32) {
            $("#t"+(c+0)).removeClass("current").addClass("outgoing");
            $("#t"+(c+1)).removeClass("incoming").addClass("current");
            $("#t"+(c+2)).removeClass("outgoing").addClass("incoming");
            c++;
        }
    }
});
});
});