package org.joshy.gfx.node.control;

/**
* Created by IntelliJ IDEA.
* User: josh
* Date: Jan 29, 2010
* Time: 8:38:31 PM
* To change this template use File | Settings | File Templates.
*/
public interface ListModel<E> {
    public E get(int i);
    public int size();
}
