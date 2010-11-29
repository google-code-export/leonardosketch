importClass(org.joshy.gfx.draw.FlatColor);

println("doing a rainbow duplicate. context = " + ctx);


if(!ctx.selection.isEmpty()) {

    //duplicate the first time five times
   var item = ctx.selection.firstItem();
   for(i=0; i<5; i++) {
      var dupe = item.duplicate(null);

      //offset each item
      dupe.translateX = item.translateX + i*(dupe.bounds.width + 10);
      if(typeof dupe.setFillPaint == 'function') {
          dupe.fillPaint = FlatColor.hsb(30*i,1,1);
      }

      //add to the model
      ctx.document.currentPage.model.add(dupe);
   }

   //redraw the screen
   ctx.document.dirty = true;
   ctx.canvas.redraw();
}