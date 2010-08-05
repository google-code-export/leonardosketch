importClass(org.joshy.gfx.draw.FlatColor);

println("doing a rainbow duplicate. context = " + ctx);

var doc = ctx.getDocument();
var sel = ctx.getSelection();
if(!sel.isEmpty()) {
   var item = sel.firstItem();
   for(i=0; i<5; i++) {
      var dupe = item.duplicate(null);
      dupe.setTranslateX(dupe.getTranslateX()+i*(dupe.getBounds().getWidth()+10));
      doc.getCurrentPage().model.add(dupe);
      if(typeof dupe.setFillPaint == 'function') {
          dupe.setFillPaint(FlatColor.hsb(30*i,1,1));
      }
   }
   doc.setDirty(true);
   ctx.getCanvas().redraw();
}
